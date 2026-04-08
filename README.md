# Course Registration System

基于 Spring Boot 3、MyBatis-Plus 和 MySQL 的学生选课系统。

## 技术栈

- Java 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7
- MySQL
- Maven
- Vue 3（静态资源方式集成）

## 功能概览

- 管理员、教师、学生三种角色登录
- 课程管理
- 学生选课与退课
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
DB_URL=jdbc:mysql://localhost:3306/app_stu_select?serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false&createDatabaseIfNotExist=true
DB_USERNAME=app_stu_select
DB_PASSWORD=app_stu_select
```

3. 启动项目：

```bash
mvn spring-boot:run
```

4. 打开浏览器访问：

```text
http://localhost:8081
```

## 初始化数据

项目启动时会自动执行 `schema.sql` 和 `data.sql` 初始化表结构与演示数据。

示例管理员账号：

- 用户名：`admin01`
- 密码：`123456`

## 目录结构

```text
src/main/java        后端 Java 代码
src/main/resources   配置、SQL、前端静态资源
```
