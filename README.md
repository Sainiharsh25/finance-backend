# Finance Backend

A backend system I built for a finance dashboard. It handles user management, financial records, and some basic analytics. Built with Spring Boot and MySQL.

---

## What this does

- Users can log in and get a JWT token
- Three roles — Admin, Analyst, and Viewer — each with different access
- Admins can create/update/delete transactions and manage users
- Analysts can view transactions and dashboard data
- Viewers can only see the dashboard summary
- Dashboard API returns totals, category breakdowns, and monthly trends

---

## Tech used

- Java 21 + Spring Boot 3.3.4
- MySQL 8
- Spring Security with JWT
- Spring Data JPA
- Maven
- Lombok (to reduce boilerplate)
- JUnit 5 + Mockito for tests

---

## How to run it

### What you need first
- Java 21 installed
- MySQL running locally
- Maven

### Steps

**1. Clone the repo**
```bash
git clone https://github.com/your-username/finance-backend.git
cd finance-backend
```

**2. Create the database**

Just make sure MySQL is running. The app will create the `finance_db` database automatically on first run because of this in application.properties:
```
createDatabaseIfNotExist=true
```

**3. Update your database password**

Open `src/main/resources/application.properties` and change this line:
```
spring.datasource.password=your_password
```

**4. Run it**
```bash
mvn spring-boot:run
```

Server starts at http://localhost:8080

**5. Test it**

Three users are created automatically when the app starts:

| Email                  | Password | Role |
|------------------------|---|---|
| admin@financeapp.com   | admin123 | ADMIN |
| analyst@financeapp.com | analyst123 | ANALYST |
| viewer@financeapp.com  | viewer123 | VIEWER |

Login first to get a token, then use that token in the Authorization header for everything else.

---

## Project structure

```
src/main/java/com/harsh/finance/
├── config/         → security config, data seeder
├── controller/     → REST endpoints
├── dto/            → request and response classes
├── entity/         → User and Transaction
├── enums/          → Role, TransactionType
├── exception/      → custom exceptions + global handler
├── repository/     → database queries
├── security/       → JWT filter
├── service/        → business logic
└── util/           → JWT utilities
```

I kept services as interfaces with separate impl classes mainly so mocking in tests is easier.

---

## API endpoints

### Auth

**POST /api/auth/login**

No token needed for this one.

```json
{
  "email": "admin@financeapp.com",
  "password": "admin123"
}
```

Returns a token. Use it as `Authorization: Bearer <token>` in all other requests.

---

### Transactions

| Method | URL | Who can use it |
|---|---|---|
| POST | /api/transactions | Admin only |
| GET | /api/transactions | Admin, Analyst |
| GET | /api/transactions/{id} | Admin, Analyst |
| PUT | /api/transactions/{id} | Admin only |
| DELETE | /api/transactions/{id} | Admin only |

**Creating a transaction:**
```json
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "date": "2026-03-01",
  "notes": "March salary"
}
```

**Filtering transactions (all optional):**
```
GET /api/transactions?type=INCOME&category=Salary&startDate=2026-01-01&endDate=2026-12-31&page=0&size=20
```

Deleting a transaction doesn't actually remove it from the database — it just sets a `deleted` flag. I did this because financial records probably shouldn't be permanently deleted.

---

### Users (Admin only)

| Method | URL | What it does |
|---|---|---|
| POST | /api/users | Create user |
| GET | /api/users | List all users |
| GET | /api/users/{id} | Get one user |
| PUT | /api/users/{id} | Update user |
| DELETE | /api/users/{id} | Deactivate user |

---

### Dashboard

**GET /api/dashboard/summary?year=2026**

Anyone logged in can hit this. Returns:
- Total income and expenses
- Net balance
- Breakdown by category
- Last 10 transactions
- Month by month income vs expenses for the year

---

## Roles and what they can do

| | VIEWER | ANALYST | ADMIN |
|---|---|---|---|
| Login | ✅ | ✅ | ✅ |
| Dashboard | ✅ | ✅ | ✅ |
| View transactions | ❌ | ✅ | ✅ |
| Create/edit transactions | ❌ | ❌ | ✅ |
| Manage users | ❌ | ❌ | ✅ |

---

## Running tests

```bash
mvn test
```

Tests use H2 (in-memory database) so you don't need MySQL running to run them.

I wrote unit tests for the service layer using Mockito, and a controller test for the auth endpoint using MockMvc.

---

## Some decisions I made

**Why soft delete?**
Didn't want to permanently remove financial records. Setting `deleted = true` and filtering it out in queries felt safer.

**Why BigDecimal for amount?**
Double has floating point issues which is a real problem with money. Even small precision errors add up.

**Why two layers of access control?**
I have URL-level rules in SecurityConfig and also `@PreAuthorize` on methods. Probably overkill for this scale but it means one misconfiguration doesn't open everything up.

**Category as free text**
Kept it simple — just a string field. A separate categories table would make sense if this grew into a real product.

**Monthly trends not weekly**
Went with monthly because it felt more useful for a finance dashboard. Weekly could be added as a query param later without changing the schema.

---

## What I'd add with more time

- Weekly trends in the dashboard
- A proper audit log table tracking every change
- Rate limiting on the auth endpoint
- More integration tests that test the full request-to-database flow
- Password reset flow

---

## Notes

- Make sure you change `spring.datasource.password` before running
- JWT tokens expire after 24 hours by default (configurable in application.properties)
- The app creates the database automatically but you still need MySQL running
