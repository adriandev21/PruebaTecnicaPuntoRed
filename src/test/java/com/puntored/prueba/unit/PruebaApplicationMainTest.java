package com.puntored.prueba.unit;

import com.puntored.prueba.PruebaApplication;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PruebaApplicationMainTest {

    @Test
    void mainInvocaSpringApplicationRunSinLevantarContexto() {
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext fakeCtx = mock(ConfigurableApplicationContext.class);
            mocked.when(() -> SpringApplication.run(eq(PruebaApplication.class), any(String[].class)))
                    .thenReturn(fakeCtx);

            assertThatNoException().isThrownBy(() -> PruebaApplication.main(new String[]{"--spring.main.web-application-type=none"}));

            mocked.verify(() -> SpringApplication.run(eq(PruebaApplication.class), any(String[].class)));
        }
    }
}
