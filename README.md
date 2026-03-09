# Pern Merch Store — Full-Stack E-Commerce Application

A full-stack e-commerce web application for selling band merchandise (clothing, vinyl, posters, accessories), built with **Spring Boot** on the backend and **React** on the frontend. Features JWT-based authentication, role-based access control, a shopping cart, and order management.

---

## What I Built

### Backend — Spring Boot REST API

The core of this project is a **Spring Boot 3.2.3** REST API serving as the backbone for all e-commerce functionality. The backend is organized into a layered architecture: **controllers → services → repositories → entities**, with DTOs decoupling the API contract from the database models.

**Entities & Relationships**

- **User** — stores credentials (BCrypt-hashed password) and a role (USER or ADMIN). Has one-to-many relationships with CartItems and Orders.
- **Product** — catalog item with name, description, price, category, size, image URL, and stock quantity.
- **CartItem** — join entity linking a User to a Product with a quantity. Represents a user's shopping cart.
- **Order / OrderItem** — when a user checks out, their cart items become OrderItems within an Order. The price is captured at time of purchase so it's immune to future price changes. Orders track status via an `OrderStatus` enum (PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED).

**REST API Endpoints**

| Area | Method | Endpoint | Access |
|------|--------|----------|--------|
| Auth | POST | `/api/auth/register` | Public |
| Auth | POST | `/api/auth/login` | Public |
| Products | GET | `/api/products`, `/api/products/{id}` | Public |
| Products | POST/PUT/DELETE | `/api/products/**` | Admin only |
| Cart | GET/POST/PUT/DELETE | `/api/cart/**` | Authenticated |
| Orders | POST | `/api/orders/checkout` | Authenticated |
| Orders | GET | `/api/orders` | Authenticated |

**Security & JWT Authentication**

- Configured **Spring Security** with stateless session management — no server-side sessions, all auth is token-based.
- `JwtUtil` generates and validates HMAC-SHA signed tokens with a 24-hour expiration.
- `JwtAuthFilter` intercepts every request, extracts the Bearer token, validates it, and sets the `SecurityContext`.
- `CustomUserDetailsService` bridges Spring Security with the User entity.
- CORS is configured for the Vite dev server origins.
- Public endpoints (auth, product browsing) are open; cart/order endpoints require authentication; product mutation endpoints require the ADMIN role.

**Services & Business Logic**

- **AuthService** — handles registration (duplicate checks, password hashing, JWT generation) and login (credential validation, token issuance).
- **ProductService** — full CRUD with optional category filtering and entity-to-DTO conversion.
- **CartService** — manages cart state per user (add, update quantity, remove, clear).
- **OrderService** — transactional checkout that atomically converts cart items into an Order with OrderItems and clears the cart.

**Other Backend Details**

- `DataSeeder` seeds the H2 database on startup with an admin account and 7 sample products.
- `GlobalExceptionHandler` provides centralized, consistent error responses.
- Uses an **H2 in-memory database** for development with `create-drop` DDL auto mode.

### Frontend — React + Vite SPA

A single-page application handling all user interactions:

- **React Router v6** for client-side routing with protected route wrappers.
- **Context API** for global state — `AuthContext` (token, role, login/logout) and `CartContext` (cart items, count, CRUD operations).
- **Pages**: Home (hero + featured products), Products (category filter tabs), Product Detail, Cart (quantity controls + checkout), Order History, Login/Register, Admin panel (product CRUD table).
- Centralized API module with automatic JWT injection on authenticated requests.

---

## What I Learned

### Spring Boot & Backend Concepts

- **Layered architecture** — structuring a Spring Boot app into controllers, services, repositories, and entities, and understanding the responsibility of each layer.
- **Spring Data JPA** — defining entities with `@Entity`, mapping relationships (`@ManyToOne`, `@OneToMany`), using cascade types and orphan removal, and writing repository interfaces that Spring auto-implements.
- **Spring Security with JWT** — implementing stateless authentication from scratch: generating tokens, writing a custom filter chain, configuring `SecurityFilterChain` with endpoint-level access rules, and integrating `UserDetailsService`.
- **Role-based authorization** — restricting endpoints by role using `requestMatchers` and `hasRole()` in the security config.
- **DTOs & data mapping** — separating the API response shape from the internal entity model to avoid leaking database structure and to control serialization.
- **Transactional operations** — using `@Transactional` to ensure checkout (creating an order + clearing the cart) happens atomically.
- **Exception handling** — using `@RestControllerAdvice` and `@ExceptionHandler` for centralized, consistent error responses across the API.
- **Password security** — hashing passwords with BCrypt via Spring Security's `PasswordEncoder`.
- **Data seeding** — using a `CommandLineRunner`-style component to bootstrap test data on startup.

### Frontend & Full-Stack Integration

- **React Context** for managing global auth and cart state without prop drilling.
- **Protected routes** with role-based redirects on the client side.
- **Connecting a React SPA to a Spring Boot API** — handling CORS, sending JWTs in headers, and managing auth flow across the stack.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 17, Spring Boot 3.2.3, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt 0.12.5), BCrypt |
| Database | H2 (in-memory, dev) |
| Frontend | React 18, React Router v6, Vite 5 |
| Build | Maven (backend), npm (frontend) |

---

## Running Locally

**Backend**
```bash
cd pern-merch-backend
./mvnw spring-boot:run
```
Server starts on `http://localhost:8080`. H2 console available at `/h2-console`.

**Frontend**
```bash
cd pern-merch-frontend
npm install
npm run dev
```
Dev server starts on `http://localhost:5173`.

**Seeded Accounts**
| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
