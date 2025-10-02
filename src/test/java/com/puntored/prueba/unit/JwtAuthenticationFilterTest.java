package com.puntored.prueba.unit;

import com.puntored.prueba.security.JwtAuthenticationFilter;
import com.puntored.prueba.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock JwtTokenService tokenService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;

    @InjectMocks JwtAuthenticationFilter filter;

    @AfterEach
    void clear(){
        SecurityContextHolder.clearContext();
    }

    @Test
    void estableceAutenticacionConTokenValido() throws IOException, ServletException {
        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn("Bearer abc");
        Claims claims = mock(Claims.class);
        given(claims.getSubject()).willReturn("user1");
        given(claims.get("role")).willReturn("USER");
        given(tokenService.parse("abc")).willReturn(claims);

        filter.doFilter(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("user1");
        verify(chain).doFilter(request, response);
    }

    @Test
    void ignoraCuandoNoHayHeaderOAunValido() throws IOException, ServletException {
        given(request.getHeader(HttpHeaders.AUTHORIZATION)).willReturn(null);
        filter.doFilter(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, times(1)).doFilter(request, response);
    }
}
