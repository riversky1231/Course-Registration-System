const VIEW_META = {
  profile: {
    key: "profile",
    label: "个人资料",
    caption: "个人中心",
    description: "查看并修改当前账号信息。",
    searchPlaceholder: "",
  },
  gradeReport: {
    key: "gradeReport",
    label: "我的成绩",
    caption: "成绩与绩点",
    description: "查看本人已选课程成绩、通过学分与 GPA 汇总。",
    searchPlaceholder: "按课程名称、教师或上课时间搜索成绩",
  },
  admins: {
    key: "admins",
    label: "管理员管理",
    caption: "后台账号",
    description: "维护教务与系统管理员账号信息。",
    searchPlaceholder: "按账号、姓名或电话查询管理员",
  },
  auditLogs: {
    key: "auditLogs",
    label: "审计日志",
    caption: "操作追踪",
    description: "记录管理员对账号、人员和课程的关键增删改操作。",
    searchPlaceholder: "按管理员、操作对象或详情搜索日志",
  },
  notifications: {
    key: "notifications",
    label: "消息中心",
    caption: "站内消息",
    description: "查看系统通过消息中心发送的站内通知记录。",
    searchPlaceholder: "按标题、状态或内容搜索消息",
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
    description: "查看课程编号、学分、开课学院、容量、上课时间与授课教师安排。",
    searchPlaceholder: "按课程名称、编号或教师搜索课程",
  },
  selections: {
    key: "selections",
    label: "选课记录",
    caption: "选课管理",
    description: "跟踪选课、退选和成绩录入状态。",
    searchPlaceholder: "按课程、学生或教师关键字搜索选课记录",
  },
  selectionWindows: {
    key: "selectionWindows",
    label: "选课窗口",
    caption: "开放时间",
    description: "管理学生选课、退课操作的开放时间范围。",
    searchPlaceholder: "按窗口名称、类型或说明搜索",
  },
};

const VIEW_ICONS = {
  profile: "User",
  gradeReport: "TrophyBase",
  admins: "Avatar",
  auditLogs: "Document",
  notifications: "Bell",
  teachers: "Reading",
  students: "UserFilled",
  courses: "Notebook",
  selections: "Tickets",
  selectionWindows: "Clock",
};

const TABLE_COLUMNS = {
  gradeReport: [
    { key: "courseName", label: "课程", minWidth: 160 },
    { key: "teacherName", label: "授课教师", minWidth: 120 },
    { key: "courseDept", label: "开课学院", minWidth: 140 },
    { key: "courseCredit", label: "学分", width: 110 },
    { key: "timeSlot", label: "上课时间", minWidth: 140 },
    { key: "score", label: "成绩", width: 120 },
  ],
  admins: [
    { key: "username", label: "账号", minWidth: 140 },
    { key: "name", label: "姓名", minWidth: 120 },
    { key: "tele", label: "电话", minWidth: 140 },
  ],
  auditLogs: [
    { key: "adminUsername", label: "操作管理员", minWidth: 130 },
    { key: "action", label: "操作", width: 110 },
    { key: "targetType", label: "对象类型", width: 120 },
    { key: "targetName", label: "对象名称", minWidth: 140 },
    { key: "detail", label: "详情", minWidth: 200 },
    { key: "createTime", label: "时间", minWidth: 170 },
  ],
  notifications: [
    { key: "recipientName", label: "接收人", minWidth: 120 },
    { key: "channel", label: "渠道", width: 100 },
    { key: "status", label: "状态", width: 110 },
    { key: "title", label: "标题", minWidth: 160 },
    { key: "resultMessage", label: "结果", minWidth: 160 },
    { key: "createTime", label: "创建时间", minWidth: 170 },
  ],
  teachers: [
    { key: "username", label: "账号", minWidth: 120 },
    { key: "numb", label: "教工号", minWidth: 120 },
    { key: "tname", label: "姓名", minWidth: 110 },
    { key: "tposition", label: "职位", minWidth: 120 },
    { key: "ttel", label: "电话", minWidth: 130 },
    { key: "gender", label: "性别", width: 90 },
  ],
  students: [
    { key: "username", label: "账号", minWidth: 120 },
    { key: "numb", label: "学号", minWidth: 120 },
    { key: "sname", label: "姓名", minWidth: 110 },
    { key: "sdept", label: "学院", minWidth: 130 },
    { key: "smajor", label: "专业", minWidth: 130 },
    { key: "sclass", label: "班级", minWidth: 110 },
  ],
  courses: [
    { key: "name", label: "课程名称", minWidth: 150 },
    { key: "numb", label: "课程编号", minWidth: 120 },
    { key: "dept", label: "开课学院", minWidth: 130 },
    { key: "teacherName", label: "授课教师", minWidth: 120 },
    { key: "score", label: "学分", width: 110 },
    { key: "maxStudents", label: "容量", width: 100 },
    { key: "timeSlot", label: "上课时间", minWidth: 140 },
    { key: "jianjie", label: "课程简介", minWidth: 200 },
  ],
  selections: [
    { key: "courseName", label: "课程", minWidth: 150 },
    { key: "courseDept", label: "开课学院", minWidth: 130 },
    { key: "courseCredit", label: "学分", width: 100 },
    { key: "timeSlot", label: "上课时间", minWidth: 140 },
    { key: "studentName", label: "学生", minWidth: 110 },
    { key: "teacherName", label: "教师", minWidth: 110 },
    { key: "score", label: "成绩", width: 120 },
    { key: "createTime", label: "选课时间", minWidth: 170 },
  ],
  selectionWindows: [
    { key: "actionType", label: "类型", width: 120 },
    { key: "name", label: "窗口名称", minWidth: 150 },
    { key: "startTime", label: "开始时间", minWidth: 170 },
    { key: "endTime", label: "结束时间", minWidth: 170 },
    { key: "enabled", label: "启用状态", width: 110 },
    { key: "active", label: "当前状态", width: 110 },
    { key: "description", label: "说明", minWidth: 180 },
  ],
};

