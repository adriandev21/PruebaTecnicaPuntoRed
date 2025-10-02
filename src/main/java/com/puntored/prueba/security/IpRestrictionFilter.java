package com.puntored.prueba.security;

import com.puntored.prueba.service.IpRestrictionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class IpRestrictionFilter extends OncePerRequestFilter {

    private final IpRestrictionService ipRestrictionService;

    public IpRestrictionFilter(IpRestrictionService ipRestrictionService) {
        this.ipRestrictionService = ipRestrictionService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!ipRestrictionService.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        String ip = request.getRemoteAddr();
        if (ipRestrictionService.isAllowed(ip)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":403,\"message\":\"IP no permitida\",\"data\":null}");
        }
    }
}

