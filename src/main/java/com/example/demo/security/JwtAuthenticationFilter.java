package com.example.demo.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.demo.login.LoginUserAuthority;
import com.example.demo.login.service.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter{

    private final JwtProvider jwtProvider;

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 로그인, 회원가입 등은 JWT 필터를 적용하지 않음
        return path.equals("/login") || path.startsWith("/h2-console");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        
        // JWT 토큰이 있으면 JWT 인증 시도
        if (token != null) {
            try {
                String empNo = jwtProvider.getEmpNoFromToken(token);
                LoginUserAuthority authority = jwtProvider.getAuthorityFromToken(token);
                
                // empNo를 principal로, authority를 권한으로 설정
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                    empNo, null, List.of(() -> authority.getAuthority()));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // 토큰이 유효하지 않으면 세션 인증 시도
                trySessionAuthentication(request);
            }
        } else {
            // JWT 토큰이 없으면 세션 인증 시도
            trySessionAuthentication(request);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void trySessionAuthentication(HttpServletRequest request) {
        try {
            jakarta.servlet.http.HttpSession session = request.getSession(false);
            if (session != null) {
                String empNo = (String) session.getAttribute("empNo");
                LoginUserAuthority authority = (LoginUserAuthority) session.getAttribute("authority");
                
                if (empNo != null && authority != null) {
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        empNo, null, List.of(() -> authority.getAuthority()));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            // 세션 인증도 실패하면 인증 정보를 설정하지 않음
            SecurityContextHolder.clearContext();
        }
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
