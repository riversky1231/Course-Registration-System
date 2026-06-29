<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    class="record-dialog"
    width="640px"
    align-center
    @update:model-value="(v) => emit('update:modelValue', v)"
    @closed="emit('closed')"
  >
    <el-form class="data-form" label-position="top" @submit.prevent="emit('save')">
      <el-row :gutter="18">
        <template v-if="view === 'admins'">
          <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="密码"><el-input v-model="form.password" type="password" maxlength="20" show-password /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.name" maxlength="18" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.tele" maxlength="11" /></el-form-item></el-col>
        </template>

        <template v-if="view === 'teachers'">
          <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="密码"><el-input v-model="form.password" type="password" maxlength="20" show-password /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="教工号"><el-input v-model.trim="form.numb" maxlength="32" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.tname" maxlength="18" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="职称/职位"><el-input v-model.trim="form.tposition" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="出生日期"><el-date-picker v-model="form.tbirthday" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.ttel" maxlength="11" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="性别"><el-select v-model="form.gender" class="full-width" placeholder="请选择"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="120" class="full-width" controls-position="right" /></el-form-item></el-col>
        </template>

        <template v-if="view === 'students'">
          <el-col :xs="24" :sm="12"><el-form-item label="账号"><el-input v-model.trim="form.username" maxlength="20" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="密码"><el-input v-model="form.password" type="password" maxlength="20" show-password /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="学号"><el-input v-model.trim="form.numb" maxlength="32" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="姓名"><el-input v-model.trim="form.sname" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="学院"><el-input v-model.trim="form.sdept" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="专业"><el-input v-model.trim="form.smajor" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="班级"><el-input v-model.trim="form.sclass" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="年级"><el-input-number v-model="form.grade" :min="1" :max="8" class="full-width" controls-position="right" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="入学年份"><el-input-number v-model="form.enrollmentYear" :min="1900" :max="2100" class="full-width" controls-position="right" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="出生日期"><el-date-picker v-model="form.sbirthday" type="date" value-format="YYYY-MM-DD" class="full-width" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="电话"><el-input v-model.trim="form.tele" maxlength="11" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="邮箱"><el-input v-model.trim="form.email" maxlength="255" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="性别"><el-select v-model="form.ssex" class="full-width" placeholder="请选择"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="年龄"><el-input-number v-model="form.age" :min="0" :max="120" class="full-width" controls-position="right" /></el-form-item></el-col>
        </template>

        <template v-if="view === 'courses'">
          <el-col :xs="24" :sm="12"><el-form-item label="课程名称"><el-input v-model.trim="form.name" maxlength="18" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="课程编号"><el-input v-model.trim="form.numb" maxlength="32" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="开课学院"><el-input v-model.trim="form.dept" maxlength="255" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="学分"><el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" class="full-width" controls-position="right" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="容量上限"><el-input-number v-model="form.maxStudents" :min="0" :step="1" class="full-width" controls-position="right" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="课程类型"><el-input v-model.trim="form.courseType" maxlength="64" placeholder="如：专业必修" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="年级限制"><el-input-number v-model="form.gradeLimit" :min="1" :max="8" class="full-width" controls-position="right" placeholder="不限" /></el-form-item></el-col>
          <el-col v-if="role === 'admin'" :xs="24" :sm="12">
            <el-form-item label="授课教师">
              <el-select v-model="form.tid" class="full-width" placeholder="待安排" clearable filterable>
                <el-option v-for="t in references.teachers" :key="t.id" :label="t.tname || t.username" :value="t.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="上课时间"><el-input v-model.trim="form.timeSlot" maxlength="64" placeholder="如：周一第3-4节" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="课程简介"><el-input v-model.trim="form.jianjie" type="textarea" :rows="4" /></el-form-item></el-col>
        </template>

        <template v-if="view === 'selections'">
          <el-col v-if="mode === 'edit' && role === 'teacher'" :span="24">
            <el-alert type="info" :closable="false" show-icon :title="(form.courseName || '未命名课程') + ' · ' + (form.studentName || '未命名学生')" description="当前记录" />
          </el-col>

          <template v-if="role === 'student'">
            <el-col :span="24">
              <el-form-item label="选择课程">
                <el-select v-model="form.courseId" class="full-width" filterable placeholder="请选择课程">
                  <el-option
                    v-for="c in references.courses"
                    :key="c.id"
                    :label="courseLabel(c)"
                    :value="c.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24" v-if="feedback.text">
              <el-alert :type="alertType(feedback.type)" :closable="false" show-icon :title="feedback.text" />
            </el-col>
            <el-col :span="24" v-else-if="courseStatus">
              <el-alert :type="alertType(courseStatus.type)" :closable="false" show-icon :title="courseStatus.text" />
            </el-col>
            <el-col :span="24">
              <el-alert type="info" :closable="false" title="提交说明" description="学生发起选课后，成绩会由授课教师在后续统一录入。" />
            </el-col>
          </template>

          <template v-else-if="role === 'teacher'">
            <el-col :span="24"><el-form-item label="成绩录入"><el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" class="full-width" controls-position="right" /></el-form-item></el-col>
          </template>

          <template v-else>
            <el-col :xs="24" :sm="12">
              <el-form-item label="课程">
                <el-select v-model="form.courseId" class="full-width" filterable placeholder="请选择课程">
                  <el-option v-for="c in references.courses" :key="c.id" :label="courseLabel(c)" :value="c.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="学生">
                <el-select v-model="form.studentId" class="full-width" filterable placeholder="请选择学生">
                  <el-option v-for="s in references.students" :key="s.id" :label="s.sname || s.username" :value="s.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24" v-if="feedback.text">
              <el-alert :type="alertType(feedback.type)" :closable="false" show-icon :title="feedback.text" />
            </el-col>
            <el-col :span="24"><el-form-item label="成绩"><el-input-number v-model="form.score" :min="0" :max="100" :step="0.5" class="full-width" controls-position="right" /></el-form-item></el-col>
          </template>
        </template>

        <template v-if="view === 'selectionWindows'">
          <el-col :xs="24" :sm="12">
            <el-form-item label="窗口类型">
              <el-select v-model="form.actionType" class="full-width">
                <el-option label="选课窗口" value="SELECT" />
                <el-option label="退课窗口" value="DROP" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="窗口名称"><el-input v-model.trim="form.name" maxlength="64" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" class="full-width" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" class="full-width" /></el-form-item></el-col>
          <el-col :xs="24" :sm="12"><el-form-item label="启用状态"><el-switch v-model="form.enabled" active-text="启用" inactive-text="停用" inline-prompt /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="说明"><el-input v-model.trim="form.description" type="textarea" :rows="4" /></el-form-item></el-col>
        </template>
      </el-row>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">关闭</el-button>
      <el-button type="primary" :loading="loading" @click="emit('save')">保存变更</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from "vue";
