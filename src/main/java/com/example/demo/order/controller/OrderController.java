package com.example.demo.order.controller;

import com.example.demo.order.OrderSession;
import com.example.demo.order.dto.CustomerInfoDto;
import com.example.demo.order.dto.ModelSelectionDto;
import com.example.demo.order.service.OrderService;
import com.example.demo.product.Product;
import com.example.demo.product.service.ProductService;
import com.example.demo.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/order")
public class OrderController {
    
    private final OrderService orderService;
    private final ProductService productService;
    
    public OrderController(OrderService orderService, ProductService productService) {
        this.orderService = orderService;
        this.productService = productService;
    }
    
    /**
     * 1단계: 고객 등록 페이지
     */
    @GetMapping("/step1")
    public String step1(Model model, HttpSession httpSession) {
        // 현재 로그인한 교사 정보 가져오기
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return "redirect:/login";
        }
        
        // 새로운 주문 세션 시작
        String sessionId = orderService.startOrderSession(currentTeacher.getEmpNo());
        httpSession.setAttribute("orderSessionId", sessionId);
        
        // 기존 세션 데이터가 있는지 확인
        Optional<OrderSession> existingSession = orderService.getOrderSession(sessionId, currentTeacher.getEmpNo());
        
        CustomerInfoDto customerInfoDto = new CustomerInfoDto();
        if (existingSession.isPresent() && existingSession.get().getCustomerInfo() != null) {
            // 기존 데이터로 폼 채우기
            OrderSession.CustomerInfo customerInfo = existingSession.get().getCustomerInfo();
            customerInfoDto.setParentName(customerInfo.getParentName());
            customerInfoDto.setParentPhone(customerInfo.getParentPhone());
            customerInfoDto.setParentEmail(customerInfo.getParentEmail());
            customerInfoDto.setAddress(customerInfo.getAddress());
            customerInfoDto.setChildName(customerInfo.getChildName());
            customerInfoDto.setChildGrade(customerInfo.getChildGrade());
            customerInfoDto.setSchool(customerInfo.getSchool());
        }
        
        model.addAttribute("customerInfo", customerInfoDto);
        model.addAttribute("teacher", currentTeacher);
        
