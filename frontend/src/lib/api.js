const DEFAULT_API_BASE = 'http://localhost:8080/api';
const API_STORAGE_KEY = 'criczone_api_base';

export const normalizeApiBase = (value = '') => String(value || '').trim().replace(/\/+$/, '');

export const isNativePlatform = () => {
  try {
    return Boolean(
      window.Capacitor &&
        typeof window.Capacitor.isNativePlatform === 'function' &&
        window.Capacitor.isNativePlatform()
    );
  } catch {
    return false;
  }
};

export const normalizeApiInput = (inputValue = '') => {
  let normalized = normalizeApiBase(inputValue);
  if (!normalized) return '';
  if (!/^https?:\/\//i.test(normalized)) return '';
  if (!/\/api$/i.test(normalized)) normalized = `${normalized}/api`;
  return normalized;
};

export const readStoredApiBase = () => {
  try {
    return normalizeApiBase(localStorage.getItem(API_STORAGE_KEY));
  } catch {
    return '';
  }
};

export const saveApiBase = (apiBase) => {
  const normalized = normalizeApiInput(apiBase);
  if (!normalized) return false;
  localStorage.setItem(API_STORAGE_KEY, normalized);
  return true;
};

export const getApiBase = () => {
  const stored = readStoredApiBase();
  if (stored) {
    try {
      const storedUrl = new URL(stored);
      const isStoredLocalhost = storedUrl.hostname === 'localhost' || storedUrl.hostname === '127.0.0.1';
      if (isStoredLocalhost && storedUrl.port !== '8080') {
        localStorage.removeItem(API_STORAGE_KEY);
      } else {
        return stored;
      }
    } catch {
      return stored;
    }
  }

  const runtime = normalizeApiBase(window.__API_BASE__);
  const envBase = normalizeApiBase(import.meta.env.VITE_API_BASE_URL);
  const override = runtime || envBase;

  const { protocol, hostname, origin, port } = window.location;
  const isHttp = protocol === 'http:' || protocol === 'https:';
  const isLocalhost = hostname === 'localhost' || hostname === '127.0.0.1';
  const isLocalOverride = /^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?\/api$/i.test(override);

  if (isHttp && isLocalhost && !isNativePlatform()) {
    if (override && isLocalOverride) {
      const overrideUrl = new URL(override);
      if (overrideUrl.port === '8080') return override;
    }
    if (port !== '8080') return `${protocol}//${hostname}:8080/api`;
    return `${origin}/api`;
  }

  if (override) {
    const isSecureWeb = protocol === 'https:' && !isNativePlatform();
    const isInsecureOverride = /^http:\/\//i.test(override);
    if (!(isSecureWeb && isInsecureOverride)) return override;
  }

  if (isHttp) return `${origin}/api`;
  return DEFAULT_API_BASE;
};

export const buildUrl = (path, query) => {
  const base = getApiBase();
  const url = new URL(`${base}${path.startsWith('/') ? path : `/${path}`}`);
  if (query && typeof query === 'object') {
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        url.searchParams.set(key, String(value));
      }
    });
  }
  return url.toString();
};

export async function apiRequest(path, { method = 'GET', body, token, query, responseType = 'json' } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json';
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(buildUrl(path, query), {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  if (responseType === 'blob') {
    if (!response.ok) {
      const message = await response.text().catch(() => '');
      throw new Error(message || `Request failed (${response.status})`);
    }
    return response.blob();
  }

  const text = await response.text();
  let payload = {};
  if (text) {
    try {
      payload = JSON.parse(text);
    } catch {
      payload = { raw: text };
    }
  }

  if (!response.ok) {
    const message =
      payload?.message ||
      payload?.error ||
      payload?.raw ||
      `Request failed (${response.status})`;
    throw new Error(message);
  }

  return payload;
}
