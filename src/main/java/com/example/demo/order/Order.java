package com.example.demo.order;

import com.example.demo.customer.Customer;
import com.example.demo.product.Product;
import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String orderNumber;     // 주문번호
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_emp_no", nullable = false)
    private User teacher;           // 주문 등록 교사
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;      // 고객
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;        // 선택된 상품
    
    @Column(nullable = false)
    private Integer quantity;       // 수량
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount; // 총액
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;     // 주문 상태
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (orderNumber == null) {
            generateOrderNumber();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private void generateOrderNumber() {
        // 주문번호 생성 로직: ORD + 현재시간(yyyyMMddHHmmss) + 랜덤 3자리
        String timestamp = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
                .format(LocalDateTime.now());
        String randomSuffix = String.format("%03d", (int)(Math.random() * 1000));
        this.orderNumber = "ORD" + timestamp + randomSuffix;
    }
    
    public enum OrderStatus {
        PENDING,    // 대기중
        CONFIRMED,  // 확인됨
        PROCESSING, // 처리중
        COMPLETED,  // 완료
        CANCELLED   // 취소됨
    }
}