import { resolveWsBaseUrl } from "@/utils/runtime-config";
import { getAccessToken, getDeviceId } from "@/utils/session";

const SOCKET_URL = resolveWsBaseUrl();
const HEARTBEAT_INTERVAL = 25000;
const RECONNECT_BASE_DELAY = 2000;
const RECONNECT_MAX_DELAY = 15000;
const RECONNECT_MAX_ATTEMPTS = 6;

let socketTask = null;
let connected = false;
let connecting = false;
let manualClose = false;
let connectPromise = null;
let heartbeatTimer = null;
let reconnectTimer = null;
let reconnectAttempts = 0;
let lastCredentials = {
  token: "",
  deviceId: ""
};
const listeners = new Set();

function emit(payload) {
  listeners.forEach((listener) => {
    try {
      listener(payload);
    } catch (error) {
      console.error("chat socket listener error", error);
    }
  });
}

function clearHeartbeat() {
  if (heartbeatTimer) {
    clearInterval(heartbeatTimer);
    heartbeatTimer = null;
  }
}

function clearReconnectTimer() {
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}

function cleanupConnectionState() {
  connected = false;
  connecting = false;
  connectPromise = null;
  clearHeartbeat();
}

function resolveReconnectCredentials() {
  return {
    token: getAccessToken() || lastCredentials.token,
    deviceId: getDeviceId() || lastCredentials.deviceId
  };
}

function scheduleReconnect(reason = "socket closed") {
  if (manualClose || reconnectTimer || connecting || connected) {
    return;
  }
  const credentials = resolveReconnectCredentials();
  if (!credentials.token || !credentials.deviceId) {
    emit({ event: "SOCKET_RECONNECT_ABORTED", data: { reason: "missing credentials" } });
    return;
  }
  if (reconnectAttempts >= RECONNECT_MAX_ATTEMPTS) {
    emit({ event: "SOCKET_RECONNECT_GIVEUP", data: { attempt: reconnectAttempts, reason } });
    return;
  }
  reconnectAttempts += 1;
  const attempt = reconnectAttempts;
  const delay = Math.min(RECONNECT_BASE_DELAY * (2 ** (attempt - 1)), RECONNECT_MAX_DELAY);
  emit({ event: "SOCKET_RECONNECT_SCHEDULED", data: { attempt, delay, reason } });
  reconnectTimer = setTimeout(async () => {
    reconnectTimer = null;
    emit({ event: "SOCKET_RECONNECTING", data: { attempt } });
    const nextCredentials = resolveReconnectCredentials();
    try {
      await chatSocket.connect({
        token: nextCredentials.token,
        deviceId: nextCredentials.deviceId
      });
      emit({ event: "SOCKET_RECONNECTED", data: { attempt } });
    } catch (error) {
      emit({
        event: "SOCKET_RECONNECT_FAILED",
        data: {
          attempt,
          message: error?.message || "reconnect failed"
        }
      });
    }
  }, delay);
}

function startHeartbeat() {
  clearHeartbeat();
  heartbeatTimer = setInterval(() => {
    if (!connected || !socketTask) {
      return;
    }
    chatSocket.send({
      event: "HEARTBEAT",
      requestId: `hb-${Date.now()}`,
      data: {}
    }).catch(() => {});
  }, HEARTBEAT_INTERVAL);
}

function parseMessage(data) {
  if (typeof data !== "string") {
    return data;
  }
  try {
    return JSON.parse(data);
  } catch (error) {
    return {
      event: "RAW_MESSAGE",
      data
    };
  }
}

export const chatSocket = {
  subscribe(listener) {
    listeners.add(listener);
    return () => listeners.delete(listener);
  },
  isConnected() {
    return connected;
  },
  isConnecting() {
    return connecting;
  },
  async connect({ token, deviceId }) {
    if (connected) {
      return true;
    }
    if (connectPromise) {
      return connectPromise;
    }
    if (!token || !deviceId) {
      throw new Error("missing websocket credentials");
    }

    lastCredentials = { token, deviceId };
    manualClose = false;
    connecting = true;
    clearReconnectTimer();
    connectPromise = new Promise((resolve, reject) => {
      let settled = false;
      const url = `${SOCKET_URL}?token=${encodeURIComponent(token)}&deviceId=${encodeURIComponent(deviceId)}`;
      socketTask = uni.connectSocket({ url, complete: () => {} });

      socketTask.onOpen(() => {
        connected = true;
        connecting = false;
        reconnectAttempts = 0;
        startHeartbeat();
        emit({ event: "SOCKET_OPEN", data: { deviceId } });
        if (!settled) {
          settled = true;
          resolve(true);
        }
      });

      socketTask.onMessage((message) => {
        emit(parseMessage(message.data));
      });

      socketTask.onError((error) => {
        emit({ event: "SOCKET_ERROR", data: error });
        cleanupConnectionState();
        scheduleReconnect(error?.errMsg || "websocket error");
        if (!settled) {
          settled = true;
          reject(new Error(error.errMsg || "websocket error"));
        }
      });

      socketTask.onClose((closeEvent) => {
        const wasManual = manualClose;
        cleanupConnectionState();
        emit({ event: wasManual ? "SOCKET_CLOSED" : "SOCKET_CLOSE", data: closeEvent });
        if (!wasManual) {
          scheduleReconnect(closeEvent?.reason || "websocket closed");
        }
        if (!settled && !wasManual) {
          settled = true;
          reject(new Error(closeEvent.reason || "websocket closed"));
        }
      });
    });

    try {
      await connectPromise;
      return true;
    } finally {
      connectPromise = null;
    }
  },
  async send(payload) {
    if (!socketTask || !connected) {
      throw new Error("websocket not connected");
    }
    return new Promise((resolve, reject) => {
      socketTask.send({
        data: JSON.stringify(payload),
        success: resolve,
        fail: (error) => reject(new Error(error.errMsg || "websocket send failed"))
      });
    });
  },
  disconnect() {
    manualClose = true;
    clearHeartbeat();
    clearReconnectTimer();
    reconnectAttempts = 0;
    if (socketTask) {
      try {
        socketTask.close({ code: 1000, reason: "client logout" });
      } catch (error) {
        console.warn("chat socket close warning", error);
      }
    }
    socketTask = null;
    cleanupConnectionState();
  }
};
