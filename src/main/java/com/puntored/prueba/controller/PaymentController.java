package com.puntored.prueba.controller;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.model.PaymentStatus;
import com.puntored.prueba.model.dto.ApiResponse;
import com.puntored.prueba.model.dto.PaymentCancelRequestDTO;
import com.puntored.prueba.model.dto.PaymentCreateRequestDTO;
import com.puntored.prueba.model.dto.PaymentResponseDTO;
import com.puntored.prueba.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) { this.paymentService = paymentService; }

    @PostMapping("/payment")
    public ResponseEntity<ApiResponse<PaymentResponseDTO>> create(@RequestBody @Valid PaymentCreateRequestDTO dto){
        Payment p = paymentService.create(dto);
        var resp = PaymentResponseDTO.builder()
                .paymentId(p.getPaymentId())
                .reference(p.getReference())
                .status(p.getStatus())
                .message("Pago creado")
                .build();
        return ResponseEntity.status(201).body(new ApiResponse<>(201, "Creado", resp));
    }

    @GetMapping("/payment/{reference}/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> get(@PathVariable String reference, @PathVariable Long paymentId){
        Payment p = paymentService.get(reference, paymentId);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", p));
    }

    @GetMapping("/payments/search")
    public ResponseEntity<ApiResponse<Page<Payment>>> search(@RequestParam(required = false) LocalDate startDate,
                                                             @RequestParam(required = false) LocalDate endDate,
                                                             @RequestParam(required = false) String status,
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "10") int size){
        PaymentStatus st = null;
        if (status != null && !status.isBlank()) {
            st = PaymentStatus.fromCode(status);
        }
        Page<Payment> result = paymentService.search(startDate, endDate, st, page, size);
        return ResponseEntity.ok(new ApiResponse<>(200, "OK", result));
    }

    @PutMapping("/payment/cancel")
    public ResponseEntity<ApiResponse<Payment>> cancel(@RequestBody @Valid PaymentCancelRequestDTO dto){
        Payment p = paymentService.cancel(dto.getReference(), dto.getUpdateDescription());
        return ResponseEntity.ok(new ApiResponse<>(200, "Cancelado", p));
    }
}

