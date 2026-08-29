# Sunrise Dental Clinic — Patient & Appointment Management System

A web-based patient, appointment, and billing management system built for Sunrise Dental Clinic,
replacing a manual paper-based process that caused double bookings, lost records, and billing
errors. Built for **CIS6003 Advanced Programming** (Cardiff Metropolitan University).

## Features

- **Secure staff login** with three role levels — Administrator, Receptionist, Dentist — each
  restricted to the parts of the system relevant to their job
- **Patient registration** with search, edit, and deactivation
- **Appointment booking** with real-time dentist availability checking (prevents double booking
  at both the application layer and the database layer via a trigger)
- **Appointment rescheduling, cancellation, and treatment status tracking**
- **Automatic bill generation and settlement**, with the total computed by a database trigger
  (treatment fee + consultation fee − discount)
- **PDF invoice and report export**, and CSV export for reports
- **Email notifications** (booking confirmation, next-day reminders, bill receipts) sent
  asynchronously so the UI is never blocked waiting on SMTP
- **Analytics dashboard** — revenue trends, pending receivables, dentist workload, treatment
  popularity
- **REST API** (JSON) alongside the web UI, sharing the exact same session-based authentication
  and business logic — no duplicated rules between the two front ends
- **Built-in Help section** with step-by-step instructions for new staff

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 26 |
| Web framework | Jakarta Servlets 6.0 + JSP 3.1 + JSTL 3.0 (Jakarta EE 10) |
| Application server | Apache Tomcat 11 |
| Database | MySQL 8 |
| Build tool | Maven |
| Testing | JUnit 5 |
| PDF generation | Apache PDFBox |
| Email | Jakarta Mail |
| JSON (REST API) | org.json |

## Architecture

A distributed, three-tier architecture:

```
Servlet (controller)  →  Service (business logic)  →  DAO (data access)  →  MySQL
        │                        │
        │                        └── Observer pattern: notifies EmailNotificationListener
        │                             on booking / cancellation / billing events
        └── REST API layer (lk.sunrise.dental.api) exposes the same services as JSON,
            authenticated through the same session cookie as the web UI
```

Design patterns used: **Factory** (`ConnectionFactory` — centralises JDBC connection creation),
**Singleton** (`EmailService`, `AppConfig` — one shared instance each), **Observer**
(`NotificationListener` / `EmailNotificationListener` — decouples business events from
notification side-effects).


## Project Structure

```
sunrise-dental-clinic/
├── database/
│   └── setup.sql                  # Schema, triggers, functions, stored procedures, seed data
├── src/
│   ├── main/
│   │   ├── java/lk/sunrise/dental/
│   │   │   ├── model/             # Domain classes (User, Patient, Appointment, Bill)
│   │   │   ├── dao/                # Data access layer
│   │   │   ├── service/            # Business logic layer (+ event/ for the Observer pattern)
│   │   │   ├── servlet/            # Web controllers (JSP-facing)
│   │   │   ├── api/                # REST API controllers (JSON-facing)
│   │   │   ├── filter/             # Authentication filter
│   │   │   └── util/               # Config, DB connection factory, security, validation, PDF/CSV
│   │   ├── resources/
│   │   │   └── app.properties.example   # Copy to app.properties and fill in your own values
│   │   └── webapp/
│   │       ├── WEB-INF/views/      # JSP views, organised by feature
│   │       └── assets/             # CSS and JavaScript
│   └── test/java/lk/sunrise/dental/     # JUnit 5 unit test suite
├── pom.xml
└── StartApp.bat                    # Windows convenience script: build, deploy, and launch
```

## Getting Started

### Prerequisites

- JDK 26
- Apache Maven
- Apache Tomcat 11
- MySQL 8

### 1. Set up the database

```sql
-- In MySQL, run:
SOURCE database/setup.sql;
```

This creates the `sunrise_dental_db` schema, all tables, triggers, functions, stored procedures,
and seed data (including demo accounts, see below).

### 2. Configure the application

```bash
cp src/main/resources/app.properties.example src/main/resources/app.properties
```

Edit `app.properties` with your own MySQL credentials and (optionally) SMTP credentials for email
notifications. This file is gitignored so your real credentials are never committed.

### 3. Build and deploy

```bash
mvn clean package
```

Deploy the resulting `target/sunrise-dental-clinic.war` to your Tomcat `webapps/` folder, then
start Tomcat and visit:

```
http://localhost:8080/sunrise-dental-clinic/login
```

On Windows, `StartApp.bat` automates build + deploy + launch — edit the `JAVA_HOME` and file
paths at the top of the script to match your own machine first.

### 4. Run the test suite

```bash
mvn test
```

### Demo accounts (seeded by `setup.sql`)

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | Administrator |
| `receptionist` | `reception123` | Receptionist |
| `dentist` | `dentist123` | Dentist |