package com.puntored.prueba.controller;

import com.puntored.prueba.model.dto.ApiResponse;
import com.puntored.prueba.security.AllowedIp;
import com.puntored.prueba.service.IpRestrictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final IpRestrictionService ipRestrictionService;

    public AdminController(IpRestrictionService ipRestrictionService) { this.ipRestrictionService = ipRestrictionService; }

    @PostMapping("/ip")
    public ResponseEntity<ApiResponse<AllowedIp>> addIp(@RequestParam String ip){
        AllowedIp a = ipRestrictionService.addIp(ip);
        return ResponseEntity.ok(new ApiResponse<>(200, "IP agregada", a));
    }

    @DeleteMapping("/ip/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteIp(@PathVariable Long id){
        ipRestrictionService.removeIp(id);
        return ResponseEntity.ok(new ApiResponse<>(200, "IP eliminada", null));
    }

    @PutMapping("/ip-restriction/enabled/{enabled}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> setEnabled(@PathVariable boolean enabled){
        ipRestrictionService.setEnabled(enabled);
        return ResponseEntity.ok(new ApiResponse<>(200, "Actualizado", Map.of("enabled", enabled)));
    }
}

