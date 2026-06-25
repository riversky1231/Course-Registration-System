<template>
  <div>
    <PageHeader :title="title" :caption="meta.caption" :description="description">
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
        <el-button v-if="allowCreate" type="primary" :icon="'Plus'" @click="mod.openCreate">
          {{ auth.role === "student" ? "发起选课" : "新增选课" }}
        </el-button>
      </template>
    </PageHeader>

    <!-- 学生：卡片式我的选课 -->
    <div v-if="auth.role === 'student'" v-loading="mod.loading.value">
      <div class="stat-strip">
        <div class="stat-item"><span>已选课程</span><strong>{{ mod.pager.total }}</strong></div>
        <div class="stat-item"><span>已出成绩</span><strong>{{ gradedCount }}</strong></div>
        <div class="stat-item"><span>待出成绩</span><strong>{{ mod.rows.value.length - gradedCount }}</strong></div>
      </div>

      <el-empty v-if="!mod.rows.value.length" description="你还没有选修任何课程" />

      <div v-else class="selection-grid">
        <article v-for="item in mod.rows.value" :key="item.id" class="selection-card">
          <div class="sel-top">
            <div class="sel-icon"><el-icon><Tickets /></el-icon></div>
            <el-tag :type="item.graded ? 'success' : 'warning'" effect="light" round>
              {{ item.graded ? formatNumber(item.score) + " 分" : "待录入" }}
            </el-tag>
          </div>
          <h3>{{ item.courseName || "未命名课程" }}</h3>
          <div class="sel-meta">
            <span><el-icon><Reading /></el-icon>{{ item.teacherName || "待安排教师" }}</span>
            <span><el-icon><Clock /></el-icon>{{ item.timeSlot || "未排课" }}</span>
            <span><el-icon><School /></el-icon>{{ item.courseDept || "-" }}</span>
            <span><el-icon><Medal /></el-icon>{{ formatNumber(item.courseCredit) }} 学分</span>
          </div>
          <div class="sel-foot">
            <small>选课时间 {{ formatDateTime(item.createTime) }}</small>
            <el-button type="danger" plain size="small" :icon="'Remove'" @click="mod.remove(item, '退选')">退选</el-button>
          </div>
        </article>
      </div>
    </div>

    <!-- 教师 / 管理员：表格 -->
    <el-card v-else class="panel" shadow="never" v-loading="mod.loading.value">
      <el-empty v-if="!mod.rows.value.length" description="暂无选课记录" />
      <template v-else>
        <el-table :data="mod.rows.value" class="data-table" stripe>
          <el-table-column prop="courseName" label="课程" min-width="150" show-overflow-tooltip />
          <el-table-column prop="courseDept" label="开课学院" min-width="130" show-overflow-tooltip />
          <el-table-column label="学分" width="100">
            <template #default="{ row }">{{ formatNumber(row.courseCredit) }} 学分</template>
          </el-table-column>
          <el-table-column prop="timeSlot" label="上课时间" min-width="130" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生" min-width="110" />
          <el-table-column prop="teacherName" label="教师" min-width="110" />
          <el-table-column label="成绩" width="120">
            <template #default="{ row }">
              <el-tag :type="row.graded ? 'success' : 'warning'" effect="light" round>
                {{ row.graded ? formatNumber(row.score) + " 分" : "待录入" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="选课时间" min-width="170">
            <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="160" align="center">
            <template #default="{ row }">
              <el-button v-if="allowEdit" type="primary" link :icon="'EditPen'" @click="mod.openEdit(row)">
                {{ auth.role === "teacher" ? "录入成绩" : "编辑" }}
              </el-button>
              <el-button v-if="allowDelete" type="danger" link :icon="'Delete'" @click="mod.remove(row, '删除')">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>

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
    </el-card>

    <!-- 学生卡片视图的分页 -->
    <div class="pager" v-if="auth.role === 'student' && mod.pager.total > 0">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="mod.pager.total"
        :current-page="mod.pager.page"
        :page-size="mod.pager.pageSize"
        @current-change="mod.handlePageChange"
      />
    </div>

    <RecordDialog
      v-model="mod.dialog.visible"
      view="selections"
      :mode="mod.dialog.mode"
      :form="mod.dialog.form"
      :feedback="mod.dialog.feedback"
      :loading="mod.loading.value"
      @save="mod.save"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import PageHeader from "@/components/PageHeader.vue";
import RecordDialog from "@/components/RecordDialog.vue";
import { useModule } from "@/composables/useModule";
import { useAuthStore } from "@/stores/auth";
import { useReferenceStore } from "@/stores/reference";
import { VIEW_META, PAGE_SIZE_OPTIONS, canCreate, canEdit, canDelete } from "@/constants/modules";
import { formatNumber, formatDateTime, normalizeOptionalNumber } from "@/utils/format";

const auth = useAuthStore();
const references = useReferenceStore();
const meta = VIEW_META.selections;
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const allowCreate = computed(() => canCreate(auth.role, "selections"));
const allowEdit = computed(() => canEdit(auth.role, "selections"));
const allowDelete = computed(() => canDelete(auth.role, "selections"));

const title = computed(() => (auth.role === "student" ? "我的选课" : meta.label));
const description = computed(() =>
  auth.role === "student" ? "查看你已选修的课程与成绩状态，可在退课窗口内退选。" : meta.description
);

const gradedCount = computed(() => mod.rows.value.filter((r) => r.graded).length);

const mod = useModule("selections", {
  emptyForm: () => ({ courseId: "", studentId: "", score: null }),
  normalize: (form, mode) => {
    const score = normalizeOptionalNumber(form.score);
    if (auth.role === "student") return { courseId: form.courseId };
    if (auth.role === "teacher" && mode === "edit") return { score };
    return { courseId: form.courseId, studentId: form.studentId, score };
  },
  keepDialogOpenOnSave: true,
  savedMessage: "选课提交成功，系统已生成选课记录。",
  onSaved: () => references.load(true),
});

onMounted(mod.load);
</script>

<style scoped>
.header-search { width: 280px; max-width: 50vw; }

.stat-strip {
  display: flex;
  gap: 14px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

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

.selection-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.selection-card {
  display: flex;
  flex-direction: column;
  padding: 18px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.selection-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }

.sel-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }

.sel-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(91, 91, 240, 0.12);
  color: var(--brand);
  font-size: 20px;
}

.selection-card h3 { margin: 0 0 12px; font-size: 17px; }

.sel-meta { display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-bottom: 14px; }
.sel-meta span { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; color: var(--ink-soft); }
.sel-meta .el-icon { color: var(--brand); }

.sel-foot { margin-top: auto; display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.sel-foot small { color: var(--ink-faint); font-size: 11px; }
</style>
