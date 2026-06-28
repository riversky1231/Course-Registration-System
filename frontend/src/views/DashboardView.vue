<template>
  <div class="page-stack" v-loading="loading">
    <!-- 欢迎横幅 -->
    <section class="hero">
      <div class="hero-copy">
        <p class="hero-eyebrow">{{ semesterLabel }} · {{ auth.roleLabel }}</p>
        <h1>{{ greeting }}，{{ auth.displayName }}</h1>
        <p class="hero-desc">{{ heroDesc }}</p>
        <div class="hero-actions">
          <el-button type="primary" round :icon="primaryAction.icon" @click="go(primaryAction.route)">
            {{ primaryAction.label }}
          </el-button>
          <el-button round plain :icon="'User'" @click="go('/profile')">个人资料</el-button>
        </div>
      </div>
      <div class="hero-art">
        <div class="hero-orb hero-orb-a"></div>
        <div class="hero-orb hero-orb-b"></div>
        <el-icon class="hero-glyph"><Histogram /></el-icon>
      </div>
    </section>

    <!-- KPI 指标卡 -->
    <section class="kpi-grid">
      <article v-for="(kpi, idx) in kpis" :key="kpi.label" class="kpi-card" :class="'tone-' + (idx % 4)">
        <div class="kpi-icon"><el-icon><component :is="kpi.icon" /></el-icon></div>
        <div class="kpi-body">
          <span class="kpi-label">{{ kpi.label }}</span>
          <strong class="kpi-value">{{ kpi.value }}</strong>
          <span class="kpi-hint">{{ kpi.hint }}</span>
        </div>
      </article>
    </section>

    <div class="dash-columns">
      <!-- 左列 -->
      <div class="page-stack">
        <el-card v-if="departmentSpotlights.length && auth.role === 'admin'" class="panel" shadow="never">
          <template #header>
            <div class="panel-head">
              <div><p class="eyebrow">学院分布</p><h3>开课学院概览</h3></div>
              <el-icon class="head-glyph"><School /></el-icon>
            </div>
          </template>
          <div class="bar-list">
            <div v-for="dept in departmentSpotlights" :key="dept.title" class="bar-row">
              <div class="bar-info">
                <strong>{{ dept.title }}</strong>
                <span>{{ dept.metric }}</span>
              </div>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(dept.metric) + '%' }"></div>
              </div>
              <small>{{ dept.detail }}</small>
            </div>
          </div>
        </el-card>

        <el-card class="panel" shadow="never">
          <template #header>
            <div class="panel-head">
              <div><p class="eyebrow">动态</p><h3>近期选课记录</h3></div>
              <el-tag type="info" effect="plain" round>{{ recentSelections.length }} 条</el-tag>
            </div>
          </template>
          <el-empty v-if="!recentSelections.length" description="暂无选课动态" :image-size="90" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in recentSelections"
              :key="item.id"
              :timestamp="formatDateTime(item.createTime)"
              placement="top"
              :type="item.graded ? 'success' : 'primary'"
              :hollow="!item.graded"
            >
              <div class="timeline-card">
                <strong>{{ item.courseName || "未命名课程" }}</strong>
                <div class="timeline-meta">
                  <span><el-icon><UserFilled /></el-icon>{{ item.studentName || "-" }}</span>
                  <span><el-icon><Reading /></el-icon>{{ item.teacherName || "待安排" }}</span>
                  <span v-if="item.timeSlot"><el-icon><Clock /></el-icon>{{ item.timeSlot }}</span>
                </div>
              </div>
              <el-tag :type="item.graded ? 'success' : 'warning'" effect="light" round size="small">
                {{ item.graded ? formatNumber(item.score) + " 分" : "待录入" }}
              </el-tag>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>

      <!-- 右列 -->
      <div class="page-stack">
        <el-card class="panel" shadow="never">
          <template #header>
            <div class="panel-head">
              <div><p class="eyebrow">提示</p><h3>智能工作提示</h3></div>
              <el-icon class="head-glyph"><BellFilled /></el-icon>
            </div>
          </template>
          <el-empty v-if="!notices.length" description="暂无提示" :image-size="80" />
          <ul v-else class="notice-list">
            <li v-for="(notice, idx) in notices" :key="idx">
              <span class="notice-idx">{{ idx + 1 }}</span>
              <span class="notice-text">{{ notice }}</span>
            </li>
          </ul>
        </el-card>

        <el-card v-if="courseSpotlights.length" class="panel" shadow="never">
          <template #header>
            <div class="panel-head">
              <div><p class="eyebrow">热度</p><h3>热门课程榜</h3></div>
              <el-icon class="head-glyph"><TrophyBase /></el-icon>
            </div>
          </template>
          <div class="spot-list">
            <div v-for="(course, idx) in courseSpotlights" :key="idx" class="spot-row">
              <span class="spot-rank" :class="idx < 3 ? 'rank-top' : ''">{{ idx + 1 }}</span>
              <div class="spot-main">
                <strong>{{ course.title }}</strong>
                <small>{{ course.subtitle }}</small>
              </div>
              <el-tag size="small" effect="light" round>{{ course.metric }}</el-tag>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { dashboardApi } from "@/api";
