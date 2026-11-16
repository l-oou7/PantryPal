(async function () {
  const { $, esc } = UI;
  const app = $('#app');
  let list, meta;

  try { await UI.requireUser('shopping'); meta = await UI.getMeta(); } catch (e) { return; }

  const today = UI.isoDate(new Date());
  const sunday = (() => { const d = new Date(); d.setDate(d.getDate() + ((7 - d.getDay()) % 7)); return UI.isoDate(d); })();

  app.innerHTML = `
    <div class="page-head"><div><h1>Shopping list</h1><p id="sub">Only what your planned meals need, minus what's already in your pantry.</p></div></div>
    <div class="shop-grid">
      <section class="notepad" id="pad">${UI.loading()}</section>
      <div class="stack">
        <section class="panel mint">
          <h2>Build from my meal plan</h2>
          <form id="gen" class="form-grid">
            <label>From<input type="date" name="from" value="${today}" required></label>
            <label>To<input type="date" name="to" value="${sunday}" required></label>
            <button class="btn butter full" type="submit">${Art.pixel('cart', 18)} Generate list</button>
          </form>
          <p class="small" style="margin:12px 0 0">Items you added yourself and items already ticked off are kept.</p>
        </section>
        <section class="panel">
          <h2>Add an item</h2>
          <form id="add" class="form-grid" novalidate>
            <label class="full">Item<input name="name" required maxlength="80" placeholder="e.g. Lemons"></label>
            <label>Quantity<input name="quantity" type="number" min="0.01" step="any" value="1" required></label>
            <label>Unit<select name="unit">${meta.units.map(u => `<option value="${u.value}" ${u.value === 'PCS' ? 'selected' : ''}>${esc(u.label)}</option>`).join('')}</select></label>
            <label class="full">Category<select name="category">${meta.categories.map(c => `<option value="${c.value}" ${c.value === 'OTHERS' ? 'selected' : ''}>${esc(c.label)}</option>`).join('')}</select></label>
            <button class="btn coral full" type="submit">Add to list</button>
          </form>
        </section>
      </div>
    </div>`;

  $('#gen').onsubmit = async (e) => {
    e.preventDefault();
    const d = UI.formData(e.currentTarget);
    await UI.busy($('[type=submit]', e.currentTarget), async () => {
      try {
        const r = await Api.post('/api/shopping-list/generate', d);
        list = r.list; render();
        r.added ? UI.celebrate(r.message) : UI.toast(r.message);
      } catch (err) { UI.fail(err); }
    });
  };
  $('#add').onsubmit = async (e) => {
    e.preventDefault();
    const f = e.currentTarget, d = UI.formData(f);
    try {
      list = await Api.post('/api/shopping-list/items', { ...d, quantity: Number(d.quantity) });
      render(); f.reset(); f.quantity.value = 1; f.unit.value = 'PCS'; f.category.value = 'OTHERS';
      UI.toast(`${d.name} added to your list.`);
    } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
  };
  $('#pad').addEventListener('click', onPad);
  $('#pad').addEventListener('change', onToggle);

  try { list = await Api.get('/api/shopping-list'); render(); } catch (e) { UI.fail(e); }

  function render() {
    const items = list.items;
    if (!items.length) {
      $('#pad').innerHTML = UI.empty('riceBag', 'Your list is empty', 'Plan a few meals and press "Generate list", or add items by hand.',
        '<a class="btn lav" href="/planner.html">Open the planner</a>');
      return;
    }
    const groups = {};
    items.filter(i => !i.purchased).forEach(i => (groups[i.categoryLabel] = groups[i.categoryLabel] || []).push(i));
    const bought = items.filter(i => i.purchased);
    const range = list.generatedFrom ? `Generated for ${UI.niceDate(list.generatedFrom)} – ${UI.niceDate(list.generatedTo)}. ` : '';
    $('#pad').innerHTML = `
      <div class="panel-title"><h2>${list.remaining} to buy</h2><span class="small muted">${range}</span></div>
      ${Object.entries(groups).map(([cat, arr]) => `<div class="cat-group"><h3>${Art.food(arr[0].name, arr[0].category, 'happy', 26)} ${esc(cat)}</h3>${arr.map(row).join('')}</div>`).join('')}
      ${bought.length ? `<div class="cat-group"><h3>${Art.pixel('star', 18)} In the basket (${bought.length})</h3>${bought.map(row).join('')}</div>
        <div class="row-end"><button class="btn ghost small" data-act="clear">Clear ticked items</button><button class="btn mint small" data-act="move">Move ticked items to pantry</button></div>` : ''}`;
  }

  function row(i) {
    return `<div class="sitem ${i.purchased ? 'done' : ''}" data-id="${i.id}">
      <input type="checkbox" ${i.purchased ? 'checked' : ''} aria-label="Bought ${esc(i.name)}">
      <span class="name">${esc(i.name)}${i.source === 'AUTO' ? '<span class="src">plan</span>' : ''}${i.note ? `<span class="note">${esc(i.note)}</span>` : ''}</span>
      <span class="row"><span class="q">${UI.qty(i.quantity)} ${esc(i.unitSymbol)}</span>
        <span class="tools"><button class="iconbtn" data-act="edit" aria-label="Edit ${esc(i.name)}">✎</button><button class="iconbtn" data-act="del" aria-label="Remove ${esc(i.name)}">✕</button></span></span>
    </div>`;
  }

  async function onToggle(e) {
    if (e.target.type !== 'checkbox') return;
    const id = e.target.closest('[data-id]').dataset.id;
    try { list = await Api.patch(`/api/shopping-list/items/${id}/purchased`, { purchased: e.target.checked }); render(); } catch (err) { UI.fail(err); }
  }

  async function onPad(e) {
    const b = e.target.closest('[data-act]'); if (!b) return;
    const act = b.dataset.act;
    try {
      if (act === 'clear') { list = await Api.post('/api/shopping-list/clear-purchased'); render(); UI.toast('Cleared.'); return; }
      if (act === 'move') { list = await Api.post('/api/shopping-list/move-to-pantry'); render(); UI.celebrate('Groceries put away in your pantry!'); return; }
      const id = Number(b.closest('[data-id]').dataset.id);
      const item = list.items.find(x => x.id === id);
      if (act === 'del') { list = await Api.del(`/api/shopping-list/items/${id}`); render(); return; }
      if (act === 'edit') editItem(item);
    } catch (err) { UI.fail(err); }
  }

  function editItem(item) {
    UI.modal('Edit item', `
      <form class="form-grid" novalidate>
        <label class="full">Item<input name="name" value="${esc(item.name)}" required maxlength="80"></label>
        <label>Quantity<input name="quantity" type="number" min="0.01" step="any" value="${item.quantity}" required></label>
        <label>Unit<select name="unit">${meta.units.map(u => `<option value="${u.value}" ${u.value === item.unit ? 'selected' : ''}>${esc(u.label)}</option>`).join('')}</select></label>
        <div class="full row-end"><button type="button" class="btn ghost cancel">Cancel</button><button class="btn coral" type="submit">Save changes</button></div>
      </form>`, {
      onOpen(el, close) {
        const f = $('form', el);
        $('.cancel', el).onclick = close;
        f.onsubmit = async (e) => {
          e.preventDefault();
          const d = UI.formData(f);
          try {
            list = await Api.put(`/api/shopping-list/items/${item.id}`, { ...d, quantity: Number(d.quantity), category: item.category, purchased: item.purchased });
            close(); render(); UI.toast('Saved!');
          } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
        };
      }
    });
  }
})();
