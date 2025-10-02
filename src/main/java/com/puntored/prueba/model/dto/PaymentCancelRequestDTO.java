package com.puntored.prueba.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class PaymentCancelRequestDTO {
    @NotBlank
    private String reference;

    @NotBlank
    @Pattern(regexp = "03", message = "status debe ser 03 para cancelación")
    private String status;

    @NotBlank
    private String updateDescription;
}

