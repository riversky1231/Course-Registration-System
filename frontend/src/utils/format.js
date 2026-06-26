export function safeText(value, fallback = "-") {
  if (value === null || value === undefined) return fallback;
  const text = String(value).trim();
  return text ? text : fallback;
}

export function clonePlain(value) {
  return JSON.parse(JSON.stringify(value ?? {}));
}

export function formatNumber(value) {
  if (value === null || value === undefined || value === "") return "-";
  const numeric = Number(value);
  if (Number.isNaN(numeric)) return String(value);
  return Number.isInteger(numeric) ? String(numeric) : numeric.toFixed(1);
}

export function formatDateTime(value) {
  if (!value) return "-";
  return new Date(value).toLocaleString("zh-CN", { hour12: false });
}

export function formatDate(value) {
  if (!value) return "-";
  return new Date(value).toLocaleDateString("zh-CN");
}

export function formatDateInput(value) {
  if (!value) return "";
  return String(value).slice(0, 10);
}

export function toDateTimeValue(value) {
  if (!value) return null;
  return value.length === 10 ? `${value}T00:00:00` : value;
}

export function normalizeOptionalNumber(value) {
  if (value === null || value === undefined || value === "") return null;
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : null;
}

export function buildQuery(params) {
  const query = new URLSearchParams();
  Object.entries(params || {}).forEach(([key, value]) => {
    if (value === null || value === undefined || value === "") return;
    query.set(key, String(value));
  });
  const text = query.toString();
  return text ? `?${text}` : "";
}

export function emptyPage() {
  return { items: [], total: 0, page: 1, pageSize: 10, totalPages: 0 };
}

export function initials(name) {
  const text = safeText(name, "U");
  return text.slice(0, 1).toUpperCase();
}
