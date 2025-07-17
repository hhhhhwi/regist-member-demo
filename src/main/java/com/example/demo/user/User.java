package com.example.demo.user;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "userTable")
public class User {
    @Id
    private String empNo;

    private String empName;
    private String deptCode;
    private String birthDate;

    @Enumerated(EnumType.STRING)
    private TeacType teacType;

    @Enumerated(EnumType.STRING)
    private ScType scType;

    private String cntrTypCd;

    public enum TeacType {
        T, // Teacher
        M, // Manager
        C  // Center Director
    }

    public enum ScType {
        E, // Elementary
        M  // Middle school
    }
} 