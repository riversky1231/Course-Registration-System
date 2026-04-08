const VIEW_META = {
  dashboard: {
    key: "dashboard",
    label: "学期总览",
    caption: "首页概览",
    description: "查看当前学期的课程、人员、成绩与选课运行情况。",
    searchPlaceholder: "",
  },
  profile: {
    key: "profile",
    label: "个人资料",
    caption: "个人中心",
    description: "维护当前账号的基础信息、联系方式和身份资料。",
    searchPlaceholder: "",
  },
  admins: {
    key: "admins",
    label: "管理员管理",
    caption: "后台账号",
    description: "维护教务与系统管理员账号信息。",
    searchPlaceholder: "按账号、姓名或电话查询管理员",
  },
  teachers: {
    key: "teachers",
    label: "教师档案",
    caption: "教师信息",
    description: "查看和维护教师资料、教工号及岗位信息。",
    searchPlaceholder: "按账号、教工号、姓名或职位查询教师",
  },
  students: {
    key: "students",
    label: "学生档案",
    caption: "学生信息",
    description: "管理学生学号、学院、专业和班级等基础信息。",
    searchPlaceholder: "按账号、学号、姓名、学院、专业或班级查询学生",
  },
  courses: {
    key: "courses",
    label: "课程中心",
    caption: "课程信息",
    description: "查看课程编号、学分、授课教师和课程简介。",
    searchPlaceholder: "按课程名称、编号或教师搜索课程",
  },
  selections: {
    key: "selections",
    label: "选课记录",
    caption: "选课管理",
    description: "跟踪选课、退选和成绩录入状态。",
    searchPlaceholder: "按课程、学生或教师关键字搜索选课记录",
  },
};

const TABLE_COLUMNS = {
  admins: [
    { key: "username", label: "账号" },
    { key: "name", label: "姓名" },
    { key: "tele", label: "电话" },
  ],
  teachers: [
    { key: "username", label: "账号" },
    { key: "numb", label: "教工号" },
    { key: "tname", label: "姓名" },
    { key: "tposition", label: "职位" },
    { key: "ttel", label: "电话" },
    { key: "gender", label: "性别" },
  ],
  students: [
    { key: "username", label: "账号" },
    { key: "numb", label: "学号" },
    { key: "sname", label: "姓名" },
    { key: "sdept", label: "学院" },
    { key: "smajor", label: "专业" },
    { key: "sclass", label: "班级" },
  ],
  courses: [
    { key: "name", label: "课程名称" },
    { key: "numb", label: "课程编号" },
    { key: "teacherName", label: "授课教师" },
    { key: "score", label: "学分" },
    { key: "jianjie", label: "课程简介" },
  ],
  selections: [
    { key: "courseName", label: "课程" },
    { key: "studentName", label: "学生" },
    { key: "teacherName", label: "教师" },
    { key: "score", label: "成绩" },
    { key: "createTime", label: "选课时间" },
  ],
};

const ENDPOINTS = {
  admins: "/api/admins",
  teachers: "/api/teachers",
  students: "/api/students",
  courses: "/api/courses",
  selections: "/api/selections",
};

const ROLE_LABELS = {
  admin: "管理员工作台",
  teacher: "教师工作台",
  student: "学生工作台",
};

const SUMMARY_HINTS = {
  admins: "教务后台可见的管理员账号数量",
  teachers: "当前角色可见的教师数量",
  students: "当前角色可见的学生数量",
  courses: "当前课程池中的课程总量",
  selections: "当前角色可见的选课记录总数",
};

const SEMESTER_LABEL = "2026 春季学期";

const { createApp, ref, reactive, computed, onMounted } = Vue;

function clonePlain(value) {
  return JSON.parse(JSON.stringify(value));
}

function safeText(value, fallback = "-") {
  if (value === null || value === undefined) {
    return fallback;
  }
  const text = String(value).trim();
  return text ? text : fallback;
}

function formatDateInput(value) {
  if (!value) {
    return "";
  }
  return String(value).slice(0, 10);
}

