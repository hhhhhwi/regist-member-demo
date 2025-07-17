package com.example.demo.login.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.demo.login.LoginUserAuthority;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;
    @Value("${jwt.expiration}")
    private long jwtExpiration;

   public String generateToken(String empNo, LoginUserAuthority authority) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
            .subject(empNo)
            .claim("auth", authority.toString())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(SignatureAlgorithm.HS256, jwtSecret)
            .compact();
   }

   public String getEmpNoFromToken(String token) {
        return Jwts.parser()
            .setSigningKey(jwtSecret)
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
   }

   public LoginUserAuthority getAuthorityFromToken(String token) {
    return LoginUserAuthority.valueOf(Jwts.parser()
        .setSigningKey(jwtSecret)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .get("auth", String.class));
   }
}
