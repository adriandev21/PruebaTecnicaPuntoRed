package com.puntored.prueba.service;

import com.puntored.prueba.repository.AllowedIpRepository;
import com.puntored.prueba.repository.IpRestrictionConfigRepository;
import com.puntored.prueba.security.AllowedIp;
import com.puntored.prueba.security.IpRestrictionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IpRestrictionService {
    private final AllowedIpRepository allowedIpRepository;
    private final IpRestrictionConfigRepository configRepository;

    @Cacheable("allowedIps")
    public Set<String> getAllowedIps(){
        return allowedIpRepository.findByEnabledTrue().stream().map(AllowedIp::getIpAddress).collect(Collectors.toSet());
    }

    public boolean isAllowed(String ip){
        return getAllowedIps().contains(ip);
    }

    public boolean isEnabled(){
        return configRepository.findById(1L).map(IpRestrictionConfig::isEnabled).orElse(false);
    }

    @Transactional
    @CacheEvict(value = "allowedIps", allEntries = true)
    public AllowedIp addIp(String ip){
        AllowedIp a = AllowedIp.builder().ipAddress(ip).enabled(true).build();
        return allowedIpRepository.save(a);
    }

    @Transactional
    @CacheEvict(value = "allowedIps", allEntries = true)
    public void removeIp(Long id){
        allowedIpRepository.deleteById(id);
    }

    @Transactional
    public void setEnabled(boolean enabled){
        IpRestrictionConfig cfg = configRepository.findById(1L).orElseGet(() -> {
            IpRestrictionConfig c = new IpRestrictionConfig();
            c.setId(1L);
            c.setEnabled(false);
            return c;
        });
        cfg.setEnabled(enabled);
        configRepository.save(cfg);
    }
}

