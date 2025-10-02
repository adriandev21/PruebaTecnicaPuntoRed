package com.puntored.prueba.service;

import com.puntored.prueba.model.Payment;
import com.puntored.prueba.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

@Service
public class CallbackService {
    private static final Logger log = LoggerFactory.getLogger(CallbackService.class);

    private final PaymentRepository paymentRepository;
    private final RestClient restClient;

    @Autowired
    public CallbackService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
        this.restClient = RestClient.create();
    }

    CallbackService(PaymentRepository paymentRepository, RestClient restClient) {
        this.paymentRepository = paymentRepository;
        this.restClient = restClient != null ? restClient : RestClient.create();
    }

    public void scheduleNext(Payment p){
        int attempts = p.getCallbackAttempts();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next;
        if (attempts == 0) next = now.plusMinutes(1);
        else if (attempts == 1) next = now.plusMinutes(2);
        else if (attempts == 2) next = now.plusMinutes(3);
        else next = now.plusMinutes(10);
        p.setNextCallbackAt(next);
        paymentRepository.save(p);
    }

    public Map<String, Object> sendCallback(String url, Map<String, Object> body){
        return restClient.post().uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);
    }

    public void triggerCallback(Payment p){
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("paymentId", p.getPaymentId());
            body.put("reference", p.getReference());
            body.put("status", "02");
            body.put("acknowledgeExpected", true);
            var resp = sendCallback(p.getCallbackURL(), body);
            if (resp != null && "ACK".equals(String.valueOf(resp.get("status"))) && resp.get("acknowledgeId") != null) {
                p.setAcknowledged(true);
                p.setLastCallbackResponse("ACK:" + resp.get("acknowledgeId"));
                p.setNextCallbackAt(null);
            } else {
                p.setLastCallbackResponse("NO_ACK");
            }
        } catch (Exception e) {
            p.setLastCallbackResponse("ERR:" + e.getMessage());
        } finally {
            p.setCallbackAttempts(p.getCallbackAttempts() + 1);
            if (!p.isAcknowledged() && p.getCallbackAttempts() < 10) {
                scheduleNext(p);
            }
            paymentRepository.save(p);
        }
    }

    // Procesa callbacks pendientes cada minuto
    @Scheduled(fixedDelay = 60000)
    public void processPendingCallbacks(){
        LocalDateTime now = LocalDateTime.now();
        paymentRepository.findAll().stream()
                .filter(p -> p.getStatus().name().equals("PAID"))
                .filter(p -> !p.isAcknowledged())
                .filter(p -> p.getNextCallbackAt() == null || !p.getNextCallbackAt().isAfter(now))
                .filter(p -> p.getCallbackAttempts() < 10)
                .forEach(this::triggerCallback);
    }
}
