package com.example.demo.customer.service;

import com.example.demo.customer.Customer;
import com.example.demo.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    
    /**
     * 키워드로 고객 검색 (부모 이름, 자녀 이름, 연락처 통합 검색)
     */
    public List<Customer> searchCustomers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        return customerRepository.searchByKeyword(keyword.trim());
    }
    
    /**
     * 부모 이름으로 고객 검색
     */
    public List<Customer> searchByParentName(String parentName) {
        if (parentName == null || parentName.trim().isEmpty()) {
            return List.of();
        }
        return customerRepository.findByParentNameContainingIgnoreCase(parentName.trim());
    }
    
    /**
     * 자녀 이름으로 고객 검색
     */
    public List<Customer> searchByChildName(String childName) {
        if (childName == null || childName.trim().isEmpty()) {
            return List.of();
        }
        return customerRepository.findByChildNameContainingIgnoreCase(childName.trim());
    }
    
    /**
     * 연락처로 고객 검색
     */
    public List<Customer> searchByPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return List.of();
        }
        return customerRepository.findByParentPhoneContaining(phone.trim());
    }
    
    /**
     * 고객 ID로 조회
     */
    public Optional<Customer> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return customerRepository.findById(id);
    }
    
    /**
     * 고객 정보 저장
     */
    @Transactional
    public Customer saveCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("고객 정보가 null입니다.");
        }
        
        // 필수 정보 검증
        validateCustomer(customer);
        
        return customerRepository.save(customer);
    }
    
    /**
     * 중복 고객 확인 (부모 이름 + 연락처 기준)
     */
    public boolean isDuplicateCustomer(String parentName, String parentPhone) {
        if (parentName == null || parentPhone == null) {
            return false;
        }
        return customerRepository.existsByParentNameAndParentPhone(
            parentName.trim(), parentPhone.trim());
    }
    
    /**
     * 고객 정보 유효성 검사
     */
    private void validateCustomer(Customer customer) {
        if (customer.getParentName() == null || customer.getParentName().trim().isEmpty()) {
            throw new IllegalArgumentException("부모 이름은 필수입니다.");
        }
        
        if (customer.getParentPhone() == null || customer.getParentPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("부모 연락처는 필수입니다.");
        }
        
        if (customer.getChildName() == null || customer.getChildName().trim().isEmpty()) {
            throw new IllegalArgumentException("자녀 이름은 필수입니다.");
        }
        
        if (customer.getChildGrade() == null || customer.getChildGrade().trim().isEmpty()) {
            throw new IllegalArgumentException("자녀 학년은 필수입니다.");
        }
        
        // 연락처 형식 검증 (간단한 패턴)
        String phone = customer.getParentPhone().trim();
        if (!phone.matches("^\\d{2,3}-\\d{3,4}-\\d{4}$")) {
            throw new IllegalArgumentException("연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)");
        }
    }
    
    /**
     * 모든 고객 조회 (관리자용)
     */
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }
    
    /**
     * 고객 정보 업데이트
     */
    @Transactional
    public Customer updateCustomer(Customer customer) {
        if (customer == null || customer.getId() == null) {
            throw new IllegalArgumentException("업데이트할 고객 정보가 유효하지 않습니다.");
        }
        
        // 기존 고객 존재 확인
        Optional<Customer> existingCustomer = customerRepository.findById(customer.getId());
        if (existingCustomer.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 고객입니다.");
        }
        
        // 유효성 검사
        validateCustomer(customer);
        
        return customerRepository.save(customer);
    }
}