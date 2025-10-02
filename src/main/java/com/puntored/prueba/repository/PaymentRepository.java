package com.puntored.prueba.repository;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByReferenceAndPaymentId(String reference, Long paymentId);

    Optional<Payment> findByReference(String reference);

    boolean existsByReference(String reference);

    @Query("select p from Payment p where (:status is null or p.status = :status) and (:start is null or p.createdAt >= :start) and (:end is null or p.createdAt <= :end)")
    Page<Payment> search(@Param("status") PaymentStatus status, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);
}
