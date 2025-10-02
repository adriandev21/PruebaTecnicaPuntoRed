package com.puntored.prueba.controller;

import com.puntored.prueba.model.dto.ApiResponse;
import com.puntored.prueba.model.dto.AuthRequestDTO;
import com.puntored.prueba.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/authenticate")
    public ResponseEntity<ApiResponse<Map<String, String>>> authenticate(@RequestBody @Valid AuthRequestDTO request){
        String token = authService.authenticate(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new ApiResponse<>(200, "Token generado", Map.of("token", token)));
    }
}

