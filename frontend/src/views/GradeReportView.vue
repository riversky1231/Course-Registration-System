<template>
  <div v-loading="loading">
    <PageHeader :title="meta.label" :caption="meta.caption" :description="meta.description" />

    <div class="grade-top">
      <!-- 挂科预警 -->
      <el-alert
        v-if="report.failedCourses > 0"
        type="error"
        :closable="false"
        show-icon
        class="fail-alert"
      >
        <template #title>挂科预警</template>
        你有 <strong>{{ report.failedCourses }}</strong> 门课程成绩低于 60 分，请及时关注相关课程的重修安排。
      </el-alert>

      <!-- GPA 仪表盘 -->
      <el-card class="panel gpa-card" shadow="never">
        <p class="eyebrow">学业绩点</p>
        <el-progress
          type="dashboard"
          :percentage="gpaPercent"
          :width="170"
          :stroke-width="12"
          :color="gpaColor"
        >
          <template #default>
            <div class="gpa-center">
              <strong>{{ formatNumber(report.gpa) }}</strong>
              <span>GPA / 5.0</span>
            </div>
          </template>
        </el-progress>
        <div class="gpa-foot">
          <el-tag :type="gpaTag.type" effect="light" round>{{ gpaTag.label }}</el-tag>
        </div>
      </el-card>

      <!-- 统计卡 -->
      <div class="grade-stats">
        <article v-for="(card, idx) in statCards" :key="card.label" class="stat-card" :class="'tone-' + idx">
          <div class="stat-icon"><el-icon><component :is="card.icon" /></el-icon></div>
          <span class="stat-label">{{ card.label }}</span>
          <strong class="stat-value">{{ card.value }}</strong>
          <span class="stat-hint">{{ card.hint }}</span>
        </article>
      </div>
    </div>

    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-head">
          <div><p class="eyebrow">成绩单</p><h3>我的成绩明细</h3></div>
          <div class="head-tools">
            <el-input
              v-model.trim="keyword"
              class="head-search"
              :placeholder="meta.searchPlaceholder"
              prefix-icon="Search"
              clearable
              @keyup.enter="search"
              @clear="search"
            />
            <el-button :icon="'Search'" @click="search">搜索</el-button>
            <el-space>
              <el-tag type="success" effect="light" round>已出 {{ report.gradedCourses }} 门</el-tag>
              <el-tag type="warning" effect="light" round>待出 {{ report.pendingCourses }} 门</el-tag>
              <el-tag v-if="report.failedCourses > 0" type="danger" effect="light" round>挂科 {{ report.failedCourses }} 门</el-tag>
            </el-space>
          </div>
        </div>
      </template>

      <el-empty v-if="!rows.length" description="当前还没有成绩记录" />

      <template v-else>
        <el-table :data="rows" class="data-table" stripe>
          <el-table-column prop="courseName" label="课程" min-width="160" show-overflow-tooltip />
          <el-table-column prop="teacherName" label="授课教师" min-width="120" />
          <el-table-column prop="courseDept" label="开课学院" min-width="130" show-overflow-tooltip />
          <el-table-column label="学分" width="100">
            <template #default="{ row }">{{ formatNumber(row.courseCredit) }} 学分</template>
          </el-table-column>
          <el-table-column prop="timeSlot" label="上课时间" min-width="130" show-overflow-tooltip />
          <el-table-column label="成绩" width="120">
            <template #default="{ row }">
              <el-tag :type="row.graded ? 'success' : 'warning'" effect="light" round>
                {{ row.graded ? formatNumber(row.score) + " 分" : "待录入" }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager" v-if="pager.total > 0">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="pager.total"
            :current-page="pager.page"
            :page-size="pager.pageSize"
            :page-sizes="pageSizeOptions"
            @current-change="onPage"
            @size-change="onSize"
          />
        </div>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import PageHeader from "@/components/PageHeader.vue";
import { gradeApi } from "@/api";
import { VIEW_META, PAGE_SIZE_OPTIONS } from "@/constants/modules";
import { formatNumber, emptyPage } from "@/utils/format";

const meta = VIEW_META.gradeReport;
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const loading = ref(false);
const keyword = ref("");
const report = reactive({ gpa: 0, averageScore: 0, earnedCredits: 0, totalCredits: 0, gradedCourses: 0, pendingCourses: 0, failedCourses: 0 });
const pager = reactive(emptyPage());
const rows = ref([]);

const gpaPercent = computed(() => Math.min(100, Math.round((Number(report.gpa) / 5) * 100)));
const gpaColor = computed(() => {
  const g = Number(report.gpa);
  if (g >= 3.5) return "#059669";
  if (g >= 3) return "#5b5bf0";
  if (g >= 2) return "#d97706";
  return "#dc2626";
});

const gpaTag = computed(() => {
  const g = Number(report.gpa);
  if (g >= 3.5) return { label: "优秀 · 学霸特权", type: "success" };
  if (g >= 3) return { label: "良好", type: "primary" };
  if (g >= 2) return { label: "正常", type: "warning" };
  return { label: "需努力", type: "danger" };
});

const statCards = computed(() => [
  { label: "平均成绩", value: formatNumber(report.averageScore), hint: "已出成绩课程的平均分", icon: "DataLine" },
  { label: "已获学分", value: formatNumber(report.earnedCredits), hint: "成绩达到及格线的累计学分", icon: "Medal" },
  { label: "总学分", value: formatNumber(report.totalCredits), hint: "已选课程累计学分", icon: "Collection" },
  { label: "课程数", value: report.gradedCourses + report.pendingCourses, hint: "已选课程总数", icon: "Notebook" },
]);

async function load() {
  loading.value = true;
  try {
    const data = await gradeApi.report({ keyword: keyword.value, page: pager.page, pageSize: pager.pageSize });
    Object.assign(report, {
      gpa: data?.gpa ?? 0,
      averageScore: data?.averageScore ?? 0,
      earnedCredits: data?.earnedCredits ?? 0,
      totalCredits: data?.totalCredits ?? 0,
      gradedCourses: data?.gradedCourses ?? 0,
      pendingCourses: data?.pendingCourses ?? 0,
      failedCourses: data?.failedCourses ?? 0,
    });
    const page = data?.page || emptyPage();
    rows.value = page.items || [];
    pager.total = page.total || 0;
    pager.totalPages = page.totalPages || 0;
    pager.page = page.page || pager.page;
    pager.pageSize = page.pageSize || pager.pageSize;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

function search() {
  pager.page = 1;
  load();
}

function onPage(page) {
  pager.page = page;
  load();
}

function onSize(size) {
  pager.pageSize = size;
  pager.page = 1;
  load();
}

onMounted(load);
</script>

<style scoped>
.grade-top { display: grid; grid-template-columns: 280px 1fr; gap: 20px; margin-bottom: 20px; align-items: stretch; }

.fail-alert { grid-column: 1 / -1; }

.gpa-card { text-align: center; }
.gpa-card :deep(.el-card__body) { display: flex; flex-direction: column; align-items: center; gap: 10px; padding: 24px; }
.gpa-center { display: flex; flex-direction: column; }
.gpa-center strong { font-size: 36px; font-weight: 800; color: var(--ink); }
.gpa-center span { font-size: 12px; color: var(--ink-faint); }
.gpa-foot { margin-top: 4px; }

.grade-stats { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }

.stat-card {
  padding: 18px 20px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
}

.stat-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  font-size: 22px;
  margin-bottom: 12px;
}

.tone-0 .stat-icon { background: rgba(37, 99, 235, 0.12); color: #2563eb; }
.tone-1 .stat-icon { background: rgba(5, 150, 105, 0.12); color: #059669; }
.tone-2 .stat-icon { background: rgba(217, 119, 6, 0.12); color: #d97706; }
.tone-3 .stat-icon { background: rgba(91, 91, 240, 0.12); color: #5b5bf0; }

.stat-label { font-size: 13px; color: var(--ink-soft); }
.stat-value { display: block; font-size: 28px; font-weight: 800; margin: 4px 0; }
.stat-hint { font-size: 12px; color: var(--ink-faint); }

.head-tools { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.head-search { width: 240px; }

@media (max-width: 900px) {
  .grade-top { grid-template-columns: 1fr; }
  .grade-stats { grid-template-columns: 1fr 1fr; }
}
</style>
