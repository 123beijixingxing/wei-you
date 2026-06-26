import fs from "fs";
import path from "path";
import { defineConfig } from "vite";
import uniH5 from "@dcloudio/uni-h5-vite";
import uni from "@dcloudio/vite-plugin-uni";

process.env.UNI_INPUT_DIR = path.resolve(__dirname);

function ensureLegacyUniPluginSsrFiles() {
  const ssrDir = path.resolve(__dirname, "node_modules/@dcloudio/vite-plugin-uni/lib/ssr");
  if (!fs.existsSync(ssrDir)) {
    fs.mkdirSync(ssrDir, { recursive: true });
  }

  const entryServerPath = path.join(ssrDir, "entry-server.js");
  const definePath = path.join(ssrDir, "define.js");

  if (!fs.existsSync(entryServerPath)) {
    fs.writeFileSync(
      entryServerPath,
      `import { createApp } from "./main.js";

export { createApp };

export default async function render() {
  const appContext = createApp();
  return appContext;
}
`
    );
  }

  if (!fs.existsSync(definePath)) {
    fs.writeFileSync(
      definePath,
      `globalThis.__UNI_SSR_DEFINES__ = __DEFINES__;
globalThis.__UNI_SSR_UNIT__ = __UNIT__;
globalThis.__UNI_SSR_UNIT_RATIO__ = __UNIT_RATIO__;
globalThis.__UNI_SSR_UNIT_PRECISION__ = __UNIT_PRECISION__;
`
    );
  }
}

ensureLegacyUniPluginSsrFiles();

const gatewayTarget = process.env.VITE_GATEWAY_TARGET || "http://localhost:18090";

export default defineConfig({
  define: {
    __UNI_FEATURE_WXS__: false,
    __UNI_FEATURE_WX__: false,
    __UNI_FEATURE_TABBAR__: true,
    __UNI_FEATURE_UNI_CLOUD__: false,
    __UNI_FEATURE_ROUTER_MODE__: JSON.stringify("hash"),
    __UNI_FEATURE_PAGES__: true
  },
  server: {
    host: "0.0.0.0",
    port: 8088,
    proxy: {
      "/api": {
        target: gatewayTarget,
        changeOrigin: true,
        bypass(req) {
          if (/^\/api\/.*\.(js|css|map)(\?.*)?$/i.test(req.url || "")) {
            return req.url;
          }
        }
      },
      "/ws": {
        target: gatewayTarget.replace(/^http/, "ws"),
        ws: true,
        changeOrigin: true
      }
    }
  },
  resolve: {
    alias: {
      vue: path.resolve(__dirname, "node_modules/@dcloudio/uni-h5-vue/dist/vue.runtime.esm.js")
    }
  },
  plugins: [...uniH5(), uni()]
});
