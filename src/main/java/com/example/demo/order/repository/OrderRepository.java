package com.example.demo.order.repository;

import com.example.demo.order.Order;
import com.example.demo.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 교사별 주문 목록 조회 (페이징)
     */
    Page<Order> findByTeacherOrderByCreatedAtDesc(User teacher, Pageable pageable);
    
    /**
     * 교사별 주문 목록 조회
     */
    List<Order> findByTeacherOrderByCreatedAtDesc(User teacher);
    
    /**
     * 주문번호로 조회
     */
    Optional<Order> findByOrderNumber(String orderNumber);
    
    /**
     * 교사와 주문 ID로 조회 (권한 검사용)
     */
    Optional<Order> findByIdAndTeacher(Long id, User teacher);
    
    /**
     * 교사별 특정 기간 주문 조회
     */
    @Query("SELECT o FROM Order o WHERE o.teacher = :teacher AND o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<Order> findByTeacherAndDateRange(@Param("teacher") User teacher, 
                                         @Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);
    
    /**
     * 교사별 주문 상태별 조회
     */
    List<Order> findByTeacherAndStatusOrderByCreatedAtDesc(User teacher, Order.OrderStatus status);
    
    /**
     * 교사별 주문 개수 조회
     */
    long countByTeacher(User teacher);
}