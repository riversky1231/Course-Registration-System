<template>
  <div>
    <PageHeader :title="meta.label" :caption="meta.caption" :description="meta.description">
      <template #actions>
        <el-input
          v-model.trim="mod.keyword.value"
          class="header-search"
          :placeholder="meta.searchPlaceholder"
          prefix-icon="Search"
          clearable
          @keyup.enter="mod.search"
          @clear="mod.search"
        />
        <el-button v-if="allowCreate" type="primary" :icon="'Plus'" @click="mod.openCreate">新增课程</el-button>
      </template>
    </PageHeader>

    <!-- 筛选条 -->
    <el-card class="panel filter-card" shadow="never">
      <div class="filter-bar">
        <el-select v-model="filters.dept" class="filter-item" placeholder="全部学院" clearable>
          <el-option v-for="d in departments" :key="d" :label="d" :value="d" />
        </el-select>
        <el-select v-model="filters.teacherId" class="filter-item" placeholder="全部教师" clearable filterable :disabled="auth.role === 'teacher'">
          <el-option v-for="t in references.teachers" :key="t.id" :label="t.tname || t.username" :value="t.id" />
        </el-select>
        <el-input-number v-model="filters.minScore" :min="0" :step="0.5" :controls="false" class="filter-num" placeholder="最低学分" />
        <span class="filter-sep">~</span>
        <el-input-number v-model="filters.maxScore" :min="0" :step="0.5" :controls="false" class="filter-num" placeholder="最高学分" />
        <el-checkbox v-model="filters.onlyAvailable" border>仅看可选</el-checkbox>
        <el-button type="primary" :icon="'Filter'" @click="mod.search">筛选</el-button>
        <el-button :icon="'RefreshLeft'" @click="resetFilters">重置</el-button>
      </div>
    </el-card>

    <div v-loading="mod.loading.value">
      <el-empty v-if="!mod.rows.value.length" description="没有符合条件的课程" />

      <div v-else class="course-grid">
        <article v-for="(course, idx) in mod.rows.value" :key="course.id" class="course-card">
          <div class="course-cover" :class="'cover-' + (idx % 5)">
            <span class="course-credit">{{ formatNumber(course.score) }} 学分</span>
            <h3>{{ course.name }}</h3>
            <span class="course-numb">No.{{ course.numb || "-" }}</span>
          </div>

          <div class="course-body">
            <div class="course-meta">
              <span><el-icon><School /></el-icon>{{ course.dept || "未设学院" }}</span>
              <span><el-icon><Reading /></el-icon>{{ course.teacherName || "待安排教师" }}</span>
              <span><el-icon><Clock /></el-icon>{{ course.timeSlot || "未排课" }}</span>
            </div>

            <p class="course-intro">{{ course.jianjie || "暂无课程简介。" }}</p>

            <div class="course-capacity">
              <div class="cap-line">
                <span>选课人数</span>
                <strong>{{ capText(course) }}</strong>
              </div>
              <el-progress
                :percentage="capPercent(course)"
                :stroke-width="8"
                :color="capColor(course)"
                :show-text="false"
              />
            </div>
          </div>

          <div class="course-actions">
            <el-button
              v-if="auth.role === 'student'"
              type="primary"
              round
              :icon="'CirclePlus'"
              :disabled="isFull(course)"
              :loading="selectingId === course.id"
              @click="selectCourse(course)"
            >
              {{ isFull(course) ? "名额已满" : "选这门课" }}
            </el-button>
            <template v-if="allowEdit">
              <el-button :icon="'Edit'" @click="mod.openEdit(course)">{{ auth.role === "teacher" ? "维护" : "编辑" }}</el-button>
            </template>
            <el-button v-if="allowDelete" type="danger" plain :icon="'Delete'" @click="mod.remove(course, '删除')">删除</el-button>
          </div>
        </article>
      </div>

      <div class="pager" v-if="mod.pager.total > 0">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="mod.pager.total"
          :current-page="mod.pager.page"
          :page-size="mod.pager.pageSize"
          :page-sizes="pageSizeOptions"
          @current-change="mod.handlePageChange"
          @size-change="mod.handleSizeChange"
        />
      </div>
    </div>

    <RecordDialog
      v-model="mod.dialog.visible"
      view="courses"
      :mode="mod.dialog.mode"
      :form="mod.dialog.form"
      :feedback="mod.dialog.feedback"
      :loading="mod.loading.value"
      @save="mod.save"
    />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import PageHeader from "@/components/PageHeader.vue";
import RecordDialog from "@/components/RecordDialog.vue";
import { useModule } from "@/composables/useModule";
import { useAuthStore } from "@/stores/auth";
import { useReferenceStore } from "@/stores/reference";
import { createModule } from "@/api";
import { ENDPOINTS, VIEW_META, PAGE_SIZE_OPTIONS, canCreate, canEdit, canDelete } from "@/constants/modules";
import { formatNumber, safeText } from "@/utils/format";

