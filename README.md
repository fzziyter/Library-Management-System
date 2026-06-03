# 📚 Library Management System

A full-stack web application for managing a library, built as a JEE project.  
It handles books, categories, and users with JWT-based authentication and role-based access control (USER / MANAGER / ADMIN).

---

## 🏗️ Architecture

```
Bibliotheque_projetJEE/
├── backend/          # REST API — Spring Boot 3.3 + Spring Security + JPA
└── frontend/         # User Interface — React 19 + Vite + Recharts
```

---

## ⚙️ Tech Stack

| Side | Technology |
|------|------------|
| Backend | Java 17, Spring Boot 3.3, Spring Security, Spring Data JPA |
| Database | MySQL (dev) / H2 in-memory (tests) |
| Authentication | JWT (jjwt 0.11.5) |
| Mapping | MapStruct 1.5.5 + Lombok |
| API Docs | Springdoc OpenAPI / Swagger UI |
| Export | iText 7 (PDF), Apache POI (Excel) |
| Frontend | React 19, React Router 7, Axios, Recharts |
| Frontend Build | Vite 8 |
| Testing | JUnit 5, Mockito, @SpringBootTest, Spring Security Test |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven (or use the included `mvnw` wrapper)
- MySQL running locally
- Node.js 18+

### 1. Backend

```bash
cd backend
```

Check and adapt the configuration in `src/main/resources/application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/projet_jee?createDatabaseIfNotExist=true
    username: root
    password: ""
```

Start the application:

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

The API will be available at **http://localhost:8080**

### 2. Frontend

```bash
cd frontend
npm install
npm run dev
```

The UI will be available at **http://localhost:5173**

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/signin` | Public | Login — returns a JWT token |
| POST | `/api/auth/signup` | Public | Register a new user |

### Books
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/livres` | USER+ | List all books |
| GET | `/api/livres/{id}` | USER+ | Get a book by ID |
| POST | `/api/livres` | ADMIN/MANAGER | Create a book |
| PUT | `/api/livres/{id}` | ADMIN/MANAGER | Update a book |
| DELETE | `/api/livres/{id}` | ADMIN/MANAGER | Delete a book |

### Categories
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/categories` | USER+ | List all categories |
| POST | `/api/categories` | ADMIN/MANAGER | Create a category |
| PUT | `/api/categories/{id}` | ADMIN/MANAGER | Update a category |
| DELETE | `/api/categories/{id}` | ADMIN/MANAGER | Delete a category |

### Users (Admin)
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/admin/users` | ADMIN | List all users |
| POST | `/api/admin/users?role={ROLE}` | ADMIN | Create a user with a given role |
| PUT | `/api/admin/users/{id}` | ADMIN | Update a user |
| DELETE | `/api/admin/users/{id}` | ADMIN | Delete a user |

### Export
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| GET | `/api/export/pdf` | USER+ | Export book list as PDF |
| GET | `/api/export/excel` | USER+ | Export book list as Excel |

### Interactive API Documentation
Available at **http://localhost:8080/swagger** once the app is running.

---

## 🧪 Testing

Tests cover the **Book** and **Category** modules using JUnit 5 + Mockito.

```bash
# Run all tests
./mvnw test               # Linux / macOS
mvnw.cmd test             # Windows

# Run a specific test class
mvnw.cmd test -Dtest=LivreServiceTest
mvnw.cmd test -Dtest="LivreControllerTest,CategorieControllerTest"
```

### Test Coverage

| Class | Type | Tests |
|-------|------|-------|
| `LivreServiceTest` | Unit (Mockito) | 12 tests |
| `CategorieServiceTest` | Unit (Mockito) | 8 tests |
| `LivreControllerTest` | Integration (@SpringBootTest) | 9 tests |
| `CategorieControllerTest` | Integration (@SpringBootTest) | 8 tests |

> Integration tests use an **H2 in-memory database** via the `test` profile.  
> Configuration file: `src/test/resources/application-test.properties`

---

## 🔐 Roles & Permissions

| Role | Permissions |
|------|-------------|
| `USER` | Read books and categories, export |
| `MANAGER` | USER + create / update / delete books and categories |
| `ADMIN` | MANAGER + user management |

---

## 📁 Backend Structure

```
src/main/java/com/example/backend/
├── controller/       # REST controllers (Livre, Categorie, Auth, Admin, Export)
├── service/          # Business logic
├── repository/       # Database access (Spring Data JPA)
├── entity/           # JPA entities (Livre, Categorie, DetailsLivre, User, Role)
├── dto/              # Data Transfer Objects
├── mapper/           # MapStruct mappers (LivreMapper)
├── security/         # Spring Security config + JWT filter
└── exception/        # Global exception handling (ResourceNotFoundException)
```

---

## 🛠️ Production Build

### Backend (JAR)
```bash
cd backend
./mvnw package -DskipTests
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### Frontend (static dist)
```bash
cd frontend
npm run build
# Output files are generated in frontend/dist/
```