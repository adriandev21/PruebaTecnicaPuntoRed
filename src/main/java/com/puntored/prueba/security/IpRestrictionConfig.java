package com.puntored.prueba.security;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ip_restriction_config")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IpRestrictionConfig {
    @Id
    @Column(name = "id")
    private Long id; // usar 1L

    @Column(nullable = false)
    private boolean enabled;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist(){
        if (id == null) id = 1L;
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}

