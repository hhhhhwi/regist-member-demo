# Product Overview

## Teacher Order Registration System (교사 주문 등록 시스템)

A Spring Boot web application that enables teachers to register customer information and place orders through a 3-step process. The system extends an existing authentication system and provides role-based access to different product models based on teacher permissions.

### Key Features

- **3-Step Order Process**: Customer registration → Model selection → Order confirmation
- **Role-Based Access Control**: Teachers can only access products based on their authority level (TeacType, CntrTyCd, deptCode)
- **Customer Management**: Register both parent and child information with search functionality
- **Product Catalog**: Hierarchical product selection (Grade → Management Type → Pad Type → Model)
- **Order Management**: View and manage orders with automatic order number generation

### Target Users

- **Teachers**: Primary users who register orders for customers
- **Customers**: Parents and children whose information is managed in the system

### Business Context

The system serves educational institutions where teachers act as intermediaries to register orders for educational products/services on behalf of parents and students. Different teacher roles have access to different product tiers based on their authority level within the organization.