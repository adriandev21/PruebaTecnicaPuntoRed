package com.puntored.prueba.unit;

import com.puntored.prueba.repository.AllowedIpRepository;
import com.puntored.prueba.repository.IpRestrictionConfigRepository;
import com.puntored.prueba.security.AllowedIp;
import com.puntored.prueba.security.IpRestrictionConfig;
import com.puntored.prueba.service.IpRestrictionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class IpRestrictionServiceTest {

    @Mock AllowedIpRepository allowedIpRepository;
    @Mock IpRestrictionConfigRepository configRepository;

    @InjectMocks IpRestrictionService service;

    @Test
    void obtieneIpsPermitidasYValidaSiEstaPermitida(){
        AllowedIp a = AllowedIp.builder().id(1L).ipAddress("127.0.0.1").enabled(true).build();
        given(allowedIpRepository.findByEnabledTrue()).willReturn(List.of(a));
        Set<String> ips = service.getAllowedIps();
        assertThat(ips).contains("127.0.0.1");
        assertThat(service.isAllowed("127.0.0.1")).isTrue();
        assertThat(service.isAllowed("10.0.0.1")).isFalse();
    }

    @Test
    void isEnabledDevuelveFalsePorDefecto(){
        given(configRepository.findById(1L)).willReturn(Optional.empty());
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void habilitarDeshabilitarGuardaConfiguracion(){
        given(configRepository.findById(1L)).willReturn(Optional.of(new IpRestrictionConfig()));
        service.setEnabled(true);
        verify(configRepository).save(any(IpRestrictionConfig.class));
    }

    @Test
    void agregaYEliminaIp(){
        AllowedIp saved = AllowedIp.builder().id(99L).ipAddress("1.2.3.4").enabled(true).build();
        given(allowedIpRepository.save(any(AllowedIp.class))).willReturn(saved);
        AllowedIp result = service.addIp("1.2.3.4");
        assertThat(result.getId()).isEqualTo(99L);
        service.removeIp(99L);
        verify(allowedIpRepository).deleteById(eq(99L));
    }
}
