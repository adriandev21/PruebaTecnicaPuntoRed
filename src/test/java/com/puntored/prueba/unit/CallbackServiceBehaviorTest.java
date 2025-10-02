package com.puntored.prueba.unit;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.repository.PaymentRepository;
import com.puntored.prueba.service.CallbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallbackServiceBehaviorTest {

    @Mock
    PaymentRepository paymentRepository;

    @Test
    void triggerCallbackMarcaAckCuandoServicioRespondeACK(){
        CallbackService svc = spy(new CallbackService(paymentRepository));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        Payment p = Payment.builder().paymentId(1L).reference("REF").callbackURL("http://cb").build();
        doReturn(Map.of("status","ACK","acknowledgeId","ok-1"))
                .when(svc).sendCallback(eq("http://cb"), anyMap());

        svc.triggerCallback(p);

        assertThat(p.isAcknowledged()).isTrue();
        assertThat(p.getLastCallbackResponse()).isEqualTo("ACK:ok-1");
        assertThat(p.getNextCallbackAt()).isNull();
        assertThat(p.getCallbackAttempts()).isEqualTo(1);
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void triggerCallbackPoneNoAckYAgendaCuandoFaltaAck(){
        CallbackService svc = spy(new CallbackService(paymentRepository));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        Payment p = Payment.builder().paymentId(2L).reference("REF2").callbackURL("http://cb").build();
        doReturn(Map.of("status","NOPE")).when(svc).sendCallback(eq("http://cb"), anyMap());

        LocalDateTime before = LocalDateTime.now();
        svc.triggerCallback(p);

        assertThat(p.isAcknowledged()).isFalse();
        assertThat(p.getLastCallbackResponse()).isEqualTo("NO_ACK");
        assertThat(p.getCallbackAttempts()).isEqualTo(1);
        assertThat(Duration.between(before, p.getNextCallbackAt()).toMinutes()).isBetween(1L, 3L);
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void triggerCallbackPoneErrorYAgendaAnteExcepcion(){
        CallbackService svc = spy(new CallbackService(paymentRepository));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));
        Payment p = Payment.builder().paymentId(3L).reference("REF3").callbackURL("http://cb").build();
        doThrow(new RuntimeException("boom")).when(svc).sendCallback(eq("http://cb"), anyMap());

        LocalDateTime before = LocalDateTime.now();
        svc.triggerCallback(p);

        assertThat(p.isAcknowledged()).isFalse();
        assertThat(p.getLastCallbackResponse()).startsWith("ERR:");
        assertThat(p.getCallbackAttempts()).isEqualTo(1);
        assertThat(Duration.between(before, p.getNextCallbackAt()).toMinutes()).isBetween(1L, 3L);
        verify(paymentRepository, atLeastOnce()).save(any(Payment.class));
    }

    @Test
    void procesaPendientesSoloParaPagosDebidosNoAckYBajoLimite(){
        CallbackService svc = spy(new CallbackService(paymentRepository));
        Payment duePaid = Payment.builder().status(com.puntored.prueba.model.PaymentStatus.PAID)
                .acknowledged(false).callbackAttempts(0).nextCallbackAt(LocalDateTime.now().minusMinutes(1)).build();
        Payment notDue = Payment.builder().status(com.puntored.prueba.model.PaymentStatus.PAID)
                .acknowledged(false).callbackAttempts(0).nextCallbackAt(LocalDateTime.now().plusMinutes(5)).build();
        Payment acked = Payment.builder().status(com.puntored.prueba.model.PaymentStatus.PAID)
                .acknowledged(true).build();
        Payment created = Payment.builder().status(com.puntored.prueba.model.PaymentStatus.CREATED)
                .acknowledged(false).build();
        Payment tooMany = Payment.builder().status(com.puntored.prueba.model.PaymentStatus.PAID)
                .acknowledged(false).callbackAttempts(10).build();
        when(paymentRepository.findAll()).thenReturn(List.of(duePaid, notDue, acked, created, tooMany));

        doNothing().when(svc).triggerCallback(any(Payment.class));
        svc.processPendingCallbacks();

        verify(svc, times(1)).triggerCallback(any(Payment.class));
    }
}
