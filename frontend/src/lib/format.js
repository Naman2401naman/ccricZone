export const formatDateTime = (value) => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short'
  }).format(date);
};

export const formatDate = (value) => {
  if (!value) return 'N/A';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return new Intl.DateTimeFormat('en-IN', { dateStyle: 'medium' }).format(date);
};

export const formatMoney = (value, currency = 'INR') => {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency
  }).format(Number.isFinite(amount) ? amount : 0);
};

export const formatNumber = (value) => {
  const amount = Number(value || 0);
  return new Intl.NumberFormat('en-IN').format(Number.isFinite(amount) ? amount : 0);
};

export const shortText = (value, limit = 96) => {
  const text = String(value || '').trim();
  if (text.length <= limit) return text;
  return `${text.slice(0, limit - 3).trimEnd()}...`;
};

export const formatOversFromBallCount = (ballCount = 0) => {
  const safeBallCount = Math.max(0, Number.parseInt(ballCount, 10) || 0);
  const overs = Math.floor(safeBallCount / 6);
  const balls = safeBallCount % 6;
  return `${overs}.${balls}`;
};

export const formatPrizePool = (prizePool) => {
  if (!prizePool) return 'TBD';
  if (typeof prizePool === 'string') return prizePool.trim() || 'TBD';
  if (typeof prizePool === 'object') {
    const total = String(prizePool.total || '').trim();
    const currency = String(prizePool.currency || 'INR').trim();
    return total ? `${currency} ${total}` : 'TBD';
  }
  return 'TBD';
};
