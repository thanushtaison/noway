# NO WAY - Full-Stack E-Commerce Application

A basic but fully functional e-commerce web application built with **Java 21**, **Spring Boot 4.1**, **Spring MVC**, **Spring Data JPA / Hibernate**, **Spring Security**, **MySQL**, **Thymeleaf**, **HTML5**, **CSS3**, **vanilla JavaScript**, **Maven** and **Git**. This is the first working foundation of a capstone project; Seller and Admin management modules are designed to be added in Phase 2.

> The project was originally specified as "ThanushMart" and renamed to **NO WAY** per request.

---

## Features

- User registration (BUYER / SELLER) with validation
- Login / logout with Spring Security form authentication
- BCrypt password hashing (passwords are never stored as plain text)
- Role-based authorization (BUYER, SELLER, ADMIN)
- MySQL persistence with Spring Data JPA / Hibernate
- Product listing, search by name, category filtering, product details
- Shopping cart stored in MySQL (add, update quantity, remove, backend totals)
- Basic checkout (no real payment gateway) with transactional order creation
- Stock reduction on checkout
- Order history and order details (buyers only see their own orders)
- Placeholder Seller and Admin dashboards (Phase 2)
- Default admin account seeded at startup
- Sample products seeded on first run
- Global exception handling (friendly JSON error messages, no stack traces)
- Responsive UI (desktop / tablet / mobile)
- REST APIs for products, cart, orders and auth
- Automated tests (30 tests using an in-memory H2 database)

---

## Technology Stack

| Layer     | Technology |
|-----------|------------|
| Language  | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Web       | Spring MVC + Thymeleaf |
| Persistence | Spring Data JPA / Hibernate |
| Security  | Spring Security (BCrypt, form login, method security) |
| Database  | MySQL 8 (H2 in-memory for tests) |
| Build     | Maven |
| Frontend  | HTML5, CSS3, JavaScript (vanilla) |

---

## Requirements

- JDK 17 or 21 (the project compiles at Java 21; developed on JDK 26 runtime)
- Maven 3.6+ (or use any IDE with bundled Maven)
- MySQL 8.x running on `localhost:3306`

---

## Project Structure

```
noway/
├── pom.xml
├── .gitignore
├── README.md
└── src/
    ├── main/
    │   ├── java/com/noway/
    │   │   ├── config/        # SecurityConfig, DataInitializer, GlobalModelAdvice
    │   │   ├── controller/    # Auth, Home, Product, Cart, Order, Seller, Admin
    │   │   ├── service/       # Auth, Product, Cart, Order
    │   │   ├── repository/    # Spring Data JPA repositories
    │   │   ├── entity/        # User, Product, CartItem, Order, OrderItem
    │   │   ├── dto/           # Request/response records
    │   │   ├── security/      # CustomUserDetailsService, SecurityUtils
    │   │   ├── exception/     # Custom exceptions + GlobalExceptionHandler
    │   │   └── NowayApplication.java
    │   └── resources/
    │       ├── templates/     # Thymeleaf pages
    │       ├── static/        # css/, js/, images/
    │       └── application.properties
    └── test/
        ├── java/              # Unit + integration tests
        └── resources/application.properties   # H2 test config
```

---

## MySQL Setup

1. Install and start MySQL Server 8.
2. The application creates the `noway` database automatically on first start:
   `spring.datasource.url=jdbc:mysql://localhost:3306/noway?createDatabaseIfNotExist=true&...`
   (or create it manually with `CREATE DATABASE noway;`)
3. Set your MySQL credentials via environment variables so no password is committed to Git:

   - Windows (PowerShell):
     ```powershell
     $env:DB_USERNAME="root"
     $env:DB_PASSWORD="your_password"
     ```
   - Linux / macOS:
     ```bash
     export DB_USERNAME=root
     export DB_PASSWORD=your_password
     ```

---

## Running the Application

```bash
mvn clean package
java -jar target/noway-app-1.0.0.jar
```

Then open <http://localhost:8080>.

## Deploying to Render

This project includes a `Dockerfile` and `render.yaml` for deployment as a Render web service.
The application requires a MySQL 8 database. Render's managed database is PostgreSQL, so use an
external MySQL provider and set these environment variables in the Render service:

- `DB_URL`: Complete JDBC URL, for example `jdbc:mysql://HOST:3306/noway?useSSL=true&serverTimezone=UTC`
- `DB_USERNAME`: MySQL username
- `DB_PASSWORD`: MySQL password

To deploy:

1. Push the repository to GitHub or GitLab.
2. In Render, choose **New > Blueprint** and select the repository.
3. Render detects `render.yaml`, creates the `noway` web service, and asks for the three database variables.
4. Enter the JDBC URL and credentials from your MySQL provider, then deploy.

Render supplies `PORT` automatically. The app uses that value and falls back to port `8080` for local development.

---

## Default Admin Account

The application seeds this account on first start if no users exist:

| Field    | Value |
|----------|----------|
| Email    | `admin@noway.com` |
| Password | `Admin@123` |

Only the BCrypt hash is stored in MySQL.

### Changing the admin password

Option 1 - Change it in the database directly (recommended for a quick demo):

