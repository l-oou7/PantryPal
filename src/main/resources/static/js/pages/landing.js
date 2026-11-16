(function () {
  const { $, $$ } = UI;
  const brand = `${Art.pixel('pot', 26)}<span>PantryPal</span>`;
  $('#brand').innerHTML = brand;
  $('#footBrand').innerHTML = brand;

  // hero: chef on a mint counter, surrounded by floating ingredients
  const floats = [
    ['strawberry', '6%', '18%', 70], ['carrot', '78%', '10%', 74], ['tomato', '2%', '58%', 66],
    ['broccoli', '82%', '52%', 70], ['egg', '18%', '2%', 54], ['avocado', '60%', '0%', 58], ['mushroom', '70%', '78%', 56], ['bread', '8%', '80%', 58]
  ];
  $('#heroScene').insertAdjacentHTML('beforeend',
    Art.mascot('excited', 260) +
    floats.map(([f, x, y, s]) => `<div class="float" style="left:${x};top:${y}">${Art.food(f, null, 'happy', s)}</div>`).join('') +
    [['40%', '6%'], ['92%', '34%'], ['28%', '40%'], ['50%', '92%']].map(([x, y], i) =>
      `<div class="sparkle" style="left:${x};top:${y};animation-delay:${i * .5}s">${Art.pixel('sparkle', 18)}</div>`).join(''));

  $$('[data-food]').forEach(el => { el.outerHTML = Art.food(el.dataset.food, null, 'happy', 64); });
  $$('[data-pixel]').forEach(el => { el.outerHTML = Art.pixel(el.dataset.pixel, 40); });
  $('#ctaInner').insertAdjacentHTML('afterbegin', Art.mascot('happy', 110));

  // feature illustrations, made of the same parts the app uses
  const item = (f, name, q, cls, label) => `<div class="demo-item">${Art.food(f, null, cls === 'lv-today' ? 'worried' : 'happy', 40)}<b>${name}</b><span class="small">${q}</span><span class="exp ${cls}">${label}</span></div>`;
  $('#artPantry').innerHTML = item('milk', 'Milk', '1 l', 'lv-soon', '2 days') + item('riceBag', 'Flour', '1 kg', 'lv-fresh', 'Fresh') + item('egg', 'Eggs', '6 pcs', 'lv-fresh', 'Fresh');
  $('#artMatch').innerHTML = `<div style="position:relative">${Art.dish('bowl:#F2C14E,#E4572E,#76B041', 170)}<div class="match good" style="--p:80"><span>80%</span></div></div>
    <div class="subcard" style="max-width:320px"><div class="swap">Butter <span class="arrow">→</span> Olive oil <span class="tag-have">30 g</span></div></div>`;
  $('#artWaste').innerHTML = `<div class="row">${Art.mascot('alert', 110)}<div>${item('leaf', 'Spinach', '150 g', 'lv-today', 'Today')}<br>${item('banana', 'Bananas', '3 pcs', 'lv-soon', '1 day')}</div></div>`;
  $('#artPlan').innerHTML = `<div class="row">${['Mon', 'Tue', 'Wed'].map((d, i) => `<div class="day" style="width:96px"><div class="day-head"><b>${d}</b></div><div class="slots"><div class="meal">${Art.dish(['stack:#F2C14E,#9C6644,#FF9B8E', 'bowl:#F2C14E,#E4572E,#76B041', 'pot:#E4572E,#F28F3B,#76B041'][i], 80)}</div></div></div>`).join('')}</div>`;

  // live recipes from the API (guest access)
  const diets = [['', 'All'], ['VEGAN', 'Vegan'], ['VEGETARIAN', 'Vegetarian'], ['KETO', 'Keto']];
  $('#guestFilters').innerHTML = diets.map(([v, l], i) => `<button class="chip" data-diet="${v}" aria-pressed="${i === 0}">${l}</button>`).join('');
  async function load(diet) {
    const box = $('#guestRecipes');
    box.innerHTML = UI.loading('Opening the recipe box…');
    try {
      const list = await Api.get('/api/recipes' + Api.qs({ diet }), { redirectOn401: false });
      box.innerHTML = list.slice(0, 6).map(r => UI.recipeCard(r)).join('');
    } catch (e) {
      box.innerHTML = UI.empty('jar', "Recipes are napping", e.message);
    }
  }
  $('#guestFilters').addEventListener('click', (e) => {
    const b = e.target.closest('.chip'); if (!b) return;
    $$('.chip', e.currentTarget).forEach(c => c.setAttribute('aria-pressed', String(c === b)));
    load(b.dataset.diet);
  });
  load('');

  // logged-in visitors go straight to their kitchen
  Api.get('/api/auth/me', { redirectOn401: false }).then(me => {
    $('#loginLink').textContent = 'Open my kitchen';
    $('#loginLink').href = '/dashboard.html';
    $('#getStarted').href = '/dashboard.html';
  }).catch(() => {});
})();
