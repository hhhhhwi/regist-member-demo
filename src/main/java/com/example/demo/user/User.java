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

    @Enumerated(EnumType.STRING)
    @Transient
    private String cntrTyCd;

    public CntrTyCd getCntrTyCd() {
        if (deptCode != null) {
            switch (deptCode) {
                case "1500":
                case "1503":
                case "1504":
                case "1600":
                    return CntrTyCd.CNSTN;
                case "1501":
                case "1502":
                case "1505":
                case "1601":
                    return CntrTyCd.CHRG;
                case "1602":
                    return CntrTyCd.PRLSN;
                case "1506":
                    return CntrTyCd.SPRT;
            }
        } else if (TeacType.C.equals(teacType)) {
            return CntrTyCd.UNTY;
        }
        return null;
    }

    public enum TeacType {
        T, // Teacher
        M, // Manager
        C  // Center Director
    }

    public enum ScType {
        E, // Elementary
        M  // Middle school
    }

    public enum CntrTyCd {
        CNSTN, 
        CHRG, 
        PRLSN, 
        SPRT, 
        UNTY
    }
} 