# Technology Stack

## Core Framework
- **Spring Boot 3.2.6** - Main application framework
- **Java 17** - Programming language
- **Maven** - Build system and dependency management

## Key Dependencies
- **Spring Boot Starter Web** - REST API and web layer
- **Spring Boot Starter Security** - Authentication and authorization
- **Spring Boot Starter Data JPA** - Database ORM layer
- **Spring Boot Starter JDBC** - Database connectivity
- **Spring Boot Starter Thymeleaf** - Server-side templating engine
- **Lombok 1.18.32** - Code generation for boilerplate reduction
- **H2 Database** - In-memory database for development/testing
- **JWT (jjwt 0.12.3)** - JSON Web Token authentication

## Architecture Patterns
- **MVC Pattern** - Controller → Service → Repository layers
- **Repository Pattern** - Data access abstraction with Spring Data JPA
- **DTO Pattern** - Data transfer between layers
- **Service Layer Pattern** - Business logic encapsulation

## Common Commands

### Build & Run
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package application
mvn clean package

# Run application
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Development
```bash
# Skip tests during build
mvn clean package -DskipTests

# Run in debug mode
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

## Database
- **H2 Console**: Available at `/h2-console` when running locally
- **JPA/Hibernate**: Entity management with automatic schema generation
- **Connection**: In-memory database for development, configurable for production

## Security
- **JWT Authentication**: Token-based authentication system
- **Spring Security**: Method-level and URL-based security
- **Role-Based Access**: TeacType, CntrTyCd, and deptCode based permissions