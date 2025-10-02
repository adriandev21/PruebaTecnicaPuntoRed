package com.puntored.prueba.model.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentCreateRequestDTO {
    @NotBlank
    private String externalId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @NotBlank
    @Size(max = 512)
    private String description;

    @NotNull
    @Future
    private LocalDateTime dueDate;

    @NotBlank
    @URL
    private String callbackURL;
}

