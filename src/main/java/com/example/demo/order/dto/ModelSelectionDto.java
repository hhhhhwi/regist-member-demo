package com.example.demo.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ModelSelectionDto {
    
    @NotBlank(message = "학년을 선택해주세요")
    private String grade;
    
    @NotBlank(message = "관리 유형을 선택해주세요")
    private String managementType;
    
    @NotBlank(message = "패드 종류를 선택해주세요")
    private String padType;
    
    @NotNull(message = "모델을 선택해주세요")
    private Long productId;
    
    @NotNull(message = "수량을 입력해주세요")
    @Min(value = 1, message = "수량은 1 이상이어야 합니다")
    private Integer quantity;
    
    // 기본 생성자
    public ModelSelectionDto() {
    }
    
    // 전체 생성자
    public ModelSelectionDto(String grade, String managementType, String padType, Long productId, Integer quantity) {
        this.grade = grade;
        this.managementType = managementType;
        this.padType = padType;
        this.productId = productId;
        this.quantity = quantity;
    }
    
    // Getter and Setter methods
    public String getGrade() {
        return grade;
    }
    
    public void setGrade(String grade) {
        this.grade = grade;
    }
    
    public String getManagementType() {
        return managementType;
    }
    
    public void setManagementType(String managementType) {
        this.managementType = managementType;
    }
    
    public String getPadType() {
        return padType;
    }
    
    public void setPadType(String padType) {
        this.padType = padType;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return "ModelSelectionDto{" +
                "grade='" + grade + '\'' +
                ", managementType='" + managementType + '\'' +
                ", padType='" + padType + '\'' +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }
}