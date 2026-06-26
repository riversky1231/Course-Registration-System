# Course Registration System

基于 Spring Boot 3、MyBatis-Plus 和 MySQL 的学生选课系统。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- MySQL
- Maven
- 前端：Vue 3 + Element Plus + axios（以本地静态资源方式集成，无需额外构建）

> 前端依赖（Vue、Element Plus、Element Plus Icons、中文语言包、axios）均已下载到
> `src/main/resources/static/assets/vendor/` 本地目录，配合后端 CSP（`script-src 'self'`）直接由应用提供，无需访问外网 CDN。

## 功能概览

- 管理员、教师、学生三种角色登录
- BCrypt 密码加密存储
- 课程管理
- 学生选课与退课
- 重复选课、容量、时间冲突校验
- 教师成绩录入权限限制
- 学生成绩 / GPA 查询
- 课程组合筛选
- 统一分页返回
- 选退课时间窗口控制
- AI 选课助手 / 智能推荐 / 课程大纲生成
- `/actuator/health` 健康检查
- 教师与学生信息管理
- 个人资料维护
- 仪表盘数据展示

## 运行要求

- JDK 17
- Maven 3.9+
- MySQL 8.x

## 快速启动

1. 创建数据库用户，或通过环境变量覆盖默认配置。
2. 默认数据库连接如下：

```yaml
DB_URL=jdbc:mysql://127.0.0.1:3308/app_stu_select?serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME=app_stu_select
DB_PASSWORD=app_stu_select
```

3. 直接启动项目：

```bash
mvn spring-boot:run
```

4. 打开浏览器访问：

```text
http://localhost:8081
```

## 数据库初始化

项目现在只保留一份数据库脚本：

- `app_stu_select-Mysql.sql`

团队协作时统一维护这一个文件，项目里不再拆分 `schema.sql` 和 `data.sql`。

启动步骤：

1. 先在 MySQL 客户端执行 `app_stu_select-Mysql.sql`
2. 再启动项目：

```bash
mvn spring-boot:run
```

说明：

- Spring Boot 已关闭自动 SQL 初始化
- 项目启动时不会再自动建表、改列、补索引或创建数据库
- 因此需要你先手工执行 `app_stu_select-Mysql.sql`
- 如果你没有建库权限，可以先手工创建 `app_stu_select` 数据库，再从脚本中的建表部分开始执行
- 脚本中的演示账号密码已按 BCrypt 哈希写入，登录明文仍然是 `123456`

示例管理员账号（共 5 个，密码均为 `123456`）：

- 用户名：`admin`（系统管理员）
- 用户名：`admin_jiaowu`（教务管理员）
- 其余：`admin_audit`、`admin_xuanke`、`admin_super`

演示数据规模：管理员 5、教师 30、学生 80。

## 目录结构

```text
src/main/java        后端 Java 代码
src/main/resources   配置、SQL、前端静态资源
app_stu_select-Mysql.sql  手工导库的一体化脚本
```
