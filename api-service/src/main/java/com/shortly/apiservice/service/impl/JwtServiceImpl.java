package com.shortly.apiservice.service.impl;

import com.shortly.apiservice.configuration.JwtSecretConfig;
import com.shortly.apiservice.dto.UserInfo;
import com.shortly.apiservice.dto.response.TokenResponse;
import com.shortly.apiservice.enumaration.ExceptionType;
import com.shortly.apiservice.exception.ApplicationException;
import com.shortly.apiservice.service.JwtService;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    private final JwtSecretConfig jwtSecretConfig;
    private final SecretKey signKey;

    @Override
    public TokenResponse generateToken(UserInfo userInfo) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + jwtSecretConfig.getJwtExpirationMs());
        String token = Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(signKey)
                .compact();
        return TokenResponse.from(token, expireDate);
    }


    @Override
    public boolean validateToken(String token) {
        try {
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(signKey)
                    .build();
            jwtParser.parseClaimsJws(token);
            return true;
        } catch (SignatureException | IllegalArgumentException | UnsupportedJwtException ex) {
            throw new ApplicationException(ExceptionType.BAD_REQUEST, "Token not valid or expire");
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        JwtParser jwtParser = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build();
        return jwtParser.parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}
