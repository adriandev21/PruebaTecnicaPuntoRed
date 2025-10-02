package com.puntored.prueba.unit;

import com.puntored.prueba.service.AuthService;
import com.puntored.prueba.security.JwtTokenService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;

    @Mock
    JwtTokenService jwtTokenService;

    @InjectMocks
    AuthService service;

    @Test
    void autenticaYGeneraToken(){
        Authentication auth = new UsernamePasswordAuthenticationToken("client", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).willReturn(auth);
        given(jwtTokenService.generateToken("client", "USER")).willReturn("jwt-token");

        String token = service.authenticate("client", "pwd");
        assertThat(token).isEqualTo("jwt-token");
    }
}
