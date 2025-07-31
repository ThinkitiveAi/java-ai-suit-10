# Health First - Healthcare Provider Management System

## Overview
Health First is a comprehensive healthcare provider management system built with Spring Boot, designed to facilitate seamless interactions between healthcare providers and patients. The system provides robust APIs for patient registration, authentication, provider availability management, and appointment scheduling.

## Tech Stack
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security with JWT
- **Database:** JPA/Hibernate with support for both SQL (PostgreSQL/MySQL) and NoSQL (MongoDB)
- **Documentation:** OpenAPI/Swagger
- **Testing:** JUnit 5, Mockito
- **Build Tool:** Maven
- **Java Version:** 17+

## Features

### 1. Authentication (AuthController)
Generic authentication controller handling both patient and provider login flows.

#### Endpoints

##### Login
```http
POST /api/v1/auth/login
```
Unified login endpoint for both patients and providers.

**Request Body:**
```json
{
  "identifier": "john.doe@example.com",
  "password": "SecurePass123!",
  "userType": "patient"  // Optional: "patient" or "provider"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 1800,
    "token_type": "Bearer",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe"
    }
  }
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "identifier": "john.doe@example.com",
    "password": "SecurePass123!",
    "userType": "patient"
  }'
```

### 2. Patient Management (PatientController)

#### Endpoints

##### Register Patient
```http
POST /api/v1/patient/register
```
Register a new patient with comprehensive validation.

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  },
  "emergencyContact": {
    "name": "Jane Doe",
    "phone": "+1234567891",
    "relationship": "Spouse"
  },
  "medicalHistory": ["No known allergies", "Healthy"],
  "insuranceInfo": {
    "provider": "Blue Cross Blue Shield",
    "policyNumber": "BC123456789"
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "Patient registered successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "j***e@example.com",
    "phoneNumber": "+12***7890",
    "emailVerified": false,
    "phoneVerified": false
  }
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/patient/register \
  -H "Content-Type: application/json" \
  -d @- << 'EOF'
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+1234567890",
  "password": "SecurePass123!",
  "confirmPassword": "SecurePass123!",
  "dateOfBirth": "1990-05-15",
  "gender": "MALE",
  "address": {
    "street": "123 Main Street",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  },
  "emergencyContact": {
    "name": "Jane Doe",
    "phone": "+1234567891",
    "relationship": "Spouse"
  },
  "medicalHistory": ["No known allergies", "Healthy"],
  "insuranceInfo": {
    "provider": "Blue Cross Blue Shield",
    "policyNumber": "BC123456789"
  }
}
EOF
```

##### Check Email Availability
```http
GET /api/v1/patient/check-email?email=john.doe@example.com
```

**Response:**
```json
{
  "available": true,
  "message": "Email is available"
}
```

**cURL Example:**
```bash
curl "http://localhost:8080/api/v1/patient/check-email?email=john.doe@example.com"
```

##### Check Phone Availability
```http
GET /api/v1/patient/check-phone?phone=+1234567890
```

**Response:**
```json
{
  "available": true,
  "message": "Phone number is available"
}
```

**cURL Example:**
```bash
curl "http://localhost:8080/api/v1/patient/check-phone?phone=%2B1234567890"
```

### 3. Provider Availability Management (ProviderAvailabilityController)

#### Endpoints

##### Create Availability
```http
POST /api/v1/provider/availability
```
Create availability slots with optional recurrence patterns.

**Request Body:**
```json
{
  "availabilityDate": "2024-02-01",
  "startTime": "09:00",
  "endTime": "17:00",
  "slotDurationMinutes": 30,
  "appointmentType": "CONSULTATION",
  "timezone": "America/New_York",
  "price": 150.00,
  "location": "Main Clinic",
  "recurrencePattern": "WEEKLY",
  "recurrenceEndDate": "2024-03-01",
  "bufferTimeMinutes": 5,
  "maxConsecutiveSlots": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Availability created successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "providerId": "123e4567-e89b-12d3-a456-426614174000",
    "providerName": "Dr. Jane Smith",
    "specialization": "Cardiology",
    "availabilityDate": "2024-02-01",
    "startTime": "09:00",
    "endTime": "17:00",
    "slotDurationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "timezone": "America/New_York"
  }
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/provider/availability \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "availabilityDate": "2024-02-01",
    "startTime": "09:00",
    "endTime": "17:00",
    "slotDurationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "timezone": "America/New_York",
    "price": 150.00,
    "location": "Main Clinic",
    "recurrencePattern": "WEEKLY",
    "recurrenceEndDate": "2024-03-01",
    "bufferTimeMinutes": 5,
    "maxConsecutiveSlots": 2
  }'