function toDateTimeValue(value) {
  if (!value) {
    return null;
  }
  return value.length === 10 ? `${value}T00:00:00` : value;
}

function formatNumber(value) {
  if (value === null || value === undefined || value === "") {
    return "-";
  }
  const numeric = Number(value);
  if (Number.isNaN(numeric)) {
    return String(value);
  }
  return Number.isInteger(numeric) ? String(numeric) : numeric.toFixed(1);
}

function createEmptyProfile() {
  return {
    username: "",
    password: "",
    name: "",
    tele: "",
    numb: "",
    tname: "",
    tbirthday: "",
    tposition: "",
    ttel: "",
    age: null,
    gender: "",
    sname: "",
    sdept: "",
    sbirthday: "",
    ssex: "",
    smajor: "",
    sclass: "",
  };
}

function createEmptyInsights() {
  return {
    role: "",
    roleLabel: "",
    displayName: "",
    admins: 0,
    teachers: 0,
    students: 0,
    courses: 0,
    selections: 0,
    averageScore: 0,
    averageCredits: 0,
    pendingGrades: 0,
    unassignedCourses: 0,
    recentSelections: [],
    courseSpotlights: [],
    peopleSpotlights: [],
    departmentSpotlights: [],
    notices: [],
  };
}

function createEmptyModal(view) {
  if (view === "admins") {
    return { username: "", password: "", name: "", tele: "" };
  }
  if (view === "teachers") {
    return {
      username: "",
      password: "",
      numb: "",
      tname: "",
      tbirthday: "",
      tposition: "",
      ttel: "",
      age: null,
      gender: "男",
    };
  }
  if (view === "students") {
    return {
      username: "",
      password: "",
      numb: "",
      sname: "",
      sdept: "",
      sbirthday: "",
      tele: "",
      ssex: "男",
      age: null,
      smajor: "",
      sclass: "",
    };
  }
  if (view === "courses") {
    return {
      name: "",
      numb: "",
      score: 0,
      tid: "",
      jianjie: "",
    };
  }
  return {
    courseId: "",
    studentId: "",
    score: 0,
  };
}

