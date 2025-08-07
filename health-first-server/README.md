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

### 5. Provider Selection and Availability Management

#### Endpoints

##### Get All Providers
```http
GET /api/v1/providers
```
Retrieve list of all active and verified providers for selection.

**Response:**
```json
{
  "success": true,
  "message": "Providers retrieved successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "Jane",
      "lastName": "Smith",
      "displayName": "Dr. Jane Smith",
      "email": "jane.smith@example.com",
      "phoneNumber": "+1234567890",
      "specialization": "Cardiology",
      "yearsOfExperience": 15,
      "verificationStatus": "VERIFIED",
      "licenseNumber": "MD123456789",
      "isActive": true,
      "createdAt": "2024-01-15T10:30:00Z",
      "updatedAt": "2024-02-01T14:22:00Z",
      "clinicAddress": {
        "street": "123 Medical Center Dr",
        "city": "New York",
        "state": "NY",
        "zip": "10001",
        "formatted": "123 Medical Center Dr, New York, NY 10001"
      }
    }
  ],
  "count": 1
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/v1/providers
```

##### Set Provider Availability
```bash
curl -X POST http://localhost:8080/api/v1/providers/{providerId}/availability \
  -H "Content-Type: application/json" \
  -d '{
    "availabilityDate": "2024-12-30",
    "startTime": "09:00",
    "endTime": "17:00",
    "slotDurationMinutes": 30,
    "appointmentType": "CONSULTATION",
    "timezone": "UTC",
    "price": 150.00,
    "location": "Main Clinic - Room 302",
    "description": "Cardiology consultation available",
    "recurrencePattern": "WEEKLY",
    "recurrenceEndDate": "2025-06-30",
    "recurrenceDaysOfWeek": [1, 2, 3, 4, 5],
    "maxConsecutiveSlots": 2,
    "bufferTimeMinutes": 15,
    "isBlocked": false,
    "blockReason": null,
    "consultationType": "In-person",
    "allowWalkIns": false,
    "advanceBookingDays": 30,
    "sameDayBooking": false,
    "consultationDurationMinutes": 30,
    "breakBetweenAppointments": 10,
    "maxAppointmentsPerDay": 16,
    "consultationFee": 150.00,
    "emergencyAvailable": false,
    "notesForPatients": "Please bring your medical records and arrive 10 minutes early",
    "requiresConfirmation": false,
    "sendReminders": true,
    "reminderTimeHours": 24,
    "allowCancellation": true,
    "cancellationHoursBefore": 24
  }'
```

### 6. Appointment Booking Management

#### Endpoints

##### Book an Appointment
```http
POST /api/v1/appointments
```
Book an appointment for a patient with a provider based on available slots.

**Request Body:**
```json
{
  "slotId": "550e8400-e29b-41d4-a716-446655440000",
  "patientId": "123e4567-e89b-12d3-a456-426614174000",
  "providerId": "456e7890-e89b-12d3-a456-426614174000",
  "bookingReason": "Regular cardiology checkup - chest pain concerns",
  "patientNotes": "Patient has been experiencing mild chest discomfort for the past week",
  "requiresConfirmation": false
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Appointment booked successfully",
  "data": {
    "appointmentId": "550e8400-e29b-41d4-a716-446655440000",
    "providerId": "456e7890-e89b-12d3-a456-426614174000",
    "providerName": "Dr. Sarah Johnson",
    "patientId": "123e4567-e89b-12d3-a456-426614174000",
    "patientName": "John Doe",
    "startDateTime": "2024-12-30T09:00:00Z",
    "endDateTime": "2024-12-30T09:30:00Z",
    "appointmentType": "CONSULTATION",
    "price": 150.00,
    "location": "Main Clinic - Room 302",
    "bookingReason": "Regular cardiology checkup - chest pain concerns",
    "confirmed": true,
    "bookedAt": "2024-12-25T14:30:00Z"
  }
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Appointment slot is not available",
  "errorCode": "SLOT_NOT_AVAILABLE"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/v1/appointments \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "slotId": "550e8400-e29b-41d4-a716-446655440000",
    "patientId": "123e4567-e89b-12d3-a456-426614174000",
    "providerId": "456e7890-e89b-12d3-a456-426614174000",
    "bookingReason": "Regular cardiology checkup - chest pain concerns",
    "patientNotes": "Patient has been experiencing mild chest discomfort for the past week"
  }'
```

##### List Appointments
```http
GET /api/v1/appointments
```
Get a paginated list of appointments with comprehensive filtering options.

**Parameters:**
- `patientId` (optional): Filter appointments for a specific patient
- `providerId` (optional): Filter appointments for a specific provider
- `startDate` (optional): Start date for filtering (YYYY-MM-DD)
- `endDate` (optional): End date for filtering (YYYY-MM-DD)
- `filterType` (optional): Filter by appointment status (`upcoming`, `past`, `cancelled`, `all`). Default: `all`
- `appointmentType` (optional): Filter by appointment type (e.g., `CONSULTATION`, `FOLLOW_UP`)
- `confirmationStatus` (optional): Filter by confirmation status (`confirmed`, `unconfirmed`, `all`). Default: `all`
- `sortBy` (optional): Sort field (`startTime`, `endTime`, `bookedAt`, `price`). Default: `startTime`
- `ascending` (optional): Sort direction (`true` for ascending, `false` for descending). Default: `true`
- `page` (optional): Page number (1-based). Default: 1
- `pageSize` (optional): Items per page (10-100). Default: 20

**Response:**
```json
{
  "success": true,
  "message": "Appointments retrieved successfully",
  "data": {
    "appointments": [
      {
        "appointmentId": "550e8400-e29b-41d4-a716-446655440000",
        "providerId": "456e7890-e89b-12d3-a456-426614174000",
        "providerName": "Dr. Sarah Johnson",
        "providerSpecialization": "Cardiology",
        "providerImage": "https://example.com/images/dr-sarah.jpg",
        "patientId": "123e4567-e89b-12d3-a456-426614174000",
        "patientName": "John Doe",
        "patientImage": "https://example.com/images/john-doe.jpg",
        "startDateTime": "2024-12-30T09:00:00Z",
        "endDateTime": "2024-12-30T09:30:00Z",
        "appointmentType": "CONSULTATION",
        "appointmentStatus": "UPCOMING",
        "price": 150.00,
        "location": "Main Clinic - Room 302",
        "bookingReason": "Regular cardiology checkup",
        "confirmed": true,
        "bookedAt": "2024-12-25T14:30:00Z",
        "isUpcoming": true,
        "consultationType": "In-person",
        "patientNotes": "Patient has been experiencing mild chest discomfort",
        "providerNotes": "Follow up on previous ECG results"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "pageSize": 20,
      "totalItems": 45,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Invalid date range",
  "errorCode": "INVALID_DATE_RANGE"
}
```

**cURL Example:**
```bash
curl -X GET "http://localhost:8080/api/v1/appointments?patientId=123e4567-e89b-12d3-a456-426614174000&filterType=upcoming&page=1&pageSize=20" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
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

# Timezone Configuration (Global Healthcare App)
APP_TIMEZONE_DEFAULT=UTC
APP_TIMEZONE_STORAGE=UTC
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

## Global Timezone Handling
This application is designed for global use and implements strict UTC timezone handling:

- **Storage**: All timestamps are stored in UTC in the database
- **API Responses**: All datetime fields are returned in UTC with 'Z' suffix
- **Frontend Integration**: Frontend should convert UTC times to user's local timezone for display
- **Availability Times**: Provider availability times are stored with timezone information but normalized to UTC

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