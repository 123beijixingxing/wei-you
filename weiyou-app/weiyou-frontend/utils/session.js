import { safeReLaunch } from "@/utils/navigation";

const ACCESS_TOKEN_KEY = "weiyou_access_token";
const REFRESH_TOKEN_KEY = "weiyou_refresh_token";
const DEVICE_ID_KEY = "weiyou_device_id";

export function getAccessToken() {
  return uni.getStorageSync(ACCESS_TOKEN_KEY) || "";
}

export function getRefreshToken() {
  return uni.getStorageSync(REFRESH_TOKEN_KEY) || "";
}

export function getDeviceId() {
  return uni.getStorageSync(DEVICE_ID_KEY) || "";
}

export function ensureDeviceId() {
  let deviceId = getDeviceId();
  if (!deviceId) {
    deviceId = `device-${Date.now()}-${Math.floor(Math.random() * 100000)}`;
    uni.setStorageSync(DEVICE_ID_KEY, deviceId);
  }
  return deviceId;
}

export function saveSession({ accessToken, refreshToken, deviceId }) {
  if (accessToken) {
    uni.setStorageSync(ACCESS_TOKEN_KEY, accessToken);
  }
  if (refreshToken) {
    uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken);
  }
  if (deviceId) {
    uni.setStorageSync(DEVICE_ID_KEY, deviceId);
  }
}

export function clearSession() {
  uni.removeStorageSync(ACCESS_TOKEN_KEY);
  uni.removeStorageSync(REFRESH_TOKEN_KEY);
}

export function isLoggedIn() {
  return Boolean(getAccessToken());
}

export function redirectToLogin() {
  const pages = getCurrentPages();
  const currentRoute = pages.length ? `/${pages[pages.length - 1].route}` : "";
  if (currentRoute === "/pages/auth/login") {
    return;
  }
  if (typeof window !== "undefined") {
    const app = typeof getApp === "function" ? getApp() : null;
    if (!app || !app.$router) {
      safeReLaunch("/pages/auth/login");
      return;
    }
  }
  safeReLaunch("/pages/auth/login");
}
