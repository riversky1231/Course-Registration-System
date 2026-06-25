<template>
  <el-container class="workspace">
    <el-aside class="sidebar" width="264px">
      <div class="sidebar-brand">
        <span class="brand-mark">SC</span>
        <div class="brand-copy">
          <p class="eyebrow">Student Course System</p>
          <h2>学生选课系统</h2>
        </div>
      </div>

      <div class="identity-card">
        <el-avatar :size="44" class="identity-avatar">{{ initial }}</el-avatar>
        <div class="identity-copy">
          <strong>{{ auth.displayName }}</strong>
          <span>{{ auth.roleLabel }}</span>
        </div>
      </div>

      <el-scrollbar class="nav-scroll">
        <el-menu :default-active="activePath" router class="nav-menu">
          <el-menu-item v-for="view in views" :key="view.key" :index="view.route">
            <el-icon><component :is="view.icon" /></el-icon>
            <span class="nav-label">{{ view.label }}</span>
            <el-badge
              v-if="summary[view.key] !== undefined"
              :value="summary[view.key]"
              :max="999"
              :show-zero="false"
              class="nav-badge"
            />
          </el-menu-item>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-summary">
        <div v-for="chip in summaryChips" :key="chip.label" class="summary-chip">
          <el-icon><component :is="chip.icon" /></el-icon>
          <div>
            <span>{{ chip.label }}</span>
            <strong>{{ chip.value }}</strong>
          </div>
        </div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="topbar" height="64px">
        <div class="topbar-title">
          <span class="topbar-badge">{{ currentMeta.caption }}</span>
          <h2>{{ currentMeta.label }}</h2>
        </div>
        <div class="topbar-right">
          <el-tag type="warning" effect="light" round>{{ semesterLabel }}</el-tag>
          <el-dropdown trigger="click" @command="onCommand">
            <div class="user-pill">
              <el-avatar :size="32" class="identity-avatar">{{ initial }}</el-avatar>
              <div class="user-pill-copy">
                <strong>{{ auth.displayName }}</strong>
                <span>{{ auth.roleLabel }}</span>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" :icon="'User'">个人资料</el-dropdown-item>
                <el-dropdown-item command="dashboard" :icon="'Histogram'">工作台</el-dropdown-item>
                <el-dropdown-item command="logout" :icon="'SwitchButton'" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-body">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="route.path" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, onMounted, reactive } from "vue";
import { useRoute, useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { useReferenceStore } from "@/stores/reference";
import { dashboardApi } from "@/api";
import { getViewsForRole, VIEW_META, SEMESTER_LABEL } from "@/constants/modules";
import { initials } from "@/utils/format";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();
const references = useReferenceStore();
const semesterLabel = SEMESTER_LABEL;

const summary = reactive({});

const views = computed(() => getViewsForRole(auth.role));
const initial = computed(() => initials(auth.displayName));
const activePath = computed(() => route.path);
const currentMeta = computed(() => VIEW_META[route.meta.view] || VIEW_META.dashboard);

const summaryChips = computed(() => [
  { label: "课程", value: summary.courses ?? 0, icon: "Notebook" },
  { label: "选课", value: summary.selections ?? 0, icon: "Tickets" },
  { label: "待录成绩", value: summary.pendingGrades ?? 0, icon: "EditPen" },
]);

async function loadSummary() {
  try {
    const [s, insights] = await Promise.all([
      dashboardApi.summary().catch(() => ({})),
      dashboardApi.insights().catch(() => ({})),
    ]);
    Object.assign(summary, s || {});
    summary.pendingGrades = insights?.pendingGrades ?? 0;
  } catch (error) {
    // 摘要失败不阻断主流程
  }
}

function onCommand(command) {
  if (command === "logout") {
    return onLogout();
  }
  router.push(VIEW_META[command]?.route || "/dashboard");
}

async function onLogout() {
  await auth.logout();
  ElMessage.success("已退出登录");
  router.push("/login");
}

onMounted(() => {
  references.load();
  loadSummary();
});
</script>

<style scoped>
.workspace {
  min-height: 100vh;
}

.sidebar {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 22px 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(246, 247, 253, 0.96));
  border-right: 1px solid var(--line);
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 8px;
}

.brand-mark {
  width: 44px;
  height: 44px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(140deg, var(--brand), var(--brand-3));
  color: #fff;
  font-weight: 800;
  font-size: 17px;
  box-shadow: 0 10px 22px rgba(91, 91, 240, 0.32);
}

.brand-copy h2 { margin: 2px 0 0; font-size: 16px; color: var(--ink); }

.identity-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  border-radius: 16px;
  background: linear-gradient(140deg, rgba(91, 91, 240, 0.1), rgba(155, 125, 249, 0.08));
  border: 1px solid rgba(91, 91, 240, 0.12);
}

.identity-avatar {
  background: linear-gradient(140deg, var(--brand), var(--brand-3));
  color: #fff;
  font-weight: 700;
  flex: none;
}

.identity-copy { display: flex; flex-direction: column; min-width: 0; }
.identity-copy strong { font-size: 15px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.identity-copy span { font-size: 12px; color: var(--ink-soft); }

.nav-scroll { flex: 1; margin: 0 -6px; }
.nav-menu { border-right: none; background: transparent; padding: 0 6px; }

.nav-menu .el-menu-item {
  height: 46px;
  line-height: 46px;
  margin: 4px 0;
  border-radius: 12px;
  color: var(--ink-soft);
  font-weight: 500;
}

.nav-menu .el-menu-item:hover { background: rgba(91, 91, 240, 0.08); color: var(--brand); }

.nav-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, var(--brand), var(--brand-2));
  color: #fff;
  box-shadow: 0 10px 22px rgba(91, 91, 240, 0.3);
}

.nav-menu .el-menu-item.is-active .el-icon { color: #fff; }
.nav-label { flex: 1; }
.nav-badge { margin-left: auto; }

.sidebar-summary { display: grid; gap: 10px; }

.summary-chip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
}

.summary-chip .el-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  background: rgba(91, 91, 240, 0.1);
  color: var(--brand);
  font-size: 18px;
  flex: none;
}

.summary-chip span { font-size: 12px; color: var(--ink-soft); display: block; }
.summary-chip strong { font-size: 18px; color: var(--ink); }

.main-container { background: transparent; }

.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  background: rgba(255, 255, 255, 0.7);
  border-bottom: 1px solid var(--line);
  backdrop-filter: blur(10px);
}

.topbar-title { display: flex; align-items: center; gap: 12px; }
.topbar-title h2 { margin: 0; font-size: 18px; }

.topbar-badge {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(91, 91, 240, 0.1);
  color: var(--brand);
  font-size: 12px;
  font-weight: 600;
}

.topbar-right { display: flex; align-items: center; gap: 14px; }

.user-pill {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 10px 5px 5px;
  border-radius: 999px;
  cursor: pointer;
  border: 1px solid var(--line);
  background: var(--surface);
}

.user-pill-copy { display: flex; flex-direction: column; line-height: 1.2; }
.user-pill-copy strong { font-size: 13px; }
.user-pill-copy span { font-size: 11px; color: var(--ink-soft); }

.main-body { padding: 26px 30px 40px; }

@media (max-width: 860px) {
  .sidebar { display: none; }
  .topbar { padding: 0 16px; }
  .main-body { padding: 18px 16px 30px; }
}
</style>
