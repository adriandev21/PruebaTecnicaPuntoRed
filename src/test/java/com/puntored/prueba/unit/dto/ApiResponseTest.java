package com.puntored.prueba.unit.dto;

import com.puntored.prueba.model.dto.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiResponseTest {

    @Test
    void fabricaOkConstruye200YMensaje(){
        ApiResponse<String> resp = ApiResponse.ok("todo bien", "DATA");
        assertThat(resp.getStatus()).isEqualTo(200);
        assertThat(resp.getMessage()).isEqualTo("todo bien");
        assertThat(resp.getData()).isEqualTo("DATA");

        resp.setStatus(201);
        resp.setMessage("creado");
        resp.setData("X");
        assertThat(resp.getStatus()).isEqualTo(201);
        assertThat(resp.getMessage()).isEqualTo("creado");
        assertThat(resp.getData()).isEqualTo("X");
    }
}