const auth = useAuthStore();
const references = useReferenceStore();
const meta = VIEW_META.courses;
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const filters = reactive({ dept: "", teacherId: "", minScore: undefined, maxScore: undefined, onlyAvailable: false });
const selectingId = ref("");

const allowCreate = computed(() => canCreate(auth.role, "courses"));
const allowEdit = computed(() => canEdit(auth.role, "courses"));
const allowDelete = computed(() => canDelete(auth.role, "courses"));

const departments = computed(() =>
  [...new Set(references.courses.map((c) => safeText(c.dept, "")).filter(Boolean))].sort()
);

const mod = useModule("courses", {
  extraParams: () => ({
    dept: filters.dept,
    teacherId: filters.teacherId,
    minScore: filters.minScore ?? "",
    maxScore: filters.maxScore ?? "",
    onlyAvailable: filters.onlyAvailable || "",
  }),
  emptyForm: () => ({ name: "", numb: "", score: 0, tid: "", jianjie: "", dept: "", maxStudents: 0, timeSlot: "" }),
  normalize: (form) => {
    const payload = { ...form };
    payload.maxStudents = Number.isFinite(Number(payload.maxStudents)) ? Number(payload.maxStudents) : 0;
    payload.timeSlot = safeText(payload.timeSlot, "").trim();
    payload.dept = safeText(payload.dept, "").trim();
    if (auth.role === "teacher") delete payload.tid;
    return payload;
  },
  onSaved: () => references.load(true),
});

function resetFilters() {
  filters.dept = "";
  filters.teacherId = "";
  filters.minScore = undefined;
  filters.maxScore = undefined;
  filters.onlyAvailable = false;
  mod.search();
}

function isFull(course) {
  const max = Number(course.maxStudents || 0);
  if (max <= 0) return false;
  return Number(course.selectedCount || 0) >= max;
}

function capText(course) {
  const max = Number(course.maxStudents || 0);
  const sel = Number(course.selectedCount || 0);
  return max > 0 ? `${sel} / ${max} 人` : `${sel} 人 · 不限`;
}

function capPercent(course) {
  const max = Number(course.maxStudents || 0);
  if (max <= 0) return Math.min(100, Number(course.selectedCount || 0) * 4);
  return Math.min(100, Math.round((Number(course.selectedCount || 0) / max) * 100));
}

function capColor(course) {
  const p = capPercent(course);
  if (p >= 100) return "#dc2626";
  if (p >= 80) return "#d97706";
  return "#5b5bf0";
}

async function selectCourse(course) {
  selectingId.value = course.id;
  try {
    await createModule(ENDPOINTS.selections, { courseId: course.id });
    ElMessage.success(`已成功选修《${course.name}》`);
    await Promise.all([mod.load(), references.load(true)]);
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    selectingId.value = "";
  }
}

onMounted(() => {
  if (auth.role === "teacher") filters.teacherId = auth.session?.id || "";
  mod.load();
});
</script>

<style scoped>
.header-search { width: 280px; max-width: 50vw; }

.filter-card { margin-bottom: 20px; }
.filter-card :deep(.el-card__body) { padding: 16px 18px; }
.filter-bar { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.filter-item { width: 170px; }
.filter-num { width: 120px; }
.filter-sep { color: var(--ink-faint); }

.course-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 18px;
}

.course-card {
  display: flex;
  flex-direction: column;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.course-card:hover { transform: translateY(-4px); box-shadow: var(--shadow-md); }

.course-cover {
  position: relative;
  padding: 22px 20px 18px;
  color: #fff;
}

.cover-0 { background: linear-gradient(135deg, #5b5bf0, #8b7bf7); }
.cover-1 { background: linear-gradient(135deg, #2563eb, #38bdf8); }
.cover-2 { background: linear-gradient(135deg, #059669, #34d399); }
.cover-3 { background: linear-gradient(135deg, #d97706, #fbbf24); }
.cover-4 { background: linear-gradient(135deg, #db2777, #f472b6); }

.course-credit {
  position: absolute;
  top: 16px;
  right: 16px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.22);
  font-size: 12px;
  font-weight: 600;
}

.course-cover h3 { margin: 6px 0 6px; font-size: 19px; padding-right: 70px; }
.course-numb { font-size: 12px; color: rgba(255, 255, 255, 0.82); }

.course-body { padding: 16px 18px; flex: 1; }
.course-meta { display: grid; gap: 8px; margin-bottom: 12px; }
.course-meta span { display: inline-flex; align-items: center; gap: 8px; font-size: 13px; color: var(--ink-soft); }
.course-meta .el-icon { color: var(--brand); }

.course-intro {
  margin: 0 0 14px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--ink-soft);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.course-capacity { margin-top: auto; }
.cap-line { display: flex; justify-content: space-between; font-size: 12px; color: var(--ink-soft); margin-bottom: 6px; }
.cap-line strong { color: var(--ink); }

.course-actions {
  display: flex;
  gap: 8px;
  padding: 14px 18px;
  border-top: 1px solid var(--line);
  background: var(--bg-2);
}

.course-actions .el-button { flex: 1; }
</style>
