package com.puntored.prueba.repository;

import com.puntored.prueba.security.IpRestrictionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IpRestrictionConfigRepository extends JpaRepository<IpRestrictionConfig, Long> {
}

