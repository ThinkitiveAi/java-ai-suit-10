# HealthFirst Provider Registration Backend

A comprehensive Spring Boot application for healthcare provider registration with secure authentication, validation, and email verification.

## Features

- 🔐 **Secure Provider Registration** with comprehensive validation
- 📧 **Email Verification** with automated email notifications
- 🚦 **Rate Limiting** (5 registration attempts per IP per hour)
- 🔒 **Password Security** with BCrypt hashing (12 rounds)
- 📊 **Swagger API Documentation** with OpenAPI 3.0
- 🛡️ **Input Sanitization** to prevent injection attacks
- ✅ **Comprehensive Validation** for all fields
- 📱 **REST API** with proper HTTP status codes
- 🧪 **Unit & Integration Tests** with MockMvc
- 🏗️ **Clean Architecture** with separation of concerns

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security 6.2.0**
- **Spring Data JPA**
- **H2 Database** (development) / **MySQL** (production)
- **Maven** for dependency management
- **Swagger/OpenAPI 3** for API documentation
- **JUnit 5** & **Mockito** for testing
- **BCrypt** for password hashing
- **Jakarta Validation** for input validation

## Project Structure

```
src/
├── main/
│   ├── java/com/healthfirst/
│   │   ├── config/              # Configuration classes
│   │   │   ├── SecurityConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/          # REST controllers
│   │   │   └── ProviderController.java
│   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── ProviderRegistrationRequest.java
│   │   │   └── ProviderRegistrationResponse.java
│   │   ├── entity/              # JPA entities
│   │   │   ├── Provider.java
│   │   │   └── embedded/
│   │   │       └── ClinicAddress.java
│   │   ├── enums/               # Enumerations
│   │   │   └── VerificationStatus.java
│   │   ├── exception/           # Exception handling
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── middleware/          # Custom middleware
│   │   │   └── RateLimitingService.java
│   │   ├── repository/          # Data access layer
│   │   │   └── ProviderRepository.java
│   │   ├── service/             # Business logic
│   │   │   ├── EmailService.java
│   │   │   ├── ProviderService.java
│   │   │   └── ValidationService.java
│   │   ├── util/                # Utility classes
│   │   │   └── PasswordUtil.java
│   │   └── HealthFirstServerApplication.java
│   └── resources/
│       └── application.yml
└── test/                        # Test classes
    ├── java/com/healthfirst/
    │   ├── controller/
    │   │   └── ProviderControllerIntegrationTest.java
    │   └── service/
    │       └── ProviderServiceTest.java
    └── resources/
        └── application-test.yml
```

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- Git

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd health-first-server
   ```

2. **Build the project:**
   ```bash
   mvn clean compile
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080/api/v1`

### Configuration

#### Environment Variables

Set these environment variables for production:

```bash
# Database Configuration (for MySQL)
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/healthfirst
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# JWT Secret
JWT_SECRET=your-super-secret-jwt-key-here
```

#### Application Properties

Key configuration options in `application.yml`:

```yaml
# Server Configuration
server:
  port: 8080
  servlet:
    context-path: /api/v1

# Database Configuration (H2 for development)
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password

# Rate Limiting
app:
  rate-limit:
    registration:
      capacity: 5        # Maximum attempts
      refill-period: 3600 # 1 hour in seconds
```

## API Documentation

### Swagger UI

Once the application is running, access the Swagger UI at:
- **Swagger UI**: `http://localhost:8080/api/v1/swagger-ui.html`
- **API Docs**: `http://localhost:8080/api/v1/api-docs`

### API Endpoints

#### Provider Registration

**POST** `/provider/register`

Register a new healthcare provider.

**Request Body:**
```json
{
  "first_name": "John",
  "last_name": "Doe",
  "email": "john.doe@clinic.com",
  "phone_number": "+1234567890",
  "password": "SecurePassword123!",
  "confirm_password": "SecurePassword123!",
  "specialization": "Cardiology",
  "license_number": "MD123456789",
  "years_of_experience": 10,
  "clinic_address": {
    "street": "123 Medical Center Dr",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  }
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Provider registered successfully. Verification email sent.",
  "data": {
    "provider_id": "uuid-here",
    "email": "john.doe@clinic.com",
    "verification_status": "PENDING"
  }
}
```

#### Get Valid Specializations

**GET** `/provider/specializations`

Returns a list of valid medical specializations.

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    "Cardiology",
    "Dermatology",
    "Emergency Medicine",
    "Family Medicine",
    "Internal Medicine"
  ],
  "count": 25
}
```

#### Check Email Availability

**GET** `/provider/check-email?email=test@example.com`

Check if an email address is available for registration.

**Response (200 OK):**
```json
{
  "success": true,
  "available": true,
  "email": "test@example.com"
}
```

#### Check Phone Availability

**GET** `/provider/check-phone?phoneNumber=+1234567890`

Check if a phone number is available for registration.

#### Check License Availability

**GET** `/provider/check-license?licenseNumber=MD123456789`

Check if a license number is available for registration.

## Validation Rules

### Provider Registration Validation

- **First Name**: 2-50 characters, required
- **Last Name**: 2-50 characters, required
- **Email**: Valid email format, unique, required
- **Phone Number**: Valid international format, unique, required
- **Password**: 8+ characters with uppercase, lowercase, number, and special character
- **Specialization**: Must be from predefined list
- **License Number**: Alphanumeric, unique, required
- **Years of Experience**: 0-50, required
- **Clinic Address**: All fields required with proper formatting

### Security Features

- **Password Hashing**: BCrypt with 12 salt rounds
- **Rate Limiting**: 5 registration attempts per IP per hour
- **Input Sanitization**: XSS and injection prevention
- **Email Verification**: Required for account activation
- **CORS Configuration**: Configurable allowed origins
- **Security Headers**: XSS protection, content type options

## Database Schema

### Provider Table

```sql
CREATE TABLE providers (
    id BINARY(16) PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    license_number VARCHAR(50) NOT NULL UNIQUE,
    years_of_experience INT NOT NULL,
    street VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(50) NOT NULL,
    zip VARCHAR(10) NOT NULL,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') DEFAULT 'PENDING',
    license_document_url TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

## Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProviderServiceTest

# Run tests with coverage
mvn test jacoco:report
```

### Test Categories

1. **Unit Tests**: Service layer testing with mocks
2. **Integration Tests**: Full application context testing
3. **Validation Tests**: Input validation testing
4. **Security Tests**: Authentication and authorization testing

## Monitoring & Debugging

### H2 Console (Development)

Access the H2 database console at:
- **URL**: `http://localhost:8080/api/v1/h2-console`
- **JDBC URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: `password`

### Logging

The application uses SLF4J with Logback. Log levels can be configured in `application.yml`:

```yaml
logging:
  level:
    com.healthfirst: DEBUG
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
```

## Production Deployment

### Building for Production

```bash
# Create production JAR
mvn clean package -Pprod

# Run production JAR
java -jar target/health-first-server-1.0.0.jar
```

### Production Checklist

- [ ] Configure MySQL database
- [ ] Set up email service (SMTP)
- [ ] Configure JWT secret
- [ ] Set up HTTPS/SSL
- [ ] Configure logging levels
- [ ] Set up monitoring and health checks
- [ ] Configure backup strategy
- [ ] Set up CI/CD pipeline

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- **Email**: admin@healthfirst.com
- **Documentation**: [API Documentation](http://localhost:8080/api/v1/swagger-ui.html)
- **Issues**: [GitHub Issues](https://github.com/your-repo/issues) 