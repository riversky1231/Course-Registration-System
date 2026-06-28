import { defineStore } from "pinia";
import { authApi } from "@/api";
import { onUnauthorized } from "@/api/request";
import { ROLE_LABELS } from "@/constants/modules";
import { safeText } from "@/utils/format";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    session: null,
    ready: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.session),
    role: (state) => state.session?.role || "",
    roleLabel: (state) => ROLE_LABELS[state.session?.role] || "课程资源工作台",
    displayName: (state) => safeText(state.session?.displayName || state.session?.username, "用户"),
  },
  actions: {
    async bootstrap() {
      try {
        this.session = await authApi.status();
      } catch (error) {
        this.session = null;
      } finally {
        this.ready = true;
      }
      return this.session;
    },
    async login(form) {
      await authApi.login(form);
      this.session = await authApi.me();
      return this.session;
    },
    async register(form) {
      await authApi.register({ ...form, role: "student" });
    },
    async logout() {
      try {
        await authApi.logout();
      } finally {
        this.session = null;
      }
    },
    setDisplayName(name) {
      if (this.session) {
        this.session.displayName = name;
      }
    },
    clear() {
      this.session = null;
    },
  },
});

// 注册 401 回调：被踢下线时清空会话。
export function registerAuthGuards(store) {
  onUnauthorized(() => {
    store.clear();
  });
}
