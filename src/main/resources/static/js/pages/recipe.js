(async function () {
  const { $, esc } = UI;
  const app = $('#app');
  const params = new URLSearchParams(location.search);
  const id = params.get('id');
  const planId = params.get('plan');
  let servings = params.get('servings') ? Number(params.get('servings')) : null;
  let user, data, meta;

  if (!id) { app.innerHTML = UI.empty('egg', 'No recipe chosen', 'Pick one from the recipe list.', '<a class="btn" href="/recipes.html">Browse recipes</a>'); return; }
  user = await UI.optionalUser('recipes');
  await load();

  async function load() {
    try {
      data = await Api.get(`/api/recipes/${id}` + Api.qs({ servings }), { redirectOn401: false });
      servings = data.requestedServings;
      document.title = data.card.name + ' · PantryPal';
      render();
    } catch (e) { app.innerHTML = UI.empty('tomato', "Couldn't find that recipe", e.message, '<a class="btn" href="/recipes.html">Back to recipes</a>'); }
  }

  function lineItem(l) {
    const cls = { HAVE_ENOUGH: 'have', NEED_MORE: 'short', MISSING: 'miss', CAN_SUBSTITUTE: 'sub', OPTIONAL: '' }[l.status] || '';
    let note = '';
    if (l.status === 'NEED_MORE') note = `You have ${esc(l.available)}, need ${esc(l.shortfall)} more`;
    if (l.status === 'CAN_SUBSTITUTE') note = 'Swap available, see below';
    if (l.status === 'OPTIONAL') note = 'Optional';
    if (l.unitMismatch) note = "In your pantry (in different units, so we couldn't compare amounts)";
    if (l.status === 'HAVE_ENOUGH' && l.expiringSoon) note = 'Expiring soon, great time to use it!';
    if (!l.status && l.optional) note = 'Optional';
    return `<li class="${cls}"><span>${esc(l.name)}${note ? `<span class="note">${note}</span>` : ''}</span><span class="amt">${esc(l.required)}</span></li>`;
  }

  function render() {
    const c = data.card;
    const hasMatch = c.matchPercent !== null;
    const missingLines = data.missing.filter(l => l.status !== 'OPTIONAL');
    const optionalMissing = data.missing.filter(l => l.status === 'OPTIONAL');
    const ring = hasMatch ? `<div class="match ${c.matchPercent === 100 ? 'full' : c.matchPercent >= 60 ? 'good' : 'low'}" style="--p:${c.matchPercent}"><span>${c.matchPercent}%</span></div>` : '';

    app.innerHTML = `
      <p><a href="javascript:history.back()" class="btn ghost small">Back</a></p>
      <section class="rd-hero">
        <div class="rd-art">${Art.dish(c.illustration, 220)}${ring}</div>
        <div class="rd-info">
          <h1>${esc(c.name)}</h1>
          <p>${esc(c.description)}</p>
          ${c.reason && hasMatch ? `<p><b>${esc(c.reason)}</b></p>` : ''}
          <ul class="facts">
            <li>Prep ${c.prepMinutes} min</li><li>Cook ${c.cookMinutes} min</li><li>${UI.pretty(c.difficulty)}</li>
            <li class="diet diet-${c.dietaryCategory}">${UI.pretty(c.dietaryCategory)}</li>${c.cuisine ? `<li>${esc(c.cuisine)}</li>` : ''}
          </ul>
          <div class="row">
            <div class="servings" aria-label="Servings"><button id="less" aria-label="Fewer servings">−</button><span>${servings} serving${servings === 1 ? '' : 's'}</span><button id="more" aria-label="More servings">+</button></div>
            ${user ? `
              <button class="btn ${c.bookmarked ? 'butter' : ''}" id="save">${Art.pixel('heart', 16)} ${c.bookmarked ? 'Saved' : 'Save recipe'}</button>
              <button class="btn lav" id="plan">${Art.pixel('cal', 16)} Add to planner</button>
              <button class="btn coral" id="cook">${Art.pixel('pot', 16)} I cooked this</button>` : ''}
          </div>
        </div>
      </section>

      <div class="rd-cols">
        <section class="panel">
          <h2>Ingredients</h2>
          ${!user ? `<ul class="ing-list">${data.allIngredients.map(lineItem).join('')}</ul>
               <p class="explain"><a href="/login.html?next=${encodeURIComponent(location.pathname + location.search)}">Log in</a> to see which of these you already have.</p>` : `
            <h3>Available in your pantry (${data.available.length})</h3>
            ${data.available.length ? `<ul class="ing-list">${data.available.map(lineItem).join('')}</ul>` : '<p class="muted">None yet.</p>'}
            <h3>Missing or not enough (${missingLines.length})</h3>
            ${missingLines.length ? `<ul class="ing-list">${missingLines.map(lineItem).join('')}</ul>` : `<p class="muted">Nothing missing. You're ready to cook!</p>`}
            ${optionalMissing.length ? `<h3>Optional extras</h3><ul class="ing-list">${optionalMissing.map(lineItem).join('')}</ul>` : ''}
            <h3>Suggested substitutions</h3>
            ${data.substitutions.length ? data.substitutions.map(s => `
              <div class="subcard"><div class="swap">${esc(s.original)} <span class="arrow" aria-label="can be replaced by">→</span> ${esc(s.replacement)}
                <span class="tag-have">${esc(s.recommendedQuantity)}</span>${s.inPantry ? '<span class="tag-have" style="background:var(--butter)">In your pantry</span>' : ''}</div>
                <p>${esc(s.explanation)}</p></div>`).join('')
              : '<p class="muted">No swaps needed, or none that suit this dish and your diet.</p>'}
            <p class="explain"><b>How the match is calculated.</b> ${esc(data.matchExplanation)}</p>`}
        </section>
        <section class="panel">
          <h2>Step by step</h2>
          <ol class="steps">${data.steps.map(s => `<li><p>${esc(s)}</p></li>`).join('')}</ol>
        </section>
      </div>`;

    $('#less').onclick = () => { if (servings > 1) { servings--; load(); } };
    $('#more').onclick = () => { if (servings < 20) { servings++; load(); } };
    if (!user) return;

    $('#save').onclick = async () => {
      try {
        if (c.bookmarked) { await Api.del(`/api/bookmarks/${c.id}`); UI.toast('Removed from saved recipes.'); }
        else { await Api.post(`/api/bookmarks/${c.id}`); UI.celebrate('Saved! Find it under Recipes › Saved.'); }
        load();
      } catch (e) { UI.fail(e); }
    };
    $('#cook').onclick = async () => {
      if (!await UI.confirmBox('Cooked it?', `PantryPal will take the ingredients for ${servings} serving${servings === 1 ? '' : 's'} out of your pantry, using the ones that expire first.`, 'Update my pantry')) return;
      try {
        const r = await Api.post(`/api/recipes/${c.id}/cook`, { servings, mealPlanId: planId ? Number(planId) : null });
        r.itemsRescued > 0 ? UI.celebrate(r.message) : UI.toast(r.message, 'yay');
        load();
      } catch (e) { UI.fail(e); }
    };
    $('#plan').onclick = openPlan;
  }

  async function openPlan() {
    meta = meta || await UI.getMeta();
    const today = UI.isoDate(new Date());
    UI.modal('Add to meal planner', `
      <form class="form-grid" novalidate>
        <label>Day<input type="date" name="date" value="${today}" required></label>
        <label>Meal<select name="mealType">${meta.mealTypes.map(t => `<option value="${t}" ${t === 'DINNER' ? 'selected' : ''}>${UI.pretty(t)}</option>`).join('')}</select></label>
        <label class="full">Servings<input type="number" name="servings" min="1" max="20" value="${servings}"></label>
        <div class="full row-end"><button type="button" class="btn ghost cancel">Cancel</button><button class="btn lav" type="submit">Add to planner</button></div>
      </form>`, {
      onOpen(el, close) {
        const f = $('form', el);
        $('.cancel', el).onclick = close;
        f.onsubmit = async (e) => {
          e.preventDefault();
          const d = UI.formData(f);
          try {
            await Api.post('/api/meal-plans', { date: d.date, mealType: d.mealType, recipeId: data.card.id, servings: Number(d.servings) });
            close(); UI.celebrate('Added to your meal plan!');
          } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
        };
      }
    });
  }
})();