import { useReferenceStore } from "@/stores/reference";
import { useAuthStore } from "@/stores/auth";

const props = defineProps({
  modelValue: Boolean,
  view: { type: String, required: true },
  mode: { type: String, default: "create" },
  form: { type: Object, required: true },
  feedback: { type: Object, default: () => ({ type: "info", text: "" }) },
  loading: Boolean,
});

const emit = defineEmits(["update:modelValue", "save", "closed"]);

const references = useReferenceStore();
const auth = useAuthStore();
const role = computed(() => auth.role);

const title = computed(() => {
  const labels = {
    admins: "管理员",
    teachers: "教师",
    students: "学生",
    courses: "课程",
    selections: "选课记录",
    selectionWindows: "选课窗口",
  };
  return `${props.mode === "create" ? "新增" : "编辑"}${labels[props.view] || ""}`;
});

function courseLabel(course) {
  const cap = course.maxStudents > 0 ? `${course.maxStudents}人` : "不限";
  return `${course.name} / ${course.teacherName || "待安排教师"} / ${course.timeSlot || "未排课"} / ${cap}`;
}

function alertType(type) {
  if (type === "error") return "error";
  if (type === "success") return "success";
  if (type === "warning") return "warning";
  return "info";
}

const courseStatus = computed(() => {
  if (props.view !== "selections" || !props.form.courseId) return null;
  const course = references.courses.find((c) => c.id === props.form.courseId);
  if (!course) return null;
  const max = Number(course.maxStudents || 0);
  const selected = Number(course.selectedCount || 0);
  if (max <= 0) {
    return { type: "info", text: `当前课程不限制容量，已选 ${selected} 人。` };
  }
  const remaining = max - selected;
  if (remaining <= 0) {
    return { type: "error", text: `当前已选 ${selected}/${max} 人，课程已满。` };
  }
  return { type: "success", text: `当前已选 ${selected}/${max} 人，剩余 ${remaining} 个名额。` };
});
</script>

<style scoped>
.record-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}
.record-dialog .data-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
</style>
