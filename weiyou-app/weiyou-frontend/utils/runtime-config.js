function hasWindowLocation() {
  return typeof window !== "undefined" && typeof window.location !== "undefined";
}

function readEnv(key) {
  if (typeof import.meta !== "undefined" && import.meta.env && import.meta.env[key]) {
    return String(import.meta.env[key]).trim();
  }
  return "";
}

function trimTrailingSlash(value) {
  return String(value || "").replace(/\/+$/, "");
}

function httpProtocol() {
  if (hasWindowLocation()) {
    return window.location.protocol;
  }
  return "http:";
}

function wsProtocol() {
  return httpProtocol() === "https:" ? "wss:" : "ws:";
}

function hostName() {
  if (hasWindowLocation()) {
    return window.location.hostname;
  }
  return "localhost";
}

function hostPort() {
  if (hasWindowLocation()) {
    return window.location.port;
  }
  return "";
}

function isLocalBrowserHost() {
  const host = hostName();
  return host === "localhost" || host === "127.0.0.1";
}

function isFrontendDevPort() {
  return ["8088", "18088", "19088"].includes(hostPort());
}

function shouldUseSameOriginProxy() {
  return hasWindowLocation() && isFrontendDevPort();
}

function isLoopbackUrl(url) {
  return /^https?:\/\/(127\.0\.0\.1|localhost)(:\d+)?/i.test(String(url || ""));
}

export function resolveApiBaseUrl() {
  const envBaseUrl = readEnv("VITE_API_BASE_URL");
  if (envBaseUrl && !(shouldUseSameOriginProxy() && !isLocalBrowserHost() && isLoopbackUrl(envBaseUrl))) {
    return trimTrailingSlash(envBaseUrl);
  }
  if (hasWindowLocation()) {
    if (shouldUseSameOriginProxy()) {
      return `${window.location.origin}/api`;
    }
    return `${httpProtocol()}//${hostName()}:8080/api`;
  }
  return "http://localhost:8080/api";
}

export function resolveWsBaseUrl() {
  const envWsUrl = readEnv("VITE_WS_BASE_URL");
  if (envWsUrl && !(shouldUseSameOriginProxy() && !isLocalBrowserHost() && /^wss?:\/\/(127\.0\.0\.1|localhost)(:\d+)?/i.test(String(envWsUrl || "")))) {
    return trimTrailingSlash(envWsUrl);
  }
  if (hasWindowLocation()) {
    if (shouldUseSameOriginProxy()) {
      return `${wsProtocol()}//${window.location.host}/ws/chat`;
    }
    return `${wsProtocol()}//${hostName()}:8081/ws/chat`;
  }
  return "ws://localhost:8081/ws/chat";
}