        return "order/step1";
    }
    
    /**
     * 1단계: 고객 정보 저장
     */
    @PostMapping("/step1")
    public String saveStep1(@Valid @ModelAttribute("customerInfo") CustomerInfoDto customerInfoDto,
                           BindingResult bindingResult,
                           HttpSession httpSession,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        // 현재 로그인한 교사 정보 가져오기
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return "redirect:/login";
        }
        
        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            model.addAttribute("teacher", currentTeacher);
            return "order/step1";
        }
        
        try {
            // 세션 ID 가져오기
            String sessionId = (String) httpSession.getAttribute("orderSessionId");
            if (sessionId == null) {
                redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 다시 시작해주세요.");
                return "redirect:/order/step1";
            }
            
            // DTO를 CustomerInfo로 변환
            OrderSession.CustomerInfo customerInfo = new OrderSession.CustomerInfo();
            customerInfo.setParentName(customerInfoDto.getParentName());
            customerInfo.setParentPhone(customerInfoDto.getParentPhone());
            customerInfo.setParentEmail(customerInfoDto.getParentEmail());
            customerInfo.setAddress(customerInfoDto.getAddress());
            customerInfo.setChildName(customerInfoDto.getChildName());
            customerInfo.setChildGrade(customerInfoDto.getChildGrade());
            customerInfo.setSchool(customerInfoDto.getSchool());
            
            // 고객 정보 저장
            orderService.saveCustomerInfo(sessionId, currentTeacher.getEmpNo(), customerInfo);
            
            redirectAttributes.addFlashAttribute("success", "고객 정보가 저장되었습니다.");
            return "redirect:/order/step2";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "고객 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
            model.addAttribute("teacher", currentTeacher);
            return "order/step1";
        }
    }
    
    /**
     * 2단계: 모델 선택 페이지
     */
    @GetMapping("/step2")
    public String step2(Model model, HttpSession httpSession, RedirectAttributes redirectAttributes) {
        // 현재 로그인한 교사 정보 가져오기
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return "redirect:/login";
        }
        
        // 세션 ID 확인
        String sessionId = (String) httpSession.getAttribute("orderSessionId");
        if (sessionId == null) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 1단계부터 다시 시작해주세요.");
            return "redirect:/order/step1";
        }
        
        // 주문 세션 조회
        Optional<OrderSession> orderSessionOpt = orderService.getOrderSession(sessionId, currentTeacher.getEmpNo());
        if (orderSessionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "주문 세션을 찾을 수 없습니다. 1단계부터 다시 시작해주세요.");
            return "redirect:/order/step1";
        }
        
        OrderSession orderSession = orderSessionOpt.get();
        
        // 1단계 완료 여부 확인
        if (orderSession.getCustomerInfo() == null) {
            redirectAttributes.addFlashAttribute("error", "고객 정보를 먼저 입력해주세요.");
            return "redirect:/order/step1";
        }
        
        // 기존 모델 선택 정보가 있는지 확인
        ModelSelectionDto modelSelectionDto = new ModelSelectionDto();
        if (orderSession.getSelectedGrade() != null) {
            modelSelectionDto.setGrade(orderSession.getSelectedGrade());
            modelSelectionDto.setManagementType(orderSession.getSelectedManagementType());
            modelSelectionDto.setPadType(orderSession.getSelectedPadType());
            modelSelectionDto.setProductId(orderSession.getSelectedProductId());
            modelSelectionDto.setQuantity(orderSession.getQuantity());
        }
        
        // 학년 옵션 조회
        List<String> gradeOptions = productService.getGradeOptions();
        
        // 관리 유형 옵션 조회
        List<String> managementTypes = productService.getManagementTypes();
        
        // 교사가 접근 가능한 패드 종류 조회
        List<String> padTypes = productService.getPadTypesByTeacher(currentTeacher);
        
        model.addAttribute("modelSelection", modelSelectionDto);
        model.addAttribute("gradeOptions", gradeOptions);
        model.addAttribute("managementTypes", managementTypes);
        model.addAttribute("padTypes", padTypes);
        model.addAttribute("teacher", currentTeacher);
        model.addAttribute("customerInfo", orderSession.getCustomerInfo());
        
        return "order/step2";
    }
    
    /**
     * 2단계: 모델 선택 정보 저장
     */
    @PostMapping("/step2")
    public String saveStep2(@Valid @ModelAttribute("modelSelection") ModelSelectionDto modelSelectionDto,
                           BindingResult bindingResult,
                           HttpSession httpSession,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        
        // 현재 로그인한 교사 정보 가져오기
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return "redirect:/login";
        }
        
        // 세션 ID 확인
        String sessionId = (String) httpSession.getAttribute("orderSessionId");
        if (sessionId == null) {
            redirectAttributes.addFlashAttribute("error", "세션이 만료되었습니다. 1단계부터 다시 시작해주세요.");
            return "redirect:/order/step1";
        }
        
        // 유효성 검사 실패 시
        if (bindingResult.hasErrors()) {
            // 필요한 데이터 다시 로드
            List<String> gradeOptions = productService.getGradeOptions();
            List<String> managementTypes = productService.getManagementTypes();
            List<String> padTypes = productService.getPadTypesByTeacher(currentTeacher);
            
            model.addAttribute("gradeOptions", gradeOptions);
            model.addAttribute("managementTypes", managementTypes);
            model.addAttribute("padTypes", padTypes);
            model.addAttribute("teacher", currentTeacher);
            
            return "order/step2";
        }
        
        try {
            // 선택된 상품 정보 확인
            Optional<Product> productOpt = productService.findById(modelSelectionDto.getProductId());
            if (productOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "선택된 모델을 찾을 수 없습니다.");
                return "redirect:/order/step2";
            }
            
            Product selectedProduct = productOpt.get();
            
            // 교사의 상품 접근 권한 확인
            if (!productService.isProductAccessible(currentTeacher, selectedProduct)) {
                redirectAttributes.addFlashAttribute("error", "선택하신 모델에 대한 접근 권한이 없습니다.");
                return "redirect:/order/step2";
            }
            
            // 모델 선택 정보 저장
            orderService.saveModelSelection(sessionId, currentTeacher.getEmpNo(),
                    modelSelectionDto.getGrade(),
                    modelSelectionDto.getManagementType(),
                    modelSelectionDto.getPadType(),
                    modelSelectionDto.getProductId(),
                    modelSelectionDto.getQuantity());
            
            redirectAttributes.addFlashAttribute("success", "모델 선택 정보가 저장되었습니다.");
            return "redirect:/order/step3";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "모델 선택 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
            return "redirect:/order/step2";
        }
    }
    
    /**
     * 주문 취소 (세션 삭제)
     */
    @PostMapping("/cancel")
    public String cancelOrder(HttpSession httpSession, RedirectAttributes redirectAttributes) {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher != null) {
            String sessionId = (String) httpSession.getAttribute("orderSessionId");
            if (sessionId != null) {
                orderService.deleteOrderSession(sessionId, currentTeacher.getEmpNo());
                httpSession.removeAttribute("orderSessionId");
            }
        }
        
        redirectAttributes.addFlashAttribute("info", "주문이 취소되었습니다.");
        return "redirect:/";
    }
    
    /**
     * 현재 로그인한 교사 정보 가져오기
     */
    private User getCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}