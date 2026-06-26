function hasWindowLocation() {
  return typeof window !== "undefined" && typeof window.location !== "undefined";
}

function normalizeUrl(url) {
  if (!url) {
    return "/pages/chat/index";
  }
  return url.startsWith("/") ? url : `/${url}`;
}

function toHashUrl(url) {
  const normalized = normalizeUrl(url);
  return `${window.location.origin}/#${normalized}`;
}

export function safeReLaunch(url) {
  const normalized = normalizeUrl(url);
  if (hasWindowLocation()) {
    window.location.href = toHashUrl(normalized);
    return;
  }
  uni.reLaunch({ url: normalized });
}

export function safeSwitchTab(url) {
  const normalized = normalizeUrl(url);
  if (hasWindowLocation()) {
    window.location.href = toHashUrl(normalized);
    return;
  }
  uni.switchTab({ url: normalized });
}
