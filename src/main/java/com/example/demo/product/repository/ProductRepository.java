package com.example.demo.product.repository;

import com.example.demo.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 활성화된 상품만 조회
     */
    List<Product> findByActiveTrue();
    
    /**
     * 학년별 상품 조회
     */
    List<Product> findByGradeAndActiveTrue(String grade);
    
    /**
     * 관리 유형별 상품 조회
     */
    List<Product> findByManagementTypeAndActiveTrue(String managementType);
    
    /**
     * 패드 종류별 상품 조회
     */
    List<Product> findByPadTypeAndActiveTrue(String padType);
    
    /**
     * 제한 없는 상품 조회 (모든 교사가 접근 가능)
     */
    List<Product> findByActiveTrueAndRestrictedAccessFalse();
    
    /**
     * 제한된 상품 조회
     */
    List<Product> findByActiveTrueAndRestrictedAccessTrue();
    
    /**
     * 패드 종류 목록 조회 (활성화된 상품만)
     */
    @Query("SELECT DISTINCT p.padType FROM Product p WHERE p.active = true")
    List<String> findDistinctPadTypes();
}