package com.puntored.prueba.unit;

import com.puntored.prueba.controller.AuthController;
import com.puntored.prueba.model.dto.ApiResponse;
import com.puntored.prueba.model.dto.AuthRequestDTO;
import com.puntored.prueba.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock AuthService authService;
    @InjectMocks AuthController controller;

    @Test
    void autenticaDevuelveToken(){
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setUsername("client");
        dto.setPassword("pwd");
        given(authService.authenticate("client","pwd")).willReturn("jwt");
        ResponseEntity<ApiResponse<java.util.Map<String,String>>> resp = controller.authenticate(dto);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().getData().get("token")).isEqualTo("jwt");
    }
}
