# E-Commerce Platform Backend API

## Overview
This is a robust and scalable Spring Boot backend for an E-commerce platform. It provides a complete RESTful API solution covering user authentication, product catalog management, shopping cart operations, order processing, and a simulated Stripe payment gateway integration. 

Built with best practices for a junior/fresher development standard, the system emphasizes clean architecture, comprehensive exception handling, and full stateless security via JSON Web Tokens (JWT).

## 🚀 Key Features
* **User Authentication**: Secure registration and login flow using JWT (`jjwt`) and BCrypt password hashing.
* **Role-Based Access Control**: Differentiates between `USER` and `ADMIN` roles (e.g., only admins can create products).
* **Product Catalog**: Full CRUD operations for managing products.
* **Cart Management**: Add products to cart, view cart contents, and calculate totals. 
* **Order Processing**: Checkout cart items, create an immutable order, and seamlessly deduct stock from inventory.
* **Payment Integration**: Integrates Stripe PaymentIntent logic. A specialized "Mock Mode" allows testing the full payment flow without needing active Stripe API keys.
* **Global Error Handling**: Centralized exception handling to ensure consistent, standardized JSON error responses (e.g., for 404s, Validation errors, etc.).
* **H2 In-Memory Database**: Zero-configuration database strictly for development and testing. (Ready to be easily switched back to MySQL).
* **Automated Testing**: Comprehensive unit tests covering the core logic (`AuthService`, `CartService`, `OrderService`, `PaymentService`).

## 🛠 Tech Stack
* **Java 17**
* **Spring Boot 3.2.5** (Web, Data JPA, Security, Validation)
* **H2 Database** (In-Memory for pure testing convenience)
* **JSON Web Tokens (JWT)**
* **Stripe Java SDK**
* **Swagger/OpenAPI 3.0**
* **JUnit 5 / Mockito**

---

## ⚙️ Setup and Installation

### Prerequisites
* Java Development Kit (JDK) 17 or higher
* Maven 3.6+

### 1. Clone & Build
Navigate to the project directory and build the application:
```bash
mvn clean install
```

### 2. Run the Server
Start the Spring Boot application using the Maven Wrapper or directly:
```bash
mvn spring-boot:run
```
The server will start on port `8080`.

*(Note: Because this project uses an H2 in-memory database, the database is fresh upon every restart. No database credentials or local SQL servers are required!)*

---

## 🧪 Testing the API via Swagger UI

Swagger UI is configured out-of-the-box and includes JWT authorization support.

📍 **Access Swagger here:**  
👉 **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Full End-to-End Testing Flow

Because the database resets on startup, use the following sequence to test the entire E-Commerce flow successfully from start to finish:

#### Step 1: Register an Admin Account
To create products, you must be an `ADMIN`. 
* **Endpoint:** `POST /api/auth/register`
* **Request Body:**
```json
{
  "name": "Admin User",
  "email": "admin@shop.com",
  "password": "admin123",
  "role": "ADMIN"
}
```
* **Action:** Copy the `token` from the response.

#### Step 2: Authorize in Swagger
* Scroll to the very top of the Swagger page.
* Click the green **Authorize 🔒** button.
* Paste your copied `token` into the input field and click **Authorize**.

#### Step 3: Create a Product
* **Endpoint:** `POST /api/products`
* **Request Body:**
```json
{
  "name": "Sony Wireless Headphones",
  "description": "High quality sound",
  "price": 299.99,
  "stockQuantity": 15,
  "imageUrl": "https://example.com/sony.jpg"
}
```
* **Expected Result:** `201 Created` returning the new product with `"id": 1`.

#### Step 4: Add the Product to Your Cart
* **Endpoint:** `POST /api/cart/items`
* **Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```
* **Expected Result:** `200 OK` showing the Cart JSON populated with your items.

#### Step 5: Checkout & Create an Order
* **Endpoint:** `POST /api/orders/checkout`
* **Request Body:**
```json
{
  "shippingAddress": "123 Main Street, NY"
}
```
* **Expected Result:** `201 Created`. Your cart is cleared, and an Order is generated with `"paymentStatus": "PENDING"`.
* **Action:** **Take note of the order `id` from the response** (e.g., `1`).

#### Step 6: Create a Payment Intent
* **Endpoint:** `POST /api/payments/create-payment-intent`
* **Parameters:** Type `1` (or your order ID) into the `orderId` parameter text box.
* **Expected Result:** `200 OK` returning a mock payment payload.
* **Action:** **Copy the `paymentIntentId`** from the response (e.g., `"pi_mock_1_174xxx"`).

#### Step 7: Confirm Payment
* **Endpoint:** `POST /api/payments/confirm`
* **Request Body:**
```json
{
  "orderId": 1,
  "paymentIntentId": "pi_mock_1_174xxx"
}
```
* **Expected Result:** `200 OK`. The returned Order will clearly show `"paymentStatus": "PAID"`.

---

## 🔬 Running Unit Tests
Unit tests extensively cover the core logic (`AuthService`, `CartService`, `OrderService`, `PaymentService`) ensuring components behave as expected under isolated conditions without database connections.

To run the complete test suite:
```bash
mvn test
```
**Expected Output:** `BUILD SUCCESS` with 16 test cases passing.


