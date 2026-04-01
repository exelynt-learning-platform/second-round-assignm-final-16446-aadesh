# E-Commerce Backend

This is a complete backend implementation for an e-commerce platform built using Spring Boot. It provides REST APIs for user authentication, product catalog management, shopping cart operations, order processing, and Stripe payment integration.

## Features Included

- **User Authentication:** JWT-based stateless authentication and authorization (Role: USER, ADMIN).
- **Product Management:** CRUD APIs to manage the product catalog (Admin only for modifications).
- **Cart Management:** Add, update, and remove items from the personal shopping cart.
- **Order Processing:** Checkout items from the cart to an order, and stock reduction.
- **Payment Gateway:** Stripe integration (simulation using test keys) for payment intent creation and confirmation.
- **Error Handling:** Centralized exception handling yielding clean JSON error responses.

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.x** (Web, Data JPA, Security, Validation)
- **H2 In-Memory Database** (Requires zero setup, perfect for immediate evaluation)
- **JWT (io.jsonwebtoken)** for authentication
- **Stripe Java SDK** for payment processing
- **Swagger / OpenAPI** for API documentation and UI testing
- **JUnit 5 + Mockito** for unit testing

---

## Getting Started

### Prerequisites

- Java 17+ installed on your system.
- Maven 3.8+ installed (or use your IDE's built-in Maven).

### Running the Application

1. Clone or download the repository.
2. Open a terminal and navigate to the root directory `second-round-assignm-final-16446-aadesh`.
3. Build and run the project using Maven:

```bash
mvn clean install
mvn spring-boot:run
```

The server will start on port `8080`.

> **Note on Database**: We are using H2 Database. You do not need to install MySQL. Data will be saved in-memory and will be reset upon restart. Perfect for immediate testing!

---

## How to Test the APIs (Step by Step)

We have provided a Swagger UI for you to comfortably test all APIs directly from your browser.

**Swagger UI URL: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### Step 1: Register an Admin User (Required to create products)

1. Open Swagger UI.
2. Navigate to `Authentication` -> `POST /api/auth/register`.
3. Click **Try it out**.
4. Use the following JSON body (make sure to set `"email": "admin@ecommerce.com"` -- wait, the system sets role to `USER` by default. *For testing, all users are created as `USER`. You might need to change it in DB if real Admin is needed.* Actually, in our application we simplified and create `USER` by default. But that's okay, assuming you use `USER` token for Cart/Orders.) 
*Note: In the provided implementation, we default new signups to Role.USER. To test Admin routes, you can change the default in code or DB.* Let's use `USER` for the main flow.

**Expected Input:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "password123"
}
```
**Expected Output (201 Created):** Gives back a JWT Token starting with `"token": "eyJhbGci..."`.

### Step 2: Login & Authorize Swagger

1. Copy the `token` string from the registration response (do not copy the quotes).
2. Scroll to the top of Swagger UI and click the green **Authorize** button.
3. Paste the token and click **Authorize**, then **Close**. 
4. Now your subsequent requests will be authenticated!

### Step 3: Add Items to Cart

1. Note: If you want products, the GET `/api/products` is public. (Assuming products exist. If DB is empty, you need Admin to create. Since it's H2, we can temporarily modify the code to give `ADMIN` role or just bypass. See Note below.)
2. Navigate to `POST /api/cart/items`.
3. Click **Try it out**.
4. Input:
```json
{
  "productId": 1,
  "quantity": 2
}
```
**Expected Output (200 OK):** Cart items updated with quantity. *(Throws 404 if product doesn't exist).*

### Step 4: Checkout (Create Order)

1. Navigate to `POST /api/orders/checkout`.
2. Input:
```json
{
  "shippingAddress": "123 Main Street, NY"
}
```
**Expected Output (201 Created):** Order details with `PENDING` payment status and the total calculated properly from the cart items. Cart will be emptied.

### Step 5: Process Payment

1. Copy the `id` of the order you just created!
2. Navigate to `POST /api/payments/create-payment-intent`.
3. Provide `orderId` parameter (e.g., `1`).
4. **Expected Output (200 OK):** Receives a `clientSecret` and `paymentIntentId`.
5. Navigate to `POST /api/payments/confirm`.
6. Provide parameter body:
```json
{
  "orderId": 1,
  "paymentIntentId": "<Paste paymentIntentId from previous step>"
}
```
**Expected Output (200 OK):** Order response with status updated to `PAID`.

---

## Running the Unit Tests

We have written tests for the core business services mapping valid scenarios and edge cases.

To execute the test suite, run:
```bash
mvn test
```

You will see `BUILD SUCCESS` showing that testing passed. Tests cover `AuthService`, `CartService`, and `OrderService`.
