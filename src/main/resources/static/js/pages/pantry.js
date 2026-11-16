(async function () {
  const { $, $$, esc } = UI;
  const app = $('#app');
  let items = [], meta, category = '', query = '', timer;

  try {
    await UI.requireUser('pantry');
    meta = await UI.getMeta();
  } catch (e) { return; }

  app.innerHTML = `
    <div class="page-head">
      <div><h1>My pantry</h1><p class="counter" id="counter"></p></div>
      <button class="btn coral" id="addBtn">${Art.pixel('pot', 18)} Add ingredient</button>
    </div>
    <div class="toolbar">
      <label class="search"><span class="sr-only">Search ingredients</span>${Art.pixel('search', 16)}<input id="q" type="search" placeholder="Search your pantry…"></label>
      <div class="chips" id="cats" role="group" aria-label="Filter by category">
        <button class="chip" data-cat="" aria-pressed="true">All</button>
        ${meta.categories.map(c => `<button class="chip" data-cat="${c.value}" aria-pressed="false">${esc(c.label)}</button>`).join('')}
      </div>
    </div>
    <div id="list">${UI.loading()}</div>`;

  $('#addBtn').onclick = () => openForm();
  $('#q').addEventListener('input', (e) => { clearTimeout(timer); timer = setTimeout(() => { query = e.target.value; load(); }, 250); });
  $('#cats').addEventListener('click', (e) => {
    const b = e.target.closest('.chip'); if (!b) return;
    $$('#cats .chip').forEach(c => c.setAttribute('aria-pressed', String(c === b)));
    category = b.dataset.cat; load();
  });
  $('#list').addEventListener('click', onCardAction);

  await load();
  if (new URLSearchParams(location.search).get('add')) openForm();

  async function load() {
    try {
      items = await Api.get('/api/pantry' + Api.qs({ q: query, category }));
      render();
    } catch (e) { UI.fail(e); }
  }

  function render() {
    const total = items.length;
    $('#counter').textContent = query || category ? `${total} matching item${total === 1 ? '' : 's'}` : `${total} of 200 spaces used`;
    if (!total) {
      $('#list').innerHTML = query || category
        ? UI.empty('egg', 'Nothing matches', 'Try another search or category.')
        : UI.empty('jar', 'Your pantry is empty', 'Add the first ingredient and PantryPal will start finding recipes for you.',
            '<button class="btn coral" data-act="add">Add ingredient</button>');
      return;
    }
    $('#list').innerHTML = `<div class="pgrid">${items.map(card).join('')}</div>`;
  }

  function card(i) {
    const lv = UI.levelClass[i.alertLevel];
    return `<article class="pcard ${i.alertLevel === 'EXPIRED' ? 'lv-expired-card' : ''}" data-id="${i.id}">
      ${Art.food(i.name, i.category, UI.levelMood[i.alertLevel], 64)}
      <div>
        <h3>${esc(i.name)}</h3>
        <div><span class="q">${UI.qty(i.quantity)} ${esc(i.unitSymbol)}</span> <span class="cat">· ${esc(i.categoryLabel)}</span></div>
        <span class="exp ${lv}" title="Expires ${esc(i.expirationDate)}">${UI.expiryText(i.daysLeft)}</span>
      </div>
      ${i.wasteRisk > 0 ? `<div class="risk" title="Waste risk ${i.wasteRisk} of 100">risk ${i.wasteRisk}<i style="--r:${i.wasteRisk}"></i></div>` : ''}
      <div class="actions">
        ${i.alertLevel === 'EXPIRED' ? '' : `<button class="btn small mint" data-act="use">Use some</button>`}
        <button class="btn small" data-act="edit">Edit</button>
        ${i.alertLevel === 'EXPIRED' ? `<button class="btn small coral" data-act="discard">Throw away</button>` : ''}
        <button class="btn small ghost" data-act="delete" aria-label="Delete ${esc(i.name)}">Delete</button>
      </div>
    </article>`;
  }

  async function onCardAction(e) {
    const b = e.target.closest('[data-act]'); if (!b) return;
    if (b.dataset.act === 'add') return openForm();
    const id = Number(b.closest('[data-id]').dataset.id);
    const item = items.find(x => x.id === id);
    if (b.dataset.act === 'edit') return openForm(item);
    if (b.dataset.act === 'use') return openUse(item);
    if (b.dataset.act === 'discard') {
      if (!await UI.confirmBox('Throw it away?', `${item.name} will be removed and counted as wasted food.`, 'Throw away')) return;
      try { const r = await Api.post(`/api/pantry/${id}/discard`); UI.toast(r.message); load(); } catch (err) { UI.fail(err); }
    }
    if (b.dataset.act === 'delete') {
      if (!await UI.confirmBox('Delete this item?', `${item.name} will be removed from your pantry. Use "Throw away" or "Use some" instead if you want it counted in your progress.`, 'Delete')) return;
      try { await Api.del(`/api/pantry/${id}`); UI.toast(`${item.name} deleted.`); load(); } catch (err) { UI.fail(err); }
    }
  }

  function openForm(item) {
    const today = UI.isoDate(new Date());
    const def = item || { name: '', category: 'VEGETABLES', quantity: '', unit: 'PCS', expirationDate: UI.isoDate(new Date(Date.now() + 7 * 864e5)) };
    const m = UI.modal(item ? 'Edit ingredient' : 'Add an ingredient', `
      <form class="form-grid" novalidate>
        <div class="full row" id="preview" aria-hidden="true"></div>
        <label class="full">Ingredient name<input name="name" required maxlength="80" value="${esc(def.name)}" placeholder="e.g. Tomatoes"></label>
        <label>Category<select name="category">${meta.categories.map(c => `<option value="${c.value}" ${c.value === def.category ? 'selected' : ''}>${esc(c.label)}</option>`).join('')}</select></label>
        <label>Expiration date<input name="expirationDate" type="date" required value="${def.expirationDate}" ${item ? '' : `min="${today}"`}></label>
        <label>Quantity<input name="quantity" type="number" min="0.01" step="any" required value="${def.quantity}"></label>
        <label>Unit<select name="unit">${meta.units.map(u => `<option value="${u.value}" ${u.value === def.unit ? 'selected' : ''}>${esc(u.label)}</option>`).join('')}</select></label>
        <div class="full row-end" style="margin-top:4px"><button type="button" class="btn ghost cancel">Cancel</button><button class="btn coral" type="submit">${item ? 'Save changes' : 'Add to pantry'}</button></div>
      </form>`, {
      onOpen(el, close) {
        const f = $('form', el);
        const preview = () => { $('#preview', el).innerHTML = Art.food(f.name.value || 'jar', f.category.value, 'happy', 72) +
          `<p class="muted small">Tip: dates are checked hourly, so you'll get a heads-up before it goes off.</p>`; };
        f.name.addEventListener('input', preview); f.category.addEventListener('change', preview); preview();
        $('.cancel', el).onclick = close;
        f.addEventListener('submit', async (ev) => {
          ev.preventDefault();
          const d = UI.formData(f);
          const body = { ...d, quantity: d.quantity === '' ? null : Number(d.quantity) };
          await UI.busy($('[type=submit]', f), async () => {
            try {
              if (item) { await Api.put(`/api/pantry/${item.id}`, body); UI.toast('Saved!'); }
              else { await Api.post('/api/pantry', body); UI.celebrate('Yay! Another ingredient added to your pantry!'); }
              close(); load(); UI.refreshBell();
            } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
          });
        });
      }
    });
  }

  function openUse(item) {
    UI.modal(`Use ${item.name}`, `
      <form novalidate>
        <p>You have <b>${UI.qty(item.quantity)} ${esc(item.unitSymbol)}</b>. How much did you use?</p>
        <label>Amount (${esc(item.unitSymbol)})<input name="quantity" type="number" min="0.01" step="any" max="${item.quantity}" value="${item.quantity}" required></label>
        <div class="row-end"><button type="button" class="btn ghost cancel">Cancel</button><button class="btn mint" type="submit">Mark as used</button></div>
      </form>`, {
      onOpen(el, close) {
        const f = $('form', el);
        $('.cancel', el).onclick = close;
        f.addEventListener('submit', async (ev) => {
          ev.preventDefault();
          await UI.busy($('[type=submit]', f), async () => {
            try {
              const r = await Api.post(`/api/pantry/${item.id}/use`, { quantity: Number(f.quantity.value) });
              close();
              r.rescued ? UI.celebrate(r.message) : UI.toast(r.message);
              load();
            } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
          });
        });
      }
    });
  }
})();
