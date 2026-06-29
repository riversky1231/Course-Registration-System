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
        <el-button :icon="'Search'" @click="mod.search">搜索</el-button>
        <el-button v-if="allowCreate" type="primary" :icon="'Plus'" @click="mod.openCreate">
          {{ isTeacher ? "新增教师" : "新增学生" }}
        </el-button>
      </template>
    </PageHeader>

    <div v-loading="mod.loading.value">
      <el-empty v-if="!mod.rows.value.length" description="暂无人员档案" />

      <div v-else class="people-grid">
        <article v-for="person in mod.rows.value" :key="person.id" class="people-card">
          <div class="people-head">
            <el-avatar :size="52" class="people-avatar" :class="isTeacher ? 'avatar-teacher' : 'avatar-student'">
              {{ initial(person) }}
            </el-avatar>
            <div class="people-id">
              <strong>{{ name(person) }}</strong>
              <span>{{ subtitle(person) }}</span>
            </div>
            <el-tag size="small" effect="light" round :type="isTeacher ? 'warning' : 'primary'">
              {{ isTeacher ? "教师" : "学生" }}
            </el-tag>
          </div>

          <div class="people-fields">
            <div v-for="field in fields(person)" :key="field.label" class="people-field">
              <span>{{ field.label }}</span>
              <strong>{{ field.value }}</strong>
            </div>
          </div>

          <div v-if="allowEdit || allowDelete" class="people-actions">
            <el-button v-if="allowEdit" :icon="'Edit'" size="small" @click="mod.openEdit(person)">编辑</el-button>
            <el-button v-if="allowDelete" type="danger" plain size="small" :icon="'Delete'" @click="mod.remove(person, '删除')">删除</el-button>
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
      :view="viewKey"
      :mode="mod.dialog.mode"
      :form="mod.dialog.form"
      :loading="mod.loading.value"
      @save="mod.save"
    />
  </div>
</template>

<script setup>
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import PageHeader from "@/components/PageHeader.vue";
import RecordDialog from "@/components/RecordDialog.vue";
import { useModule } from "@/composables/useModule";
import { useAuthStore } from "@/stores/auth";
import { useReferenceStore } from "@/stores/reference";
import { VIEW_META, PAGE_SIZE_OPTIONS, canCreate, canEdit, canDelete } from "@/constants/modules";
import { safeText, formatDateInput, toDateTimeValue, initials } from "@/utils/format";

const route = useRoute();
const auth = useAuthStore();
const references = useReferenceStore();
const pageSizeOptions = PAGE_SIZE_OPTIONS;

const viewKey = route.meta.view; // 'teachers' | 'students'
const isTeacher = computed(() => viewKey === "teachers");
const meta = VIEW_META[viewKey];

const allowCreate = computed(() => canCreate(auth.role, viewKey));
const allowEdit = computed(() => canEdit(auth.role, viewKey));
const allowDelete = computed(() => canDelete(auth.role, viewKey));

const mod = useModule(viewKey, {
  emptyForm: () =>
    isTeacher.value
      ? { username: "", password: "", numb: "", tname: "", tbirthday: "", tposition: "", ttel: "", age: null, gender: "男" }
      : {
          username: "",
          password: "",
          numb: "",
          sname: "",
          sdept: "",
          sbirthday: "",
          tele: "",
          email: "",
          ssex: "男",
          age: null,
          smajor: "",
          sclass: "",
          grade: 1,
          enrollmentYear: new Date().getFullYear(),
        },
  prepareForm: (form) => {
    if (isTeacher.value && form.tbirthday) form.tbirthday = formatDateInput(form.tbirthday);
    if (!isTeacher.value && form.sbirthday) form.sbirthday = formatDateInput(form.sbirthday);
    return form;
  },
  normalize: (form) => {
    const payload = { ...form };
    if (isTeacher.value) payload.tbirthday = toDateTimeValue(payload.tbirthday);
    else payload.sbirthday = toDateTimeValue(payload.sbirthday);
    return payload;
  },
  onSaved: () => references.load(true),
});

function name(person) {
  return safeText(isTeacher.value ? person.tname : person.sname, person.username);
}

function initial(person) {
  return initials(name(person));
}

function subtitle(person) {
  if (isTeacher.value) return `${safeText(person.tposition, "教师")} · ${safeText(person.username)}`;
  return `${safeText(person.smajor, "专业未填")} · ${safeText(person.username)}`;
}

function fields(person) {
  if (isTeacher.value) {
    return [
      { label: "教工号", value: safeText(person.numb) },
      { label: "电话", value: safeText(person.ttel) },
      { label: "性别", value: safeText(person.gender) },
      { label: "年龄", value: safeText(person.age) },
    ];
  }
  return [
    { label: "学号", value: safeText(person.numb) },
    { label: "学院", value: safeText(person.sdept) },
    { label: "年级", value: person.grade ? `大${person.grade}` : "-" },
    { label: "班级", value: safeText(person.sclass) },
    { label: "邮箱", value: safeText(person.email) },
  ];
}

onMounted(mod.load);
</script>

<style scoped>
.header-search { width: 280px; max-width: 50vw; }

.people-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.people-card {
  padding: 18px;
  border-radius: 18px;
  background: var(--surface);
  border: 1px solid var(--line);
  box-shadow: var(--shadow-sm);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.people-card:hover { transform: translateY(-3px); box-shadow: var(--shadow-md); }

.people-head { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.people-avatar { color: #fff; font-weight: 700; flex: none; }
.avatar-teacher { background: linear-gradient(140deg, #d97706, #fbbf24); }
.avatar-student { background: linear-gradient(140deg, #5b5bf0, #9b7df9); }

.people-id { flex: 1; min-width: 0; }
.people-id strong { display: block; font-size: 16px; }
.people-id span { font-size: 12px; color: var(--ink-soft); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.people-fields {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 14px;
  border-radius: 14px;
  background: var(--bg-2);
}

.people-field span { display: block; font-size: 11px; color: var(--ink-faint); }
.people-field strong { font-size: 14px; word-break: break-all; }

.people-actions { display: flex; gap: 8px; margin-top: 14px; }
</style>
