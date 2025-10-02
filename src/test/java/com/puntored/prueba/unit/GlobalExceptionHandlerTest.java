package com.puntored.prueba.unit;

import com.puntored.prueba.exception.GlobalExceptionHandler;
import com.puntored.prueba.model.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void notFoundDevuelve404(){
        ResponseEntity<ApiResponse<Object>> r = handler.handleNotFound(new EntityNotFoundException("x"));
        assertThat(r.getStatusCode().value()).isEqualTo(404);
        assertThat(r.getBody().getMessage()).isEqualTo("x");
    }

    @Test
    void badRequestDevuelve400(){
        var r = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertThat(r.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    void conflictoDevuelve409(){
        var r = handler.handleConflict(new IllegalStateException("conflict"));
        assertThat(r.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void errorInternoDevuelve500(){
        var r = handler.handleOther(new RuntimeException("e"));
        assertThat(r.getStatusCode().value()).isEqualTo(500);
    }
}
