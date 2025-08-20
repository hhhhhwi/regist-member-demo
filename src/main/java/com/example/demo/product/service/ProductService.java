package com.example.demo.product.service;

import com.example.demo.product.Product;
import com.example.demo.product.repository.ProductRepository;
import com.example.demo.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    /**
     * 학년 옵션 조회
     */
    public List<String> getGradeOptions() {
        return Arrays.asList("초1", "초2", "초3", "초4", "초5", "초6", "중1", "중2", "중3");
    }
    
    /**
     * 관리 유형 옵션 조회
     */
    public List<String> getManagementTypes() {
        return Arrays.asList("관리", "비관리");
    }
    
    /**
     * 교사 권한에 따른 패드 종류 조회
     */
    public List<String> getPadTypesByTeacher(User teacher) {
        if (teacher == null) {
            return List.of();
        }
        
        // 모든 패드 종류 조회
        List<String> allPadTypes = productRepository.findDistinctPadTypes();
        
        // 교사가 접근 가능한 패드 종류만 필터링
        return allPadTypes.stream()
                .filter(padType -> hasAccessToPadType(teacher, padType))
                .collect(Collectors.toList());
    }
    
    /**
     * 패드 종류별 모델 조회 (교사 권한 고려)
     */
    public List<Product> getModelsByPadType(User teacher, String padType) {
        if (teacher == null || padType == null || padType.trim().isEmpty()) {
            return List.of();
        }
        
        List<Product> allModels = productRepository.findByPadTypeAndActiveTrue(padType.trim());
        
        // 교사가 접근 가능한 모델만 필터링
        return allModels.stream()
                .filter(product -> isProductAccessible(teacher, product))
                .collect(Collectors.toList());
    }
    
    /**
     * 학년별 상품 조회 (교사 권한 고려)
     */
    public List<Product> getProductsByGrade(User teacher, String grade) {
        if (teacher == null || grade == null || grade.trim().isEmpty()) {
            return List.of();
        }
        
        List<Product> allProducts = productRepository.findByGradeAndActiveTrue(grade.trim());
        
        // 교사가 접근 가능한 상품만 필터링
        return allProducts.stream()
                .filter(product -> isProductAccessible(teacher, product))
                .collect(Collectors.toList());
    }
    
    /**
     * 관리 유형별 상품 조회 (교사 권한 고려)
     */
    public List<Product> getProductsByManagementType(User teacher, String managementType) {
        if (teacher == null || managementType == null || managementType.trim().isEmpty()) {
            return List.of();
        }
        
        List<Product> allProducts = productRepository.findByManagementTypeAndActiveTrue(managementType.trim());
        
        // 교사가 접근 가능한 상품만 필터링
        return allProducts.stream()
                .filter(product -> isProductAccessible(teacher, product))
                .collect(Collectors.toList());
    }
    
    /**
     * 상품 ID로 조회
     */
    public Optional<Product> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return productRepository.findById(id);
    }
    
    /**
     * 모델 가격 조회
     */
    public BigDecimal getModelPrice(Long productId) {
        if (productId == null) {
            return BigDecimal.ZERO;
        }
        
        Optional<Product> product = productRepository.findById(productId);
        return product.map(Product::getMonthlyFee).orElse(BigDecimal.ZERO);
    }
    
    /**
     * 총액 계산 (월회비 × 수량)
     */
    public BigDecimal calculateTotalAmount(Long productId, Integer quantity) {
        if (productId == null || quantity == null || quantity <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal monthlyFee = getModelPrice(productId);
        return monthlyFee.multiply(new BigDecimal(quantity));
    }
    
    /**
     * 교사의 상품 접근 권한 확인
     */
    public boolean isProductAccessible(User teacher, Product product) {
        if (teacher == null || product == null) {
            return false;
        }
        
        // 비활성화된 상품은 접근 불가
        if (!product.isActive()) {
            return false;
        }
        
        // 제한이 없는 상품은 모든 교사가 접근 가능
        if (!product.isRestrictedAccess()) {
            return true;
        }
        
        // 제한된 상품의 경우 allowedTeacherTypes 확인
        String allowedTypes = product.getAllowedTeacherTypes();
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return false;
        }
        
        // 교사의 CntrTyCd 또는 TeacType이 허용 목록에 있는지 확인
        User.CntrTyCd cntrTyCd = teacher.getCntrTyCd();
        User.TeacType teacType = teacher.getTeacType();
        
        List<String> allowedTypeList = Arrays.asList(allowedTypes.split(","));
        
        return (cntrTyCd != null && allowedTypeList.contains(cntrTyCd.name())) ||
               (teacType != null && allowedTypeList.contains(teacType.name()));
    }
    
    /**
     * 교사가 특정 패드 종류에 접근 가능한지 확인
     */
    private boolean hasAccessToPadType(User teacher, String padType) {
        // 해당 패드 종류의 상품 중 하나라도 접근 가능하면 true
        List<Product> products = productRepository.findByPadTypeAndActiveTrue(padType);
        return products.stream().anyMatch(product -> isProductAccessible(teacher, product));
    }
    
    /**
     * 교사별 접근 가능한 모든 상품 조회
     */
    public List<Product> getAccessibleProducts(User teacher) {
        if (teacher == null) {
            return List.of();
        }
        
        List<Product> allProducts = productRepository.findByActiveTrue();
        
        return allProducts.stream()
                .filter(product -> isProductAccessible(teacher, product))
                .collect(Collectors.toList());
    }
    
    /**
     * 교사의 권한 레벨 확인 (높은 권한부터 낮은 권한 순)
     */
    public String getTeacherAuthLevel(User teacher) {
        if (teacher == null) {
            return "NONE";
        }
        
        User.CntrTyCd cntrTyCd = teacher.getCntrTyCd();
        if (cntrTyCd != null) {
            switch (cntrTyCd) {
                case CHRG:
                case PRLSN:
                    return "PREMIUM";
                case CNSTN:
                    return "ADVANCED";
                case SPRT:
                case UNTY:
                    return "BASIC";
            }
        }
        
        return "BASIC";
    }
    
    /**
     * 상품 정보 유효성 검사
     */
    public boolean isValidProduct(Product product) {
        if (product == null) {
            return false;
        }
        
        return product.getGrade() != null && !product.getGrade().trim().isEmpty() &&
               product.getManagementType() != null && !product.getManagementType().trim().isEmpty() &&
               product.getPadType() != null && !product.getPadType().trim().isEmpty() &&
               product.getModelName() != null && !product.getModelName().trim().isEmpty() &&
               product.getMonthlyFee() != null && product.getMonthlyFee().compareTo(BigDecimal.ZERO) > 0;
    }
}