import { useAuthStore } from "@/stores/auth";
import { SEMESTER_LABEL } from "@/constants/modules";
import { formatDateTime, formatNumber } from "@/utils/format";

const router = useRouter();
const auth = useAuthStore();
const semesterLabel = SEMESTER_LABEL;

const loading = ref(false);
const insights = ref({});

const recentSelections = computed(() => insights.value.recentSelections || []);
const departmentSpotlights = computed(() => insights.value.departmentSpotlights || []);
const notices = computed(() => insights.value.notices || []);
const courseSpotlights = computed(() => (insights.value.courseSpotlights || []).slice(0, 5));

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 6) return "夜深了";
  if (hour < 12) return "早上好";
  if (hour < 14) return "中午好";
  if (hour < 18) return "下午好";
  return "晚上好";
});


const primaryAction = computed(() => {
  if (auth.role === "admin") return { label: "进入课程中心", route: "/courses", icon: "Notebook" };
  if (auth.role === "teacher") return { label: "录入课程成绩", route: "/courses", icon: "EditPen" };
  return { label: "浏览课程中心", route: "/courses", icon: "Notebook" };
});

const kpis = computed(() => {
  const d = insights.value;
  if (auth.role === "admin") {
    return [
      { label: "课程总量", value: d.courses ?? 0, hint: "当前课程池中的开课数量", icon: "Notebook" },
      { label: "选课记录", value: d.selections ?? 0, hint: "系统已生成的选课数据量", icon: "Tickets" },
      { label: "待录成绩", value: d.pendingGrades ?? 0, hint: "需要教师继续补录的成绩", icon: "EditPen" },
      { label: "未排教师", value: d.unassignedCourses ?? 0, hint: "尚未完成教师分配的课程", icon: "WarningFilled" },
    ];
  }
  if (auth.role === "teacher") {
    return [
      { label: "我的课程", value: d.courses ?? 0, hint: "当前由你负责的课程数量", icon: "Notebook" },
      { label: "相关选课", value: d.selections ?? 0, hint: "与你课程关联的选课记录", icon: "Tickets" },
      { label: "待录成绩", value: d.pendingGrades ?? 0, hint: "等待你录入成绩的记录", icon: "EditPen" },
      { label: "平均成绩", value: formatNumber(d.averageScore), hint: "基于当前可见记录统计", icon: "DataLine" },
    ];
  }
  return [
    { label: "已选课程", value: d.selections ?? 0, hint: "与你本人关联的选课记录", icon: "Tickets" },
    { label: "平均成绩", value: formatNumber(d.averageScore), hint: "已出成绩课程的平均分", icon: "DataLine" },
    { label: "平均学分", value: formatNumber(d.averageCredits), hint: "已选课程的平均学分", icon: "Medal" },
    { label: "待出成绩", value: d.pendingGrades ?? 0, hint: "教师尚未录入成绩的课程", icon: "Clock" },
  ];
});

function barWidth(metric) {
  const values = departmentSpotlights.value.map((d) => parseFloat(d.metric) || 0);
  const max = Math.max(1, ...values);
  return Math.round(((parseFloat(metric) || 0) / max) * 100);
}

function go(route) {
  router.push(route);
}