```sql
UPDATE users SET password = '$2a$10$<new-bcrypt-hash>' WHERE email = 'admin@noway.com';
```

Generate a BCrypt hash with: <https://www.bcryptcalculator.com/> or:

```java
System.out.println(new BCryptPasswordEncoder().encode("NewPassword@123"));
```

Option 2 - Update `DataInitializer.seedDefaultAdmin()` in `src/main/java/com/noway/config/DataInitializer.java` and delete the existing user row (or drop the tables) so the new credentials are seeded on next start.

---

## Sample Products (auto-seeded)

| Product     | Price   | Category    | Stock |
|-------------|---------|-------------|-------|
| Laptop      | ₹45,000 | Electronics | 10    |
| Smartphone  | ₹18,000 | Electronics | 20    |
| Headphones  | ₹2,500  | Accessories | 30    |
| Keyboard    | ₹1,500  | Accessories | 25    |
| Smart Watch | ₹5,000  | Wearables   | 15    |

Seeded only when the `products` table is empty.

---

## Pages & Routes

| URL | Description | Access |
|-----|-------------|--------|
| `/` | Homepage (hero, categories, featured products) | Public |
| `/login` | Login page | Public |
| `/register` | Registration (BUYER / SELLER) | Public |
| `/products` | Product listing + search + category filter | Public |
| `/products/{id}` | Product details | Public |
| `/cart` | Shopping cart | Authenticated |
| `/checkout` | Checkout (confirms order) | Authenticated |
| `/order-success` | Order success page (`?orderId=`) | Authenticated |
| `/orders` | My orders | Authenticated |
| `/orders/{id}` | Order details | Owner only |
| `/seller` | Seller dashboard placeholder | SELLER |
| `/admin` | Admin dashboard placeholder | ADMIN |

---

## REST API Endpoints

| Method | URL | Description | Access |
|--------|-----|-------------|--------|
| POST | `/api/auth/register` | Register a user | Public |
| POST | `/api/auth/login` | Login (session-based) | Public |
| POST | `/api/auth/logout` | Logout | Public |
| GET | `/api/products` | List products (`?search=` / `?category=`) | Public |
| GET | `/api/products/{id}` | Product by id | Public |
| GET | `/api/cart` | Current user's cart | Authenticated |
| POST | `/api/cart` | Add to cart (`{"productId":1,"quantity":2}`) | Authenticated |
| PUT | `/api/cart/{id}` | Update quantity (`{"quantity":3}`) | Authenticated |
| DELETE | `/api/cart/{id}` | Remove cart item | Authenticated |
| POST | `/api/orders` | Place an order | Authenticated |
| GET | `/api/orders` | Current user's orders | Authenticated |
| GET | `/api/orders/{id}` | Order details | Owner only |

---

## Configuration

Key settings in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/noway?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

CSRF is enabled for HTML forms and disabled only for `/api/**` (the JavaScript API calls).

---

## Progressive Web App (PWA)

The site is installable as a standalone app on desktop and mobile:

- `src/main/resources/static/manifest.json` - app name, theme colour, start URL, icons
- `src/main/resources/static/sw.js` - service worker (offline shell caching + network-first navigation)
- `src/main/resources/static/images/icon-192.png` / `icon-512.png` - install icons
- Registration happens automatically from `static/js/main.js`; the manifest + meta tags are injected via the shared `head` fragment in `templates/fragments.html`

**To install:** open the site in Chrome or Edge (localhost works), then use the install icon in the address bar / menu (`Ctrl+Shift+U` in Edge). On a phone, use "Add to Home Screen". The app then runs full-screen with its own icon and an offline fallback for the app shell.

> Note: service workers only register on secure origins - `https://` or `localhost`. On a real server you need HTTPS for installability.

---

## Testing

```bash
mvn test
```

Tests run against an in-memory H2 database (MySQL mode) - no MySQL needed for tests. 30 tests cover registration, login, password hashing, role authorization and role-based redirects, product search/filtering, cart, checkout, order creation, stock reduction, empty-cart handling and order ownership enforcement.

---

## How It Works (summary)

- **Authentication:** Spring Security `UserDetailsService` loads users from MySQL; BCrypt verifies passwords; the `AuthenticationSuccessHandler` redirects by role (`BUYER -> /products`, `SELLER -> /seller`, `ADMIN -> /admin`).
- **Authorization:** URL rules in `SecurityConfig` plus `@PreAuthorize` on controllers; `@EnableMethodSecurity` is enabled.
- **Frontend <-> backend:** Thymeleaf pages are server-rendered; cart actions use `fetch()` against the REST API (same session cookie).
- **Cart:** stored in `cart_items` with a unique `(user_id, product_id)` constraint; totals are always computed on the backend.
- **Checkout:** `OrderService.placeOrder()` is `@Transactional` - it validates the cart, checks stock, creates the order + order items (price snapshot), reduces stock and clears the cart. Any failure rolls back.

---

## Future Development (Phase 2)

- Seller: add/edit/delete products, view received orders, inventory & sales dashboard
- Admin: view/manage users, manage orders, remove products
- Reviews, ratings, comments and review management
- Wishlist, order tracking, sales dashboard, AI chatbot

---

## License

Capstone project - free to use for learning purposes.
