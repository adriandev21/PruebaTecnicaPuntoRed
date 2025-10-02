package com.puntored.prueba.unit;

import com.puntored.prueba.model.PaymentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PaymentStatusTest {

    @Test
    void codigosYFromCode(){
        assertThat(PaymentStatus.CREATED.getCode()).isEqualTo("01");
        assertThat(PaymentStatus.PAID.getCode()).isEqualTo("02");
        assertThat(PaymentStatus.CANCELED.getCode()).isEqualTo("03");
        assertThat(PaymentStatus.EXPIRED.getCode()).isEqualTo("04");

        assertThat(PaymentStatus.fromCode("01")).isEqualTo(PaymentStatus.CREATED);
        assertThat(PaymentStatus.fromCode("02")).isEqualTo(PaymentStatus.PAID);
        assertThatThrownBy(() -> PaymentStatus.fromCode("99")).isInstanceOf(IllegalArgumentException.class);
    }
}
