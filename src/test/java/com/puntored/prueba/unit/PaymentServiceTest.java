package com.puntored.prueba.unit;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.model.dto.PaymentCreateRequestDTO;
import com.puntored.prueba.repository.PaymentRepository;
import com.puntored.prueba.service.PaymentService;
import com.puntored.prueba.service.ReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ReferenceService referenceService;

    @InjectMocks
    PaymentService service;

    PaymentCreateRequestDTO validDto;

    @BeforeEach
    void setup(){
        validDto = new PaymentCreateRequestDTO();
        validDto.setExternalId("ext-1");
        validDto.setAmount(new BigDecimal("10.00"));
        validDto.setDescription("desc");
        validDto.setDueDate(LocalDateTime.now().plusDays(1));
        validDto.setCallbackURL("http://localhost/cb");
    }

    @Test
    void creaPagoValido() {
        given(referenceService.generateUniqueReference()).willReturn("A".repeat(30));
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

        Payment p = service.create(validDto);

        assertThat(p.getStatus()).isEqualTo(PaymentStatus.CREATED);
        assertThat(p.getReference()).hasSize(30);
        assertThat(p.getCallbackURL()).isEqualTo("http://localhost/cb");
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void rechazaMontoCeroONegativo(){
        validDto.setAmount(new BigDecimal("0"));
        assertThatThrownBy(() -> service.create(validDto)).isInstanceOf(IllegalArgumentException.class);
        validDto.setAmount(new BigDecimal("-1"));
        assertThatThrownBy(() -> service.create(validDto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaFechaVencidaEnPasado(){
        validDto.setDueDate(LocalDateTime.now().minusMinutes(1));
        assertThatThrownBy(() -> service.create(validDto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void obtienePagoYLanzaNotFound(){
        Payment mock = Payment.builder().paymentId(1L).reference("R").build();
        given(paymentRepository.findByReferenceAndPaymentId("R", 1L)).willReturn(Optional.of(mock));
        assertThat(service.get("R", 1L)).isSameAs(mock);

        given(paymentRepository.findByReferenceAndPaymentId("X", 2L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.get("X", 2L)).isInstanceOf(jakarta.persistence.EntityNotFoundException.class);
    }

    @Test
    void buscaConRangoValidoYEvaluaErroresDeRango(){
        Page<Payment> page = new PageImpl<>(List.of(), PageRequest.of(0,10), 0);
        given(paymentRepository.search(isNull(), any(), any(), any())).willReturn(page);
        assertThat(service.search(null, null, null, 0, 10)).isSameAs(page);

        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1);
        assertThatThrownBy(() -> service.search(start, end, null, 0, 10)).isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.search(LocalDate.now().minusDays(40), LocalDate.now(), null, 0, 10))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void cancelaSoloSiEstaEnCreated(){
        Payment created = Payment.builder().reference("R1").status(PaymentStatus.CREATED).build();
        given(paymentRepository.findByReference("R1")).willReturn(Optional.of(created));
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
        Payment cancelled = service.cancel("R1", "why");
        assertThat(cancelled.getStatus()).isEqualTo(PaymentStatus.CANCELED);
        assertThat(cancelled.getCanceledAt()).isNotNull();

        Payment paid = Payment.builder().reference("R2").status(PaymentStatus.PAID).build();
        given(paymentRepository.findByReference("R2")).willReturn(Optional.of(paid));
        assertThatThrownBy(() -> service.cancel("R2", "why")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void expiraVencidosMarcaYCuenta(){
        Payment a = Payment.builder().status(PaymentStatus.CREATED).dueDate(LocalDateTime.now().minusDays(1)).build();
        Payment b = Payment.builder().status(PaymentStatus.CREATED).dueDate(LocalDateTime.now().plusDays(1)).build();
        Payment c = Payment.builder().status(PaymentStatus.PAID).dueDate(LocalDateTime.now().minusDays(1)).build();
        given(paymentRepository.findAll()).willReturn(List.of(a,b,c));
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
        int count = service.expireOverdue();
        assertThat(count).isEqualTo(1);
        assertThat(a.getStatus()).isEqualTo(PaymentStatus.EXPIRED);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void marcaComoPagadoIdempotente(){
        Payment p = Payment.builder().status(PaymentStatus.CREATED).build();
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));
        Payment paid = service.markAsPaid(p);
        assertThat(paid.getStatus()).isEqualTo(PaymentStatus.PAID);

        Payment already = Payment.builder().status(PaymentStatus.PAID).build();
        assertThat(service.markAsPaid(already)).isSameAs(already);
    }
}
