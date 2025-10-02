package com.puntored.prueba.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_pay_reference", columnList = "reference", unique = true),
        @Index(name = "idx_pay_status", columnList = "status"),
        @Index(name = "idx_pay_dueDate", columnList = "dueDate")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(nullable = false, unique = true, length = 30)
    private String reference;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 512)
    private String description;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PaymentStatus status;

    @Column(name = "callback_url", nullable = false, length = 1024)
    private String callbackURL;

    @Column
    private String updateDescription;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime canceledAt;

    @Column
    private LocalDateTime expiredAt;

    // Callback tracking
    @Column(nullable = false)
    private int callbackAttempts;

    @Column
    private LocalDateTime nextCallbackAt;

    @Column
    private String lastCallbackResponse;

    @Column(nullable = false)
    private boolean acknowledged;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        callbackAttempts = 0;
        acknowledged = false;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
