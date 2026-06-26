<template>
  <section class="auth-shell">
    <div class="auth-aurora auth-aurora-a"></div>
    <div class="auth-aurora auth-aurora-b"></div>
    <div class="auth-aurora auth-aurora-c"></div>

    <div class="auth-layout">
      <div class="auth-card">
        <div class="auth-logo">
          <span class="auth-logo-mark">SC</span>
          <div>
            <p class="eyebrow">Student Course System</p>
            <h3>学生选课系统</h3>
          </div>
        </div>
        <div class="auth-card-head">
          <p class="eyebrow">Portal Access</p>
          <h2>{{ mode === "login" ? "登录系统" : "注册账号" }}</h2>
          <p>{{ mode === "login" ? "请输入账号、密码并选择身份进入系统。" : "创建新的学生账号以进入选课平台。" }}</p>
        </div>

        <el-radio-group v-model="mode" class="auth-switch" size="large">
          <el-radio-button value="login">登录</el-radio-button>
          <el-radio-button value="register">注册</el-radio-button>
        </el-radio-group>

        <el-form v-if="mode === 'login'" class="auth-form" label-position="top" size="large" @submit.prevent="onLogin">
          <el-form-item label="用户名">
            <el-input v-model.trim="loginForm.username" maxlength="20" placeholder="请输入账号" prefix-icon="User" clearable />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="loginForm.password" type="password" maxlength="20" placeholder="请输入密码" prefix-icon="Lock" show-password />
          </el-form-item>
          <el-form-item label="登录身份">
            <el-select v-model="loginForm.role" class="full-width" placeholder="选择身份">
              <el-option label="管理员" value="admin" />
              <el-option label="教师" value="teacher" />
              <el-option label="学生" value="student" />
            </el-select>
          </el-form-item>
          <el-button class="auth-submit" type="primary" size="large" native-type="submit" :loading="pending" round>
            进入学生选课系统
          </el-button>
        </el-form>

        <el-form v-else class="auth-form" label-position="top" size="large" @submit.prevent="onRegister">
          <el-form-item label="用户名">
            <el-input v-model.trim="registerForm.username" placeholder="创建登录账号" prefix-icon="User" clearable />
            <small class="field-hint">{{ rules.usernameHint }}</small>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="registerForm.password" type="password" placeholder="创建登录密码" prefix-icon="Lock" show-password />
            <small class="field-hint">{{ rules.passwordHint }}</small>
          </el-form-item>
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="当前仅开放学生自助注册"
            description="注册身份固定为学生。教师和管理员账号请由管理员在系统内创建。"
          />
          <el-button class="auth-submit" type="primary" size="large" native-type="submit" :loading="pending" round>
            创建账号
          </el-button>
        </el-form>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useAuthStore } from "@/stores/auth";
import { AUTH_RULES } from "@/constants/modules";

const router = useRouter();
const auth = useAuthStore();
const rules = AUTH_RULES;

const mode = ref("login");
const pending = ref(false);
const loginForm = reactive({ username: "", password: "", role: "admin" });
const registerForm = reactive({ username: "", password: "" });

function validateLogin() {
  if (!loginForm.username.trim()) return "用户名不能为空";
  if (!loginForm.password.trim()) return "密码不能为空";
  return "";
}

function validateRegister() {
  if (!registerForm.username.trim()) return "用户名不能为空";
  if (!rules.usernamePattern.test(registerForm.username.trim())) return rules.usernameHint;
  if (!registerForm.password.trim()) return "密码不能为空";
  if (!rules.passwordPattern.test(registerForm.password)) return rules.passwordHint;
  return "";
}

async function onLogin() {
  const message = validateLogin();
  if (message) return ElMessage.error(message);
  pending.value = true;
  try {
    await auth.login(loginForm);
    ElMessage.success("登录成功，正在进入工作台");
    router.push("/dashboard");
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    pending.value = false;
  }
}

async function onRegister() {
  const message = validateRegister();
  if (message) return ElMessage.error(message);
  pending.value = true;
  try {
    await auth.register(registerForm);
    loginForm.username = registerForm.username;
    loginForm.password = registerForm.password;
    loginForm.role = "student";
    registerForm.username = "";
    registerForm.password = "";
    mode.value = "login";
    ElMessage.success("注册完成，请使用新账号登录");
  } catch (error) {
    ElMessage.error(error.message);
  } finally {
    pending.value = false;
  }
}
</script>

<style scoped>
.auth-shell {
  position: relative;
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 32px;
  overflow: hidden;
}

.auth-aurora {
  position: absolute;
  border-radius: 50%;
  filter: blur(70px);
  opacity: 0.55;
  z-index: 0;
}

.auth-aurora-a { width: 520px; height: 520px; background: #7b6ef6; top: -160px; left: -120px; }
.auth-aurora-b { width: 460px; height: 460px; background: #5b5bf0; bottom: -180px; right: -80px; }
.auth-aurora-c { width: 380px; height: 380px; background: #b89bff; top: 40%; left: 45%; opacity: 0.32; }

.auth-layout {
  position: relative;
  z-index: 1;
  width: min(440px, 100%);
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(255, 255, 255, 0.85);
  border-radius: 28px;
  box-shadow: var(--shadow-lg);
  overflow: hidden;
  backdrop-filter: blur(18px);
}

.auth-card {
  padding: 44px 40px;
  background: var(--surface);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.auth-logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.auth-logo-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 14px;
  background: linear-gradient(140deg, var(--brand), var(--brand-3));
  color: #fff;
  font-weight: 800;
  font-size: 18px;
  box-shadow: 0 10px 22px rgba(91, 91, 240, 0.32);
}

.auth-logo h3 { margin: 2px 0 0; font-size: 17px; color: var(--ink); }

.auth-card-head h2 { margin: 8px 0 6px; font-size: 26px; }
.auth-card-head p { margin: 0; color: var(--ink-soft); font-size: 14px; }

.auth-switch { width: 100%; }
.auth-switch :deep(.el-radio-button) { flex: 1; }
.auth-switch :deep(.el-radio-button__inner) { width: 100%; }

.auth-form { display: flex; flex-direction: column; }

.field-hint {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--ink-faint);
  line-height: 1.5;
}

.auth-submit {
  margin-top: 8px;
  width: 100%;
  font-size: 15px;
  font-weight: 600;
  height: 46px;
}

@media (max-width: 520px) {
  .auth-card { padding: 32px 22px; }
}
</style>
