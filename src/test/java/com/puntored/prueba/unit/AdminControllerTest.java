package com.puntored.prueba.unit;

import com.puntored.prueba.controller.AdminController;
import com.puntored.prueba.model.dto.ApiResponse;
import com.puntored.prueba.security.AllowedIp;
import com.puntored.prueba.service.IpRestrictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock IpRestrictionService service;
    @InjectMocks AdminController controller;

    @Test
    void agregaIpDevuelveEntidadGuardada(){
        AllowedIp saved = AllowedIp.builder().id(1L).ipAddress("127.0.0.1").enabled(true).build();
        given(service.addIp("127.0.0.1")).willReturn(saved);
        ResponseEntity<ApiResponse<AllowedIp>> resp = controller.addIp("127.0.0.1");
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(resp.getBody().getData().getIpAddress()).isEqualTo("127.0.0.1");
    }

    @Test
    void eliminaIpInvocaServicio(){
        ResponseEntity<ApiResponse<Void>> resp = controller.deleteIp(5L);
        verify(service).removeIp(eq(5L));
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void actualizaHabilitadoInvocaServicio(){
        ResponseEntity<ApiResponse<java.util.Map<String,Object>>> resp = controller.setEnabled(true);
        verify(service).setEnabled(anyBoolean());
        assertThat(resp.getBody().getData().get("enabled")).isEqualTo(true);
    }
}
