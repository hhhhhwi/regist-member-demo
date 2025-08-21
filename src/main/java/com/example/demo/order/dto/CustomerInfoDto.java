package com.example.demo.order.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerInfoDto {
    
    // 부모 정보
    @NotBlank(message = "부모 이름은 필수입니다.")
    private String parentName;
    
    @NotBlank(message = "부모 연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "연락처 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String parentPhone;
    
    private String parentEmail;
    private String address;
    
    // 자녀 정보
    @NotBlank(message = "자녀 이름은 필수입니다.")
    private String childName;
    
    @NotBlank(message = "자녀 학년은 필수입니다.")
    private String childGrade;
    
    private String school;
}