# 🏦 Finance Bank — JDBC Action Plan
### ما تعرفه + ما ناقصك + إزاي تبدأ

---

## ✅ اللي عندك فعلاً (من الـ CS50 repo)

بص على الـ lectures اللي خلصتها — أنت **مش مبتدئ**:

| CS50 Lecture | اللي اتعلمته | الـ status في مشروعك |
|---|---|---|
| L0 Querying | SELECT, WHERE, ORDER BY, LIKE, Aggregates | ✅ تعرفه |
| L1 Relating | JOINs, Subqueries, Set Operations | ✅ تعرفه |
| L2 Designing | Schema Design, Foreign Keys, Constraints | ✅ تعرفه |
| L3 Writing | INSERT, UPDATE, DELETE, Transactions | ✅ تعرفه |
| L4 Viewing | Views, CTEs, Soft Delete | ✅ تعرفه |
| L5 Optimizing | Indexes, EXPLAIN, Locking | ✅ تعرفه |
| L6 Scaling | PostgreSQL, Stored Procedures, JDBC | ✅ تعرفه — بس في Java ناقص |

**الـ dont-panic-java** في repo بتاعك — ده proof إنك شفت JDBC من قبل.  
الفرق الوحيد: CS50 استخدم **SQLite**، إحنا هنستخدم **PostgreSQL**.

---

## ❌ اللي ناقصك فعلاً — حاجة واحدة بس

```
ACID Transactions في Java
```

يعني:
```java
connection.setAutoCommit(false);  // ← ده اللي CS50 ماعلمكش في Java
try {
    // UPDATE balance
    // INSERT transaction record
    connection.commit();           // ✅ الاتنين نجحوا
} catch (Exception e) {
    connection.rollback();         // ❌ حاجة فشلت — رجّع كل حاجة
}
```

CS50 علّمك `BEGIN / COMMIT` في SQL — بس في Java بيتكتب بالطريقة دي.

---

## 🎬 الـ Video اللي هتشوفه

**"JDBC Tutorial - Crash Course" — Marco Codes (Marco Behler)**

```
https://www.youtube.com/watch?v=7v2OnUti2eM
```

**ليه ده تحديداً؟**
- بيشرح DriverManager و DataSource و Connection Pool في نفس الـ video
- بيوضح PreparedStatement وليه Statement خطر (SQL Injection)
- بيشرح HikariCP بكود حقيقي
- مش طويل — Crash Course

**اللي هتلاقيه في الـ video مألوف ليك من CS50:**
- JDBC Drivers ← زي الـ SQLite driver اللي استخدمته في dont-panic
- PreparedStatement ← زي `?` parameters اللي شفتها في CS50
- SQL queries ← نفس الـ SQL بالظبط

**اللي هيكون جديد:**
- DataSource vs DriverManager
- HikariCP Connection Pool
- ACID في Java (commit / rollback)

---

## 📋 ما تعمله قبل ما تبدأ تكتب كود

### الخطوة 1 — Install PostgreSQL
```bash
# Download from:
https://www.postgresql.org/download/

# بعد التثبيت، افتح psql وتأكد:
psql --version
```

### الخطوة 2 — Create the database
```sql
-- في psql:
CREATE DATABASE finance_bank;
CREATE USER bank_user WITH PASSWORD 'bank_pass';
GRANT ALL PRIVILEGES ON DATABASE finance_bank TO bank_user;
```

### الخطوة 3 — Run the DDL script
الـ schema موجود في `docs/PostgreSQL_JDBC_Plan.docx` —
انسخ الـ SQL وشغّله في psql.

### الخطوة 4 — شوف الـ video
شوف **Marco Codes JDBC Crash Course** كامل قبل ما تكتب أي كود.

### الخطوة 5 — ابدأ الـ Problem Set
الترتيب الصح:

```
Part 3 (Basic JDBC)     ← ابدأ هنا  ~1 يوم
Part 4 (ACID)           ← ده الجديد ~2 يوم
Part 5 (Stored Procs)   ← تعرفه من CS50 ~1 يوم
Part 6 (Integration)    ← الربط النهائي ~1 يوم
```

> Parts 1 و 2 تقدر تتخطاهم — SQL بتاعهم تعرفه.

---

## 📁 الـ Files في الـ repo

```
banking-system/
├── docs/
│   ├── REFACTORING.md            ← تاريخ المشروع كامل
│   ├── PostgreSQL_JDBC_Plan.docx ← الـ schema + JDBC code
│   └── DB_Problem_Set.md         ← الـ exercises
├── README.md
└── src/
```

**ترتيب القراءة:**
1. `PostgreSQL_JDBC_Plan.docx` — الـ schema والـ code
2. شوف الـ video
3. `DB_Problem_Set.md` — ابدأ من Part 3

---

## 🗺️ الـ Roadmap الكامل

```
دلوقتي
   │
   ▼
شوف Marco Codes JDBC video (1-2 ساعة)
   │
   ▼
Install PostgreSQL + run DDL schema
   │
   ▼
Part 3: Basic JDBC — DatabaseConnection + CustomerDao
   │
   ▼
Part 4: ACID — TransactionDao مع commit/rollback  ← الجوهر
   │
   ▼
Part 5: Stored Procedures — CallableStatement
   │
   ▼
Part 6: Wire into BankService — الـ 30 JUnit tests تعدي كلها
   │
   ▼
✅ مشروعك بقى enterprise-level banking system بـ real persistence
   │
   ▼
Spring Boot (المرحلة الجاية)
@Transactional بدل manual commit/rollback
Spring Data JPA بدل manual DAOs
```

---

## ⚡ ملخص

| | |
|---|---|
| **الـ video** | Marco Codes — JDBC Crash Course |
| **الـ DBMS** | PostgreSQL |
| **اللي ناقصك** | ACID في Java فقط — مش SQL |
| **الوقت المتوقع** | 5-6 أيام شغل فعلي |
| **أهم part** | Part 4 — ACID Transactions |
| **التالي بعده** | Spring Boot + `@Transactional` |