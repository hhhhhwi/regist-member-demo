package com.example.demo.customer.controller;

import com.example.demo.customer.Customer;
import com.example.demo.customer.service.CustomerService;
import com.example.demo.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final CustomerService customerService;
    private final com.example.demo.user.repository.UserRepository userRepository;

    public CustomerController(CustomerService customerService,
            com.example.demo.user.repository.UserRepository userRepository) {
        this.customerService = customerService;
        this.userRepository = userRepository;
    }

    /**
     * 고객 검색 팝업 페이지
     */
    @GetMapping("/search")
    public String searchPopup(Model model) {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return "redirect:/login";
        }

        model.addAttribute("teacher", currentTeacher);
        return "customer/search-popup";
    }

    /**
     * 고객 검색 API (Ajax)
     */
    @PostMapping("/search")
    @ResponseBody
    public ResponseEntity<List<Customer>> searchCustomers(@RequestParam("keyword") String keyword) {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<Customer> customers = customerService.searchCustomers(keyword);
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 고객 상세 정보 조회 API (Ajax)
     */
    @GetMapping("/detail/{id}")
    @ResponseBody
    public ResponseEntity<Customer> getCustomerDetail(@PathVariable Long id) {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            return customerService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 현재 로그인한 교사 정보 가져오기
     */
    private User getCurrentTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String empNo = (String) authentication.getPrincipal();
            return userRepository.findById(empNo).orElse(null);
        }
        return null;
    }
}