package com.puntored.prueba.unit;

import com.puntored.prueba.repository.PaymentRepository;
import com.puntored.prueba.service.ReferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ReferenceServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @InjectMocks
    ReferenceService service;

    @Test
    void generaReferenciaUnicaDe30Caracteres(){
        given(paymentRepository.existsByReference(anyString())).willReturn(false);
        String ref = service.generateUniqueReference();
        assertThat(ref).hasSize(30);
    }

    @Test
    void lanzaExcepcionCuandoTodosChocan(){
        given(paymentRepository.existsByReference(anyString())).willReturn(true);
        assertThatThrownBy(() -> service.generateUniqueReference()).isInstanceOf(IllegalStateException.class);
    }
}
