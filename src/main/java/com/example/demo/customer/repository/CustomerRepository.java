package com.example.demo.customer.repository;

import com.example.demo.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    /**
     * 부모 이름으로 고객 검색
     */
    List<Customer> findByParentNameContainingIgnoreCase(String parentName);
    
    /**
     * 자녀 이름으로 고객 검색
     */
    List<Customer> findByChildNameContainingIgnoreCase(String childName);
    
    /**
     * 부모 연락처로 고객 검색
     */
    List<Customer> findByParentPhoneContaining(String parentPhone);
    
    /**
     * 부모 이름, 자녀 이름, 연락처로 통합 검색
     */
    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.parentName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.childName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "c.parentPhone LIKE CONCAT('%', :keyword, '%')")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);
    
    /**
     * 중복 고객 확인 (부모 이름 + 연락처)
     */
    boolean existsByParentNameAndParentPhone(String parentName, String parentPhone);
}