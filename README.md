
# 🚀 Rev Workforce HRM 

**Rev Workforce HRM** is a **Java-based Human Resource Management Console Application** built using **Core Java, JDBC, MySQL**, and a clean **Layered (N-Tier) Architecture**.
It provides role-based access for **Admin, Manager, and Employee** to manage organizational operations efficiently.

---

## 📌 Key Features

### 👑 Admin

* Employee onboarding & management
* Department & designation management
* Manager assignment
* Audit log tracking

### 🧑‍💼 Manager

* View team members
* Approve / reject leave requests
* Submit performance reviews
* Receive system notifications

### 👨‍💻 Employee

* Secure login & password management
* Apply for leave
* Track leave balance
* View notifications
* View performance reviews

---

## 🏗️ Architecture Overview

The application follows a **Layered (N-Tier) Architecture** ensuring separation of concerns and scalability.

```
UI Layer → Service Layer → DAO Layer → Database
```

### Layer Responsibilities

* **UI Layer (`ui`)**
  Console-based menus and user interaction.
* **Service Layer (`service`)**
  Business logic, validations, and workflow handling.
* **DAO Layer (`dao`)**
  JDBC-based database operations.
* **Model Layer (`model`)**
  POJO/entity classes.
* **Utility Layer (`util`)**
  Common helpers (DB connection, password hashing, validation).
* **Exception Layer (`exception`)**
  Centralized custom exception handling.

---

## 📂 Project Structure

**Base Path**

```
D:\CodeLab\hrm-console-application\rev-workforce-hrm
```

```
rev-workforce-hrm
│
├── database
│   ├── hrm_setup.sql
│   ├── hrm_schema.sql
│   ├── hrm_seed.sql
│   ├── hrm_auth_update.sql
│   ├── hrm_performance_setup.sql
│   ├── hrm_patch.sql
│   └── temp*.sql
│
├── src/main/java/com/revature
│   ├── dao
│   │   ├── EmployeeDAO / EmployeeDAOImpl
│   │   ├── DepartmentDAO / DepartmentDAOImpl
│   │   ├── DesignationDAO / DesignationDAOImpl
│   │   ├── LeaveRequestDAO / LeaveRequestDAOImpl
│   │   ├── LeaveBalanceDAO / LeaveBalanceDAOImpl
│   │   ├── LeaveTypeDAO / LeaveTypeDAOImpl
│   │   ├── NotificationDAO / NotificationDAOImpl
│   │   ├── PerformanceReviewDAO / PerformanceReviewDAOImpl
│   │   └── GenericDAO
│   │
│   ├── model
│   │   ├── Employee
│   │   ├── Department
│   │   ├── Designation
│   │   ├── LeaveRequest
│   │   ├── LeaveBalance
│   │   ├── LeaveType
│   │   ├── Notification
│   │   └── PerformanceReview
│   │
│   ├── service
│   │   ├── AuthService
│   │   ├── EmployeeService
│   │   ├── LeaveService
│   │   ├── NotificationService
│   │   └── PerformanceService
│   │
│   ├── ui
│   │   ├── LoginUI
│   │   ├── AdminUI
│   │   ├── ManagerUI
│   │   ├── EmployeeUI
│   │   └── NotificationUI
│   │
│   ├── util
│   │   ├── DBConnection
│   │   ├── ConnectionFactory
│   │   ├── PasswordUtil
│   │   ├── ValidationUtil
│   │   ├── DateUtil
│   │   ├── SessionManager
│   │   └── Constants
│   │
│   ├── exception
│   │   └── AppException
│   │
│   └── Main.java
│
├── src/main/resources
│   └── database.properties
│
├── pom.xml
└── README.md
```

---

## 📊 Entity Relationship Diagram (ERD)

The system architecture is **centered around the `employees` table**, which acts as the core entity of the application.

### Core Relationships

* **employees → leave_balances** (1 : 1)
  Each employee has a dedicated leave balance record.
* **employees → leave_requests** (1 : N)
  Employees can raise multiple leave requests.
* **employees → performance_reviews** (1 : N)
  Managers submit periodic reviews for employees.
* **departments → employees** (1 : N)
  Each department contains multiple employees.
* **designations → employees** (1 : N)
  Employees are mapped to specific designations.
* **employees → notifications** (1 : N)
  System-generated notifications for actions and updates.

> 📌 This ERD ensures data normalization, referential integrity, and clear ownership of records.

---

## 🔄 System Workflows

### 1️⃣ Leave Application Workflow

```
Employee applies for leave
        ↓
Manager receives notification
        ↓
Manager approves / rejects request
        ↓
Leave balance updated
        ↓
Notification sent to employee
```

### 2️⃣ Performance Review Workflow

```
Manager sets goals
        ↓
Review period begins
        ↓
Manager submits rating & feedback
        ↓
Employee views performance review
```

---

## 🗄️ Database Details

### Database Name

```
rev_workforce_hrm
```

### Major Tables

* employees
* departments
* designations
* leave_requests
* leave_balances
* leave_types
* notifications
* performance_reviews
* audit_logs

---

## 🧩 SQL Execution Order (Important)

Run the SQL scripts **in the exact order** below:

1. `hrm_setup.sql`
2. `hrm_schema.sql`
3. `hrm_seed.sql`
4. `hrm_auth_update.sql`
5. `hrm_performance_setup.sql`
6. `hrm_patch.sql`

---

## ⚙️ Configuration

### `database.properties`

```properties
db.url=jdbc:mysql://localhost:3306/rev_workforce_hrm
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

---

## ▶️ How to Run the Application

1. Import the project into **IntelliJ IDEA / Eclipse**
2. Execute database SQL scripts
3. Update `database.properties`
4. Build the project using Maven
5. Run:

```
com.revature.Main
```

---

## 🔐 Authentication & Session Management

* Login handled via `AuthService`
* Passwords securely hashed using `PasswordUtil`
* Active user session maintained using `SessionManager`
* Role-based UI redirection:

  * **ADMIN → AdminUI**
  * **MANAGER → ManagerUI**
  * **EMPLOYEE → EmployeeUI**

---

## 🧪 Error Handling & Validation

* Centralized custom exception: `AppException`
* Input validations via `ValidationUtil`
* Graceful handling of DB & authentication failures

---

## 🛠️ Tech Stack

* **Language:** Java
* **Database:** MySQL
* **Architecture:** Layered (N-Tier)
* **Build Tool:** Maven
* **Connectivity:** JDBC
* **Security:** Password Hashing

---

## 🚀 Future Enhancements

* Graphical UI / Web Interface
* REST API using Spring Boot
* Email notifications
* Reporting & analytics module
* Role-based access logs

---

## 👨‍💻 Author

**Anmol Kumar**
Java Backend Developer | Cloud Enthusiast

---
