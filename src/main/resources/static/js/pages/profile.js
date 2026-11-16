(async function () {
  const { $, $$, esc } = UI;
  const app = $('#app');
  let p, meta;
  try { await UI.requireUser(''); meta = await UI.getMeta(); p = await Api.get('/api/profile'); } catch (e) { return; }

  render();

  function render() {
    app.innerHTML = `
      <div class="page-head"><div><h1>Profile &amp; settings</h1></div><button class="btn ghost" id="logout">Log out</button></div>
      <div class="profile-grid">
        <div class="stack">
          <section class="panel mint">
            <div class="profile-card">${Art.mascot('happy', 96)}
              <div><h2 style="margin:0">${esc(p.displayName)}</h2><p class="muted" style="margin:0">${esc(p.email)}</p>
              <p class="small" style="margin:4px 0 0">Cooking with PantryPal since ${new Date(p.memberSince).toLocaleDateString(undefined, { month: 'long', year: 'numeric' })} · ${p.pantryCount} pantry items · ${p.bookmarkCount} saved recipes</p></div>
            </div>
          </section>
          <section class="panel">
            <h2>Account</h2>
            <form id="account" class="form-grid" novalidate>
              <label class="full">Display name<input name="displayName" value="${esc(p.displayName)}" required maxlength="60"></label>
              <label class="full">Email<input value="${esc(p.email)}" disabled></label>
              <div class="full row-end" style="margin-top:0"><button class="btn coral" type="submit">Save changes</button></div>
            </form>
          </section>
          <section class="panel">
            <h2>Password</h2>
            <form id="password" class="form-grid" novalidate>
              <label class="full">Current password<input type="password" name="currentPassword" autocomplete="current-password" required></label>
              <label class="full">New password<input type="password" name="newPassword" autocomplete="new-password" required maxlength="72"></label>
              <p class="full small muted" style="margin:0">At least 8 characters, with a letter and a number.</p>
              <div class="full row-end" style="margin-top:0"><button class="btn" type="submit">Change password</button></div>
            </form>
          </section>
        </div>
        <div class="stack">
          <section class="panel butter">
            <h2>Dietary preferences</h2>
            <p class="small">Recipes that don't fit are never recommended, and substitutions respect these too.</p>
            <form id="diet" novalidate>
              <div class="chips" role="radiogroup" aria-label="Diet" style="margin-bottom:16px">
                ${meta.diets.map(d => `<button type="button" class="chip" role="radio" data-diet="${d.value}" aria-checked="${d.value === p.dietType}" aria-pressed="${d.value === p.dietType}">${esc(d.label)}</button>`).join('')}
              </div>
              <p style="font-weight:700;margin-bottom:8px">Allergies</p>
              <div class="chips" id="allergies">
                ${meta.allergens.map(a => `<button type="button" class="chip" data-allergen="${a.value}" aria-pressed="${p.allergies.includes(a.value)}">${esc(a.label)}</button>`).join('')}
              </div>
              <div class="row-end"><button class="btn coral" type="submit">Save preferences</button></div>
            </form>
          </section>
          <section class="panel">
            <h2>Notifications</h2>
            <form id="notif" class="form-grid" novalidate>
              <label class="check full"><input type="checkbox" name="expiryAlertsEnabled" ${p.expiryAlertsEnabled ? 'checked' : ''}> Expiry alerts</label>
              <label class="full">Warn me this many days before food expires
                <input type="number" name="expiryThresholdDays" min="1" max="14" value="${p.expiryThresholdDays}"></label>
              <label class="check full"><input type="checkbox" name="celebrationsEnabled" ${p.celebrationsEnabled ? 'checked' : ''}> Celebration animations</label>
              <div class="full row-end" style="margin-top:0"><button class="btn coral" type="submit">Save notification settings</button></div>
            </form>
          </section>
        </div>
      </div>`;
    bind();
  }

  function bind() {
    $('#logout').onclick = UI.logout;
    $$('[data-diet]').forEach(b => b.onclick = () => $$('[data-diet]').forEach(x => { const on = x === b; x.setAttribute('aria-checked', on); x.setAttribute('aria-pressed', on); }));
    $$('[data-allergen]').forEach(b => b.onclick = () => b.setAttribute('aria-pressed', String(b.getAttribute('aria-pressed') !== 'true')));

    $('#account').onsubmit = (e) => save(e, '/api/profile', UI.formData(e.currentTarget), 'Account saved!');
    $('#diet').onsubmit = (e) => save(e, '/api/profile/dietary', {
      dietType: $('[data-diet][aria-checked="true"]').dataset.diet,
      allergies: $$('[data-allergen][aria-pressed="true"]').map(b => b.dataset.allergen)
    }, 'Preferences saved! Your recommendations are updated.');
    $('#notif').onsubmit = (e) => {
      const f = e.currentTarget;
      save(e, '/api/profile/notifications', {
        expiryAlertsEnabled: f.expiryAlertsEnabled.checked,
        expiryThresholdDays: Number(f.expiryThresholdDays.value),
        celebrationsEnabled: f.celebrationsEnabled.checked
      }, 'Notification settings saved!');
    };
    $('#password').onsubmit = async (e) => {
      e.preventDefault();
      const f = e.currentTarget;
      try { await Api.put('/api/profile/password', UI.formData(f)); f.reset(); UI.toast('Password changed.'); }
      catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
    };
  }

  async function save(e, url, body, msg) {
    e.preventDefault();
    const f = e.currentTarget;
    try { p = await Api.put(url, body); render(); UI.toast(msg); }
    catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
  }
})();
