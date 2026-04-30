# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot 3 course registration system for universities with three user roles: Admin, Teacher, and Student. The system manages course enrollment, grade entry, and time-window controls for selection/drop operations.

**Tech Stack:**
- Java 17
- Spring Boot 3.3.5
- MyBatis-Plus 3.5.7 (ORM)
- MySQL 8.x
- Vue 3 (static resources in `src/main/resources/static/`)
- BCrypt password hashing (Spring Security Crypto)

## Build & Run Commands

```bash
# Build the project
mvn clean package

# Run the application
mvn spring-boot:run

# Run tests (when available)
mvn test

# Access the application
# http://localhost:8081
```

## Database Setup

**CRITICAL:** Database initialization is MANUAL. Spring Boot auto-initialization is disabled (`spring.sql.init.mode: never`).

1. Execute `app_stu_select-Mysql.sql` in MySQL client first
2. Then start the application

**Default credentials:**
```yaml
DB_URL: jdbc:mysql://localhost:3306/app_stu_select?serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true&useSSL=false
DB_USERNAME: app_stu_select
DB_PASSWORD: app_stu_select
```

Override via environment variables if needed.

**Demo accounts (password: `123456`):**
- Admin: `admin`
- Audit Admin: `audit_admin`

## Architecture

### Package Structure

```
com.codeying.stuselect/
├── common/          # Shared utilities and cross-cutting concerns
├── config/          # Spring configuration beans
├── controller/      # REST API endpoints
├── dto/             # Data transfer objects
├── mapper/          # MyBatis-Plus data access layer
├── model/           # Domain entities
└── service/         # Business logic layer
```

### Key Architectural Patterns

**1. Session-Based Authentication**
- `SessionService` manages `UserSession` objects stored in HTTP session
- `requireUser()` enforces authentication
- `requireRole(Role...)` enforces role-based authorization
- No JWT/token-based auth; relies on servlet session (12h timeout)

**2. Unified API Response Envelope**
- All controllers return `ApiResponse<T>`:
  ```java
  ApiResponse.success(data)           // { success: true, message: "ok", data: {...} }
  ApiResponse.successMessage(msg)     // { success: true, message: "...", data: null }
  ApiResponse.fail(msg)               // { success: false, message: "...", data: null }
  ```
- `GlobalExceptionHandler` catches `AppException` and returns consistent error responses

**3. Time-Window Control**
- `SelectionWindowService.requireOpen(actionType, user)` enforces time-based access control
- Students can only select/drop courses during configured windows (SELECT/DROP)
- Admins/Teachers bypass time restrictions

**4. Intelligent Course Selection Validation (8-Layer Validation System) 🆕**

The system implements a sophisticated **8-layer validation pipeline** for course selection, executed in the following order:

**Layer 1-3: Basic Constraints (Existing)**
1. **Duplicate Enrollment Check** - Prevents selecting the same course twice
2. **Capacity Limit Check** - Ensures course enrollment doesn't exceed `maxStudents`
3. **Time Conflict Check** - Detects overlapping time slots with already-selected courses

**Layer 4-8: Advanced Intelligent Validation (New) 🆕**
4. **Grade Level Authorization** - Restricts lower-grade students from selecting upper-grade courses
   - Enforced via `Course.gradeLimit` and `Student.grade`
   - Example: Freshman cannot select senior-level courses

5. **Prerequisite Course Validation** - Enforces course dependency chains
   - Defined in `tb_course_prerequisite` table
   - Supports multi-level chains (A→B→C)
   - Requires minimum score threshold (e.g., 70+ for advanced courses)
   - Example: "Microservices Architecture" requires "Distributed Systems" (65+) which requires "Database Principles" (60+)

6. **Mutex Course Validation** - Prevents selecting conflicting courses
   - Defined in `tb_course_mutex` table (bidirectional relationship)
   - Example: "Java Web Development" and "Python Web Development" are mutually exclusive

7. **Course Type Limit Validation** - Caps number of courses per type
   - Defined in `tb_course_type_limit` table
   - Example: Maximum 3 general elective courses per semester

