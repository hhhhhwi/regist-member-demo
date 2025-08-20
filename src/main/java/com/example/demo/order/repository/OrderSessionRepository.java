package com.example.demo.order.repository;

import com.example.demo.order.OrderSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderSessionRepository extends JpaRepository<OrderSession, String> {
    
    /**
     * 교사별 세션 조회
     */
    Optional<OrderSession> findByTeacherEmpNo(String teacherEmpNo);
    
    /**
     * 세션 ID와 교사 번호로 조회
     */
    Optional<OrderSession> findBySessionIdAndTeacherEmpNo(String sessionId, String teacherEmpNo);
    
    /**
     * 만료된 세션 삭제
     */
    @Modifying
    @Query("DELETE FROM OrderSession os WHERE os.expiresAt < :currentTime")
    int deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * 교사의 기존 세션 삭제
     */
    void deleteByTeacherEmpNo(String teacherEmpNo);
    
    /**
     * 만료된 세션 조회
     */
    @Query("SELECT os FROM OrderSession os WHERE os.expiresAt < :currentTime")
    java.util.List<OrderSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
}