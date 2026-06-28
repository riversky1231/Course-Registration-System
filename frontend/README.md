# 选课与课程资源管理系统 · 前端（前后端分离）

基于 **Vue 3 + Vite + Element Plus + Pinia + Vue Router + axios** 的选课与课程资源管理前端工程，与 Spring Boot 后端通过 `/api` 接口对接。

## 技术栈

| 能力 | 选型 |
| --- | --- |
| 框架 | Vue 3（`<script setup>`） |
| 构建 | Vite 6 |
| UI | Element Plus + Element Plus Icons |
| 状态 | Pinia |
| 路由 | Vue Router（含角色路由守卫） |
| 请求 | axios（统一封装 + CSRF + ApiResponse 解包） |

## 目录结构

```
frontend/
├── src/
│   ├── api/            # axios 实例与接口封装
│   ├── components/     # PageHeader、RecordDialog 等通用组件
│   ├── composables/    # useModule（列表/分页/增删改通用逻辑）
│   ├── constants/      # 模块元信息、角色权限、接口地址
│   ├── layouts/        # MainLayout 侧边栏+顶栏框架
│   ├── router/         # 路由与守卫
│   ├── stores/         # auth（会话）、reference（下拉引用缓存）
│   ├── styles/         # 全局主题
│   ├── utils/          # 格式化工具
│   └── views/          # 各业务页面（每类页面独立设计）
└── vite.config.js      # /api 代理到 http://localhost:8081
```

## 页面设计差异化

| 页面 | 设计风格 |
| --- | --- |
| 工作台 Dashboard | 欢迎横幅 + KPI 指标卡 + 学院分布条形图 + 选课时间线 |
| 课程中心 Courses | 渐变封面卡片画廊 + 多维筛选 + 容量进度条 |
| 选课记录 Selections | 学生卡片视图 / 教师·管理员表格（角色化） |
| 师生档案 People | 头像通讯录卡片 |
| 管理员 Admins | 清爽数据表格 |
| 选课窗口 Windows | 开放状态卡片 |
| 成绩 GradeReport | GPA 仪表盘 + 统计卡 + 成绩单 |
| 个人中心 Profile | 渐变资料卡 + 表单 |
| AI 助手 Assistant | 角色化标签页：选课问答 / 智能推荐 / 大纲生成 |

## 开发

```bash
# 1. 先启动后端（默认 http://localhost:8081）
# 2. 安装依赖并启动前端
npm install
npm run dev      # 默认 http://localhost:5173

# 生产构建
npm run build    # 产物输出到 dist/
npm run preview
```

> 开发期由 Vite 代理把 `/api` 请求转发到后端 8081 端口，浏览器视角下与前端同源，
> 因此 Session-Cookie 与 CSRF（XSRF-TOKEN）均可直接生效，无需额外 CORS 配置。

## 生产部署提示

`npm run build` 生成的 `dist/` 为纯静态资源，可由 Nginx 托管，并把 `/api` 反向代理到后端：

```nginx
location / { root /path/to/dist; try_files $uri $uri/ /index.html; }
location /api/ { proxy_pass http://127.0.0.1:8081; }
```
