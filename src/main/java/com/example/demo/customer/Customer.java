package com.example.demo.customer;

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
@Table(name = "customer")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 부모 정보 (Parent Information)
    @Column(nullable = false, length = 100)
    private String parentName;
    
    @Column(nullable = false, length = 20)
    private String parentPhone;
    
    @Column(length = 100)
    private String parentEmail;
    
    @Column(length = 200)
    private String address;
    
    // 자녀 정보 (Child Information)
    @Column(nullable = false, length = 100)
    private String childName;
    
    @Column(nullable = false, length = 10)
    private String childGrade;
    
    @Column(length = 100)
    private String school;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}