/* REST client for the PantryPal Spring Boot API.
   Handles the CSRF token, JSON, friendly errors and redirects to login on 401. */
(function () {
  let csrf = null;

  async function loadCsrf(force) {
    if (csrf && !force) return csrf;
    const res = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
    csrf = await res.json();
    return csrf;
  }

  class ApiError extends Error {
    constructor(status, message, fieldErrors) {
      super(message);
      this.status = status;
      this.fieldErrors = fieldErrors || {};
    }
  }

  async function request(method, url, body, { retry = true, redirectOn401 = true } = {}) {
    const headers = { 'Accept': 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (method !== 'GET') {
      const t = await loadCsrf();
      headers[t.headerName] = t.token;
    }
    let res;
    try {
      res = await fetch(url, { method, headers, credentials: 'same-origin',
        body: body === undefined ? undefined : JSON.stringify(body) });
    } catch (e) {
      throw new ApiError(0, "Can't reach the PantryPal server. Is it running?");
    }
    if (res.status === 403 && method !== 'GET' && retry) {
      await loadCsrf(true); // token rotated after login/logout
      return request(method, url, body, { retry: false, redirectOn401 });
    }
    if (res.status === 401 && redirectOn401 && !location.pathname.endsWith('/login.html')) {
      location.href = '/login.html?next=' + encodeURIComponent(location.pathname + location.search);
      throw new ApiError(401, 'Please log in first.');
    }
    if (res.status === 204) return null;
    const text = await res.text();
    const data = text ? JSON.parse(text) : null;
    if (!res.ok) throw new ApiError(res.status, (data && data.message) || 'Something went wrong.', data && data.fieldErrors);
    return data;
  }

  window.Api = {
    ApiError,
    get: (u, o) => request('GET', u, undefined, o),
    post: (u, b, o) => request('POST', u, b === undefined ? {} : b, o),
    put: (u, b) => request('PUT', u, b),
    patch: (u, b) => request('PATCH', u, b),
    del: (u) => request('DELETE', u),
    refreshCsrf: () => loadCsrf(true),
    qs(params) {
      const p = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '');
      return p.length ? '?' + new URLSearchParams(p).toString() : '';
    }
  };
})();
