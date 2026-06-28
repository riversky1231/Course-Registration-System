import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/auth";
import { getViewsForRole } from "@/constants/modules";

const routes = [
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue"),
    meta: { public: true },
  },
  {
    path: "/",
    component: () => import("@/layouts/MainLayout.vue"),
    redirect: "/dashboard",
    children: [
      { path: "dashboard", name: "dashboard", component: () => import("@/views/DashboardView.vue"), meta: { view: "dashboard" } },
      { path: "assistant", name: "assistant", component: () => import("@/views/AiView.vue"), meta: { view: "assistant" } },
      { path: "profile", name: "profile", component: () => import("@/views/ProfileView.vue"), meta: { view: "profile" } },
      { path: "grades", name: "gradeReport", component: () => import("@/views/GradeReportView.vue"), meta: { view: "gradeReport" } },
      { path: "courses", name: "courses", component: () => import("@/views/CourseCenterView.vue"), meta: { view: "courses" } },
      { path: "selections", name: "selections", component: () => import("@/views/SelectionView.vue"), meta: { view: "selections" } },
      { path: "teachers", name: "teachers", component: () => import("@/views/PeopleView.vue"), meta: { view: "teachers" } },
      { path: "admins", name: "admins", component: () => import("@/views/AdminView.vue"), meta: { view: "admins" } },
      { path: "selection-windows", name: "selectionWindows", component: () => import("@/views/SelectionWindowView.vue"), meta: { view: "selectionWindows" } },
      { path: "evaluations", name: "evaluations", component: () => import("@/views/EvaluationView.vue"), meta: { view: "evaluations" } },
    ],
  },
  { path: "/:pathMatch(.*)*", redirect: "/dashboard" },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();
  if (!auth.ready) {
    await auth.bootstrap();
  }

  if (to.meta.public) {
    return auth.isAuthenticated ? { path: "/dashboard" } : true;
  }

  if (!auth.isAuthenticated) {
    return { path: "/login" };
  }

  // 角色越权访问时回退到工作台。
  const view = to.meta.view;
  if (view) {
    const allowed = getViewsForRole(auth.role).some((item) => item.key === view);
    if (!allowed) {
      return { path: "/dashboard" };
    }
  }
  return true;
});

export default router;
