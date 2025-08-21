package com.example.demo.product.controller;

import com.example.demo.product.Product;
import com.example.demo.product.service.ProductService;
import com.example.demo.user.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ProductApiController {
    
    private final ProductService productService;
    
    public ProductApiController(ProductService productService) {
        this.productService = productService;
    }
    
    /**
     * 관리/비관리 옵션 조회
     */
    @GetMapping("/management-types")
    public ResponseEntity<List<String>> getManagementTypes() {
        List<String> managementTypes = productService.getManagementTypes();
        return ResponseEntity.ok(managementTypes);
    }
    
    /**
     * 교사 권한별 패드 종류 조회
     */
    @GetMapping("/pad-types")
    public ResponseEntity<List<String>> getPadTypes() {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }
        
        List<String> padTypes = productService.getPadTypesByTeacher(currentTeacher);
        return ResponseEntity.ok(padTypes);
    }
    
    /**
     * 패드별 모델 목록 조회
     */
    @GetMapping("/models")
    public ResponseEntity<List<Map<String, Object>>> getModels(
            @RequestParam String grade,
            @RequestParam String managementType,
            @RequestParam String padType) {
        
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }
        
        // 입력 파라미터 검증
        if (grade == null || grade.trim().isEmpty() ||
            managementType == null || managementType.trim().isEmpty() ||
            padType == null || padType.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // 조건에 맞는 모델 조회
            List<Product> models = getModelsByConditions(currentTeacher, grade.trim(), managementType.trim(), padType.trim());
            
            // 응답 데이터 변환
            List<Map<String, Object>> modelData = models.stream()
                    .map(this::convertToModelData)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(modelData);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 모델 가격 조회
     */
    @GetMapping("/model-price")
    public ResponseEntity<Map<String, Object>> getModelPrice(@RequestParam Long productId) {
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }
        
        if (productId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            
            // 교사의 접근 권한 확인
            if (!productService.isProductAccessible(currentTeacher, product)) {
                return ResponseEntity.status(403).build();
            }
            
            Map<String, Object> priceData = new HashMap<>();
            priceData.put("productId", product.getId());
            priceData.put("modelName", product.getModelName());
            priceData.put("monthlyFee", product.getMonthlyFee());
            priceData.put("formattedPrice", formatPrice(product.getMonthlyFee()));
            
            return ResponseEntity.ok(priceData);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    /**
     * 총액 계산
     */
    @GetMapping("/calculate-total")
    public ResponseEntity<Map<String, Object>> calculateTotal(
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        
        User currentTeacher = getCurrentTeacher();
        if (currentTeacher == null) {
            return ResponseEntity.status(401).build();
        }
        
        if (productId == null || quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            Optional<Product> productOpt = productService.findById(productId);
            if (productOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Product product = productOpt.get();
            
            // 교사의 접근 권한 확인
            if (!productService.isProductAccessible(currentTeacher, product)) {
                return ResponseEntity.status(403).build();
            }
            
            BigDecimal totalAmount = productService.calculateTotalAmount(productId, quantity);
            
            Map<String, Object> totalData = new HashMap<>();
            totalData.put("productId", productId);
            totalData.put("quantity", quantity);
            totalData.put("monthlyFee", product.getMonthlyFee());
            totalData.put("totalAmount", totalAmount);
            totalData.put("formattedTotal", formatPrice(totalAmount));
            
            return ResponseEntity.ok(totalData);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    // === Private Helper Methods ===
    
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
    
    /**
     * 조건에 맞는 모델 조회
     */
    private List<Product> getModelsByConditions(User teacher, String grade, String managementType, String padType) {
        return productService.getModelsByConditions(teacher, grade, managementType, padType);
    }
    
    /**
     * Product를 API 응답용 Map으로 변환
     */
    private Map<String, Object> convertToModelData(Product product) {
        Map<String, Object> modelData = new HashMap<>();
        modelData.put("id", product.getId());
        modelData.put("modelName", product.getModelName());
        modelData.put("grade", product.getGrade());
        modelData.put("managementType", product.getManagementType());
        modelData.put("padType", product.getPadType());
        modelData.put("monthlyFee", product.getMonthlyFee());
        modelData.put("formattedPrice", formatPrice(product.getMonthlyFee()));
        modelData.put("active", product.isActive());
        return modelData;
    }
    
    /**
     * 가격 포맷팅 (원화 표시)
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0원";
        }
        return String.format("%,d원", price.intValue());
    }
}