package com.example.demo.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.login.LoginUserAuthority;
import com.example.demo.login.dto.LoginRequest;
import com.example.demo.login.service.JwtProvider;
import com.example.demo.login.service.LoginService;
import com.example.demo.user.User;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final LoginService loginService;
    private final JwtProvider jwtProvider;

    public LoginController(LoginService loginService, JwtProvider jwtProvider) {
        this.loginService = loginService;
        this.jwtProvider = jwtProvider;
    }

    // 로그인 페이지 (GET 요청)
    @GetMapping
    public String loginPage() {
        return "login";
    }

    // 로그인 API (POST 요청)
    @PostMapping
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 사용자 인증 (비밀번호 비교 포함)
            User user = loginService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

            // 권한 결정
            LoginUserAuthority authority = LoginUserAuthority.getAuthority(user.getTeacType(), user.getDeptCode());
            if (authority == null) {
                throw new RuntimeException("권한을 찾을 수 없습니다.");
            }

            // JWT 토큰 생성
            String token = jwtProvider.generateToken(user.getEmpNo(), authority);
            
            // 성공 시 200 상태코드와 함께 JWT 토큰과 사용자 정보 반환
            return ResponseEntity.ok()
                .header("Authorization", "Bearer " + token)
                .header("location", "/")
                .build();

        } catch (Exception e) {
            e.printStackTrace();
            // 실패 시 401 상태코드만 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
