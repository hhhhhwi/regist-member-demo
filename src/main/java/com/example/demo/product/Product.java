package com.example.demo.product;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 10)
    private String grade;           // 학년
    
    @Column(nullable = false, length = 20)
    private String managementType;  // 관리/비관리
    
    @Column(nullable = false, length = 50)
    private String padType;         // 패드 종류
    
    @Column(nullable = false, length = 100)
    private String modelName;       // 모델명
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyFee;  // 월회비
    
    @Column(nullable = false)
    private boolean active = true;
    
    // 권한 제한이 있는 상품인지 여부
    @Column(nullable = false)
    private boolean restrictedAccess = false;
    
    // 제한된 상품에 접근 가능한 교사 유형들 (JSON 형태로 저장)
    @Column(length = 500)
    private String allowedTeacherTypes; // 예: "CHRG,PRLSN" 또는 "T,M"
}