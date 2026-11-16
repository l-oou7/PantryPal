(function () {
  const { $, $$ } = UI;
  $('#brand').innerHTML = `${Art.pixel('pot', 26)}<span>PantryPal</span>`;
  $('#authScene').innerHTML = `${Art.mascot('happy', 300)}<h2>Let's see what's cooking!</h2>`;

  const params = new URLSearchParams(location.search);
  const next = params.get('next');
  const safeNext = next && next.startsWith('/') && !next.startsWith('//') ? next : '/dashboard.html';

  function setTab(register) {
    $('#tabLogin').setAttribute('aria-selected', String(!register));
    $('#tabRegister').setAttribute('aria-selected', String(register));
    $('#loginForm').hidden = register;
    $('#registerForm').hidden = !register;
    document.title = (register ? 'Create account' : 'Log in') + ' · PantryPal';
    $((register ? '#registerForm' : '#loginForm') + ' input').focus();
  }
  $('#tabLogin').onclick = () => setTab(false);
  $('#tabRegister').onclick = () => setTab(true);
  if (params.get('mode') === 'register') setTab(true);

  $('#useDemo').onclick = () => {
    $('#loginForm').email.value = 'demo@pantrypal.app';
    $('#loginForm').password.value = 'Demo1234';
  };

  const pw = $('#registerForm').password;
  pw.addEventListener('input', () => {
    const v = pw.value;
    const ok = { len: v.length >= 8, let: /[A-Za-z]/.test(v), num: /\d/.test(v) };
    $$('#pwRules li').forEach(li => li.classList.toggle('ok', ok[li.dataset.r]));
  });

  $('#loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = e.currentTarget, data = UI.formData(f);
    if (!data.email || !data.password) return UI.toast('Please enter your email and password.', 'error');
    await UI.busy(f.querySelector('[type=submit]'), async () => {
      try {
        await Api.post('/api/auth/login', data, { redirectOn401: false });
        await Api.refreshCsrf();
        location.href = safeNext;
      } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
    });
  });

  $('#registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const f = e.currentTarget, data = UI.formData(f);
    if (data.password !== data.confirmPassword) {
      UI.showFieldErrors(f, { fieldErrors: { confirmPassword: "The two passwords don't match." } });
      return;
    }
    await UI.busy(f.querySelector('[type=submit]'), async () => {
      try {
        await Api.post('/api/auth/register', data, { redirectOn401: false });
        await Api.refreshCsrf();
        location.href = '/dashboard.html?welcome=1';
      } catch (err) { UI.showFieldErrors(f, err); UI.fail(err); }
    });
  });

  // already logged in? skip the form
  Api.get('/api/auth/me', { redirectOn401: false }).then(() => { location.href = safeNext; }).catch(() => {});
})();
