package com.example.demo.login.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.login.LoginUserAuthority;
import com.example.demo.login.LoginUserDetails;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserRepository;

@Service
public class LoginUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;

    public LoginUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String empNo) throws UsernameNotFoundException {
        User user = userRepository.findById(empNo)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        LoginUserAuthority authority = resolveAuthority(user);

        return new LoginUserDetails(user.getEmpNo(), user.getBirthDate(), authority);
    }

    private LoginUserAuthority resolveAuthority(User user) {
        return LoginUserAuthority.getAuthority(user.getTeacType(), user.getDeptCode());
    }
}
