<template>
  <div class="profile-layout" v-loading="loading">
    <el-card class="panel profile-hero" shadow="never">
      <div class="hero-avatar-wrap">
        <el-avatar :size="76" class="hero-avatar">{{ initial }}</el-avatar>
      </div>
      <h2>{{ auth.displayName }}</h2>
      <div class="hero-tags">
        <el-tag effect="dark" round>{{ auth.roleLabel }}</el-tag>
        <el-tag type="warning" effect="light" round>{{ semesterLabel }}</el-tag>
      </div>
      <div class="hero-fields">
        <div v-for="item in summaryItems" :key="item.label" class="hero-field">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </div>
      </div>
    </el-card>

    <el-card class="panel" shadow="never">
      <template #header>
        <div class="panel-head">
          <div><p class="eyebrow">资料维护</p><h3>维护个人资料</h3></div>
          <el-tag type="info" effect="plain" round>更新后即时生效</el-tag>
        </div>
      </template>

      <el-form class="data-form" label-position="top" @submit.prevent="save">
        <el-row :gutter="18">
          <template v-if="auth.role === 'admin'">
            <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="新密码"><el-input v-model="form.password" type="password" maxlength="20" placeholder="留空则不修改" show-password /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.name" maxlength="18" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.tele" maxlength="11" /></el-form-item></el-col>
          </template>

          <template v-if="auth.role === 'teacher'">
            <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="新密码"><el-input v-model="form.password" type="password" maxlength="20" placeholder="留空则不修改" show-password /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="教工号"><el-input v-model.trim="form.numb" maxlength="32" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.tname" maxlength="18" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="职称/职位"><el-input v-model.trim="form.tposition" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="出生日期"><el-date-picker v-model="form.tbirthday" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.ttel" maxlength="11" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="性别"><el-select v-model="form.gender" class="full-width" placeholder="请选择"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="120" class="full-width" controls-position="right" /></el-form-item></el-col>
          </template>

          <template v-if="auth.role === 'student'">
            <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="新密码"><el-input v-model="form.password" type="password" maxlength="20" placeholder="留空则不修改" show-password /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="学号"><el-input v-model.trim="form.numb" maxlength="32" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.sname" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="学院"><el-input v-model.trim="form.sdept" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="专业"><el-input v-model.trim="form.smajor" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="班级"><el-input v-model.trim="form.sclass" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="出生日期"><el-date-picker v-model="form.sbirthday" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.tele" maxlength="11" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model.trim="form.email" maxlength="255" /></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="性别"><el-select v-model="form.ssex" class="full-width" placeholder="请选择"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
            <el-col :xs="24" :sm="12"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="120" class="full-width" controls-position="right" /></el-form-item></el-col>
          </template>
        </el-row>

        <el-button class="form-submit" type="primary" size="large" native-type="submit" :loading="loading" round>保存个人资料</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { profileApi } from "@/api";
import { useAuthStore } from "@/stores/auth";
import { SEMESTER_LABEL } from "@/constants/modules";
import { safeText, formatDateInput, toDateTimeValue, clonePlain, initials } from "@/utils/format";

const auth = useAuthStore();
const semesterLabel = SEMESTER_LABEL;
const loading = ref(false);
const form = reactive({});

const initial = computed(() => initials(auth.displayName));

const summaryItems = computed(() => {
  if (auth.role === "admin") {
    return [
      { label: "账号", value: safeText(form.username) },
      { label: "姓名", value: safeText(form.name) },
      { label: "电话", value: safeText(form.tele) },
    ];
  }
  if (auth.role === "teacher") {
    return [
      { label: "教工号", value: safeText(form.numb) },
      { label: "姓名", value: safeText(form.tname) },
      { label: "电话", value: safeText(form.ttel) },
      { label: "职位", value: safeText(form.tposition) },
    ];
  }
  return [
    { label: "学号", value: safeText(form.numb) },
    { label: "姓名", value: safeText(form.sname) },
    { label: "学院", value: safeText(form.sdept) },
    { label: "班级", value: safeText(form.sclass) },
  ];
});

function fill(data) {
  Object.keys(form).forEach((key) => delete form[key]);
  Object.assign(form, clonePlain(data || {}));
  form.tbirthday = formatDateInput(form.tbirthday);
  form.sbirthday = formatDateInput(form.sbirthday);
  form.password = "";
  syncName(data);
}

function syncName(data) {
  if (auth.role === "admin") auth.setDisplayName(safeText(data.name, safeText(data.username)));
  else if (auth.role === "teacher") auth.setDisplayName(safeText(data.tname, safeText(data.username)));
  else auth.setDisplayName(safeText(data.sname, safeText(data.username)));
}

async function load() {
  loading.value = true;
  try {
    fill(await profileApi.get());
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

async function save() {
  const payload = clonePlain(form);
  payload.tbirthday = toDateTimeValue(payload.tbirthday);
  payload.sbirthday = toDateTimeValue(payload.sbirthday);
  loading.value = true;
  try {
    fill(await profileApi.update(payload));
    ElMessage.success("个人资料已更新");
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    loading.value = false;
  }
}

onMounted(load);
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 20px;
  align-items: start;
}

.profile-hero {
  text-align: center;
  border: none;
  color: #fff;
  background: linear-gradient(155deg, #5b5bf0, #7b6ef6 55%, #9b7df9);
}

.profile-hero :deep(.el-card__body) { padding: 30px 24px; }
.hero-avatar-wrap { margin-bottom: 14px; }
.hero-avatar { background: rgba(255, 255, 255, 0.22); color: #fff; font-size: 30px; font-weight: 700; border: 3px solid rgba(255, 255, 255, 0.4); }
.profile-hero h2 { margin: 0 0 14px; font-size: 22px; }
.hero-tags { display: flex; gap: 8px; justify-content: center; margin-bottom: 22px; flex-wrap: wrap; }

.hero-fields { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
.hero-field { padding: 12px; border-radius: 14px; background: rgba(255, 255, 255, 0.16); border: 1px solid rgba(255, 255, 255, 0.22); text-align: left; }
.hero-field span { display: block; font-size: 11px; color: rgba(255, 255, 255, 0.78); margin-bottom: 4px; }
.hero-field strong { font-size: 15px; word-break: break-all; }

.form-submit { margin-top: 6px; min-width: 200px; height: 44px; font-weight: 600; }

@media (max-width: 1000px) {
  .profile-layout { grid-template-columns: 1fr; }
}
</style>
