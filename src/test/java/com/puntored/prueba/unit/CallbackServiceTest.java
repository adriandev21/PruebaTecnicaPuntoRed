package com.puntored.prueba.unit;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.repository.PaymentRepository;
import com.puntored.prueba.service.CallbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CallbackServiceTest {

    @Mock PaymentRepository paymentRepository;
    @InjectMocks CallbackService callbackService;

    @Test
    void programaSiguienteConRetrasosEsperados() {
        given(paymentRepository.save(any(Payment.class))).willAnswer(inv -> inv.getArgument(0));

        assertDelayForAttempts(0, 1);
        assertDelayForAttempts(1, 2);
        assertDelayForAttempts(2, 3);
        assertDelayForAttempts(3, 10);
        assertDelayForAttempts(9, 10);
    }

    private void assertDelayForAttempts(int attempts, int expectedMinutes){
        Payment p = Payment.builder().callbackAttempts(attempts).build();
        LocalDateTime before = LocalDateTime.now();
        callbackService.scheduleNext(p);
        LocalDateTime next = p.getNextCallbackAt();
        long minutes = Duration.between(before, next).toMinutes();
        assertThat(minutes).isEqualTo(expectedMinutes);
    }
}