8. **Credit Limit Validation (GPA-Based Dynamic Adjustment)** - Limits total credits based on academic performance
   - Defined in `tb_semester_credit_limit` table
   - GPA-based tiers:
     - GPA < 2.0: Max 15 credits (academic warning)
     - 2.0 ≤ GPA < 3.0: Max 20 credits (normal)
     - 3.0 ≤ GPA < 3.5: Max 25 credits (excellent)
     - GPA ≥ 3.5: Max 30 credits (honors privilege)

**Implementation Details:**
- All validations execute within a single transaction with pessimistic locking
- `CourseValidationService` encapsulates layers 4-8
- `SelectionService.validateSelectionConstraints()` orchestrates all 8 layers
- Each validation failure provides user-friendly error messages with context

**Data Flow Example: Student Selects "Microservices Architecture"**
```
1. Check duplicate enrollment ✓
2. Check capacity (35/40) ✓
3. Check time conflict (Tuesday 5-6) ✓
4. Check grade level (Student: Grade 4, Course: Grade 4+) ✓
5. Check prerequisites:
   → Has "Distributed Systems"? ✓ (Score: 70 ≥ 65)
     → Has "Database Principles"? ✓ (Score: 85 ≥ 60)
       → Has "Data Structures"? ✓ (Score: 90 ≥ 60)
6. Check mutex courses (none) ✓
7. Check course type limit (Professional Elective: 3/5) ✓
8. Check credit limit (GPA 3.2 → Max 25, Current: 18, Adding: 3 → 21 ≤ 25) ✓
→ Selection approved
```

**5. Transactional Constraints in Selection (Enhanced) 🆕**
- `SelectionService.validateSelectionConstraints()` now enforces 8-layer validation
- Uses pessimistic locking: `studentService.lockForSelection()` and `courseService.requireForUpdate()`
- GPA calculation integrated into validation pipeline for credit limit checks

**6. Notification System**
- `NotificationService` creates `NotificationRecord` entries for:
  - Course selection/drop
  - Grade publication
- Records are stored but not actively pushed (no email/SMS integration)

**7. Audit Logging**
- `AdminAuditLogService` records admin CRUD operations
- Captures: admin ID, object type, object ID, action, description, timestamp

**8. GPA Calculation**
- Formula: `GPA = (score - 50) / 10` for scores ≥ 60, else 0
- Weighted by course credits
- Computed in `SelectionService.gradeReport()` and `SelectionService.calculateStudentGPA()`

### Database Schema Extensions 🆕

**New Tables for Intelligent Validation:**

1. **tb_course_prerequisite** - Course prerequisite relationships
   - Supports multi-level dependency chains
   - Configurable minimum score requirements
   - Indexed on `course_id` and `prerequisite_course_id`

2. **tb_course_mutex** - Mutually exclusive courses
   - Bidirectional relationship (A↔B)
   - Indexed on both `course_id_a` and `course_id_b`

3. **tb_course_type_limit** - Course type enrollment caps
   - Limits courses per type (e.g., max 3 general electives)
   - Unique constraint on `course_type`

4. **tb_semester_credit_limit** - GPA-based credit limits
   - Dynamic credit caps based on academic performance
   - Range-based lookup on `min_gpa` and `max_gpa`

**Extended Fields:**
- `tb_course`: Added `course_type` (VARCHAR 64), `grade_limit` (INT)
- `tb_student`: Added `grade` (INT), `enrollment_year` (INT)

### Data Flow Example: Student Selects Course (Complete 8-Layer Pipeline)

### Data Flow Example: Student Selects Course (Complete 8-Layer Pipeline)

