package com.example.demo.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_session")
public class OrderSession {
    
    @Id
    @Column(length = 100)
    private String sessionId;
    
    @Column(nullable = false, length = 20)
    private String teacherEmpNo;
    
    // 1단계 정보 - 고객 정보
    @Embedded
    private CustomerInfo customerInfo;
    
    // 2단계 정보 - 모델 선택
    @Column(length = 10)
    private String selectedGrade;
    
    @Column(length = 20)
    private String selectedManagementType;
    
    @Column(length = 50)
    private String selectedPadType;
    
    private Long selectedProductId;
    
    private Integer quantity;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // 세션 만료 시간: 생성 후 2시간
        expiresAt = LocalDateTime.now().plusHours(2);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CustomerInfo {
        // 부모 정보
        @Column(length = 100)
        private String parentName;
        
        @Column(length = 20)
        private String parentPhone;
        
        @Column(length = 100)
        private String parentEmail;
        
        @Column(length = 200)
        private String address;
        
        // 자녀 정보
        @Column(length = 100)
        private String childName;
        
        @Column(length = 10)
        private String childGrade;
        
        @Column(length = 100)
        private String school;
    }
}