package com.example.demo.login.service;

import com.example.demo.user.User;
import com.example.demo.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class LoginService {
    private final UserRepository userRepository;

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User login(String empNo, String birthDate) {
        Optional<User> userOpt = userRepository.findByEmpNoAndBirthDate(empNo, birthDate);
        return userOpt.orElse(null);
    }
} 