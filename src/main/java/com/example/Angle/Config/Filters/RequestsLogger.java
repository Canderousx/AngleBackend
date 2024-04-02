package com.example.Angle.Config.Filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class RequestsLogger extends OncePerRequestFilter {

    private final Logger logger = LogManager.getLogger(RequestsLogger.class);
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.info("REQUEST RECEIVED: "+request.getRequestURI());
        logger.info("METHOD: "+request.getMethod());
        filterChain.doFilter(request,response);

    }
}
