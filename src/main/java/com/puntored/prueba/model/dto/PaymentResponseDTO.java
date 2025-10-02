package com.puntored.prueba.model.dto;

import com.puntored.prueba.model.PaymentStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentResponseDTO {
    Long paymentId;
    String reference;
    PaymentStatus status;
    String message;
}

