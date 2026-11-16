(async function () {
  const { $, $$, esc } = UI;
  const app = $('#app');
  let week, anchor = new URLSearchParams(location.search).get('date') || UI.isoDate(new Date());
  let recs = null;
  const LABEL = { BREAKFAST: 'Breakfast', LUNCH: 'Lunch', DINNER: 'Dinner' };

  try { await UI.requireUser('planner'); } catch (e) { return; }

  app.innerHTML = `
    <div class="page-head">
      <div><h1>Meal planner</h1><p>Plan the week. Recipes that match your pantry are suggested first.</p></div>
      <div class="row">
        <div class="week-nav"><button class="iconbtn" id="prev" aria-label="Previous week">‹</button><strong id="range"></strong><button class="iconbtn" id="next" aria-label="Next week">›</button></div>
        <button class="btn butter" id="toList">${Art.pixel('cart', 18)} Make shopping list</button>
      </div>
    </div>
    <div id="weekBox">${UI.loading()}</div>`;

  $('#prev').onclick = () => shift(-7);
  $('#next').onclick = () => shift(7);
  $('#toList').onclick = async (e) => {
    await UI.busy(e.currentTarget, async () => {
      try {
        const from = week.weekStart < UI.isoDate(new Date()) && week.weekEnd >= UI.isoDate(new Date()) ? UI.isoDate(new Date()) : week.weekStart;
        const r = await Api.post('/api/shopping-list/generate', { from, to: week.weekEnd });
        UI.toast(r.message, r.added ? 'yay' : 'ok');
        setTimeout(() => { location.href = '/shopping.html'; }, 900);
      } catch (err) { UI.fail(err); }
    });
  };
  $('#weekBox').addEventListener('click', onClick);
  load();

  function shift(days) {
    const d = new Date(anchor + 'T00:00:00'); d.setDate(d.getDate() + days);
    anchor = UI.isoDate(d); load();
  }

  async function load() {
    try {
      week = await Api.get('/api/meal-plans' + Api.qs({ date: anchor }));
      $('#range').textContent = `${UI.niceDate(week.weekStart, { day: 'numeric', month: 'short' })} – ${UI.niceDate(week.weekEnd, { day: 'numeric', month: 'short' })}`;
      render();
    } catch (e) { UI.fail(e); }
  }

  function render() {
    $('#weekBox').innerHTML = `<div class="week">${week.days.map(day => `
      <section class="day ${day.today ? 'today' : ''}" aria-label="${esc(day.dayName)}">
        <div class="day-head"><b>${esc(day.dayName)}</b><span>${UI.niceDate(day.date, { day: 'numeric', month: 'short' })}${day.today ? ' · today' : ''}</span></div>
        <div class="slots">${Object.keys(LABEL).map(t => {
          const m = day.meals[t];
          return `<div><div class="slot-label">${LABEL[t]}</div>${m ? `
            <button class="meal ${m.cooked ? 'cooked' : ''}" data-plan="${m.id}" aria-label="${LABEL[t]}: ${esc(m.recipeName)}. Change or remove">
              ${Art.dish(m.illustration, 90)}<b>${esc(m.recipeName)}</b>
              <small>${m.cooked ? 'Cooked' : m.matchPercent + '% in pantry'} · ${m.servings} serv.</small></button>`
            : `<button class="meal add" data-date="${day.date}" data-type="${t}" aria-label="Add ${LABEL[t].toLowerCase()} on ${esc(day.dayName)}">+ Add</button>`}</div>`;
        }).join('')}</div>
      </section>`).join('')}</div>
      ${week.plannedCount ? '' : `<p class="muted" style="margin-top:16px">Tip: tap "+ Add" on any slot to plan a meal.</p>`}`;
  }

  async function onClick(e) {
    const add = e.target.closest('.meal.add');
    const existing = e.target.closest('.meal[data-plan]');
    if (add) pick({ date: add.dataset.date, mealType: add.dataset.type });
    if (existing) {
      const m = week.days.flatMap(d => Object.values(d.meals)).find(x => x && x.id === Number(existing.dataset.plan));
      pick(m);
    }
  }

  async function pick(slot) {
    const editing = !!slot.id;
    if (!recs) recs = await Api.get('/api/recommendations?strategy=balanced');
    let chosen = editing ? slot.recipeId : null;
    const m = UI.modal(editing ? `Edit ${LABEL[slot.mealType].toLowerCase()}` : `Plan ${LABEL[slot.mealType].toLowerCase()}`, `
      <p class="muted">${UI.niceDate(slot.date, { weekday: 'long', day: 'numeric', month: 'long' })}. Best matches with your pantry come first.</p>
      <label style="margin-bottom:12px">Find a recipe<input type="search" id="pickQ" placeholder="Type to filter…"></label>
      <div class="pick-list" id="pickList"></div>
      <div class="row" style="margin-top:12px"><label style="width:140px">Servings<input type="number" id="pickServ" min="1" max="20" value="${slot.servings || ''}" placeholder="Default"></label></div>
      <div class="row-end">
        ${editing ? `<a class="btn ghost" href="/recipe.html?id=${slot.recipeId}&servings=${slot.servings}&plan=${slot.id}">Open recipe</a><button class="btn ghost" id="remove">Remove</button>` : ''}
        <button class="btn lav" id="savePick">${editing ? 'Save changes' : 'Add to plan'}</button>
      </div>`, { wide: true });
    const el = m.el;
    const draw = (f) => {
      const list = recs.filter(r => !f || r.name.toLowerCase().includes(f.toLowerCase()));
      $('#pickList', el).innerHTML = list.length ? list.map(r => `
        <button class="pick" data-id="${r.id}" aria-pressed="${r.id === chosen}">${Art.dish(r.illustration, 60)}
          <span><b>${esc(r.name)}</b><br><span class="small muted">${r.totalMinutes} min · serves ${r.servings}${r.missingCount ? ' · ' + r.missingCount + ' to buy' : ''}</span></span>
          <span class="pct-pill">${r.matchPercent}%</span></button>`).join('') : '<p class="muted">No recipes match that search.</p>';
    };
    draw('');
    $('#pickQ', el).oninput = (e) => draw(e.target.value);
    $('#pickList', el).onclick = (e) => {
      const b = e.target.closest('.pick'); if (!b) return;
      chosen = Number(b.dataset.id);
      $$('.pick', el).forEach(p => p.setAttribute('aria-pressed', String(p === b)));
    };
    if (editing) $('#remove', el).onclick = async () => {
      try { await Api.del(`/api/meal-plans/${slot.id}`); m.close(); UI.toast('Removed from your plan.'); load(); } catch (err) { UI.fail(err); }
    };
    $('#savePick', el).onclick = async () => {
      if (!chosen) return UI.toast('Pick a recipe first.', 'error');
      const sv = $('#pickServ', el).value;
      const body = { date: slot.date, mealType: slot.mealType, recipeId: chosen, servings: sv ? Number(sv) : null };
      try {
        editing ? await Api.put(`/api/meal-plans/${slot.id}`, body) : await Api.post('/api/meal-plans', body);
        m.close(); UI.celebrate(editing ? 'Plan updated!' : 'Meal planned!'); load();
      } catch (err) { UI.fail(err); }
    };
  }
})();
