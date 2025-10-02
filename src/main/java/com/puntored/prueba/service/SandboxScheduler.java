package com.puntored.prueba.service;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class SandboxScheduler {
    private static final Logger log = LoggerFactory.getLogger(SandboxScheduler.class);

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final CallbackService callbackService;

    @Value("${sandbox.enabled:true}")
    private boolean sandboxEnabled;

    // Cada 30 minutos, marca como pagados los pagos en estado CREATED que no han vencido
    @Scheduled(fixedDelay = 1800000)
    public void simulatePaid(){
        if (!sandboxEnabled) return;
        int count = 0;
        for (Payment p : paymentRepository.findAll()) {
            if (p.getStatus() == PaymentStatus.CREATED && p.getDueDate().isAfter(LocalDateTime.now())) {
                paymentService.markAsPaid(p);
                callbackService.scheduleNext(p);
                count++;
            }
        }
        if (count > 0) log.info("Sandbox: pagos marcados como PAID: {}", count);
    }

    // Revisa expiración cada 5 minutos
    @Scheduled(fixedDelay = 300000)
    public void expire(){
        paymentService.expireOverdue();
    }
}
