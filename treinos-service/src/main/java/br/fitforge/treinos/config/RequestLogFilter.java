package br.fitforge.treinos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Registra cada chamada à API com o X-Correlation-Id recebido do API Gateway,
 * permitindo seguir a mesma requisição nos logs do gateway e do microsserviço.
 */
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLogFilter.class);

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        String cid = req.getHeader("X-Correlation-Id");
        if (cid != null) res.setHeader("X-Correlation-Id", cid);
        try {
            chain.doFilter(req, res);
        } finally {
            log.info("{} {} -> {} ({} ms) cid={}", req.getMethod(), req.getRequestURI(), res.getStatus(),
                    System.currentTimeMillis() - inicio, cid != null ? cid : "-");
        }
    }
}