1. `SelectionController.create()` receives request
2. `SessionService.requireRole(ADMIN, STUDENT)` checks auth
3. `SelectionWindowService.requireOpen("SELECT", user)` checks time window
4. `SelectionService.validateSelectionConstraints()`:
   - Locks student record (`SELECT ... FOR UPDATE`)
   - Locks course record (`SELECT ... FOR UPDATE`)
   - **Layer 1**: Checks duplicate enrollment
   - **Layer 2**: Checks capacity limit
   - **Layer 3**: Checks time slot conflicts
   - Calculates student GPA for credit validation
   - **Layer 4**: `CourseValidationService.validateGradeLimit()` - Grade authorization
   - **Layer 5**: `CourseValidationService.validatePrerequisites()` - Prerequisite chain validation
   - **Layer 6**: `CourseValidationService.validateMutexCourses()` - Mutex course check
   - **Layer 7**: `CourseValidationService.validateCourseTypeLimit()` - Type limit check
   - **Layer 8**: `CourseValidationService.validateCreditLimit()` - GPA-based credit cap
5. `SelectionMapper.insert()` creates record
6. `NotificationService.notifySelectionCreated()` logs notification
7. Returns joined `SelectionRecord` with course/student/teacher names

### Role-Based Data Visibility

- **Admin**: Full access to all records
- **Teacher**: Only sees courses they teach and related students
- **Student**: Only sees their own enrollments and grades

Enforced via:
- `SessionService.requireRole()` in controllers
- Filtered queries in mappers (e.g., `SelectionMapper.selectJoinedList(keyword, studentId, teacherId)`)

## Important Implementation Notes

**MyBatis-Plus Conventions:**
- Mappers extend `BaseMapper<T>` for CRUD operations
- Custom queries use `@Select` annotations with JOIN statements
- Entity fields map to snake_case columns via `map-underscore-to-camel-case: true`

**ID Generation:**
- `IdGenerator.newId()` generates 19-digit numeric IDs (timestamp-based)
- All entities use `String` IDs, not auto-increment

**Password Handling:**
- `PasswordService` uses BCrypt (strength 10)
- Passwords hashed on registration/update, never stored plaintext

**Pagination:**
- `PageResult<T>` wraps lists with total count
- `PageQuery.of(page, pageSize)` handles in-memory pagination (not DB-level)
- Default page size: 10

**Frontend Integration:**
- Vue 3 SPA served from `src/main/resources/static/`
- API calls to `/api/*` endpoints
- No separate frontend build process; Vue is pre-bundled

## Common Development Tasks

**Adding a new entity:**
1. Create model class in `model/` (with MyBatis-Plus annotations)
2. Create mapper interface in `mapper/` extending `BaseMapper<T>`
3. Add `@MapperScan` ensures auto-discovery (already configured)
4. Create service in `service/` for business logic
5. Create controller in `controller/` for REST endpoints
6. Update SQL schema in `app_stu_select-Mysql.sql`

**Adding role-based authorization:**
```java
sessionService.requireRole(session, Role.ADMIN, Role.TEACHER);
```

**Adding time-window checks:**
```java
selectionWindowService.requireOpen("SELECT", currentUser);
```

**Adding audit logging:**
```java
adminAuditLogService.log(session, "Course", courseId, "UPDATE", "修改课程信息");
```

**Configuring intelligent validation rules (via database):**
- Add prerequisite: Insert into `tb_course_prerequisite`
- Add mutex relationship: Insert into `tb_course_mutex` (bidirectional)
- Set type limit: Insert into `tb_course_type_limit`
- Configure credit tiers: Insert into `tb_semester_credit_limit`

## Technical Highlights 🌟

**1. Complex Business Logic**
- 8-layer nested validation pipeline
- Chain-based prerequisite validation (supports A→B→C→D)
- Bidirectional mutex relationship handling
- Dynamic rule engine (GPA-based credit adjustment)

**2. Performance Optimization**
- Pessimistic locking (`SELECT ... FOR UPDATE`) prevents race conditions
- Batch queries with JOIN for prerequisites and mutex courses
- Indexed foreign keys for fast lookups

**3. User Experience**
- Friendly error messages with context (e.g., "GPA 2.5 → Max 20 credits, current 18")
- Chain validation feedback (shows which prerequisite is missing)
- Real-time status display (credits selected, GPA tier)

## Health Check

Actuator endpoint available at `/actuator/health` (configured to show minimal details).
