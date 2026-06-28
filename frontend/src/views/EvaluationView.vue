<template>
  <div>
    <PageHeader :title="meta.label" :caption="meta.caption" :description="meta.description">
      <template #actions>
        <el-input
          v-if="!isStudent"
          v-model.trim="keyword"
          class="header-search"
          :placeholder="meta.searchPlaceholder"
          prefix-icon="Search"
          clearable
          @keyup.enter="search"
          @clear="search"
        />
        <el-select
          v-if="!isStudent"
          v-model="courseFilter"
          class="course-filter"
          placeholder="按课程筛选"
          clearable
          filterable
          @change="onCourseFilterChange"
        >
          <el-option
            v-for="course in references.courses"
            :key="course.id"
            :label="course.name"
            :value="course.id"
          />
        </el-select>
      </template>
    </PageHeader>

    <!-- 学生：评教中心 -->
    <div v-if="isStudent" v-loading="loading">
      <div class="stat-strip">
        <div class="stat-item"><span>可评价课程</span><strong>{{ evaluableCourses.length }}</strong></div>
        <div class="stat-item"><span>已评价</span><strong>{{ myEvaluations.length }}</strong></div>
        <div class="stat-item"><span>我的平均评分</span><strong>{{ myAverageRating }}</strong></div>
      </div>

      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-head">
            <div><p class="eyebrow">待评价</p><h3>结课课程评价</h3></div>
            <el-tag type="warning" effect="light" round>{{ evaluableCourses.length }} 门可评</el-tag>
          </div>
        </template>
        <el-empty v-if="!evaluableCourses.length" description="暂无可评价课程（仅已录入成绩的课程可评价）" :image-size="90" />
        <div v-else class="eval-grid">
          <article v-for="item in evaluableCourses" :key="item.courseId" class="eval-card">
            <div class="eval-top">
              <div class="eval-icon"><el-icon><Star /></el-icon></div>
              <el-tag type="success" effect="light" round>{{ formatNumber(item.score) }} 分</el-tag>
            </div>
            <h3>{{ item.courseName || "未命名课程" }}</h3>
            <div class="eval-meta">
              <span><el-icon><Reading /></el-icon>{{ item.teacherName || "待安排教师" }}</span>
              <span><el-icon><Medal /></el-icon>{{ formatNumber(item.courseCredit) }} 学分</span>
            </div>
            <div class="eval-foot">
              <el-button type="primary" plain size="small" :icon="'EditPen'" @click="openEvaluate(item)">去评价</el-button>
            </div>
          </article>
        </div>
      </el-card>

      <el-card class="panel" shadow="never">
        <template #header>
          <div class="panel-head">
            <div><p class="eyebrow">我的评价</p><h3>历史评教记录</h3></div>
            <el-tag type="info" effect="plain" round>{{ myEvaluations.length }} 条</el-tag>
          </div>
        </template>
        <el-empty v-if="!myEvaluations.length" description="你还没有提交过课程评价" :image-size="90" />
        <div v-else class="my-eval-list">
          <article v-for="item in myEvaluations" :key="item.id" class="my-eval-row">
            <div class="my-eval-main">
              <div class="my-eval-title">
                <strong>{{ item.courseName || "未命名课程" }}</strong>
                <el-rate :model-value="item.rating" disabled size="small" />
                <el-tag v-if="item.anonymous" size="small" effect="plain" round>匿名</el-tag>
              </div>
              <p v-if="item.comment" class="my-eval-comment">{{ item.comment }}</p>
              <small>{{ item.teacherName || "-" }} · {{ formatDateTime(item.createTime) }}</small>
            </div>
            <el-button type="danger" plain size="small" :icon="'Delete'" @click="removeEvaluation(item)">删除</el-button>
          </article>
        </div>
      </el-card>
    </div>

    <!-- 教师 / 管理员：评教结果与统计 -->
    <div v-else v-loading="loading">
      <el-card v-if="courseFilter && summary" class="panel summary-card" shadow="never">
        <div class="summary-left">
          <p class="eyebrow">课程评教概况</p>
          <h3>{{ summary.courseName || "未命名课程" }}</h3>
          <div class="summary-score">
            <strong>{{ formatNumber(summary.averageRating) }}</strong>
            <div>
              <el-rate :model-value="summary.averageRating" disabled allow-half />
              <small>共 {{ summary.totalCount }} 条评价</small>
            </div>
          </div>
        </div>
        <div class="summary-right">
          <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="dist-row">
            <span class="dist-label">{{ star }} 星</span>
            <div class="dist-track">
              <div class="dist-fill" :style="{ width: distPercent(star) + '%' }"></div>
            </div>
            <span class="dist-count">{{ distCount(star) }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="panel" shadow="never">
        <el-empty v-if="!rows.length" description="暂无评教记录" />
        <template v-else>
          <el-table :data="rows" class="data-table" stripe>
            <el-table-column prop="courseName" label="课程" min-width="160" show-overflow-tooltip />
            <el-table-column prop="studentName" label="学生" min-width="120" />
            <el-table-column label="评分" width="180">
              <template #default="{ row }">
                <el-rate :model-value="row.rating" disabled size="small" />
              </template>
            </el-table-column>
            <el-table-column prop="comment" label="评价内容" min-width="220" show-overflow-tooltip>
              <template #default="{ row }">{{ row.comment || "（未填写）" }}</template>
            </el-table-column>
            <el-table-column v-if="isAdmin" prop="teacherName" label="授课教师" min-width="120" />
            <el-table-column label="评价时间" min-width="170">
              <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
            </el-table-column>
            <el-table-column v-if="isAdmin" label="操作" width="100" fixed="right">
              <template #default="{ row }">
                <el-button type="danger" link :icon="'Delete'" @click="removeEvaluation(row)">删除</el-button>
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

    <!-- 学生评价弹窗 -->
    <el-dialog v-model="dialogVisible" title="课程评价" width="460px" :close-on-click-modal="false">
      <div v-if="form.courseName" class="dialog-course">
        <el-icon><Notebook /></el-icon>
        <div>
          <strong>{{ form.courseName }}</strong>
          <small>{{ form.teacherName || "待安排教师" }}</small>
        </div>
      </div>
      <el-form label-position="top" class="eval-form">
        <el-form-item label="总体评分" required>
          <el-rate v-model="form.rating" show-text :texts="ratingTexts" />
        </el-form-item>
        <el-form-item label="评价内容">
          <el-input
            v-model.trim="form.comment"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="说说这门课的收获、教学体验或改进建议（选填）"
          />
        </el-form-item>
        <el-form-item label="匿名评价">
          <el-switch v-model="form.anonymous" />
          <span class="anon-hint">开启后教师将看不到你的身份</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="loading" @click="submitEvaluation">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import PageHeader from "@/components/PageHeader.vue";
import { evaluationApi, listModule } from "@/api";
import { ENDPOINTS, VIEW_META, PAGE_SIZE_OPTIONS } from "@/constants/modules";
import { useAuthStore } from "@/stores/auth";
import { useReferenceStore } from "@/stores/reference";
import { formatNumber, formatDateTime, emptyPage } from "@/utils/format";

const meta = VIEW_META.evaluations;
const pageSizeOptions = PAGE_SIZE_OPTIONS;
const ratingTexts = ["很差", "较差", "一般", "不错", "非常好"];

const auth = useAuthStore();
const references = useReferenceStore();

const isStudent = computed(() => auth.role === "student");
const isAdmin = computed(() => auth.role === "admin");

const loading = ref(false);
const keyword = ref("");
const courseFilter = ref("");

// 学生数据
const myEvaluations = ref([]);
const mySelections = ref([]);

// 教师/管理员数据
const rows = ref([]);
const pager = reactive(emptyPage());
const summary = ref(null);

const dialogVisible = ref(false);
const form = reactive({ courseId: "", courseName: "", teacherName: "", rating: 5, comment: "", anonymous: false });

const evaluatedCourseIds = computed(() => new Set(myEvaluations.value.map((item) => item.courseId)));

const evaluableCourses = computed(() =>
  mySelections.value
    .filter((item) => item.graded && !evaluatedCourseIds.value.has(item.courseId))
);

const myAverageRating = computed(() => {
  if (!myEvaluations.value.length) return "-";
  const sum = myEvaluations.value.reduce((acc, item) => acc + (Number(item.rating) || 0), 0);
  return (sum / myEvaluations.value.length).toFixed(1);
});

function distCount(star) {
  const dist = summary.value?.ratingDistribution || {};
  return Number(dist[star] ?? dist[String(star)] ?? 0);
}

function distPercent(star) {
  const total = Number(summary.value?.totalCount || 0);
  if (!total) return 0;
  return Math.round((distCount(star) / total) * 100);
}

async function loadStudent() {
  loading.value = true;
  try {
    const [evalPage, selectionPage] = await Promise.all([
      evaluationApi.list({ page: 1, pageSize: 200 }),
      listModule(ENDPOINTS.selections, { page: 1, pageSize: 200 }),
    ]);
    myEvaluations.value = evalPage?.items || [];
    mySelections.value = selectionPage?.items || [];
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

async function loadList() {
  loading.value = true;
  try {
    const data = await evaluationApi.list({
      keyword: keyword.value,
      courseId: courseFilter.value,
      page: pager.page,
      pageSize: pager.pageSize,
    });
    rows.value = data?.items || [];
    pager.total = data?.total || 0;
    pager.totalPages = data?.totalPages || 0;
    pager.page = data?.page || pager.page;
    pager.pageSize = data?.pageSize || pager.pageSize;
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

async function loadSummary() {
  if (!courseFilter.value) {
    summary.value = null;
    return;
  }
  try {
    summary.value = await evaluationApi.summary(courseFilter.value);
  } catch (error) {
    summary.value = null;
  }
}

function search() {
  pager.page = 1;
  loadList();
}

function onCourseFilterChange() {
  pager.page = 1;
  loadList();
  loadSummary();
}

function onPage(page) {
  pager.page = page;
  loadList();
}

function onSize(size) {
  pager.pageSize = size;
  pager.page = 1;
  loadList();
}

function openEvaluate(course) {
  form.courseId = course.courseId;
  form.courseName = course.courseName;
  form.teacherName = course.teacherName;
  form.rating = 5;
  form.comment = "";
  form.anonymous = false;
  dialogVisible.value = true;
}

async function submitEvaluation() {
  if (!form.rating) {
    ElMessage.warning("请先选择评分");
    return;
  }
  loading.value = true;
  try {
    await evaluationApi.create({
      courseId: form.courseId,
      rating: form.rating,
      comment: form.comment,
      anonymous: form.anonymous,
    });
    ElMessage.success("评价提交成功");
    dialogVisible.value = false;
    await loadStudent();
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

async function removeEvaluation(row) {
  try {
    await ElMessageBox.confirm("确认删除这条评价吗？此操作不可撤销。", "删除确认", {
      type: "warning",
      confirmButtonText: "删除",
      cancelButtonText: "取消",
    });
  } catch (error) {
    return;
  }
  loading.value = true;
  try {
    await evaluationApi.remove(row.id);
    ElMessage.success("删除成功");
    if (isStudent.value) {
      await loadStudent();
    } else {
      await loadList();
      await loadSummary();
    }
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

onMounted(async () => {
  if (isStudent.value) {
    await loadStudent();
  } else {
    await references.load();
    await loadList();
  }
});
</script>

<style scoped>
.header-search { width: 240px; max-width: 50vw; }
.course-filter { width: 200px; }

.stat-strip { display: flex; gap: 14px; margin-bottom: 18px; flex-wrap: wrap; }
.stat-item {
  flex: 1;
  min-width: 140px;
  padding: 16px 18px;
  border-radius: 16px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
}
.stat-item span { font-size: 13px; color: var(--ink-soft); }
.stat-item strong { display: block; font-size: 26px; font-weight: 800; margin-top: 4px; }

.panel { margin-bottom: 18px; }
.panel-head { display: flex; align-items: center; justify-content: space-between; }
.eyebrow { margin: 0; font-size: 12px; color: var(--ink-faint); }
.panel-head h3 { margin: 2px 0 0; font-size: 17px; }

.eval-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 16px; }
.eval-card {
  display: flex;
  flex-direction: column;
  padding: 18px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}
.eval-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }
.eval-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.eval-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(245, 158, 11, 0.14);
  color: #d97706;
  font-size: 20px;
}
.eval-card h3 { margin: 0 0 10px; font-size: 16px; }
.eval-meta { display: grid; gap: 8px; margin-bottom: 14px; }
.eval-meta span { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ink-soft); }
.eval-meta .el-icon { color: var(--brand); }
.eval-foot { margin-top: auto; display: flex; justify-content: flex-end; }

.my-eval-list { display: grid; gap: 12px; }
.my-eval-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 14px;
  background: var(--surface);
  border: 1px solid var(--line);
}
.my-eval-title { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.my-eval-title strong { font-size: 15px; }
.my-eval-comment { margin: 6px 0 4px; font-size: 13px; color: var(--ink-soft); }
.my-eval-row small { color: var(--ink-faint); font-size: 12px; }

.summary-card :deep(.el-card__body) { display: flex; gap: 28px; flex-wrap: wrap; }
.summary-left { min-width: 220px; }
.summary-left h3 { margin: 4px 0 12px; font-size: 18px; }
.summary-score { display: flex; align-items: center; gap: 14px; }
.summary-score strong { font-size: 40px; font-weight: 800; color: #d97706; }
.summary-score small { display: block; color: var(--ink-faint); font-size: 12px; margin-top: 4px; }

.summary-right { flex: 1; min-width: 240px; display: grid; gap: 8px; align-content: center; }
.dist-row { display: grid; grid-template-columns: 40px 1fr 36px; align-items: center; gap: 10px; }
.dist-label { font-size: 12px; color: var(--ink-soft); }
.dist-track { height: 10px; border-radius: 999px; background: var(--bg-2); overflow: hidden; }
.dist-fill { height: 100%; border-radius: 999px; background: linear-gradient(90deg, #f59e0b, #fbbf24); transition: width 0.5s ease; }
.dist-count { font-size: 12px; color: var(--ink-faint); text-align: right; }

.dialog-course {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: var(--bg-2);
  margin-bottom: 16px;
}
.dialog-course .el-icon { font-size: 22px; color: var(--brand); }
.dialog-course strong { display: block; font-size: 15px; }
.dialog-course small { color: var(--ink-faint); font-size: 12px; }
.anon-hint { margin-left: 10px; font-size: 12px; color: var(--ink-faint); }

.pager { display: flex; justify-content: flex-end; margin-top: 16px; }

@media (max-width: 700px) {
  .summary-card :deep(.el-card__body) { flex-direction: column; }
}
</style>
