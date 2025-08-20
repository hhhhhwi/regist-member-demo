# Project Structure

## Package Organization

The project follows a domain-driven package structure under `com.example.demo`:

```
src/main/java/com/example/demo/
├── user/                    # User management (existing)
│   ├── User.java           # User entity with TeacType, CntrTyCd enums
│   └── repository/
├── customer/               # Customer domain
│   ├── Customer.java       # Customer entity (parent + child info)
│   ├── service/
│   └── repository/
├── product/                # Product catalog domain
│   ├── Product.java        # Product entity with hierarchical structure
│   ├── service/
│   └── repository/
└── order/                  # Order management domain
    ├── Order.java          # Order entity
    ├── OrderSession.java   # Session management for 3-step process
    ├── service/
    └── repository/
```

## Layer Architecture

### Controller Layer
- Handle HTTP requests/responses
- Input validation and error handling
- Session management for multi-step processes
- Security context access

### Service Layer
- Business logic implementation
- Transaction management
- Role-based access control
- Data transformation between layers

### Repository Layer
- Data access using Spring Data JPA
- Custom query methods
- Database entity management

## Key Conventions

### Entity Design
- Use `@Entity` with explicit table names
- Include `createdAt` and `updatedAt` timestamps
- Use `@PrePersist` and `@PreUpdate` for automatic timestamp management
- Implement proper fetch strategies (`LAZY` for associations)

### Naming Conventions
- Entities: PascalCase (e.g., `Customer`, `OrderSession`)
- Fields: camelCase (e.g., `parentName`, `childGrade`)
- Tables: snake_case or explicit naming (e.g., `customer`, `orders`)
- Enums: UPPER_CASE (e.g., `PENDING`, `CONFIRMED`)

### Security Integration
- Leverage existing User entity for authentication
- Use TeacType, CntrTyCd, and deptCode for authorization
- Implement method-level security in service layer

### Session Management
- Use `OrderSession` entity for multi-step process state
- Store temporary data between steps
- Implement session expiration and cleanup

## File Organization

### Resources Structure
```
src/main/resources/
├── templates/              # Thymeleaf templates
│   ├── order/             # Order-related pages
│   ├── customer/          # Customer management pages
│   └── common/            # Shared templates
├── static/                # CSS, JS, images
└── application.properties # Configuration
```

### Configuration
- Database configuration in `application.properties`
- Security configuration via Java config classes
- Profile-specific configurations for different environments