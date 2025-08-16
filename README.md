# Bank REST API

Banking REST API application built with Spring Boot featuring card management and transfer functionality.

## Features

- 🔐 Authentication and authorization with USER/ADMIN roles
- 💳 CRUD operations for bank cards
- 🔒 Card number encryption in database
- 💸 Transfers between cards
- 📊 Swagger API documentation
- 🧪 Complete test coverage

## Technologies

- **Java 17**
- **Spring Boot 3.x**
- **Spring Security** (authentication and authorization)
- **Spring Data JPA** (database operations)
- **PostgreSQL** (main database)
- **H2** (test database)
- **Liquibase** (database migrations)
- **Docker & Docker Compose**
- **Maven**
- **JUnit 5 + Mockito** (testing)
- **OpenAPI/Swagger** (documentation)

## Quick Start

### 1. Clone the project
```bash
git clone <repository-url>
cd "Bank REST API"
```

### 2. Run with Docker Compose (recommended)
```bash
docker-compose up -d
```

The application will be available at: http://localhost:8080

### 3. Alternative startup

#### Requirements
- Java 17+
- Maven 3.6+
- PostgreSQL 12+

#### Database setup
```bash
# Create PostgreSQL database
createdb bankrest
```

#### Start application
```bash
./mvnw spring-boot:run
```

## API Documentation

After starting the application, Swagger UI is available at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## Authentication

### Test Users

The application creates test users on startup:

| Username | Password | Role | Description |
|----------|----------|------|-------------|
| user | password | USER | Regular user |
| admin | password | ADMIN | Administrator |

### Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "password": "password"
  }'
```

### Use Token

```bash
curl -X GET http://localhost:8080/api/cards \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Main API Endpoints

### Authentication
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - User registration

### Card Management
- `GET /api/cards` - Get user cards
- `POST /api/cards` - Create new card
- `PUT /api/cards/{id}` - Update card
- `DELETE /api/cards/{id}` - Delete card

### Transfers
- `POST /api/transfer` - Execute transfer between cards
- `GET /api/transfer/history` - Transfer history

### Administration (ADMIN only)
- `GET /api/admin/users` - List all users
- `GET /api/admin/cards` - List all cards

## Usage Examples

### Create Card
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cardNumber": "1234567890123456",
    "cardType": "DEBIT",
    "balance": 1000.00
  }'
```

### Transfer Between Cards
```bash
curl -X POST http://localhost:8080/api/transfer \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "fromCardId": 1,
    "toCardId": 2,
    "amount": 100.00
  }'
```

## Security

- **JWT tokens** for authentication
- **USER/ADMIN roles** for authorization
- **Card number encryption** in database
- **CSRF protection** for web interface
- **Data validation** at API level

## Testing

### Run all tests
```bash
./mvnw test
```

### Run specific test classes
```bash
./mvnw test -Dtest=TransferControllerTest
./mvnw test -Dtest=CardServiceTest
```

### Test Coverage
- Unit tests for services
- Integration tests for controllers
- Security tests
- Validation tests

## Project Structure

```
src/
├── main/java/com/example/bankrest/
│   ├── config/          # Configuration (Security, OpenAPI)
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── entity/         # JPA entities
│   ├── repository/     # Spring Data repositories
│   ├── service/        # Business logic
│   └── util/           # Utilities (encryption)
├── main/resources/
│   ├── db/migration/   # Liquibase migrations
│   └── docs/           # OpenAPI specification
└── test/               # Tests
```

## Configuration

### Main settings (application.properties)
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/bankrest
spring.datasource.username=bankuser
spring.datasource.password=bankpass

# JWT
app.jwt.secret=your-secret-key
app.jwt.expiration=86400000

# Encryption
app.encryption.key=your-encryption-key
```

## Docker

### Build image
```bash
docker build -t bank-rest-api .
```

### Run with PostgreSQL
```bash
docker-compose up -d
```

## Development

### Local development
```bash
# Run with development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Run with H2 database
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

### Docker container management
```bash
# Start
./docker-manager.sh start

# Stop
./docker-manager.sh stop

# Restart
./docker-manager.sh restart

# Logs
./docker-manager.sh logs
```

## Troubleshooting

### Database issues
```bash
# Check PostgreSQL connection
docker-compose logs db

# Recreate schema
docker-compose down -v
docker-compose up -d
```

### Authentication issues
- Make sure JWT token is not expired
- Check user role permissions
- Verify CORS settings

## License

MIT License

## Contact

For questions and suggestions, contact the developer.
