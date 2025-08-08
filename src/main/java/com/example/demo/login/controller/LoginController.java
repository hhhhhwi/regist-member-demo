package com.example.demo.login.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.login.LoginUserAuthority;
import com.example.demo.login.dto.LoginRequest;
import com.example.demo.login.service.JwtProvider;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserRepository;

@Controller
@RequestMapping("/login")
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
            // 사용자 조회
            User user = userRepository.findById(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 비밀번호 검증
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getBirthDate())) {
                throw new RuntimeException("비밀번호가 올바르지 않습니다.");
            }

            // 권한 결정
            LoginUserAuthority authority = LoginUserAuthority.getAuthority(user.getTeacType(), user.getDeptCode());
            if (authority == null) {
                throw new RuntimeException("권한을 찾을 수 없습니다.");
            }

            // JWT 토큰 생성
            String token = jwtProvider.generateToken(user.getEmpNo(), authority);
            
            // 성공 시 200 상태코드와 location 헤더 반환
            return ResponseEntity.ok()
                .header("location", "/")
                .build();

        } catch (Exception e) {
            // 실패 시 401 상태코드 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
