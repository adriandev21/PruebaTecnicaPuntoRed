package com.puntored.prueba.repository;

import com.puntored.prueba.security.AllowedIp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AllowedIpRepository extends JpaRepository<AllowedIp, Long> {
    List<AllowedIp> findByEnabledTrue();
}

