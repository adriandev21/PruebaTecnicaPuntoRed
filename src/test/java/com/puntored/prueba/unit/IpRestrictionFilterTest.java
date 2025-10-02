package com.puntored.prueba.unit;

import com.puntored.prueba.security.IpRestrictionFilter;
import com.puntored.prueba.service.IpRestrictionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpRestrictionFilterTest {

    @Mock IpRestrictionService service;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain chain;

    @InjectMocks IpRestrictionFilter filter;

    @Test
    void pasaCuandoDeshabilitado() throws Exception {
        given(service.isEnabled()).willReturn(false);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void pasaCuandoHabilitadoYPermitido() throws Exception {
        given(service.isEnabled()).willReturn(true);
        given(request.getRemoteAddr()).willReturn("127.0.0.1");
        given(service.isAllowed("127.0.0.1")).willReturn(true);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
    }

    @Test
    void bloqueaCuandoHabilitadoYNoPermitido() throws Exception {
        given(service.isEnabled()).willReturn(true);
        given(request.getRemoteAddr()).willReturn("10.0.0.1");
        given(service.isAllowed("10.0.0.1")).willReturn(false);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        given(response.getWriter()).willReturn(pw);

        filter.doFilter(request, response, chain);

        verify(response).setStatus(403);
        verify(chain, never()).doFilter(request, response);
        pw.flush();
        assertThat(sw.toString()).contains("IP no permitida");
    }
}
