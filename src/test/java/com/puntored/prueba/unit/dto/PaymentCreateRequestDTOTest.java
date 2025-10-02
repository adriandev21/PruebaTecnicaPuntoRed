package com.puntored.prueba.unit.dto;

import com.puntored.prueba.model.dto.PaymentCreateRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentCreateRequestDTOTest {

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

    private PaymentCreateRequestDTO valid(){
        PaymentCreateRequestDTO dto = new PaymentCreateRequestDTO();
        dto.setExternalId("ext-1");
        dto.setAmount(new BigDecimal("10.50"));
        dto.setDescription("Prueba");
        dto.setDueDate(LocalDateTime.now().plusHours(2));
        dto.setCallbackURL("http://localhost/callback");
        return dto;
    }

    @Test
    void payloadValidoNoTieneViolaciones(){
        Set<ConstraintViolation<PaymentCreateRequestDTO>> v = validator.validate(valid());
        assertThat(v).isEmpty();
    }

    @Test
    void montoDebeSerPositivo(){
        PaymentCreateRequestDTO dto = valid();
        dto.setAmount(new BigDecimal("0.00"));
        Set<ConstraintViolation<PaymentCreateRequestDTO>> v = validator.validate(dto);
        assertThat(v).isNotEmpty();
    }

    @Test
    void fechaVencimientoDebeSerFutura(){
        PaymentCreateRequestDTO dto = valid();
        dto.setDueDate(LocalDateTime.now().minusMinutes(5));
        Set<ConstraintViolation<PaymentCreateRequestDTO>> v = validator.validate(dto);
        assertThat(v).isNotEmpty();
    }

    @Test
    void callbackUrlDebeSerValida(){
        PaymentCreateRequestDTO dto = valid();
        dto.setCallbackURL("notaurl");
        Set<ConstraintViolation<PaymentCreateRequestDTO>> v = validator.validate(dto);
        assertThat(v).isNotEmpty();
    }

    @Test
    void descripcionYExternalIdNoDebenSerBlancos(){
        PaymentCreateRequestDTO dto = valid();
        dto.setDescription(" ");
        dto.setExternalId("");
        Set<ConstraintViolation<PaymentCreateRequestDTO>> v = validator.validate(dto);
        assertThat(v).hasSizeGreaterThanOrEqualTo(2);
    }
}
