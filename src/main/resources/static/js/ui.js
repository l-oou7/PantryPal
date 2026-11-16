/* Shared UI: app shell (nav), toasts, modals, celebrations, formatting helpers. */
(function () {
  const $ = (s, el = document) => el.querySelector(s);
  const $$ = (s, el = document) => [...el.querySelectorAll(s)];
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  let me = null;
  let meta = null;

  const NAV = [
    ['dashboard', 'Kitchen', 'home', '/dashboard.html'],
    ['pantry', 'Pantry', 'pot', '/pantry.html'],
    ['recipes', 'Recipes', 'book', '/recipes.html'],
    ['planner', 'Planner', 'cal', '/planner.html'],
    ['shopping', 'Shopping', 'cart', '/shopping.html']
  ];

  function shell(active) {
    const header = document.createElement('header');
    header.className = 'topbar';
    header.innerHTML = `
      <a class="brand" href="/dashboard.html" aria-label="PantryPal home">${Art.pixel('pot', 26)}<span>PantryPal</span></a>
      <nav class="mainnav" aria-label="Main">
        ${NAV.map(([k, label, icon, href]) => `<a href="${href}" class="${k === active ? 'on' : ''}" ${k === active ? 'aria-current="page"' : ''}>${Art.pixel(icon, 18)}<span>${label}</span></a>`).join('')}
      </nav>
      <div class="topbar-right">
        <a class="iconbtn bell ${active === 'alerts' ? 'on' : ''}" href="/alerts.html" aria-label="Expiry alerts">${Art.pixel('bell', 20)}<span class="badge" hidden></span></a>
        <div class="usermenu">
          <button class="iconbtn avatar" aria-haspopup="true" aria-expanded="false" aria-label="Account menu">${Art.pixel('user', 18)}</button>
          <div class="menu" hidden>
            <p class="menu-name"></p>
            <a href="/profile.html">Profile &amp; settings</a>
            <a href="/recipes.html?tab=saved">Saved recipes</a>
            <button class="logout">Log out</button>
          </div>
        </div>
      </div>`;
    document.body.prepend(header);

    const btn = $('.avatar', header), menu = $('.menu', header);
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      menu.hidden = !menu.hidden;
      btn.setAttribute('aria-expanded', String(!menu.hidden));
    });
    document.addEventListener('click', () => { menu.hidden = true; btn.setAttribute('aria-expanded', 'false'); });
    $('.logout', header).addEventListener('click', logout);
  }

  async function logout() {
    try { await Api.post('/api/auth/logout'); } catch (e) { /* session may already be gone */ }
    await Api.refreshCsrf().catch(() => {});
    location.href = '/index.html';
  }

  /** Call at the top of every logged-in page. Returns the current user or redirects to login. */
  async function requireUser(active) {
    shell(active);
    me = await Api.get('/api/auth/me');
    $('.menu-name').textContent = me.displayName;
    refreshBell();
    return me;
  }

  /** For pages guests may see: shows the app shell only when logged in. */
  async function optionalUser(active) {
    try {
      me = await Api.get('/api/auth/me', { redirectOn401: false });
      shell(active);
      $('.menu-name').textContent = me.displayName;
      refreshBell();
      return me;
    } catch (e) {
      const h = document.createElement('header');
      h.className = 'topbar';
      h.innerHTML = `<a class="brand" href="/index.html">${Art.pixel('pot', 26)}<span>PantryPal</span></a>
        <div class="topbar-right" style="margin-left:auto"><a class="btn coral small" href="/login.html?next=${encodeURIComponent(location.pathname + location.search)}">Log in to match your pantry</a></div>`;
      document.body.prepend(h);
      return null;
    }
  }

  async function refreshBell() {
    try {
      const { unread } = await Api.get('/api/alerts/unread-count');
      const b = $('.bell .badge');
      if (b) { b.hidden = !unread; b.textContent = unread > 9 ? '9+' : unread; }
    } catch (e) { /* not critical */ }
  }

  async function getMeta() {
    if (!meta) meta = await Api.get('/api/meta');
    return meta;
  }

  /* ---------- toasts ---------- */
  function toast(message, type = 'ok') {
    let host = $('.toasts');
    if (!host) {
      host = document.createElement('div');
      host.className = 'toasts';
      host.setAttribute('role', 'status');
      host.setAttribute('aria-live', 'polite');
      document.body.appendChild(host);
    }
    const t = document.createElement('div');
    t.className = 'toast toast-' + type;
    t.innerHTML = `${type === 'error' ? Art.food('tomato', null, 'worried', 30) : Art.pixel(type === 'yay' ? 'star' : 'sparkle', 20)}<span>${esc(message)}</span>`;
    host.appendChild(t);
    setTimeout(() => { t.classList.add('out'); setTimeout(() => t.remove(), 300); }, type === 'error' ? 5200 : 3400);
  }
  const fail = (e) => toast(e && e.message ? e.message : 'Something went wrong.', 'error');

  /* ---------- celebration: pixel confetti burst ---------- */
  function celebrate(message) {
    if (message) toast(message, 'yay');
    if (reduceMotion || (me && me.celebrationsEnabled === false)) return;
    const host = document.createElement('div');
    host.className = 'confetti';
    const icons = ['star', 'heart', 'sparkle', 'star'];
    for (let i = 0; i < 22; i++) {
      const p = document.createElement('span');
      p.innerHTML = Art.pixel(icons[i % icons.length], 14 + Math.random() * 10);
      p.style.left = (35 + Math.random() * 30) + 'vw';
      p.style.setProperty('--dx', (Math.random() * 60 - 30) + 'vw');
      p.style.setProperty('--rot', (Math.random() * 540 - 270) + 'deg');
      p.style.animationDelay = (Math.random() * .15) + 's';
      host.appendChild(p);
    }
    document.body.appendChild(host);
    setTimeout(() => host.remove(), 1600);
  }

  /* ---------- modal ---------- */
  function modal(title, bodyHtml, { onOpen, wide } = {}) {
    const back = document.createElement('div');
    back.className = 'modal-back';
    back.innerHTML = `<div class="modal ${wide ? 'wide' : ''}" role="dialog" aria-modal="true" aria-labelledby="modal-title">
      <div class="modal-head"><h2 id="modal-title">${esc(title)}</h2><button class="iconbtn close" aria-label="Close">✕</button></div>
      <div class="modal-body">${bodyHtml}</div></div>`;
    const prevFocus = document.activeElement;
    const close = () => { back.remove(); document.removeEventListener('keydown', onKey); prevFocus && prevFocus.focus && prevFocus.focus(); };
    const onKey = (e) => { if (e.key === 'Escape') close(); };
    back.addEventListener('mousedown', (e) => { if (e.target === back) close(); });
    $('.close', back).addEventListener('click', close);
    document.addEventListener('keydown', onKey);
    document.body.appendChild(back);
    const first = $('input, select, textarea, button:not(.close)', back);
    (first || $('.close', back)).focus();
    if (onOpen) onOpen(back, close);
    return { el: back, close };
  }

  function confirmBox(title, text, okLabel = 'Yes, do it') {
    return new Promise((resolve) => {
      const m = modal(title, `<p>${esc(text)}</p><div class="row-end"><button class="btn ghost no">Cancel</button><button class="btn coral yes">${esc(okLabel)}</button></div>`);
      $('.no', m.el).onclick = () => { m.close(); resolve(false); };
      $('.yes', m.el).onclick = () => { m.close(); resolve(true); };
      $('.yes', m.el).focus();
    });
  }

  /* ---------- form helpers ---------- */
  function formData(form) {
    const o = {};
    new FormData(form).forEach((v, k) => { o[k] = typeof v === 'string' ? v.trim() : v; });
    return o;
  }
  function showFieldErrors(form, err) {
    $$('.field-error', form).forEach(e => e.remove());
    $$('[aria-invalid]', form).forEach(e => e.removeAttribute('aria-invalid'));
    Object.entries((err && err.fieldErrors) || {}).forEach(([name, msg]) => {
      const input = form.elements[name];
      if (!input) return;
      input.setAttribute('aria-invalid', 'true');
      const p = document.createElement('p');
      p.className = 'field-error';
      p.textContent = msg;
      (input.closest('label') || input).after(p);
    });
  }
  async function busy(button, fn) {
    const label = button.innerHTML;
    button.disabled = true;
    button.classList.add('loading');
    try { return await fn(); } finally { button.disabled = false; button.classList.remove('loading'); button.innerHTML = label; }
  }

  /* ---------- formatting ---------- */
  function qty(n) { return Number.isInteger(n) ? String(n) : String(Math.round(n * 100) / 100); }
  function expiryText(days) {
    if (days < 0) return days === -1 ? 'Expired yesterday' : `Expired ${-days} days ago`;
    if (days === 0) return 'Expires today';
    if (days === 1) return 'Expires tomorrow';
    if (days <= 14) return `${days} days left`;
    const weeks = Math.round(days / 7);
    return weeks < 9 ? `${weeks} weeks left` : 'Keeps for ages';
  }
  const levelClass = { EXPIRED: 'lv-expired', EXPIRES_TODAY: 'lv-today', EXPIRING_SOON: 'lv-soon', FRESH: 'lv-fresh' };
  const levelMood = { EXPIRED: 'worried', EXPIRES_TODAY: 'worried', EXPIRING_SOON: 'happy', FRESH: 'happy' };
  const pretty = (s) => s ? s.charAt(0) + s.slice(1).toLowerCase().replace(/_/g, ' ') : '';
  const money = (v, cur) => `${cur || 'AED'} ${Number(v || 0).toFixed(2)}`;
  const isoDate = (d) => {
    const z = new Date(d.getTime() - d.getTimezoneOffset() * 60000);
    return z.toISOString().slice(0, 10);
  };
  const niceDate = (iso, opts = { weekday: 'short', day: 'numeric', month: 'short' }) =>
    new Date(iso + 'T00:00:00').toLocaleDateString(undefined, opts);

  /** Recipe card used in lists. */
  function recipeCard(r, { compact = false } = {}) {
    const hasMatch = r.matchPercent !== null && r.matchPercent !== undefined;
    const ring = hasMatch ? `<div class="match ${r.matchPercent === 100 ? 'full' : r.matchPercent >= 60 ? 'good' : 'low'}"
        style="--p:${r.matchPercent}" title="Ingredient match"><span>${r.matchPercent}%</span></div>` : '';
    return `<article class="rcard ${compact ? 'compact' : ''}">
      <a class="rcard-link" href="/recipe.html?id=${r.id}" aria-label="${esc(r.name)}">
        <div class="rcard-art">${Art.dish(r.illustration, compact ? 92 : 128)}${ring}</div>
        <div class="rcard-body">
          <h3>${esc(r.name)}</h3>
          ${r.reason ? `<p class="reason">${esc(r.reason)}</p>` : `<p class="desc">${esc(r.description)}</p>`}
          <ul class="facts">
            <li>${Art.pixel('clock', 13)} ${r.totalMinutes} min</li>
            <li>${pretty(r.difficulty)}</li>
            <li>Serves ${r.servings}</li>
            <li class="diet diet-${r.dietaryCategory}">${pretty(r.dietaryCategory)}</li>
          </ul>
          ${hasMatch && r.missingCount > 0 ? `<p class="missing">Missing or short: ${r.missingCount}</p>` : ''}
          ${hasMatch && r.missingCount === 0 ? `<p class="ready">${Art.pixel('star', 13)} Ready to cook</p>` : ''}
        </div>
      </a>
    </article>`;
  }

  function empty(food, title, text, actionHtml = '') {
    return `<div class="empty">${Art.food(food, null, 'sleepy', 84)}<h3>${esc(title)}</h3><p>${esc(text)}</p>${actionHtml}</div>`;
  }

  function loading(text = 'Stirring the pot…') {
    return `<div class="loading-state">${Art.pixel('pot', 40)}<p>${esc(text)}</p></div>`;
  }

  window.UI = { $, $$, esc, shell, requireUser, optionalUser, getMeta, toast, fail, celebrate, modal, confirmBox, formData,
    showFieldErrors, busy, qty, expiryText, levelClass, levelMood, pretty, money, isoDate, niceDate, recipeCard,
    empty, loading, refreshBell, logout, get me() { return me; } };
})();
