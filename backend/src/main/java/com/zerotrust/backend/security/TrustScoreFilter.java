package com.zerotrust.backend.security;

import com.zerotrust.backend.entities.RiskScoreHistory;
import com.zerotrust.backend.enums.RiskLevel;
import com.zerotrust.backend.repositories.RiskScoreHistoryRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TrustScoreFilter extends OncePerRequestFilter {

     @Autowired
    private RiskScoreHistoryRepository historyRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if(request.getUserPrincipal() != null){
            String email = request.getUserPrincipal().getName();
            RiskScoreHistory latest = historyRepo.findTopByUserEmailOrderByCalculatedAtDesc(email);

            if(latest != null && latest.getLevel() == RiskLevel.HIGH){
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access denied: high risk user.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
