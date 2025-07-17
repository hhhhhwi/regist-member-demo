package com.example.demo.login;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LoginUserDetails implements UserDetails{
    private String empNo;
    private String birthDate;
    private GrantedAuthority authority;

    public LoginUserDetails(String empNo, String birthDate, GrantedAuthority authority) {
        this.empNo = empNo;
        this.birthDate = birthDate;
        this.authority = authority;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(authority);
    }

    @Override
    public String getPassword() {
        return this.birthDate;
    }

    @Override
    public String getUsername() {
        return this.empNo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
