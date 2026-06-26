import axios from "axios";
import { ElMessage } from "element-plus";

// 统一的 axios 实例：携带会话 Cookie、自动附加 CSRF Token、统一解包 ApiResponse。
const request = axios.create({
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
  timeout: 20000,
});

function getCookie(name) {
  const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : null;
}

// 401 时触发，由 store 注册，避免循环依赖。
let unauthorizedHandler = null;
export function onUnauthorized(handler) {
  unauthorizedHandler = handler;
}

request.interceptors.request.use((config) => {
  const token = getCookie("XSRF-TOKEN");
  if (token) {
    config.headers["X-XSRF-TOKEN"] = token;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && typeof payload === "object" && "success" in payload) {
      if (payload.success === false) {
        return Promise.reject(new Error(payload.message || "请求失败"));
      }
      return payload.data;
    }
    return payload;
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401 && unauthorizedHandler) {
        unauthorizedHandler();
      }
      const data = error.response.data;
      const message = (data && data.message) || `请求失败(${error.response.status})`;
      return Promise.reject(new Error(message));
    }
    return Promise.reject(error);
  }
);

// 顶层通用错误提示（按需调用）。
export function toastError(error) {
  ElMessage({ type: "error", message: error?.message || "操作失败", showClose: true });
}

export default request;