createApp({
  setup() {
    const booting = ref(true);
    const pending = ref(false);
    const authMode = ref("login");
    const currentView = ref("dashboard");
    const searchText = ref("");
    const session = ref(null);
    const loginForm = reactive({ username: "", password: "", role: "admin" });
    const registerForm = reactive({ username: "", password: "", role: "student" });
    const profileForm = reactive(createEmptyProfile());
    const summary = reactive({
      admins: 0,
      teachers: 0,
      students: 0,
      courses: 0,
      selections: 0,
    });
    const insights = reactive(createEmptyInsights());
    const records = reactive({
      admins: [],
      teachers: [],
      students: [],
      courses: [],
      selections: [],
    });
    const references = reactive({
      teachers: [],
      students: [],
      courses: [],
    });
    const modal = reactive({
      visible: false,
      mode: "create",
      form: {},
    });
    const message = reactive({
      type: "info",
      text: "",
    });

    let messageTimer = null;

    const roleLabel = computed(() => ROLE_LABELS[session.value?.role] || "校园工作台");

    const availableViews = computed(() => {
      if (!session.value) {
        return [];
      }
      if (session.value.role === "admin") {
        return [
          VIEW_META.dashboard,
          VIEW_META.profile,
          VIEW_META.admins,
          VIEW_META.teachers,
          VIEW_META.students,
          VIEW_META.courses,
          VIEW_META.selections,
        ];
      }
      if (session.value.role === "teacher") {
        return [
          VIEW_META.dashboard,
          VIEW_META.profile,
          VIEW_META.students,
          VIEW_META.courses,
          VIEW_META.selections,
        ];
      }
      return [
        VIEW_META.dashboard,
        VIEW_META.profile,
        VIEW_META.teachers,
        VIEW_META.courses,
        VIEW_META.selections,
      ];
    });

    const currentMeta = computed(() => VIEW_META[currentView.value] || VIEW_META.dashboard);
    const currentColumns = computed(() => TABLE_COLUMNS[currentView.value] || []);
    const currentRows = computed(() => records[currentView.value] || []);
    const showSearchBar = computed(() => Boolean(ENDPOINTS[currentView.value]));
    const canCreateCurrent = computed(() => canCreate(currentView.value));
    const canEditRow = computed(() => canEdit(currentView.value));
    const canDeleteRow = computed(() => canDelete(currentView.value));
    const showRowActions = computed(() => canEditRow.value || canDeleteRow.value);

    const overviewCards = computed(() =>
      availableViews.value
        .filter((item) => !["dashboard", "profile"].includes(item.key))
        .map((item) => ({
          key: item.key,
          label: item.label,
          value: summary[item.key] ?? 0,
          hint: SUMMARY_HINTS[item.key] || item.description,
        }))
    );

    const dashboardHero = computed(() => {
      if (!session.value) {
        return {
          title: "",
          description: "",
          primaryView: "courses",
          primaryLabel: "查看课程",
          secondaryView: "profile",
          secondaryLabel: "个人资料",
        };
      }
      if (session.value.role === "admin") {
        return {
          title: "统一维护课程、人员与选课运行。",
          description: "",
          primaryView: "courses",
          primaryLabel: "进入课程中心",
          secondaryView: "students",
          secondaryLabel: "查看学生档案",
        };
      }
      if (session.value.role === "teacher") {
        return {
          title: "聚焦本人课程与成绩录入。",
          description: "",
          primaryView: "selections",
          primaryLabel: "录入课程成绩",
          secondaryView: "courses",
          secondaryLabel: "维护我的课程",
        };
      }
      return {
        title: "安排本学期课程并跟踪成绩进度。",
        description: "",
        primaryView: "courses",
        primaryLabel: "浏览课程中心",
        secondaryView: "selections",
        secondaryLabel: "查看我的选课",
      };
    });

    const dashboardMetrics = computed(() => {
      if (!session.value) {
        return [];
      }
      if (session.value.role === "admin") {
        return [
          { key: "courses", label: "课程总量", value: summary.courses, hint: "当前课程池中的开课数量" },
          { key: "selections", label: "选课记录", value: summary.selections, hint: "系统已生成的选课数据量" },
          { key: "pendingGrades", label: "待录成绩", value: insights.pendingGrades, hint: "需要教师继续补录的课程成绩" },
          { key: "unassignedCourses", label: "未排教师", value: insights.unassignedCourses, hint: "尚未完成教师分配的课程" },
        ];
      }
      if (session.value.role === "teacher") {
        return [
          { key: "courses", label: "我的课程", value: summary.courses, hint: "当前由你负责的课程数量" },
          { key: "selections", label: "相关选课", value: summary.selections, hint: "与你课程关联的学生选课记录" },
          { key: "pendingGrades", label: "待录成绩", value: insights.pendingGrades, hint: "等待你录入成绩的课程记录" },
          { key: "averageScore", label: "平均成绩", value: formatNumber(insights.averageScore), hint: "基于当前可见记录统计" },
        ];
      }
      return [
        { key: "courses", label: "课程池", value: summary.courses, hint: "当前可浏览的课程总量" },
        { key: "selections", label: "已选课程", value: summary.selections, hint: "与你本人关联的选课记录" },
        { key: "averageCredits", label: "平均学分", value: formatNumber(insights.averageCredits), hint: "课程池的平均学分水平" },
        { key: "pendingGrades", label: "待出成绩", value: insights.pendingGrades, hint: "教师尚未录入成绩的课程数" },
      ];
    });

    const moduleActionLabel = computed(() => {
      if (currentView.value === "admins") return "新增管理员";
      if (currentView.value === "teachers") return "新增教师";
      if (currentView.value === "students") return "新增学生";
      if (currentView.value === "courses") return "新增课程";
      if (currentView.value === "selections") {
        return session.value?.role === "student" ? "发起选课" : "新增选课";
      }
      return "新增";
    });

    const editActionLabel = computed(() => {
      if (currentView.value === "selections" && session.value?.role === "teacher") {
        return "录入成绩";
      }
      if (currentView.value === "courses" && session.value?.role === "teacher") {
        return "维护课程";
      }
      return "编辑";
    });

    const deleteActionLabel = computed(() =>
      currentView.value === "selections" && session.value?.role === "student" ? "退选" : "删除"
    );

    const profileIntro = computed(() => {
      if (!session.value) {
        return "";
      }
      if (session.value.role === "admin") return "管理员资料会影响后台账号识别与联系信息展示。";
      if (session.value.role === "teacher") return "教师资料会同步显示在课程中心和选课记录中。";
      return "学生资料用于课程查询、学院统计与个人选课身份识别。";
    });

    const profileHighlights = computed(() => {
      if (!session.value) {
        return [];
      }
      if (session.value.role === "admin") {
        return [
          { label: "账号", value: safeText(profileForm.username) },
          { label: "姓名", value: safeText(profileForm.name) },
          { label: "电话", value: safeText(profileForm.tele) },
        ];
      }
      if (session.value.role === "teacher") {
        return [
          { label: "教工号", value: safeText(profileForm.numb) },
          { label: "姓名", value: safeText(profileForm.tname) },
          { label: "职位", value: safeText(profileForm.tposition) },
        ];
      }
      return [
        { label: "学号", value: safeText(profileForm.numb) },
        { label: "学院", value: safeText(profileForm.sdept) },
        { label: "专业", value: safeText(profileForm.smajor) },
      ];
    });

    const modalTitle = computed(() => {
      const verb = modal.mode === "create" ? "新增" : "编辑";
      return `${verb}${currentMeta.value.label}`;
    });

    function setMessage(type, text) {
      message.type = type;
      message.text = text;
      if (messageTimer) {
        clearTimeout(messageTimer);
      }
      messageTimer = setTimeout(() => {
        message.text = "";
      }, 3200);
    }

    async function api(url, options = {}) {
      const config = {
        credentials: "same-origin",
        headers: {
          "Content-Type": "application/json",
          ...(options.headers || {}),
        },
        ...options,
      };

      const response = await fetch(url, config);
      let payload = null;
      try {
        payload = await response.json();
      } catch (error) {
        payload = null;
      }

      if (!response.ok) {
        if (response.status === 401) {
          session.value = null;
        }
        throw new Error(payload?.message || `请求失败(${response.status})`);
      }

      if (payload && payload.success === false) {
        throw new Error(payload.message || "请求失败");
      }

      return payload ? payload.data : null;
    }

    function canCreate(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections"].includes(view);
      }
      if (session.value.role === "teacher") {
        return view === "courses";
      }
      return view === "selections";
    }

    function canEdit(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections"].includes(view);
      }
      if (session.value.role === "teacher") {
        return view === "courses" || view === "selections";
      }
      return false;
    }

    function canDelete(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections"].includes(view);
      }
      if (session.value.role === "teacher") {
        return view === "courses";
      }
      return view === "selections";
    }

    function syncSessionDisplayName(data) {
      if (!session.value || !data) {
        return;
      }
      if (session.value.role === "admin") {
        session.value.displayName = safeText(data.name, safeText(data.username, session.value.username));
        return;
      }
      if (session.value.role === "teacher") {
        session.value.displayName = safeText(data.tname, safeText(data.username, session.value.username));
        return;
      }
      session.value.displayName = safeText(data.sname, safeText(data.username, session.value.username));
    }

    function fillProfileForm(data) {
      Object.assign(profileForm, createEmptyProfile(), clonePlain(data || {}));
      profileForm.tbirthday = formatDateInput(profileForm.tbirthday);
      profileForm.sbirthday = formatDateInput(profileForm.sbirthday);
      profileForm.password = "";
      syncSessionDisplayName(data);
    }

    function prepareModalForm(view, row) {
      const form = clonePlain(row);
      if (view === "teachers" && form.tbirthday) {
        form.tbirthday = formatDateInput(form.tbirthday);
      }
      if (view === "students" && form.sbirthday) {
        form.sbirthday = formatDateInput(form.sbirthday);
      }
      if (view === "courses" && session.value.role === "teacher") {
        form.tid = session.value.id;
      }
      return form;
    }

    function normalizePayload(view, form, mode) {
      const payload = clonePlain(form);
      if (view === "teachers") {
        payload.tbirthday = toDateTimeValue(payload.tbirthday);
        return payload;
      }
      if (view === "students") {
        payload.sbirthday = toDateTimeValue(payload.sbirthday);
        return payload;
      }
      if (view === "courses") {
        if (session.value.role === "teacher") {
          delete payload.tid;
        }
        return payload;
      }
      if (view === "selections") {
        if (session.value.role === "student") {
          return { courseId: payload.courseId };
        }
        if (session.value.role === "teacher" && mode === "edit") {
          return { score: payload.score };
        }
        return {
          courseId: payload.courseId,
          studentId: payload.studentId,
          score: payload.score,
        };
      }
      return payload;
    }

    function renderCell(row, key) {
      const value = row[key];
      if ((!value && value !== 0) || value === "") {
        if (key === "teacherName") return "待安排教师";
        return "-";
      }
      if (key === "createTime") {
        return formatDateTime(value);
      }
      if (key === "score") {
        if (currentView.value === "courses") {
          return `${formatNumber(value)} 学分`;
        }
        if (value === null || value === undefined || Number(value) <= 0) {
          return "待录入";
        }
        return `${formatNumber(value)} 分`;
      }
      return value;
    }

    function cellClass(key, value) {
      if (key === "score") {
        if (currentView.value === "courses") {
          return "cell-badge is-neutral";
        }
        return value === null || value === undefined || Number(value) <= 0
          ? "cell-badge is-warn"
          : "cell-badge is-success";
      }
      if (key === "teacherName" && !value) {
        return "cell-badge is-neutral";
      }
      return "";
    }

    function formatDateTime(value) {
      if (!value) {
        return "-";
      }
      return new Date(value).toLocaleString("zh-CN", { hour12: false });
    }

    function viewMetric(viewKey) {
      if (!Object.prototype.hasOwnProperty.call(summary, viewKey)) {
        return null;
      }
      return summary[viewKey];
    }

    async function loadSummary() {
      const data = await api("/api/dashboard/summary");
      Object.assign(summary, data || {});
    }

    async function loadInsights() {
      const data = await api("/api/dashboard/insights");
      Object.assign(insights, createEmptyInsights(), data || {});
    }

    async function loadProfile() {
      const data = await api("/api/profile");
      fillProfileForm(data);
    }

    async function loadReferences() {
      const [teachers, students, courses] = await Promise.all([
        api("/api/teachers").catch(() => []),
        api("/api/students").catch(() => []),
        api("/api/courses").catch(() => []),
      ]);
      references.teachers = teachers || [];
      references.students = students || [];
      references.courses = courses || [];
    }

    async function loadDashboardData() {
      await Promise.all([loadSummary(), loadInsights(), loadReferences()]);
    }

    async function loadModule(view) {
      if (!ENDPOINTS[view]) {
        return;
      }
      const keyword = searchText.value ? `?keyword=${encodeURIComponent(searchText.value)}` : "";
      records[view] = await api(`${ENDPOINTS[view]}${keyword}`);
    }

    async function refreshCurrent() {
      try {
        pending.value = true;
        if (currentView.value === "dashboard") {
          await loadDashboardData();
        } else if (currentView.value === "profile") {
          await loadProfile();
        } else {
          await Promise.all([loadModule(currentView.value), loadReferences()]);
        }
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function bootstrap() {
      try {
        session.value = await api("/api/auth/status");
        if (!session.value) {
          return;
        }
        await Promise.all([loadDashboardData(), loadProfile()]);
      } catch (error) {
        session.value = null;
      } finally {
        booting.value = false;
      }
    }

    async function login() {
      try {
        pending.value = true;
        session.value = await api("/api/auth/login", {
          method: "POST",
          body: JSON.stringify(loginForm),
        });
        currentView.value = "dashboard";
        await Promise.all([loadDashboardData(), loadProfile()]);
        setMessage("success", "登录成功，已进入工作台");
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function register() {
      try {
        pending.value = true;
        await api("/api/auth/register", {
          method: "POST",
          body: JSON.stringify(registerForm),
        });
        authMode.value = "login";
        loginForm.username = registerForm.username;
        loginForm.password = registerForm.password;
        loginForm.role = registerForm.role;
        registerForm.username = "";
        registerForm.password = "";
        registerForm.role = "student";
        setMessage("success", "注册完成，请使用新账号登录");
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function logout() {
      try {
        pending.value = true;
        await api("/api/auth/logout", { method: "POST" });
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        session.value = null;
        currentView.value = "dashboard";
        pending.value = false;
      }
    }

    function changeView(view) {
      currentView.value = view;
      searchText.value = "";
      refreshCurrent();
    }

    function openCreate() {
      modal.visible = true;
      modal.mode = "create";
      modal.form = createEmptyModal(currentView.value);
      if (currentView.value === "courses" && session.value?.role === "teacher") {
        modal.form.tid = session.value.id;
      }
    }

    function openEdit(row) {
      modal.visible = true;
      modal.mode = "edit";
      modal.form = prepareModalForm(currentView.value, row);
    }

    function closeModal() {
      modal.visible = false;
      modal.form = {};
    }

    async function saveModal() {
      const view = currentView.value;
      const endpoint = ENDPOINTS[view];
      const payload = normalizePayload(view, modal.form, modal.mode);
      try {
        pending.value = true;
        if (modal.mode === "create") {
          await api(endpoint, {
            method: "POST",
            body: JSON.stringify(payload),
          });
        } else {
          await api(`${endpoint}/${modal.form.id}`, {
            method: "PUT",
            body: JSON.stringify(payload),
          });
        }
        closeModal();
        await Promise.all([loadModule(view), loadSummary(), loadInsights(), loadReferences()]);
        setMessage("success", "保存成功");
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function removeRow(row) {
      if (!window.confirm(`确认${deleteActionLabel.value}当前记录吗？`)) {
        return;
      }
      try {
        pending.value = true;
        await api(`${ENDPOINTS[currentView.value]}/${row.id}`, { method: "DELETE" });
        await Promise.all([loadModule(currentView.value), loadSummary(), loadInsights(), loadReferences()]);
        setMessage("success", `${deleteActionLabel.value}成功`);
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function saveProfile() {
      const payload = clonePlain(profileForm);
      payload.tbirthday = toDateTimeValue(payload.tbirthday);
      payload.sbirthday = toDateTimeValue(payload.sbirthday);
      try {
        pending.value = true;
        const updated = await api("/api/profile", {
          method: "PUT",
          body: JSON.stringify(payload),
        });
        fillProfileForm(updated);
        await Promise.all([loadSummary(), loadInsights(), loadReferences()]);
        setMessage("success", "个人资料已更新");
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    onMounted(bootstrap);

    return {
      booting,
      pending,
      authMode,
      currentView,
      searchText,
      session,
      summary,
      loginForm,
      registerForm,
      profileForm,
      insights,
      references,
      modal,
      message,
      semesterLabel: SEMESTER_LABEL,
      roleLabel,
      availableViews,
      currentMeta,
      currentColumns,
      currentRows,
      showSearchBar,
      canCreateCurrent,
      canEditRow,
      canDeleteRow,
      showRowActions,
      overviewCards,
      dashboardHero,
      dashboardMetrics,
      moduleActionLabel,
      editActionLabel,
      deleteActionLabel,
      profileIntro,
      profileHighlights,
      modalTitle,
      renderCell,
      cellClass,
      formatDateTime,
      viewMetric,
      login,
      register,
      logout,
      changeView,
      refreshCurrent,
      openCreate,
      openEdit,
      closeModal,
      saveModal,
      removeRow,
      saveProfile,
    };
  },
}).mount("#app");
