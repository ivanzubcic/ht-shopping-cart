# Shopping Cart Microservice

A Spring Boot microservice for managing shopping carts, items, and price statistics.

## Technologies
- Java 17
- Spring Boot 3.3.x
- Spring Data MongoDB
- Jakarta Validation
- MongoDB

## Build & Run

1. **Configure MongoDB**
   - Ensure MongoDB is running (default: `mongodb://localhost:27017`)
   - Optionally, set `spring.data.mongodb.uri` in `application.properties`

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

## REST Endpoints

### Get Cart by Customer
- `GET /api/carts/{customerId}`

### Add/Update Cart
- `POST /api/carts`
  - Body: Cart JSON (see example below)

### Remove Cart
- `DELETE /api/carts/{customerId}`

### Statistics
- `GET /api/carts/statistics?offerId=...&action=...&from=...&to=...`
  - Returns count of items matching criteria in the period (timestamps in ISO-8601 format)

## Example Cart JSON
```json
{
  "customerId": "customer123",
  "items": [
    {
      "offerId": "tv-001",
      "action": "ADD",
      "actionTimestamp": "2025-07-24T21:00:00Z",
      "prices": [
        {
          "type": "RECURRING",
          "value": 19.99,
          "recurrences": 12
        }
      ]
    },
    {
      "offerId": "sub-002",
      "action": "MODIFY",
      "actionTimestamp": "2025-07-24T21:10:00Z",
      "prices": [
        {
          "type": "ONE_TIME",
          "value": 5.00
        }
      ]
    }
  ]
}
```

## Notes
- All fields are mandatory unless stated otherwise.
- `actionTimestamp` should be in ISO-8601 format (e.g., `2025-07-24T21:00:00Z`).
- `action` can be `ADD`, `MODIFY`, or `DELETE`.
- `type` can be `RECURRING` or `ONE_TIME`.
