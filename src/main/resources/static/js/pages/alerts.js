(async function () {
  const { $, esc } = UI;
  const app = $('#app');
  try { await UI.requireUser('alerts'); } catch (e) { return; }

  app.innerHTML = `
    <div class="page-head">
      <div><h1>Expiry alerts</h1><p>PantryPal checks your pantry every hour. Here's what it found.</p></div>
      <div class="row"><button class="btn" id="scan">${Art.pixel('bell', 18)} Check now</button><button class="btn ghost" id="readAll">Mark all as read</button></div>
    </div>
    <div class="dash-grid">
      <section class="panel"><h2>Notifications</h2><div id="alerts">${UI.loading()}</div></section>
      <section class="panel butter"><h2>Use these first</h2><p class="small">Ranked by waste risk: how soon it expires and how much of it there is.</p><div id="risk">${UI.loading()}</div></section>
    </div>`;

  $('#scan').onclick = (e) => UI.busy(e.currentTarget, async () => {
    try { const r = await Api.post('/api/alerts/scan'); UI.toast(r.message); load(); } catch (err) { UI.fail(err); }
  });
  $('#readAll').onclick = async () => { try { await Api.post('/api/alerts/read-all'); load(); } catch (err) { UI.fail(err); } };
  $('#alerts').addEventListener('click', async (e) => {
    const b = e.target.closest('[data-read]'); if (!b) return;
    try { await Api.post(`/api/alerts/${b.dataset.read}/read`); load(); } catch (err) { UI.fail(err); }
  });
  load();

  async function load() {
    try {
      const [a, risk] = await Promise.all([Api.get('/api/alerts'), Api.get('/api/pantry/risk')]);
      $('#alerts').innerHTML = a.notifications.length ? `<ul class="alert-list">${a.notifications.map(n => `
        <li class="alert lv-${n.level} ${n.read ? 'read' : 'unread'}">
          ${Art.food(n.ingredientName, null, n.level === 'EXPIRING_SOON' ? 'happy' : 'worried', 48)}
          <div><b>${esc(n.message)}</b><div class="when">${new Date(n.createdAt).toLocaleString(undefined, { weekday: 'short', hour: '2-digit', minute: '2-digit' })} · risk ${n.riskScore}</div></div>
          ${n.read ? '' : `<button class="btn small ghost" data-read="${n.id}">Mark read</button>`}
        </li>`).join('')}</ul>`
        : UI.empty('leaf', 'All fresh!', 'No alerts right now. We\'ll let you know when something needs using.');

      const top = risk.filter(i => i.wasteRisk > 0).slice(0, 10);
      $('#risk').innerHTML = top.length ? `<table class="risk-table"><tbody>${top.map(i => `
        <tr><td style="width:48px">${Art.food(i.name, i.category, UI.levelMood[i.alertLevel], 36)}</td>
          <td><b>${esc(i.name)}</b><br><span class="small">${UI.qty(i.quantity)} ${esc(i.unitSymbol)} · ${UI.expiryText(i.daysLeft)}</span></td>
          <td style="text-align:right"><span class="risk-num">${i.wasteRisk}</span></td></tr>`).join('')}</tbody></table>
          <div class="row-end"><a class="btn coral small" href="/recipes.html?strategy=expiring-first">Recipes that use these</a></div>`
        : '<p class="muted">Nothing at risk. Lovely!</p>';
      UI.refreshBell();
    } catch (e) { UI.fail(e); }
  }
})();
