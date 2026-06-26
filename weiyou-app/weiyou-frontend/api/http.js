import {
  clearSession,
  getAccessToken,
  getDeviceId,
  getRefreshToken,
  redirectToLogin,
  saveSession
} from "@/utils/session";
import { resolveApiBaseUrl } from "@/utils/runtime-config";

const BASE_URL = resolveApiBaseUrl();
let refreshPromise = null;

function buildHeaders(options) {
  const token = getAccessToken();
  return {
    "Content-Type": "application/json",
    ...(token && !options.skipAuth ? { Authorization: `Bearer ${token}` } : {}),
    ...(options.header || {})
  };
}

function normalizeError(response) {
  const message = response?.data?.message || response?.errMsg || "request failed";
  const error = new Error(message);
  error.response = response;
  return error;
}

function shouldRefresh(response, options) {
  const responseCode = response?.statusCode;
  const businessCode = response?.data?.code;
  return !options.skipAuth && !options._retried && (responseCode === 401 || businessCode === 401);
}

function handleUnauthorized(response) {
  const responseCode = response?.statusCode;
  const businessCode = response?.data?.code;
  if (responseCode === 401 || businessCode === 401) {
    clearSession();
    redirectToLogin();
  }
}

function sendRequest(options) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${options.url}`,
      method: options.method || "GET",
      data: options.data || {},
      header: buildHeaders(options),
      success: resolve,
      fail: reject
    });
  });
}

function sendUpload(options) {
  return new Promise((resolve, reject) => {
    const token = getAccessToken();
    uni.uploadFile({
      url: `${BASE_URL}${options.url}`,
      filePath: options.filePath,
      name: options.name || "file",
      formData: options.formData || {},
      header: {
        ...(token && !options.skipAuth ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {})
      },
      success: resolve,
      fail: reject
    });
  });
}

async function refreshAccessToken() {
  const refreshToken = getRefreshToken();
  if (!refreshToken) {
    throw new Error("missing refresh token");
  }
  if (!refreshPromise) {
    refreshPromise = new Promise((resolve, reject) => {
      uni.request({
        url: `${BASE_URL}/auth/token/refresh`,
        method: "POST",
        data: { refreshToken },
        header: {
          "Content-Type": "application/json"
        },
        success: (response) => {
          const payload = response?.data;
          const session = payload?.data;
          if (response.statusCode >= 200 && response.statusCode < 300 && payload?.code === 0 && session?.accessToken) {
            saveSession({
              accessToken: session.accessToken,
              refreshToken: session.refreshToken || refreshToken,
              deviceId: getDeviceId()
            });
            resolve(session.accessToken);
            return;
          }
          reject(normalizeError(response));
        },
        fail: (error) => reject(normalizeError(error))
      });
    }).finally(() => {
      refreshPromise = null;
    });
  }
  return refreshPromise;
}

async function retryAfterRefresh(executor, options) {
  try {
    await refreshAccessToken();
    return await executor({
      ...options,
      _retried: true
    });
  } catch (error) {
    clearSession();
    redirectToLogin();
    throw error;
  }
}

async function executeRequest(options) {
  try {
    const response = await sendRequest(options);
    if (response.statusCode >= 200 && response.statusCode < 300) {
      if (response.data && typeof response.data.code === "number") {
        if (response.data.code === 0) {
          return response.data.data;
        }
        if (shouldRefresh(response, options)) {
          return retryAfterRefresh(executeRequest, options);
        }
        handleUnauthorized(response);
        throw normalizeError(response);
      }
      return response.data;
    }
    if (shouldRefresh(response, options)) {
      return retryAfterRefresh(executeRequest, options);
    }
    handleUnauthorized(response);
    throw normalizeError(response);
  } catch (error) {
    if (error?.response) {
      throw error;
    }
    throw normalizeError(error);
  }
}

function parseUploadPayload(response) {
  let payload = response.data;
  if (typeof payload === "string") {
    payload = JSON.parse(payload);
  }
  return payload;
}

async function executeUpload(options) {
  try {
    const response = await sendUpload(options);
    let payload;
    try {
      payload = parseUploadPayload(response);
    } catch (error) {
      throw normalizeError({ data: { message: "invalid upload response" } });
    }
    const wrapped = {
      statusCode: response.statusCode,
      data: payload
    };
    if (response.statusCode >= 200 && response.statusCode < 300 && payload?.code === 0) {
      return payload.data;
    }
    if (shouldRefresh(wrapped, options)) {
      return retryAfterRefresh(executeUpload, options);
    }
    handleUnauthorized(wrapped);
    throw normalizeError(wrapped);
  } catch (error) {
    if (error?.response) {
      throw error;
    }
    throw normalizeError(error);
  }
}

export function request(options) {
  return executeRequest(options);
}

export function upload(options) {
  return executeUpload(options);
}
