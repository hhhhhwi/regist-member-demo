package com.example.demo.order.service;

import com.example.demo.customer.Customer;
import com.example.demo.customer.repository.CustomerRepository;
import com.example.demo.order.Order;
import com.example.demo.order.OrderSession;
import com.example.demo.order.repository.OrderRepository;
import com.example.demo.order.repository.OrderSessionRepository;
import com.example.demo.product.Product;
import com.example.demo.product.repository.ProductRepository;
import com.example.demo.product.service.ProductService;
import com.example.demo.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderSessionRepository orderSessionRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    
    public OrderService(OrderRepository orderRepository,
                       OrderSessionRepository orderSessionRepository,
                       CustomerRepository customerRepository,
                       ProductRepository productRepository,
                       ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderSessionRepository = orderSessionRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.productService = productService;
    }
    
    /**
     * 새로운 주문 세션 시작
     */
    @Transactional
    public String startOrderSession(String teacherEmpNo) {
        if (teacherEmpNo == null || teacherEmpNo.trim().isEmpty()) {
            throw new IllegalArgumentException("교사 번호가 필요합니다.");
        }
        
        // 기존 세션이 있으면 삭제
        orderSessionRepository.deleteByTeacherEmpNo(teacherEmpNo);
        
        // 새로운 세션 생성
        OrderSession session = new OrderSession();
        session.setSessionId(generateSessionId());
        session.setTeacherEmpNo(teacherEmpNo);
        session.onCreate();
        
        orderSessionRepository.save(session);
        return session.getSessionId();
    }
    
    /**
     * 1단계: 고객 정보 저장
     */
    @Transactional
    public void saveCustomerInfo(String sessionId, String teacherEmpNo, OrderSession.CustomerInfo customerInfo) {
        OrderSession session = getValidSession(sessionId, teacherEmpNo);
        
        if (customerInfo == null) {
            throw new IllegalArgumentException("고객 정보가 필요합니다.");
        }
        
        // 고객 정보 유효성 검사
        validateCustomerInfo(customerInfo);
        
        session.setCustomerInfo(customerInfo);
        orderSessionRepository.save(session);
    }
    
    /**
     * 2단계: 모델 선택 정보 저장
     */
    @Transactional
    public void saveModelSelection(String sessionId, String teacherEmpNo, 
                                 String grade, String managementType, String padType, 
                                 Long productId, Integer quantity) {
        OrderSession session = getValidSession(sessionId, teacherEmpNo);
        
        // 모델 선택 정보 유효성 검사
        validateModelSelection(grade, managementType, padType, productId, quantity);
        
        session.setSelectedGrade(grade);
        session.setSelectedManagementType(managementType);
        session.setSelectedPadType(padType);
        session.setSelectedProductId(productId);
        session.setQuantity(quantity);
        
        orderSessionRepository.save(session);
    }
    
    /**
     * 주문 세션 조회
     */
    public Optional<OrderSession> getOrderSession(String sessionId, String teacherEmpNo) {
        if (sessionId == null || teacherEmpNo == null) {
            return Optional.empty();
        }
        
        Optional<OrderSession> session = orderSessionRepository.findBySessionIdAndTeacherEmpNo(sessionId, teacherEmpNo);
        
        // 만료된 세션 확인
        if (session.isPresent() && session.get().isExpired()) {
            deleteOrderSession(sessionId, teacherEmpNo);
            return Optional.empty();
        }
        
        return session;
    }
    
    /**
     * 3단계: 최종 주문 생성
     */
    @Transactional
    public Order createOrder(String sessionId, String teacherEmpNo, User teacher) {
        OrderSession session = getValidSession(sessionId, teacherEmpNo);
        
        // 세션 데이터 완성도 확인
        validateCompleteSession(session);
        
        // 고객 정보로 Customer 엔티티 생성 또는 조회
        Customer customer = createOrFindCustomer(session.getCustomerInfo());
        
        // 상품 정보 조회 및 권한 확인
        Product product = getValidProduct(session.getSelectedProductId(), teacher);
        
        // 주문 생성
        Order order = new Order();
        order.setTeacher(teacher);
        order.setCustomer(customer);
        order.setProduct(product);
        order.setQuantity(session.getQuantity());
        order.setTotalAmount(calculateTotalAmount(product, session.getQuantity()));
        order.setStatus(Order.OrderStatus.PENDING);
        order.onCreate();
        
        Order savedOrder = orderRepository.save(order);
        
        // 세션 삭제
        deleteOrderSession(sessionId, teacherEmpNo);
        
        return savedOrder;
    }
    
    /**
     * 교사별 주문 목록 조회 (페이징)
     */
    public Page<Order> getOrdersByTeacher(User teacher, Pageable pageable) {
        if (teacher == null) {
            throw new IllegalArgumentException("교사 정보가 필요합니다.");
        }
        return orderRepository.findByTeacherOrderByCreatedAtDesc(teacher, pageable);
    }
    
    /**
     * 교사별 주문 목록 조회
     */
    public List<Order> getOrdersByTeacher(User teacher) {
        if (teacher == null) {
            throw new IllegalArgumentException("교사 정보가 필요합니다.");
        }
        return orderRepository.findByTeacherOrderByCreatedAtDesc(teacher);
    }
    
    /**
     * 교사 사번으로 주문 목록 조회
     */
    public List<Order> getOrdersByTeacher(String teacherEmpNo) {
        if (teacherEmpNo == null || teacherEmpNo.trim().isEmpty()) {
            throw new IllegalArgumentException("교사 사번이 필요합니다.");
        }
        return orderRepository.findByTeacherEmpNoOrderByCreatedAtDesc(teacherEmpNo);
    }
    
    /**
     * 주문 상세 조회 (권한 확인)
     */
    public Optional<Order> getOrderDetail(Long orderId, User teacher) {
        if (orderId == null || teacher == null) {
            return Optional.empty();
        }
        return orderRepository.findByIdAndTeacher(orderId, teacher);
    }
    
    /**
     * 주문번호로 조회 (권한 확인)
     */
    public Optional<Order> getOrderByNumber(String orderNumber, User teacher) {
        if (orderNumber == null || teacher == null) {
            return Optional.empty();
        }
        
        Optional<Order> order = orderRepository.findByOrderNumber(orderNumber);
        if (order.isPresent() && order.get().getTeacher().getEmpNo().equals(teacher.getEmpNo())) {
            return order;
        }
        return Optional.empty();
    }
    
    /**
     * 교사별 주문 개수 조회
     */
    public long getOrderCountByTeacher(User teacher) {
        if (teacher == null) {
            return 0;
        }
        return orderRepository.countByTeacher(teacher);
    }
    
    /**
     * 만료된 세션 정리
     */
    @Transactional
    public int cleanupExpiredSessions() {
        return orderSessionRepository.deleteExpiredSessions(LocalDateTime.now());
    }
    
    /**
     * 주문 세션 삭제
     */
    @Transactional
    public void deleteOrderSession(String sessionId, String teacherEmpNo) {
        Optional<OrderSession> session = orderSessionRepository.findBySessionIdAndTeacherEmpNo(sessionId, teacherEmpNo);
        session.ifPresent(orderSessionRepository::delete);
    }
    
    // === Private Helper Methods ===
    
    private String generateSessionId() {
        return "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    private OrderSession getValidSession(String sessionId, String teacherEmpNo) {
        Optional<OrderSession> session = getOrderSession(sessionId, teacherEmpNo);
        if (session.isEmpty()) {
            throw new IllegalArgumentException("유효하지 않거나 만료된 세션입니다.");
        }
        return session.get();
    }
    
    private void validateCustomerInfo(OrderSession.CustomerInfo customerInfo) {
        if (customerInfo.getParentName() == null || customerInfo.getParentName().trim().isEmpty()) {
            throw new IllegalArgumentException("부모 이름은 필수입니다.");
        }
        if (customerInfo.getParentPhone() == null || customerInfo.getParentPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("부모 연락처는 필수입니다.");
        }
        if (customerInfo.getChildName() == null || customerInfo.getChildName().trim().isEmpty()) {
            throw new IllegalArgumentException("자녀 이름은 필수입니다.");
        }
        if (customerInfo.getChildGrade() == null || customerInfo.getChildGrade().trim().isEmpty()) {
            throw new IllegalArgumentException("자녀 학년은 필수입니다.");
        }
    }
    
    private void validateModelSelection(String grade, String managementType, String padType, Long productId, Integer quantity) {
        if (grade == null || grade.trim().isEmpty()) {
            throw new IllegalArgumentException("학년 선택은 필수입니다.");
        }
        if (managementType == null || managementType.trim().isEmpty()) {
            throw new IllegalArgumentException("관리 유형 선택은 필수입니다.");
        }
        if (padType == null || padType.trim().isEmpty()) {
            throw new IllegalArgumentException("패드 종류 선택은 필수입니다.");
        }
        if (productId == null) {
            throw new IllegalArgumentException("모델 선택은 필수입니다.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }
    
    private void validateCompleteSession(OrderSession session) {
        if (session.getCustomerInfo() == null) {
            throw new IllegalArgumentException("고객 정보가 입력되지 않았습니다.");
        }
        if (session.getSelectedProductId() == null) {
            throw new IllegalArgumentException("모델이 선택되지 않았습니다.");
        }
        if (session.getQuantity() == null || session.getQuantity() <= 0) {
            throw new IllegalArgumentException("수량이 설정되지 않았습니다.");
        }
    }
    
    private Customer createOrFindCustomer(OrderSession.CustomerInfo customerInfo) {
        // 기존 고객 검색 (부모 이름 + 연락처)
        boolean exists = customerRepository.existsByParentNameAndParentPhone(
            customerInfo.getParentName(), customerInfo.getParentPhone());
        
        if (exists) {
            // 기존 고객 조회
            List<Customer> existingCustomers = customerRepository.findByParentNameContainingIgnoreCase(customerInfo.getParentName());
            Optional<Customer> matchingCustomer = existingCustomers.stream()
                .filter(c -> c.getParentPhone().equals(customerInfo.getParentPhone()))
                .findFirst();
            
            if (matchingCustomer.isPresent()) {
                return matchingCustomer.get();
            }
        }
        
        // 새 고객 생성
        Customer customer = new Customer();
        customer.setParentName(customerInfo.getParentName());
        customer.setParentPhone(customerInfo.getParentPhone());
        customer.setParentEmail(customerInfo.getParentEmail());
        customer.setAddress(customerInfo.getAddress());
        customer.setChildName(customerInfo.getChildName());
        customer.setChildGrade(customerInfo.getChildGrade());
        customer.setSchool(customerInfo.getSchool());
        customer.onCreate();
        
        return customerRepository.save(customer);
    }
    
    private Product getValidProduct(Long productId, User teacher) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }
        
        if (!productService.isProductAccessible(teacher, product.get())) {
            throw new IllegalArgumentException("해당 상품에 대한 접근 권한이 없습니다.");
        }
        
        return product.get();
    }
    
    private BigDecimal calculateTotalAmount(Product product, Integer quantity) {
        return product.getMonthlyFee().multiply(new BigDecimal(quantity));
    }
}