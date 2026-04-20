<div align="center">

# 🛍️ ShopWave

### A modern full-stack e-commerce platform

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-61DAFB?style=flat-square&logo=react&logoColor=black)](https://react.dev)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688?style=flat-square&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)

**Browse products · Manage your cart · Place orders · Get AI-powered recommendations**

> ShopWave is an e-commerce web app where users can browse products, manage a cart, place orders, and receive personalized product recommendations powered by a separate ML service.

[Getting Started](#-getting-started) · [API Docs](#-api-endpoints) · [Screenshots](#-screenshots) · [Tech Stack](#-tech-stack)

</div>

---

## 📸 Screenshots

> To add your own screenshots, create a `screenshots/` folder at the root of the repo and add images with the names shown below.

<table>
  <tr>
    <td align="center" width="25%">
      <img src="images/home.png" alt="Home" width="100%"/>
      <br/><sub><b>Home — Trending</b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/products.png" alt="Products" width="100%"/>
      <br/><sub><b>Products</b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/product_detail.png" alt="Product Detail" width="100%"/>
      <br/><sub><b>Product Detail</b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/cart.png" alt="Recommendations" width="100%"/>
      <br/><sub><b>Cart</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="25%">
      <img src="images/orders.png" alt="Cart" width="100%"/>
      <br/><sub><b>Orders</b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/siginin.png" alt="Orders" width="100%"/>
      <br/><sub><b>Sign in </b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/signup.png" alt="Sign In" width="100%"/>
      <br/><sub><b>Sign up </b></sub>
    </td>
    <td align="center" width="25%">
      <img src="images/addproduct.png" alt="Add Product" width="100%"/>
      <br/><sub><b>Add Product (Admin)</b></sub>
    </td>
  </tr>
</table>

---

## ✨ Features

- 🔐 **JWT Authentication** — Register, login, protected routes
- 🛒 **Cart Management** — Add, update quantity, remove, clear
- 📦 **Order System** — Place orders, view order history with status
- 🔍 **Product Search** — Search by name, filter by category
- 🤖 **ML Recommendations** — Trending products, similar items (content-based), personalized picks (collaborative filtering)
- 👑 **Admin Panel** — Create new products with live preview
- 📱 **Responsive UI** — Clean dark theme with smooth transitions

---

## 🏗️ Tech Stack

<table>
  <tr>
    <th>Layer</th>
    <th>Technology</th>
    <th>Purpose</th>
  </tr>
  <tr>
    <td>Frontend</td>
    <td>React 18 + Vite + React Router + Axios</td>
    <td>SPA with client-side routing and API calls</td>
  </tr>
  <tr>
    <td>Backend</td>
    <td>Spring Boot 3.5 + Java 21 + Spring Security</td>
    <td>REST API with JWT auth</td>
  </tr>
  <tr>
    <td>ML Service</td>
    <td>FastAPI + pandas + scikit-learn</td>
    <td>Product recommendations engine</td>
  </tr>
  <tr>
    <td>Database</td>
    <td>PostgreSQL 15+</td>
    <td>Persistent data storage</td>
  </tr>
  <tr>
    <td>ORM</td>
    <td>Spring Data JPA / Hibernate</td>
    <td>Database access layer</td>
  </tr>
  <tr>
    <td>API Docs</td>
    <td>Swagger UI (springdoc-openapi)</td>
    <td>Interactive API documentation</td>
  </tr>
</table>

---

## 📁 Project Structure

```
shopwave-ecommerce/
│
├── 📂 backend/                         # Spring Boot REST API (Java 21)
│   ├── src/main/java/com/ecommerce/backend/
│   │   ├── controller/                 # Auth, Product, Cart, Order
│   │   ├── service/                    # Business logic interfaces + impls
│   │   ├── model/                      # JPA entities (User, Product, Order…)
│   │   ├── dto/                        # Request / Response records
│   │   ├── security/                   # JwtFilter, JwtUtil, UserDetailsService
│   │   ├── config/                     # SecurityConfig, OpenApiConfig
│   │   └── exception/                  # GlobalExceptionHandler
│   └── src/main/resources/
│       └── application.yaml
│
├── 📂 ml-service/                      # FastAPI recommendation engine (Python)
│   ├── main.py                         # /trending, /similar, /recommendations
│   └── requirements-test.txt
│
├── 📂 shopwave/                        # React frontend (Vite)
│   └── src/
│       ├── api/                        # Axios client (Spring + ML)
│       ├── context/                    # Auth, Cart, Toast providers
│       ├── components/                 # Layout, ProductCard, shared UI
│       └── pages/                      # Home, Products, Cart, Orders, Auth…
│
├── 📂 screenshots/                     # Add your screenshots here
├── .gitignore
└── README.md
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Version |
|------|---------|
| Java | 21+ |
| Maven | 3.9+ |
| Python | 3.10+ |
| Node.js | 18+ |
| PostgreSQL | 13+ |

---

### Step 1 — Database

```sql
-- Run as PostgreSQL superuser
CREATE DATABASE ecommerce_db;
```

---

### Step 2 — Backend

```bash
cd backend
```

Update `src/main/resources/application.yaml` with your DB credentials:

```yaml
spring:
  datasource:
    username: postgres
    password: yourpassword       # ← change this

cors:
  allowed-origins: http://localhost:5173
```

> ⚠️ Never commit your real password. Use environment variables in production.

```bash
./mvnw spring-boot:run
```

| | URL |
|--|--|
| API Base | `http://localhost:8080/api` |
| Swagger UI | `http://localhost:8080/swagger-ui.html` |

---

### Step 3 — ML Service

```bash
cd ml-service
pip install fastapi uvicorn sqlalchemy pandas scikit-learn psycopg2-binary
```

```bash
# Linux / Mac
DATABASE_URL="postgresql://postgres:yourpassword@localhost:5432/ecommerce_db" \
uvicorn main:app --reload --port 8000

# Windows CMD
set DATABASE_URL=postgresql://postgres:yourpassword@localhost:5432/ecommerce_db
uvicorn main:app --reload --port 8000
```

| | URL |
|--|--|
| ML Service | `http://localhost:8000` |
| Health Check | `http://localhost:8000/health` |

---

### Step 4 — Frontend

```bash
cd shopwave
npm install
npm run dev
```

Open `http://localhost:5173`

---

## 🌐 API Endpoints

<details>
<summary><b>Auth</b></summary>

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `POST` | `/api/auth/register` | Public | Register new user |
| `POST` | `/api/auth/login` | Public | Login, returns JWT token |

</details>

<details>
<summary><b>Products</b></summary>

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/products` | Public | List all products |
| `GET` | `/api/products/{id}` | Public | Get product by ID |
| `GET` | `/api/products/category/{cat}` | Public | Filter by category |
| `GET` | `/api/products/search?q=` | Public | Search by name |
| `POST` | `/api/products` | Public | Create a product |

</details>

<details>
<summary><b>Cart</b></summary>

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/cart` | ✅ Required | Get cart items |
| `POST` | `/api/cart/add` | ✅ Required | Add item to cart |
| `PUT` | `/api/cart/{id}` | ✅ Required | Update quantity |
| `DELETE` | `/api/cart/{id}` | ✅ Required | Remove item |
| `DELETE` | `/api/cart/clear` | ✅ Required | Clear cart |

</details>

<details>
<summary><b>Orders</b></summary>

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| `GET` | `/api/orders` | ✅ Required | Get order history |
| `POST` | `/api/orders/place` | ✅ Required | Place order from cart |
| `GET` | `/api/orders/{id}` | ✅ Required | Get order by ID |

</details>

<details>
<summary><b>ML Service</b></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/trending` | Top 6 trending products by order count |
| `GET` | `/similar/{product_id}` | Similar products (content-based filtering) |
| `GET` | `/recommendations/{user_id}` | Personalized picks (collaborative filtering) |
| `GET` | `/health` | Service + DB health check |

</details>

---

## ⚙️ Environment Variables

<details>
<summary><b>Spring Boot</b></summary>

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ecommerce_db` | PostgreSQL JDBC URL |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | — | Database password |
| `JWT_SECRET` | dev default | Min 64-char secret for signing tokens |
| `JWT_EXPIRATION` | `86400000` | Token TTL in milliseconds (24 hours) |
| `CORS_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |
| `SERVER_PORT` | `8080` | HTTP server port |

</details>

<details>
<summary><b>ML Service</b></summary>

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | `postgresql://ecommerce_user:ecommerce123@localhost:5432/ecommerce_db` | PostgreSQL connection URL |
| `CORS_ORIGINS` | `http://localhost:5173` | Comma-separated allowed origins |

</details>

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch — `git checkout -b feature/your-feature`
3. Commit your changes — `git commit -m 'add some feature'`
4. Push to the branch — `git push origin feature/your-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
  Made with ☕ and lots of debugging
</div>
