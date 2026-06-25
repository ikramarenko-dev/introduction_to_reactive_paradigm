## Capstone project: Real-time orders aggregation with Spring WebFlux


### Objective:
Getting practical skills in developing an application using reactive programming.


### Prerequisites:
- Java 21
- Spring Boot, Spring WebFlux
- Reactor
- MongoDB (basics)
- Docker
- Logback
- WireMock


### Scope of the project:
Implement a service that provides an API for returning users' orders in real-time mode.
The application integrates (using `WebClient`) with two external services for getting appropriate data.

---
#### Order Search service
Exposes REST API for searching orders by phone number using a reactive approach. <br>
API returns a response in multi-value stream format (`application/x-ndjson`).

---
#### Product Info service
Exposes REST API for getting the list of user-friendly product names (with score) by product code. <br>
Uses a non-reactive approach and works in unstable mode (with delay). <br>
Integration uses a **5s timeout** — on failure the response is treated as an empty product list.

---

### Technical Description:
- Retrieve the user's phone number by user id from **MongoDB** using a reactive driver.
- Get a list of orders by phone number from **Order Search service**.
- For each order, fetch products from **Product Info service** by `productCode` and select the most relevant one (highest score).
- Log all responses from external services. On Product Info failure — log the error and return empty products.
- Endpoint must receive a `requestId` header. All logs must include the `requestId` value via contextual [MDC](https://logback.qos.ch/manual/mdc.html) logging using [Reactor Context](https://projectreactor.io/docs/core/release/reference/aboutDoc.html).

---

### API description

![()](https://img.shields.io/static/v1?label=&message=GET&color=0c90ff) `/api/users/{userId}/orders` **Get aggregated orders for a user**

#### path variable `userId`
#### request header `requestId`

#### response **<font color='30c030'>200</font>** OK

```json
{
  "orderNumber": "Order_0",
  "userName": "John",
  "phoneNumber": "123456789",
  "productCode": "3852",
  "productName": "Apple",
  "productId": "444"
},
{
  "orderNumber": "Order_1",
  "userName": "John",
  "phoneNumber": "123456789",
  "productCode": "5256",
  "productName": null,
  "productId": null
}
```

| Field         | Type   | Required |
|---------------|--------|----------|
| orderNumber   | string | +        |
| userName      | string | +        |
| phoneNumber   | string | +        |
| productCode   | string | +        |
| productName   | string | -        |
| productId     | string | -        |

#### response **<font color='f93e3e'>500</font>** Internal Server Error (user not found or upstream failure)

---

### Project run

1. Install [Docker](https://docs.docker.com/desktop/) and [Colima](https://github.com/abiosoft/colima) (or any Docker runtime)
2. Clone external services into the **same parent directory** as this project:
```
git clone https://github.com/gridu/Reactive_Paradigm_FOR_STUDENTS
```
> The cloned repo must sit alongside this project, e.g.:
> ```
> parent-dir/
> ├── introduction_to_reactive_paradigm/   ← this project
> └── Reactive_Paradigm_FOR_STUDENTS/      ← external services
> ```
3. Start all services:
```
colima start
docker compose up -d
```
4. Make a test call:
```
curl -N -H "requestId: req-123" http://localhost:8080/api/users/user1/orders
```
5. Stop Product Info service to validate fallback behavior:
```
docker compose stop product-info-service
```

---

### Project structure

```
src/main/java/com/gd/reactiveparadigm/
├── client/
│   ├── OrderSearchClient.java       # WebClient integration — reactive NDJSON stream
│   └── ProductInfoClient.java       # WebClient integration — with 5s timeout + fallback
├── config/
│   └── WebClientConfig.java         # WebClient beans
├── controller/
│   └── UserOrderController.java     # GET /api/users/{userId}/orders
├── domain/
│   └── User.java                    # MongoDB document
├── logging/
│   └── MdcContext.java              # Reactor Context → MDC propagation
├── model/
│   ├── Order.java
│   ├── Product.java
│   └── UserOrderResponse.java
├── repository/
│   └── UserInfoRepository.java      # ReactiveMongoRepository
└── service/
    └── UserOrderService.java        # Aggregation logic
```
