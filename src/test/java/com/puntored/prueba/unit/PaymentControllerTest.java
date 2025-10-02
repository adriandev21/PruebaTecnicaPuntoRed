package com.puntored.prueba.unit;

import com.puntored.prueba.controller.PaymentController;
import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.model.dto.PaymentCancelRequestDTO;
import com.puntored.prueba.model.dto.PaymentCreateRequestDTO;
import com.puntored.prueba.model.dto.PaymentResponseDTO;
import com.puntored.prueba.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    PaymentService service;

    @InjectMocks
    PaymentController controller;

    PaymentCreateRequestDTO dto;

    @BeforeEach
    void setUp(){
        dto = new PaymentCreateRequestDTO();
        dto.setExternalId("e1");
        dto.setAmount(new BigDecimal("1.23"));
        dto.setDescription("d");
        dto.setDueDate(LocalDateTime.now().plusDays(1));
        dto.setCallbackURL("http://localhost/cb");
    }

    @Test
    void crearDevuelve201ConRespuesta(){
        Payment p = Payment.builder().paymentId(10L).reference("X".repeat(30)).status(PaymentStatus.CREATED).build();
        given(service.create(any())).willReturn(p);
        ResponseEntity<?> resp = controller.create(dto);
        assertThat(resp.getStatusCode().value()).isEqualTo(201);
        PaymentResponseDTO data = (PaymentResponseDTO)((com.puntored.prueba.model.dto.ApiResponse<?>)resp.getBody()).getData();
        assertThat(data.getPaymentId()).isEqualTo(10L);
        assertThat(data.getReference()).hasSize(30);
        assertThat(data.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(data.getStatus().getCode()).isEqualTo("01");
    }

    @Test
    void obtienePagoPorReferencia(){
        Payment p = Payment.builder().paymentId(1L).reference("R").build();
        given(service.get("R", 1L)).willReturn(p);
        ResponseEntity<?> resp = controller.get("R", 1L);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        Payment data = (Payment)((com.puntored.prueba.model.dto.ApiResponse<?>)resp.getBody()).getData();
        assertThat(data.getPaymentId()).isEqualTo(1L);
        assertThat(data.getReference()).isEqualTo("R");
    }

    @Test
    void buscaDevuelvePagina(){
        Page<Payment> page = new PageImpl<>(List.of());
        given(service.search(null, null, null, 0, 10)).willReturn(page);
        ResponseEntity<?> resp = controller.search(null, null, null, 0, 10);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        assertThat(((com.puntored.prueba.model.dto.ApiResponse<?>)resp.getBody()).getData()).isEqualTo(page);
    }

    @Test
    void cancelaDevuelve200(){
        PaymentCancelRequestDTO c = new PaymentCancelRequestDTO();
        c.setReference("R");
        c.setStatus("03");
        c.setUpdateDescription("why");
        Payment p = Payment.builder().reference("R").status(PaymentStatus.CANCELED).build();
        given(service.cancel("R", "why")).willReturn(p);
        ResponseEntity<?> resp = controller.cancel(c);
        assertThat(resp.getStatusCode().value()).isEqualTo(200);
        Payment data = (Payment)((com.puntored.prueba.model.dto.ApiResponse<?>)resp.getBody()).getData();
        assertThat(data.getStatus()).isEqualTo(PaymentStatus.CANCELED);
    }
}