```

##### Get Provider Availability
```http
GET /api/v1/provider/{providerId}/availability?startDate=2024-02-01&endDate=2024-02-28
```

**Response:**
```json
{
  "success": true,
  "message": "Availability retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "availabilityDate": "2024-02-01",
      "startTime": "09:00",
      "endTime": "17:00",
      "appointmentType": "CONSULTATION",
      "price": 150.00,
      "location": "Main Clinic",
      "isActive": true
    }
  ],
  "count": 1
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/provider/550e8400-e29b-41d4-a716-446655440000/availability?startDate=2024-02-01&endDate=2024-02-28" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### 4. Appointment Slot Search (AvailabilitySearchController)

#### Endpoints

##### Search Available Slots
```http
GET /api/v1/availability/search
```
Quick search for available appointment slots.

**Parameters:**
- `startDate` (required): Start date (YYYY-MM-DD)
- `endDate` (required): End date (YYYY-MM-DD)
- `specialization` (optional): Provider specialization
- `appointmentType` (optional): Type of appointment
- `location` (optional): Clinic location
- `minPrice` (optional): Minimum price
- `maxPrice` (optional): Maximum price
- `maxResults` (optional, default: 50): Maximum results to return

**Response:**
```json
{
  "success": true,
  "message": "Search completed successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "providerId": "123e4567-e89b-12d3-a456-426614174000",
      "providerName": "Dr. Jane Smith",
      "startDateTime": "2024-02-01T09:00:00",
      "endDateTime": "2024-02-01T09:30:00",
      "appointmentType": "CONSULTATION",
      "price": 150.00,
      "location": "Main Clinic"
    }
  ],
  "count": 1,
  "hasMore": false
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/availability/search?startDate=2024-02-01&endDate=2024-02-28&specialization=Cardiology&appointmentType=CONSULTATION"
```

##### Advanced Search
```http
POST /api/v1/availability/search
```
Advanced search with detailed filters.

**Request Body:**
```json
{
  "startDate": "2024-02-01",
  "endDate": "2024-02-28",
  "specialization": "Cardiology",
  "appointmentType": "CONSULTATION",
  "location": "Main Clinic",
  "minPrice": 100.00,
  "maxPrice": 200.00,
  "preferredStartTime": "09:00",
  "preferredEndTime": "17:00",
  "maxResults": 50,
  "sortBy": "price",
  "ascending": true
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/availability/search \
  -H "Content-Type: application/json" \
  -d '{
    "startDate": "2024-02-01",
    "endDate": "2024-02-28",
    "specialization": "Cardiology",
    "appointmentType": "CONSULTATION",
    "location": "Main Clinic",
    "minPrice": 100.00,
    "maxPrice": 200.00,
    "preferredStartTime": "09:00",
    "preferredEndTime": "17:00",
    "maxResults": 50,
    "sortBy": "price",
    "ascending": true
  }'
```

## Authentication

### Obtaining JWT Token
1. Login using the `/api/v1/auth/login` endpoint
2. Extract the `access_token` from the response
3. Include the token in subsequent requests using the `Authorization` header:
   ```
   Authorization: Bearer YOUR_ACCESS_TOKEN
   ```

### Token Expiration
- Access tokens expire after 30 minutes
- Use the token's `expires_in` value to refresh before expiration

## Environment Setup

### Required Environment Variables
```properties
# Application
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev

# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/healthfirst
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=1800000  # 30 minutes in milliseconds

# Rate Limiting
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_DURATION=3600  # 1 hour in seconds
```

### Running the Application

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-org/health-first.git
   cd health-first
   ```

2. **Build the project:**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access Swagger UI:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

## API Documentation
Full API documentation is available via Swagger UI when the application is running:
```
http://localhost:8080/swagger-ui.html
```

## Error Handling
All endpoints follow a consistent error response format:

```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "ERROR_CODE",
  "errors": ["Detailed error message 1", "Detailed error message 2"]
}
```

Common error codes:
- `VALIDATION_ERROR`: Input validation failed
- `UNAUTHORIZED`: Authentication required
- `FORBIDDEN`: Insufficient permissions
- `NOT_FOUND`: Resource not found
- `CONFLICT`: Resource conflict (e.g., duplicate email)
- `RATE_LIMIT_EXCEEDED`: Too many requests 