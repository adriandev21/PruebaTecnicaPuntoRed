package com.puntored.prueba.service;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.model.dto.PaymentCreateRequestDTO;
import com.puntored.prueba.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ReferenceService referenceService;

    @Transactional
    public Payment create(PaymentCreateRequestDTO dto) {
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("amount debe ser > 0");
        }
        if (dto.getDueDate() == null || !dto.getDueDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("dueDate debe ser futuro");
        }
        String reference = referenceService.generateUniqueReference();
        Payment p = Payment.builder()
                .reference(reference)
                .externalId(dto.getExternalId())
                .amount(dto.getAmount())
                .description(dto.getDescription())
                .dueDate(dto.getDueDate())
                .status(PaymentStatus.CREATED)
                .callbackURL(dto.getCallbackURL())
                .build();
        return paymentRepository.save(p);
    }

    @Transactional(readOnly = true)
    public Payment get(String reference, Long paymentId) {
        return paymentRepository.findByReferenceAndPaymentId(reference, paymentId)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Pago no encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Payment> search(LocalDate startDate, LocalDate endDate, PaymentStatus status, int page, int size) {
        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(23,59,59) : null;
        if (start != null && end != null) {
            if (start.isAfter(end)) {
                throw new IllegalArgumentException("Rango de fechas inválido");
            }
            if (Duration.between(start, end).toDays() > 31) {
                throw new IllegalArgumentException("El rango máximo es de 1 mes");
            }
        }
        return paymentRepository.search(status, start, end, PageRequest.of(page, size));
    }

    @Transactional
    public Payment cancel(String reference, String updateDescription) {
        Payment p = paymentRepository.findByReference(reference)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Pago no encontrado"));
        if (p.getStatus() != PaymentStatus.CREATED) {
            throw new IllegalStateException("Solo se puede cancelar un pago en estado 01 Created");
        }
        p.setStatus(PaymentStatus.CANCELED);
        p.setCanceledAt(LocalDateTime.now());
        p.setUpdateDescription(updateDescription);
        return paymentRepository.save(p);
    }

    @Transactional
    public Payment markAsPaid(Payment p) {
        if (p.getStatus() == PaymentStatus.PAID) return p;
        p.setStatus(PaymentStatus.PAID);
        p.setPaidAt(LocalDateTime.now());
        return paymentRepository.save(p);
    }

    @Transactional
    public int expireOverdue() {
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (Payment p : paymentRepository.findAll()) {
            if (p.getStatus() == PaymentStatus.CREATED && p.getDueDate().isBefore(now)) {
                p.setStatus(PaymentStatus.EXPIRED);
                p.setExpiredAt(now);
                paymentRepository.save(p);
                count++;
            }
        }
        if (count > 0) log.info("Pagos expirados: {}", count);
        return count;
    }
}
