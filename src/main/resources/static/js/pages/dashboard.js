(async function () {
  const { $, esc } = UI;
  const app = $('#app');
  try {
    await UI.requireUser('dashboard');
    const d = await Api.get('/api/dashboard');
    render(d);
    if (new URLSearchParams(location.search).get('welcome')) UI.celebrate('Welcome to PantryPal! Let\'s fill your pantry.');
  } catch (e) {
    if (e.status !== 401) app.innerHTML = UI.empty('jar', "The kitchen didn't open", e.message);
  }

  function render(d) {
    const slotName = { BREAKFAST: 'Breakfast', LUNCH: 'Lunch', DINNER: 'Dinner' };
    app.innerHTML = `
      <section class="hello">
        ${Art.mascot(d.mascotMood, 130)}
        <div class="bubble"><h1>Hi, ${esc(d.displayName.split(' ')[0])}!</h1><p>${esc(d.greeting)}</p></div>
        <div class="quick">
          <a class="btn coral" href="/pantry.html?add=1">${Art.pixel('pot', 18)} Add ingredient</a>
          <a class="btn butter" href="/recipes.html">${Art.pixel('book', 18)} Discover recipes</a>
        </div>
      </section>

      <section class="stats" aria-label="Kitchen at a glance">
        <a class="stat" href="/pantry.html"><span class="num">${d.totalIngredients}</span><span class="lbl">Pantry ingredients</span><span class="sub">of ${d.maxIngredients} spaces</span></a>
        <a class="stat" href="/alerts.html"><span class="num">${d.expiringSoon}</span><span class="lbl">Expiring soon</span><span class="sub">${d.expiredCount ? d.expiredCount + ' already expired' : 'within ' + d.thresholdDays + ' days'}</span></a>
        <a class="stat" href="/recipes.html"><span class="num">${d.cookableNow}</span><span class="lbl">Recipes ready to cook</span><span class="sub">${d.almostCookable} more at 75%+</span></a>
        <div class="stat"><span class="num">${Math.round(d.estimatedSavings)}</span><span class="lbl">${esc(d.currency)} estimated savings</span><span class="sub">value of pantry food you used</span></div>
      </section>

      <div class="dash-grid">
        <div class="stack">
          <section class="panel butter">
            <div class="panel-title"><h2>${Art.pixel('heart', 22)} Your Ingredients Need Some Love!</h2><a class="btn small" href="/alerts.html">All alerts</a></div>
            ${d.needsLove.length ? `<ul class="love-list">${d.needsLove.map(i => `
              <li class="love-item">${Art.food(i.name, i.category, UI.levelMood[i.alertLevel], 48)}
                <div><b>${esc(i.name)}</b><div class="meta">${UI.qty(i.quantity)} ${esc(i.unitSymbol)} · waste risk ${i.wasteRisk}</div></div>
                <span class="exp ${UI.levelClass[i.alertLevel]}">${UI.expiryText(i.daysLeft)}</span></li>`).join('')}</ul>
              <div class="row-end"><a class="btn coral small" href="/recipes.html?strategy=expiring-first">Find recipes that use these</a></div>`
              : `<p class="muted">Nothing is close to its date. Your pantry is in great shape!</p>`}
          </section>

          <section>
            <div class="panel-title"><h2>${Art.pixel('star', 22)} Recommended for you</h2><a class="btn small" href="/recipes.html">See all</a></div>
            ${d.recommended.length ? `<div class="rgrid">${d.recommended.map(r => UI.recipeCard(r, { compact: true })).join('')}</div>`
              : UI.empty('egg', 'No recipes yet', 'Add a few ingredients and PantryPal will find recipes for you.', '<a class="btn coral" href="/pantry.html?add=1">Add ingredients</a>')}
          </section>
        </div>

        <div class="stack">
          <section class="panel mint">
            <div class="panel-title"><h2>${Art.pixel('leaf', 22)} Food waste reduction</h2></div>
            ${d.itemsUsed + d.itemsWasted === 0
              ? `<p>Use or cook something from your pantry and your progress shows up here.</p>`
              : `<div class="progress-wrap"><div class="pbar" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="${d.wasteReductionPercent}" aria-label="Share of food used instead of wasted"><span style="--v:${d.wasteReductionPercent}"></span></div>
                 <span class="big-pct">${d.wasteReductionPercent}%</span></div>
                 <p class="small" style="margin-top:8px">of your pantry food (by value) was eaten rather than thrown away.</p>`}
            <div class="minis">
              <div class="mini"><b>${d.itemsUsed}</b><span>times used</span></div>
              <div class="mini"><b>${d.itemsRescued}</b><span>rescued near expiry</span></div>
              <div class="mini"><b>${d.itemsWasted}</b><span>thrown away</span></div>
            </div>
            <p class="small" style="margin:12px 0 0">Rescued food is worth about <b>${UI.money(d.rescuedValue, d.currency)}</b>.</p>
          </section>

          <section class="panel">
            <div class="panel-title"><h2>${Art.pixel('cal', 22)} Today's menu</h2><a class="btn small" href="/planner.html">Planner</a></div>
            ${d.todaysMeals.length ? `<ul class="today-meals">${d.todaysMeals.map(m => `
              <li><a href="/recipe.html?id=${m.recipeId}&servings=${m.servings}&plan=${m.id}">${Art.dish(m.illustration, 56)}
                <div><span class="slot">${slotName[m.mealType]}</span><br><b>${esc(m.recipeName)}</b><br><span class="small muted">${m.cooked ? 'Cooked' : m.matchPercent + '% in your pantry'}</span></div></a></li>`).join('')}</ul>`
              : `<p class="muted">Nothing planned for today.</p><a class="btn mint small" href="/planner.html">Plan a meal</a>`}
          </section>
        </div>
      </div>`;
  }
})();
