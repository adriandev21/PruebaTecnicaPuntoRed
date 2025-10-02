package com.puntored.prueba.unit.dto;

import com.puntored.prueba.model.dto.AuthRequestDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestDTOTest {

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

    @Test
    void payloadValidoNoTieneViolaciones(){
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setUsername("user");
        dto.setPassword("secret");
        Set<ConstraintViolation<AuthRequestDTO>> v = validator.validate(dto);
        assertThat(v).isEmpty();
    }

    @Test
    void camposEnBlancoGeneranViolaciones(){
        AuthRequestDTO dto = new AuthRequestDTO();
        dto.setUsername(" ");
        dto.setPassword("");
        Set<ConstraintViolation<AuthRequestDTO>> v = validator.validate(dto);
        assertThat(v).hasSizeGreaterThanOrEqualTo(2);
    }
}
