package com.puntored.prueba.unit;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.repository.PaymentRepository;
import com.puntored.prueba.service.CallbackService;
import com.puntored.prueba.service.PaymentService;
import com.puntored.prueba.service.SandboxScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SandboxSchedulerTest {

    @Mock PaymentRepository paymentRepository;
    @Mock PaymentService paymentService;
    @Mock CallbackService callbackService;

    @InjectMocks SandboxScheduler scheduler;

    @Test
    void noHaceNadaCuandoSandboxDeshabilitado(){
        TestUtils.setField(scheduler, "sandboxEnabled", false);
        scheduler.simulatePaid();
        verifyNoInteractions(paymentRepository, paymentService, callbackService);
    }

    @Test
    void marcaPagadoSiCreatedYVencimientoFuturo(){
        TestUtils.setField(scheduler, "sandboxEnabled", true);
        Payment createdFuture = Payment.builder().status(PaymentStatus.CREATED)
                .dueDate(LocalDateTime.now().plusMinutes(10)).build();
        Payment createdPast = Payment.builder().status(PaymentStatus.CREATED)
                .dueDate(LocalDateTime.now().minusMinutes(1)).build();
        Payment paid = Payment.builder().status(PaymentStatus.PAID)
                .dueDate(LocalDateTime.now().plusDays(1)).build();
        when(paymentRepository.findAll()).thenReturn(List.of(createdFuture, createdPast, paid));

        scheduler.simulatePaid();

        verify(paymentService, times(1)).markAsPaid(createdFuture);
        verify(callbackService, times(1)).scheduleNext(createdFuture);
        verify(paymentService, never()).markAsPaid(createdPast);
        verify(paymentService, never()).markAsPaid(paid);
    }

    @Test
    void expirarDelegadoAlServicio(){
        scheduler.expire();
        verify(paymentService, times(1)).expireOverdue();
    }
}
