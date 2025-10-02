package com.puntored.prueba.unit;

import com.puntored.prueba.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    @Test
    void generaYParseaToken_correctamente(){
        JwtTokenService svc = new JwtTokenService();
        TestUtils.setField(svc, "secret", "unit-test-secret-12345678901234567890");
        TestUtils.setField(svc, "expirationMs", 60000L);

        String token = svc.generateToken("user1", "USER");
        assertThat(token).isNotBlank();

        Claims claims = svc.parse(token);
        assertThat(claims.getSubject()).isEqualTo("user1");
        assertThat(claims.get("role")).isEqualTo("USER");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }
}
