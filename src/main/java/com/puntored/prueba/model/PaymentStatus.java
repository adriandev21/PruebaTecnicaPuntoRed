package com.puntored.prueba.model;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {
    CREATED("01"),
    PAID("02"),
    CANCELED("03"),
    EXPIRED("04");

    private final String code;

    PaymentStatus(String code) { this.code = code; }

    @JsonValue
    public String getCode() { return code; }

    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus s : values()) {
            if (s.code.equals(code)) return s;
        }
        throw new IllegalArgumentException("Invalid status code: " + code);
    }
}
