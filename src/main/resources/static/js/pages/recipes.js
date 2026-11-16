(async function () {
  const { $, $$, esc } = UI;
  const app = $('#app');
  const params = new URLSearchParams(location.search);
  let meta, tab = params.get('tab') || 'for-you', strategy = params.get('strategy') || 'balanced', diet = '', course = '', q = '', timer;

  try {
    await UI.requireUser('recipes');
    meta = await UI.getMeta();
  } catch (e) { return; }

  app.innerHTML = `
    <div class="page-head">
      <div><h1>Recipes</h1><p>Ranked by what's in your pantry right now.</p></div>
    </div>
    <div class="tabbar" role="tablist">
      <button class="chip" role="tab" data-tab="for-you">For you</button>
      <button class="chip" role="tab" data-tab="browse">Browse &amp; search</button>
      <button class="chip" role="tab" data-tab="saved">Saved</button>
    </div>
    <div id="controls"></div>
    <div id="results">${UI.loading()}</div>`;

  $('.tabbar').addEventListener('click', (e) => {
    const b = e.target.closest('[data-tab]'); if (!b) return;
    tab = b.dataset.tab; history.replaceState(null, '', '?tab=' + tab); show();
  });
  show();

  function dietChips() {
    return `<div class="chips" id="diets" role="group" aria-label="Dietary filter">
      <button class="chip" data-diet="" aria-pressed="${!diet}">Any diet</button>
      ${meta.diets.filter(d => d.value !== 'GENERAL').map(d => `<button class="chip" data-diet="${d.value}" aria-pressed="${diet === d.value}">${esc(d.label)}</button>`).join('')}
    </div>`;
  }
  function bindDiets() {
    $('#diets').addEventListener('click', (e) => {
      const b = e.target.closest('.chip'); if (!b) return;
      diet = b.dataset.diet;
      $$('#diets .chip').forEach(c => c.setAttribute('aria-pressed', String(c === b)));
      load();
    });
  }

  function show() {
    $$('.tabbar .chip').forEach(c => { c.classList.toggle('on', c.dataset.tab === tab); c.setAttribute('aria-selected', String(c.dataset.tab === tab)); });
    const ctl = $('#controls');
    if (tab === 'for-you') {
      const s = meta.strategies.find(x => x.key === strategy) || meta.strategies[0];
      ctl.innerHTML = `<div class="toolbar">
        <label style="flex:0 1 300px">Recommend by
          <select id="strategy">${meta.strategies.map(x => `<option value="${x.key}" ${x.key === strategy ? 'selected' : ''}>${esc(x.label)}</option>`).join('')}</select></label>
        <p class="muted small" id="stratDesc" style="flex:1 1 200px;margin:22px 0 0">${esc(s.description)}</p>
        ${dietChips()}</div>`;
      $('#strategy').onchange = (e) => {
        strategy = e.target.value;
        $('#stratDesc').textContent = meta.strategies.find(x => x.key === strategy).description;
        load();
      };
      bindDiets();
    } else if (tab === 'browse') {
      ctl.innerHTML = `<div class="toolbar">
        <label class="search"><span class="sr-only">Search recipes</span>${Art.pixel('search', 16)}<input id="q" type="search" value="${esc(q)}" placeholder="Search by recipe, cuisine or ingredient…"></label>
        <label style="flex:0 1 200px"><span class="sr-only">Course</span><select id="course"><option value="">Any course</option>
          ${meta.courses.map(c => `<option value="${c}" ${c === course ? 'selected' : ''}>${UI.pretty(c)}</option>`).join('')}</select></label>
        ${dietChips()}</div>`;
      $('#q').addEventListener('input', (e) => { clearTimeout(timer); timer = setTimeout(() => { q = e.target.value; load(); }, 250); });
      $('#course').onchange = (e) => { course = e.target.value; load(); };
      bindDiets();
    } else {
      ctl.innerHTML = '';
    }
    load();
  }

  async function load() {
    const box = $('#results');
    box.innerHTML = UI.loading('Matching recipes to your pantry…');
    try {
      let list;
      if (tab === 'for-you') list = await Api.get('/api/recommendations' + Api.qs({ strategy, diet }));
      else if (tab === 'browse') list = await Api.get('/api/recipes' + Api.qs({ q, diet, course }));
      else list = await Api.get('/api/bookmarks');

      if (!list.length) {
        box.innerHTML = tab === 'saved'
          ? UI.empty('strawberry', 'No saved recipes yet', 'Tap "Save recipe" on any recipe to keep it here.')
          : UI.empty('mushroom', 'No recipes found', 'Try a different filter, or check your dietary preferences in your profile.');
        return;
      }
      const ready = list.filter(r => r.canCookNow).length;
      const intro = tab === 'for-you'
        ? `<p class="muted" style="margin-bottom:16px">${ready ? `Look what you can make with your ingredients! ${ready} recipe${ready === 1 ? ' is' : 's are'} ready to cook.` : 'Recipes closest to what you have, best first.'}</p>` : '';
      box.innerHTML = intro + `<div class="rgrid">${list.map(r => UI.recipeCard(r)).join('')}</div>`;
    } catch (e) { box.innerHTML = UI.empty('tomato', "Couldn't load recipes", e.message); }
  }
})();
