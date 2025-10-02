package com.puntored.prueba.unit.dto;

import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.model.dto.PaymentResponseDTO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentResponseDTOTest {

    @Test
    void builderAsignaTodosLosCampos(){
        PaymentResponseDTO dto = PaymentResponseDTO.builder()
                .paymentId(123L)
                .reference("R".repeat(30))
                .status(PaymentStatus.CREATED)
                .message("ok")
                .build();

        assertThat(dto.getPaymentId()).isEqualTo(123L);
        assertThat(dto.getReference()).hasSize(30);
        assertThat(dto.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(dto.getStatus().getCode()).isEqualTo("01");
        assertThat(dto.getMessage()).isEqualTo("ok");
    }
}
