package com.example.demo.login.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.user.User;
import com.example.demo.user.repository.UserRepository;

@Service
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User authenticate(String username, String rawPassword) {
        System.out.println("LoginService.authenticate called with username: " + username);
        
        User user = userRepository.findById(username)
            .orElseThrow(() -> {
                System.out.println("User not found: " + username);
                return new RuntimeException("사용자를 찾을 수 없습니다.");
            });

        System.out.println("User found: " + user.getEmpNo());
        System.out.println("Raw password: " + rawPassword);
        System.out.println("Stored password (encrypted): " + user.getBirthDate());
        
        boolean passwordMatches = passwordEncoder.matches(rawPassword, user.getBirthDate());
        System.out.println("Password matches: " + passwordMatches);
        
        if (!passwordMatches) {
            System.out.println("Password authentication failed");
            throw new RuntimeException("비밀번호가 올바르지 않습니다.");
        }
        
        System.out.println("Authentication successful");
        return user;
    }
} 