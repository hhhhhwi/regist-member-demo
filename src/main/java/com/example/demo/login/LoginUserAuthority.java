package com.example.demo.login;

import org.springframework.security.core.GrantedAuthority;

import com.example.demo.user.User.TeacType;

public enum LoginUserAuthority implements GrantedAuthority {
    A01(TeacType.T, new String[] {"1500", "1503", "1504"}),
    B01(TeacType.M, new String[] {"1501", "1502", "1505"}),
    C01(TeacType.C, new String[] {"1600"}),
    D01(TeacType.T, new String[] {"1601"}),
    E01(TeacType.M, new String[] {"1602"}),
    F01(TeacType.M, new String[] {"1506"}),
    A02(TeacType.T, new String[] {"1500", "1503", "1504"}),
    B02(TeacType.M, new String[] {"1501", "1502", "1505"}),
    C02(TeacType.C, new String[] {"1600"}),
    D02(TeacType.T, new String[] {"1601"}),
    E02(TeacType.M, new String[] {"1602"}),
    F02(TeacType.M, new String[] {"1506"}),
    A03(TeacType.T, new String[] {"1500", "1503", "1504"}),
    B03(TeacType.M, new String[] {"1501", "1502", "1505"}),
    C03(TeacType.C, new String[] {"1600"}),
    D03(TeacType.T, new String[] {"1601"}),
    E03(TeacType.M, new String[] {"1602"}),
    F03(TeacType.M, new String[] {"1506"});

    private TeacType teacType;
    private String[] deptCodes;

    private LoginUserAuthority(TeacType teacType, String[] deptCodes) {
        this.teacType = teacType;
        this.deptCodes = deptCodes;
    }

    @Override
    public String getAuthority() {
        return this.name();
    }

    public static LoginUserAuthority getAuthority(TeacType teacType, String deptCode) {
        for (LoginUserAuthority authority : LoginUserAuthority.values()) {
            if (authority.teacType.equals(teacType)) {
                for (String code : authority.deptCodes) {
                    if (code.equals(deptCode)) {
                        return authority;
                    }
                }
            }
        }
        return null;
    }
}