async function load() {
  loading.value = true;
  try {
    insights.value = (await dashboardApi.insights()) || {};
  } catch (error) {
    insights.value = {};
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.hero {
  position: relative;
  overflow: hidden;
  border-radius: 22px;
  padding: 34px 36px;
  color: #fff;
  background: linear-gradient(135deg, #5b5bf0 0%, #7b6ef6 52%, #9b7df9 100%);
  box-shadow: var(--shadow-md);
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
}

.hero-eyebrow { margin: 0 0 10px; font-size: 13px; font-weight: 600; color: rgba(255, 255, 255, 0.85); }
.hero h1 { margin: 0 0 10px; font-size: 28px; font-weight: 800; }
.hero-desc { margin: 0 0 20px; max-width: 560px; line-height: 1.7; color: rgba(255, 255, 255, 0.9); }
.hero-actions { display: flex; gap: 12px; flex-wrap: wrap; }
.hero-actions :deep(.el-button--primary) { background: #fff; color: var(--brand); border-color: #fff; }
.hero-actions :deep(.el-button.is-plain) { background: rgba(255, 255, 255, 0.14); border-color: rgba(255, 255, 255, 0.4); color: #fff; }

.hero-art { position: relative; width: 180px; height: 140px; flex: none; }
.hero-orb { position: absolute; border-radius: 50%; filter: blur(6px); }
.hero-orb-a { width: 120px; height: 120px; background: rgba(255, 255, 255, 0.22); top: 0; right: 10px; }
.hero-orb-b { width: 70px; height: 70px; background: rgba(255, 255, 255, 0.16); bottom: 0; right: 90px; }
.hero-glyph { position: absolute; right: 30px; top: 34px; font-size: 84px; color: rgba(255, 255, 255, 0.92); }

.kpi-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; }

.kpi-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
}

.kpi-icon {
  width: 52px;
  height: 52px;
  flex: none;
  display: grid;
  place-items: center;
  border-radius: 14px;
  font-size: 24px;
}

.tone-0 .kpi-icon { background: rgba(91, 91, 240, 0.12); color: #5b5bf0; }
.tone-1 .kpi-icon { background: rgba(37, 99, 235, 0.12); color: #2563eb; }
.tone-2 .kpi-icon { background: rgba(217, 119, 6, 0.12); color: #d97706; }
.tone-3 .kpi-icon { background: rgba(5, 150, 105, 0.12); color: #059669; }

.kpi-label { font-size: 13px; color: var(--ink-soft); }
.kpi-value { display: block; font-size: 28px; font-weight: 800; margin: 2px 0; }
.kpi-hint { font-size: 12px; color: var(--ink-faint); }

.dash-columns { display: grid; grid-template-columns: 1.4fr 1fr; gap: 20px; align-items: start; }

.head-glyph { font-size: 22px; color: var(--brand); }

.bar-list { display: grid; gap: 16px; }
.bar-row { display: grid; gap: 6px; }
.bar-info { display: flex; justify-content: space-between; font-size: 14px; }
.bar-info span { color: var(--ink-soft); }
.bar-track { height: 10px; border-radius: 999px; background: var(--bg-2); overflow: hidden; }
.bar-fill { height: 100%; border-radius: 999px; background: linear-gradient(90deg, var(--brand), var(--brand-3)); transition: width 0.6s ease; }
.bar-row small { color: var(--ink-faint); font-size: 12px; }

.timeline-card { margin-bottom: 6px; }
.timeline-card strong { font-size: 14px; }
.timeline-meta { display: flex; gap: 14px; flex-wrap: wrap; margin-top: 4px; color: var(--ink-soft); font-size: 12px; }
.timeline-meta span { display: inline-flex; align-items: center; gap: 4px; }

.notice-list { list-style: none; margin: 0; padding: 0; display: grid; gap: 12px; }
.notice-list li { display: flex; gap: 10px; align-items: flex-start; }
.notice-idx {
  flex: none;
  width: 22px;
  height: 22px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: rgba(91, 91, 240, 0.12);
  color: var(--brand);
  font-size: 12px;
  font-weight: 700;
  margin-top: 1px;
}
.notice-text { font-size: 13px; color: var(--ink-soft); line-height: 1.6; }

.spot-list { display: grid; gap: 12px; }
.spot-row { display: flex; align-items: center; gap: 12px; }
.spot-rank {
  flex: none;
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 9px;
  background: var(--bg-2);
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 800;
}
.spot-rank.rank-top { background: linear-gradient(140deg, var(--brand), var(--brand-3)); color: #fff; }
.spot-main { flex: 1; min-width: 0; }
.spot-main strong { display: block; font-size: 14px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.spot-main small { color: var(--ink-faint); font-size: 12px; }

@media (max-width: 1100px) {
  .kpi-grid { grid-template-columns: repeat(2, 1fr); }
  .dash-columns { grid-template-columns: 1fr; }
}

@media (max-width: 560px) {
  .kpi-grid { grid-template-columns: 1fr; }
  .hero-art { display: none; }
}
</style>
