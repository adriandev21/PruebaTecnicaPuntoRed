package com.puntored.prueba.unit.dto;

import com.puntored.prueba.model.dto.PaymentCancelRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCancelRequestDTOTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setup(){
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown(){
        factory.close();
    }

    private PaymentCancelRequestDTO valido(){
        PaymentCancelRequestDTO dto = new PaymentCancelRequestDTO();
        dto.setReference("REF123");
        dto.setStatus("03");
        dto.setUpdateDescription("Cancelado por cliente");
        return dto;
    }

    @Test
    void payloadValidoNoTieneViolaciones(){
        Set<ConstraintViolation<PaymentCancelRequestDTO>> v = validator.validate(valido());
        assertThat(v).isEmpty();
    }

    @Test
    void estatusDistintoA03DaViolacion(){
        PaymentCancelRequestDTO dto = valido();
        dto.setStatus("01");
        Set<ConstraintViolation<PaymentCancelRequestDTO>> v = validator.validate(dto);
        assertThat(v).isNotEmpty();
    }

    @Test
    void camposEnBlancoGeneranViolaciones(){
        PaymentCancelRequestDTO dto = new PaymentCancelRequestDTO();
        dto.setReference("");
        dto.setStatus("");
        dto.setUpdateDescription(" ");
        Set<ConstraintViolation<PaymentCancelRequestDTO>> v = validator.validate(dto);
        assertThat(v).hasSizeGreaterThanOrEqualTo(3);
    }
}
