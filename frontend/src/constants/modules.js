// 各业务模块的元信息、表格列、接口地址与角色可见性配置。

export const VIEW_META = {
  dashboard: {
    key: "dashboard",
    label: "工作台",
    caption: "总览首页",
    description: "查看当前学期课程、人员、成绩与选课运行概况。",
    icon: "Histogram",
    route: "/dashboard",
  },
  assistant: {
    key: "assistant",
    label: "AI 助手",
    caption: "智能助理",
    description: "基于你的选课数据与课程目录，提供智能选课问答、课表推荐与课程大纲生成。",
    icon: "MagicStick",
    route: "/assistant",
  },
  profile: {
    key: "profile",
    label: "个人资料",
    caption: "个人中心",
    description: "查看并修改当前账号信息。",
    icon: "User",
    route: "/profile",
  },
  gradeReport: {
    key: "gradeReport",
    label: "我的成绩",
    caption: "成绩与绩点",
    description: "查看本人已选课程成绩、通过学分与 GPA 汇总。",
    icon: "TrophyBase",
    route: "/grades",
    searchPlaceholder: "按课程名称、教师或上课时间搜索成绩",
  },
  admins: {
    key: "admins",
    label: "管理员管理",
    caption: "后台账号",
    description: "维护教务与系统管理员账号信息。",
    icon: "Avatar",
    route: "/admins",
    searchPlaceholder: "按账号、姓名或电话查询管理员",
  },
  teachers: {
    key: "teachers",
    label: "教师档案",
    caption: "教师信息",
    description: "查看和维护教师资料、教工号及岗位信息。",
    icon: "Reading",
    route: "/teachers",
    searchPlaceholder: "按账号、教工号、姓名或职位查询教师",
  },
  students: {
    key: "students",
    label: "学生档案",
    caption: "学生信息",
    description: "管理学生学号、学院、专业和班级等基础信息。",
    icon: "UserFilled",
    route: "/students",
    searchPlaceholder: "按账号、学号、姓名、学院、专业或班级查询学生",
  },
  courses: {
    key: "courses",
    label: "课程中心",
    caption: "课程信息",
    description: "浏览课程编号、学分、开课学院、容量、上课时间与授课教师安排。",
    icon: "Notebook",
    route: "/courses",
    searchPlaceholder: "按课程名称、编号或教师搜索课程",
  },
  selections: {
    key: "selections",
    label: "选课记录",
    caption: "选课管理",
    description: "跟踪选课、退选和成绩录入状态。",
    icon: "Tickets",
    route: "/selections",
    searchPlaceholder: "按课程、学生或教师关键字搜索选课记录",
  },
  selectionWindows: {
    key: "selectionWindows",
    label: "选课窗口",
    caption: "开放时间",
    description: "管理学生选课、退课操作的开放时间范围。",
    icon: "Clock",
    route: "/selection-windows",
    searchPlaceholder: "按窗口名称、类型或说明搜索",
  },
  evaluations: {
    key: "evaluations",
    label: "课程评价",
    caption: "评教中心",
    description: "结课后对所修课程进行星级评分与评价，教师与管理员可查看评教结果与统计。",
    icon: "Star",
    route: "/evaluations",
    searchPlaceholder: "按课程名称、评价内容或教师搜索",
  },
};

export const ENDPOINTS = {
  admins: "/api/admins",
  teachers: "/api/teachers",
  students: "/api/students",
  courses: "/api/courses",
  selections: "/api/selections",
  selectionWindows: "/api/selection-windows",
  evaluations: "/api/evaluations",
};

export const ROLE_LABELS = {
  admin: "管理员工作台",
  teacher: "教师工作台",
  student: "学生工作台",
};

export const SEMESTER_LABEL = "2026 春季学期";
export const PAGE_SIZE_OPTIONS = [10, 20, 50];

export const AUTH_RULES = {
  usernamePattern: /^[A-Za-z][A-Za-z0-9_]{3,19}$/,
  passwordPattern: /^[A-Za-z0-9_@#$%]{6,20}$/,
  usernameHint: "4-20位，以字母开头，仅支持字母、数字、下划线",
  passwordHint: "6-20位，仅支持字母、数字、下划线和 @ # $ %",
};

export const TABLE_COLUMNS = {
  admins: [
    { key: "username", label: "账号", minWidth: 140 },
    { key: "name", label: "姓名", minWidth: 120 },
    { key: "tele", label: "电话", minWidth: 140 },
  ],
};

// 不同角色在侧边栏可见的模块顺序。
export function getViewsForRole(role) {
  if (role === "admin") {
    return [
      "dashboard",
      "profile",
      "admins",
      "teachers",
      "students",
      "courses",
      "selections",
      "selectionWindows",
      "evaluations",
    ].map((key) => VIEW_META[key]);
  }
  if (role === "teacher") {
    return ["dashboard", "assistant", "profile", "courses", "selections", "evaluations"].map(
      (key) => VIEW_META[key]
    );
  }
  if (role === "student") {
    return [
      "dashboard",
      "assistant",
      "profile",
      "gradeReport",
      "courses",
      "selections",
      "evaluations",
    ].map((key) => VIEW_META[key]);
  }
  return [];
}

export function canCreate(role, view) {
  if (!role) return false;
  if (role === "admin") {
    return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
  }
  if (role === "teacher") {
    return view === "courses";
  }
  return view === "selections";
}

export function canEdit(role, view) {
  if (!role) return false;
  if (role === "admin") {
    return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
  }
  if (role === "teacher") {
    return view === "courses" || view === "selections";
  }
  return false;
}

export function canDelete(role, view) {
  if (!role) return false;
  if (role === "admin") {
    return ["admins", "teachers", "students", "courses", "selections", "selectionWindows"].includes(view);
  }
  if (role === "teacher") {
    return view === "courses";
  }
  return view === "selections";
}
