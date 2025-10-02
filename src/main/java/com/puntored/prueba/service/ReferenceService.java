package com.puntored.prueba.service;

import com.puntored.prueba.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class ReferenceService {
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RND = new SecureRandom();
    private final PaymentRepository paymentRepository;

    public String generateUniqueReference() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String ref = randomRef(30);
            if (!paymentRepository.existsByReference(ref)) return ref;
        }

        String ref = randomRef(30);
        if (!paymentRepository.existsByReference(ref)) return ref;
        throw new IllegalStateException("No se pudo generar referencia única");
    }

    private String randomRef(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(ALPHANUM.charAt(RND.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}