const ENDPOINTS = {
  admins: "/api/admins",
  auditLogs: "/api/admin-audit-logs",
  notifications: "/api/notifications",
  teachers: "/api/teachers",
  students: "/api/students",
  courses: "/api/courses",
  selections: "/api/selections",
  selectionWindows: "/api/selection-windows",
};

const ROLE_LABELS = {
  admin: "管理员工作台",
  teacher: "教师工作台",
  student: "学生工作台",
};

const SUMMARY_HINTS = {
  admins: "教务后台可见的管理员账号数量",
  auditLogs: "管理员关键操作的审计记录总数",
  notifications: "当前账号可见的通知记录数量",
  teachers: "当前角色可见的教师数量",
  students: "当前角色可见的学生数量",
  courses: "当前课程池中的课程总量",
  selections: "当前角色可见的选课记录总数",
  selectionWindows: "当前系统配置的选退课时间窗口数量",
};

const SEMESTER_LABEL = "2026 春季学期";
const PAGE_SIZE_OPTIONS = [10, 20, 50];
const VIEW_STORAGE_KEY = "scs-current-view";
const AUTH_RULES = {
  usernamePattern: /^[A-Za-z][A-Za-z0-9_]{3,19}$/,
  passwordPattern: /^[A-Za-z0-9_@#$%]{6,20}$/,
  usernameHint: "4-20位，以字母开头，仅支持字母、数字、下划线",
  passwordHint: "6-20位，仅支持字母、数字、下划线和 @ # $ %",
};

const { createApp, ref, reactive, computed, onMounted } = Vue;
const { ElMessage, ElMessageBox } = ElementPlus;

const http = axios.create({
  withCredentials: true,
  headers: { "Content-Type": "application/json" },
});

function getViewsForRole(role) {
  if (role === "admin") {
    return [
      VIEW_META.profile,
      VIEW_META.admins,
      VIEW_META.auditLogs,
      VIEW_META.notifications,
      VIEW_META.teachers,
      VIEW_META.students,
      VIEW_META.courses,
      VIEW_META.selections,
      VIEW_META.selectionWindows,
    ];
  }
  if (role === "teacher") {
    return [
      VIEW_META.profile,
      VIEW_META.notifications,
      VIEW_META.students,
      VIEW_META.courses,
      VIEW_META.selections,
    ];
  }
  if (role === "student") {
    return [
      VIEW_META.profile,
      VIEW_META.gradeReport,
      VIEW_META.notifications,
      VIEW_META.teachers,
      VIEW_META.courses,
      VIEW_META.selections,
    ];
  }
  return [];
}

function readStoredView() {
  try {
    return window.sessionStorage.getItem(VIEW_STORAGE_KEY);
  } catch (error) {
    return null;
  }
}

function writeStoredView(view) {
  try {
    window.sessionStorage.setItem(VIEW_STORAGE_KEY, view);
  } catch (error) {
    // Ignore storage errors in constrained browsers.
  }
}

function clearStoredView() {
  try {
    window.sessionStorage.removeItem(VIEW_STORAGE_KEY);
  } catch (error) {
    // Ignore storage errors in constrained browsers.
  }
}

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

function hasValue(value) {
  if (value === null || value === undefined) {
    return false;
  }
  if (typeof value === "string") {
    return value.trim().length > 0;
  }
  return true;
}

function formatDateInput(value) {
  if (!value) {
    return "";
  }
  return String(value).slice(0, 10);
}

function formatDateTimeLocal(value) {
  if (!value) {
    return "";
  }
  return String(value).slice(0, 16);
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

function normalizeOptionalNumber(value) {
  if (value === null || value === undefined || value === "") {
    return null;
  }
  const numeric = Number(value);
  return Number.isFinite(numeric) ? numeric : null;
}

function validateLoginForm(form) {
  if (!form.username || !form.username.trim()) {
    return "用户名不能为空";
  }
  if (!form.password || !form.password.trim()) {
    return "密码不能为空";
  }
  return "";
}

function validateRegisterForm(form) {
  if (!form.username || !form.username.trim()) {
    return "用户名不能为空";
  }
  if (!AUTH_RULES.usernamePattern.test(form.username.trim())) {
    return AUTH_RULES.usernameHint;
  }
  if (!form.password || !form.password.trim()) {
    return "密码不能为空";
  }
  if (!AUTH_RULES.passwordPattern.test(form.password)) {
    return AUTH_RULES.passwordHint;
  }
  return "";
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
    email: "",
    smajor: "",
    sclass: "",
  };
}

function createEmptyPageState() {
  return {
    items: [],
    total: 0,
    page: 1,
    pageSize: 10,
    totalPages: 0,
  };
}

function createEmptyInsights() {
  return {
    role: "",
    roleLabel: "",
    displayName: "",
    admins: 0,
    auditLogs: 0,
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

function createEmptyGradeReport() {
  return {
    gpa: 0,
    averageScore: 0,
    earnedCredits: 0,
    totalCredits: 0,
    gradedCourses: 0,
    pendingCourses: 0,
    page: createEmptyPageState(),
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
      email: "",
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
      dept: "",
      maxStudents: 0,
      timeSlot: "",
    };
  }
  if (view === "selectionWindows") {
    return {
      actionType: "SELECT",
      name: "",
      startTime: "",
      endTime: "",
      enabled: true,
      description: "",
    };
  }
  return {
    courseId: "",
    studentId: "",
    score: null,
  };
}

const AppComponent = {
  setup() {
    const booting = ref(true);
    const pending = ref(false);
    const authMode = ref("login");
    const currentView = ref("profile");
    const searchText = ref("");
    const session = ref(null);
    const loginForm = reactive({ username: "", password: "", role: "admin" });
    const registerForm = reactive({ username: "", password: "", role: "student" });
    const profileForm = reactive(createEmptyProfile());
    const summary = reactive({
      admins: 0,
      auditLogs: 0,
      notifications: 0,
      teachers: 0,
      students: 0,
      courses: 0,
      selections: 0,
      selectionWindows: 0,
    });
    const insights = reactive(createEmptyInsights());
    const gradeReport = reactive(createEmptyGradeReport());
    const records = reactive({
      admins: [],
      auditLogs: [],
      notifications: [],
      teachers: [],
      students: [],
      courses: [],
      selections: [],
      selectionWindows: [],
    });
    const pages = reactive({
      admins: createEmptyPageState(),
      auditLogs: createEmptyPageState(),
      notifications: createEmptyPageState(),
      teachers: createEmptyPageState(),
      students: createEmptyPageState(),
      courses: createEmptyPageState(),
      selections: createEmptyPageState(),
      selectionWindows: createEmptyPageState(),
      gradeReport: createEmptyPageState(),
    });
    const references = reactive({
      teachers: [],
      students: [],
      courses: [],
    });
    const courseFilters = reactive({
      dept: "",
      teacherId: "",
      minScore: "",
      maxScore: "",
      onlyAvailable: false,
    });
    const modal = reactive({
      visible: false,
      mode: "create",
      form: {},
      feedback: {
        type: "info",
        text: "",
      },
    });

    const roleLabel = computed(() => ROLE_LABELS[session.value?.role] || "校园工作台");

    const availableViews = computed(() => getViewsForRole(session.value?.role));

    const currentMeta = computed(() => VIEW_META[currentView.value] || VIEW_META.profile);
    const currentColumns = computed(() => TABLE_COLUMNS[currentView.value] || []);
    const currentRows = computed(() => records[currentView.value] || []);
    const currentPager = computed(() => pages[currentView.value] || createEmptyPageState());
    const gradeReportRows = computed(() => gradeReport.page.items || []);
    const gradeReportPager = computed(() => gradeReport.page || createEmptyPageState());
    const showSearchBar = computed(
      () => Boolean(ENDPOINTS[currentView.value]) || currentView.value === "gradeReport"
    );
    const canCreateCurrent = computed(() => canCreate(currentView.value));
    const canEditRow = computed(() => canEdit(currentView.value));
    const canDeleteRow = computed(() => canDelete(currentView.value));
    const showRowActions = computed(() => canEditRow.value || canDeleteRow.value);
    const courseDepartments = computed(() =>
      [...new Set(references.courses.map((item) => safeText(item.dept, "")).filter(Boolean))].sort()
    );
    const selectedModalCourse = computed(() => {
      if (currentView.value !== "selections" || !modal.form?.courseId) {
        return null;
      }
      return references.courses.find((course) => course.id === modal.form.courseId) || null;
    });
    const selectedCourseStatus = computed(() => {
      const course = selectedModalCourse.value;
      if (!course) {
        return null;
      }
      const maxStudents = Number(course.maxStudents || 0);
      const selectedCount = Number(course.selectedCount || 0);
      const alreadySelectedByCurrentStudent =
        (records.selections || []).some((item) => item.courseId === course.id);

      if (course.id === "C9001") {
        return {
          type: alreadySelectedByCurrentStudent ? "error" : "success",
          text: alreadySelectedByCurrentStudent
            ? "当前账号已经选过这门课；请重新导入 SQL 重置数据后再截图。"
            : "当前账号未选过这门课，这是第一次选择。第一轮植入 duplicated == null 后，点击保存若提示“该学生已选过这门课”，就是本轮缺陷现场。",
        };
      }

      const trialNote = course.id === "C9002" ? "第二轮测试课：用于容量边界截图。" : "";

      if (maxStudents <= 0) {
        return {
          type: "info",
          text: `当前课程不限制容量，已选 ${selectedCount} 人。${trialNote}`,
        };
      }

      const remaining = maxStudents - selectedCount;
      if (remaining <= 0) {
        return {
          type: "error",
          text: `当前已选 ${selectedCount}/${maxStudents} 人，课程已满；正确代码下点击保存会被拦截。${trialNote}`,
        };
      }

      return {
        type: "success",
        text: `当前已选 ${selectedCount}/${maxStudents} 人，剩余 ${remaining} 个名额。${trialNote}`,
      };
    });

    const summaryChips = computed(() => [
      { label: "课程", value: summary.courses ?? 0, icon: "Notebook" },
      { label: "选课", value: summary.selections ?? 0, icon: "Tickets" },
      { label: "待录成绩", value: insights.pendingGrades ?? 0, icon: "EditPen" },
    ]);

    const gradeOverviewCards = computed(() => [
      { label: "GPA", value: formatNumber(gradeReport.gpa), hint: "已出成绩课程的加权绩点", icon: "TrophyBase", accent: "violet" },
      { label: "平均成绩", value: formatNumber(gradeReport.averageScore), hint: "已出成绩课程的平均分", icon: "DataLine", accent: "blue" },
      { label: "已获学分", value: formatNumber(gradeReport.earnedCredits), hint: "成绩达到及格线的累计学分", icon: "Medal", accent: "green" },
      { label: "总学分", value: formatNumber(gradeReport.totalCredits), hint: "已选课程累计学分", icon: "Collection", accent: "amber" },
    ]);

    const moduleActionLabel = computed(() => {
      if (currentView.value === "admins") return "新增管理员";
      if (currentView.value === "teachers") return "新增教师";
      if (currentView.value === "students") return "新增学生";
      if (currentView.value === "courses") return "新增课程";
      if (currentView.value === "selectionWindows") return "新增时间窗口";
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

    const profileSummaryItems = computed(() => {
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
          { label: "电话", value: safeText(profileForm.ttel) },
          { label: "职位", value: safeText(profileForm.tposition) },
        ];
      }
      return [
        { label: "学号", value: safeText(profileForm.numb) },
        { label: "姓名", value: safeText(profileForm.sname) },
        { label: "学院", value: safeText(profileForm.sdept) },
        { label: "班级", value: safeText(profileForm.sclass) },
      ];
    });

    const modalTitle = computed(() => {
      const verb = modal.mode === "create" ? "新增" : "编辑";
      return `${verb}${currentMeta.value.label}`;
    });

    const userInitial = computed(() => {
      const name = session.value?.displayName || roleLabel.value || "U";
      return String(name).slice(0, 1).toUpperCase();
    });

    function setMessage(type, text) {
      if (!text) {
        return;
      }
      const normalized = type === "error" ? "error" : type === "success" ? "success" : type === "warning" ? "warning" : "info";
      ElMessage({ type: normalized, message: text, showClose: true, duration: 3200 });
    }

    function setModalFeedback(type, text) {
      modal.feedback.type = type;
      modal.feedback.text = text;
    }

    function clearModalFeedback() {
      setModalFeedback("info", "");
    }

    function iconFor(viewKey) {
      return VIEW_ICONS[viewKey] || "Menu";
    }

    function getCookie(name) {
      const match = document.cookie.match(new RegExp("(^|;\\s*)" + name + "=([^;]+)"));
      return match ? decodeURIComponent(match[2]) : null;
    }

    async function api(url, options = {}) {
      const csrfToken = getCookie("XSRF-TOKEN");
      const method = (options.method || "GET").toUpperCase();
      let data;
      if (options.body !== undefined) {
        data = typeof options.body === "string" ? JSON.parse(options.body) : options.body;
      } else if (options.data !== undefined) {
        data = options.data;
      }

      try {
        const response = await http.request({
          url,
          method,
          data,
          headers: {
            ...(csrfToken ? { "X-XSRF-TOKEN": csrfToken } : {}),
            ...(options.headers || {}),
          },
        });
        const payload = response.data;
        if (payload && payload.success === false) {
          throw new Error(payload.message || "请求失败");
        }
        return payload ? payload.data : null;
      } catch (error) {
        if (error.response) {
          if (error.response.status === 401) {
            session.value = null;
          }
          const payload = error.response.data;
          throw new Error(payload?.message || `请求失败(${error.response.status})`);
        }
        throw error;
      }
    }

    function canCreate(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
      }
      if (session.value.role === "teacher") {
        return view === "courses";
      }
      return view === "selections";
    }

    function canEdit(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
      }
      if (session.value.role === "teacher") {
        return view === "courses" || view === "selections";
      }
      return false;
    }

    function canDelete(view) {
      if (!session.value) return false;
      if (session.value.role === "admin") {
        return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
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
      if (view === "selectionWindows") {
        form.startTime = formatDateTimeLocal(form.startTime);
        form.endTime = formatDateTimeLocal(form.endTime);
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
        payload.maxStudents = Number.isFinite(Number(payload.maxStudents)) ? Number(payload.maxStudents) : 0;
        payload.timeSlot = safeText(payload.timeSlot, "").trim();
        payload.dept = safeText(payload.dept, "").trim();
        if (session.value.role === "teacher") {
          delete payload.tid;
        }
        return payload;
      }
      if (view === "selectionWindows") {
        payload.startTime = toDateTimeValue(payload.startTime);
        payload.endTime = toDateTimeValue(payload.endTime);
        payload.enabled = Boolean(payload.enabled);
        return payload;
      }
      if (view === "selections") {
        const normalizedScore = normalizeOptionalNumber(payload.score);
        if (session.value.role === "student") {
          return { courseId: payload.courseId };
        }
        if (session.value.role === "teacher" && mode === "edit") {
          return { score: normalizedScore };
        }
        return {
          courseId: payload.courseId,
          studentId: payload.studentId,
          score: normalizedScore,
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
      if (["createTime", "sentTime", "startTime", "endTime"].includes(key)) {
        return formatDateTime(value);
      }
      if (key === "action") {
        return value;
      }
      if (key === "channel") {
        if (value === "SYSTEM") return "站内";
        if (value === "EMAIL") return "邮件";
        if (value === "SMS") return "短信";
        return value;
      }
      if (key === "status") {
        if (value === "SENT") return "已发送";
        if (value === "SKIPPED") return "已跳过";
        if (value === "FAILED") return "发送失败";
        return value;
      }
      if (key === "actionType") {
        return value === "DROP" ? "退课窗口" : "选课窗口";
      }
      if (key === "enabled") {
        return value ? "已启用" : "已停用";
      }
      if (key === "active") {
        return value ? "当前生效" : "未生效";
      }
      if (key === "score") {
        if (currentView.value === "courses") {
          return `${formatNumber(value)} 学分`;
        }
        if (row.graded !== true) {
          return "待录入";
        }
        return `${formatNumber(value)} 分`;
      }
      if (key === "courseCredit") {
        return `${formatNumber(value)} 学分`;
      }
      if (key === "maxStudents") {
        return Number(value) > 0 ? `${formatNumber(value)} 人` : "不限";
      }
      return value;
    }

    function tagType(key, row) {
      const cls = cellClass(key, row[key], row);
      if (!cls) {
        return false;
      }
      if (cls.includes("is-success")) return "success";
      if (cls.includes("is-warn")) return "warning";
      return "info";
    }

    function cellClass(key, value, row = null) {
      if (key === "score") {
        if (currentView.value === "courses") {
          return "cell-badge is-neutral";
        }
        return row?.graded !== true
          ? "cell-badge is-warn"
          : "cell-badge is-success";
      }
      if (key === "teacherName" && !value) {
        return "cell-badge is-neutral";
      }
      if (key === "action") {
        if (value === "新增") return "cell-badge is-success";
        if (value === "删除") return "cell-badge is-warn";
        return "cell-badge is-neutral";
      }
      if (key === "targetType") {
        return "cell-badge is-neutral";
      }
      if (key === "channel") {
        return "cell-badge is-neutral";
      }
      if (key === "status") {
        if (value === "SENT") return "cell-badge is-success";
        if (value === "FAILED") return "cell-badge is-warn";
        return "cell-badge is-neutral";
      }
      if (key === "enabled" || key === "active") {
        return value ? "cell-badge is-success" : "cell-badge is-neutral";
      }
      if (key === "actionType") {
        return "cell-badge is-neutral";
      }
      if (key === "maxStudents" && (!value || Number(value) <= 0)) {
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

    function assignPage(view, data) {
      const normalized = {
        items: data?.items || [],
        total: data?.total || 0,
        page: data?.page || 1,
        pageSize: data?.pageSize || 10,
        totalPages: data?.totalPages || 0,
      };
      records[view] = normalized.items;
      Object.assign(pages[view], normalized);
    }

    function buildQuery(params) {
      const query = new URLSearchParams();
      Object.entries(params).forEach(([key, value]) => {
        if (value === null || value === undefined || value === "") {
          return;
        }
        query.set(key, String(value));
      });
      const text = query.toString();
      return text ? `?${text}` : "";
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
        api("/api/teachers?page=1&pageSize=200").catch(() => createEmptyPageState()),
        api("/api/students?page=1&pageSize=200").catch(() => createEmptyPageState()),
        api("/api/courses?page=1&pageSize=200").catch(() => createEmptyPageState()),
      ]);
      references.teachers = teachers?.items || [];
      references.students = students?.items || [];
      references.courses = courses?.items || [];
    }

    function resolveCurrentView(role) {
      const storedView = readStoredView();
      const allowedViews = getViewsForRole(role);
      if (storedView && allowedViews.some((view) => view.key === storedView)) {
        return storedView;
      }
      return "profile";
    }

    async function hydrateWorkspace() {
      const jobs = [loadSummary(), loadInsights(), loadReferences(), loadProfile()];
      if (session.value?.role === "student") {
        jobs.push(loadGradeReport());
      }
      if (
        currentView.value !== "profile"
        && currentView.value !== "gradeReport"
        && ENDPOINTS[currentView.value]
      ) {
        jobs.push(loadModule(currentView.value));
      }
      await Promise.all(jobs);
    }

    async function loadGradeReport() {
      const query = buildQuery({
        keyword: searchText.value,
        page: pages.gradeReport.page,
        pageSize: pages.gradeReport.pageSize,
      });
      const data = await api(`/api/student-grade-report${query}`);
      const nextPage = {
        ...createEmptyPageState(),
        ...(data?.page || {}),
      };
      Object.assign(gradeReport, createEmptyGradeReport(), data || {});
      gradeReport.page = nextPage;
      Object.assign(pages.gradeReport, nextPage);
    }

    async function loadModule(view) {
      if (!ENDPOINTS[view]) {
        return;
      }
      const params = {
        keyword: searchText.value,
        page: pages[view].page,
        pageSize: pages[view].pageSize,
      };
      if (view === "courses") {
        params.dept = courseFilters.dept;
        params.teacherId = courseFilters.teacherId;
        params.minScore = courseFilters.minScore;
        params.maxScore = courseFilters.maxScore;
        params.onlyAvailable = courseFilters.onlyAvailable || "";
      }
      assignPage(view, await api(`${ENDPOINTS[view]}${buildQuery(params)}`));
    }

    async function refreshCurrent() {
      try {
        pending.value = true;
        if (currentView.value === "profile") {
          await loadProfile();
        } else if (currentView.value === "gradeReport") {
          await Promise.all([loadGradeReport(), loadSummary()]);
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
        currentView.value = resolveCurrentView(session.value.role);
        writeStoredView(currentView.value);
        booting.value = false;
        pending.value = true;
        await hydrateWorkspace();
      } catch (error) {
        session.value = null;
      } finally {
        booting.value = false;
        pending.value = false;
      }
    }

    async function login() {
      const validationMessage = validateLoginForm(loginForm);
      if (validationMessage) {
        setMessage("error", validationMessage);
        return;
      }
      let loginAccepted = false;
      try {
        pending.value = true;
        session.value = await api("/api/auth/login", {
          method: "POST",
          body: JSON.stringify(loginForm),
        });
        loginAccepted = true;
        session.value = await api("/api/auth/me");
        currentView.value = resolveCurrentView(session.value.role);
        writeStoredView(currentView.value);
        await hydrateWorkspace();
        setMessage("success", "登录成功，已进入工作台");
      } catch (error) {
        if (loginAccepted && error.message === "请先登录") {
          setMessage("error", "登录成功，但浏览器没有保存会话，请允许 localhost 的 Cookie 后重试");
        } else {
          setMessage("error", error.message);
        }
      } finally {
        pending.value = false;
      }
    }

    async function register() {
      registerForm.role = "student";
      const validationMessage = validateRegisterForm(registerForm);
      if (validationMessage) {
        setMessage("error", validationMessage);
        return;
      }
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
        currentView.value = "profile";
        clearStoredView();
        pending.value = false;
      }
    }

    function changeView(view) {
      currentView.value = view;
      writeStoredView(view);
      searchText.value = "";
      if (pages[view]) {
        pages[view].page = 1;
      }
      refreshCurrent();
    }

    function openCreate() {
      modal.mode = "create";
      modal.form = createEmptyModal(currentView.value);
      clearModalFeedback();
      if (currentView.value === "courses" && session.value?.role === "teacher") {
        modal.form.tid = session.value.id;
      }
      modal.visible = true;
    }

    function openEdit(row) {
      modal.mode = "edit";
      modal.form = prepareModalForm(currentView.value, row);
      clearModalFeedback();
      modal.visible = true;
    }

    function closeModal() {
      modal.visible = false;
      modal.form = {};
      clearModalFeedback();
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
        const jobs = [loadModule(view), loadSummary(), loadInsights(), loadReferences()];
        if (session.value?.role === "student") {
          jobs.push(loadGradeReport());
        }
        await Promise.all(jobs);
        if (view === "selections") {
          setModalFeedback("success", session.value?.role === "student" ? "选课提交成功，系统已生成选课记录。" : "选课记录保存成功。");
        } else {
          closeModal();
        }
        setMessage("success", "保存成功");
      } catch (error) {
        if (view === "selections") {
          setModalFeedback("error", error.message);
        }
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function removeRow(row) {
      try {
        await ElMessageBox.confirm(
          `确认${deleteActionLabel.value}当前记录吗？此操作不可撤销。`,
          `${deleteActionLabel.value}确认`,
          {
            type: "warning",
            confirmButtonText: deleteActionLabel.value,
            cancelButtonText: "取消",
          }
        );
      } catch (error) {
        return;
      }
      try {
        pending.value = true;
        await api(`${ENDPOINTS[currentView.value]}/${row.id}`, { method: "DELETE" });
        const jobs = [loadModule(currentView.value), loadSummary(), loadInsights(), loadReferences()];
        if (session.value?.role === "student") {
          jobs.push(loadGradeReport());
        }
        await Promise.all(jobs);
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
        const jobs = [loadSummary(), loadInsights(), loadReferences()];
        if (session.value?.role === "student") {
          jobs.push(loadGradeReport());
        }
        await Promise.all(jobs);
        setMessage("success", "个人资料已更新");
      } catch (error) {
        setMessage("error", error.message);
      } finally {
        pending.value = false;
      }
    }

    async function applyModuleFilters() {
      if (currentView.value !== "courses") {
        return;
      }
      pages.courses.page = 1;
      await refreshCurrent();
    }

    function resetCourseFilters() {
      courseFilters.dept = "";
      courseFilters.teacherId = "";
      courseFilters.minScore = "";
      courseFilters.maxScore = "";
      courseFilters.onlyAvailable = false;
      applyModuleFilters();
    }

    async function handlePageChange(page) {
      const pager = currentView.value === "gradeReport" ? pages.gradeReport : pages[currentView.value];
      if (!pager || page < 1 || page === pager.page) {
        return;
      }
      pager.page = page;
      await refreshCurrent();
    }

    async function handleSizeChange(size) {
      const nextSize = Number(size);
      const pager = currentView.value === "gradeReport" ? pages.gradeReport : pages[currentView.value];
      if (!pager || !nextSize || nextSize === pager.pageSize) {
        return;
      }
      pager.pageSize = nextSize;
      pager.page = 1;
      await refreshCurrent();
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
      gradeReport,
      references,
      courseFilters,
      modal,
      authRules: AUTH_RULES,
      semesterLabel: SEMESTER_LABEL,
      pageSizeOptions: PAGE_SIZE_OPTIONS,
      roleLabel,
      availableViews,
      currentMeta,
      currentColumns,
      currentRows,
      currentPager,
      gradeReportRows,
      gradeReportPager,
      courseDepartments,
      selectedCourseStatus,
      showSearchBar,
      canCreateCurrent,
      canEditRow,
      canDeleteRow,
      showRowActions,
      summaryChips,
      gradeOverviewCards,
      moduleActionLabel,
      editActionLabel,
      deleteActionLabel,
      profileSummaryItems,
      modalTitle,
      userInitial,
      iconFor,
      renderCell,
      cellClass,
      tagType,
      formatDateTime,
      viewMetric,
      login,
      register,
      logout,
      changeView,
      refreshCurrent,
      applyModuleFilters,
      resetCourseFilters,
      handlePageChange,
      handleSizeChange,
      openCreate,
      openEdit,
      closeModal,
      clearModalFeedback,
      saveModal,
      removeRow,
      saveProfile,
    };
  },
};

const app = createApp(AppComponent);
app.use(ElementPlus, { locale: ElementPlusLocaleZhCn });
for (const [name, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(name, component);
}
app.mount("#app");
