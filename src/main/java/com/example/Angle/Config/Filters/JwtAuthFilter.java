package com.example.Angle.Config.Filters;

import com.example.Angle.Config.Exceptions.TokenExpiredException;
import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Config.SecServices.MyUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@AllArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final Logger logger = LogManager.getLogger(JwtAuthFilter.class);

    @Autowired
    JwtService jwtService;

    @Autowired
    MyUserDetailsService userDetailsService;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authentication");
        String email = null;
        String token = null;
        try{
            if(authHeader != null){
                if(authHeader.startsWith("Bearer ")){
                    token = authHeader.substring(7);
                    logger.info("RECEIVED TOKEN: "+token);
                    email = jwtService.extractUsername(token);
                    logger.info("RECEIVED EMAIL: "+email);
                }
            }
            if(email !=null && SecurityContextHolder.getContext().getAuthentication() == null){
                Account account = userDetailsService.loadUserByUsername(email);
                String userIP = request.getRemoteAddr();
                if(jwtService.validateToken(token,account,userIP)){
                    logger.info("Received token is valid!");
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            account,null,account.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }else{
                    logger.info("Received token invalid!");
                    throw new TokenExpiredException();

                }
            }
            filterChain.doFilter(request,response);

        }catch (ExpiredJwtException e){
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            SimpleResponse simpleResponse = new SimpleResponse("Session timed out!");
            String jsonResponse = new ObjectMapper().writeValueAsString(simpleResponse);
            response.getWriter().write(jsonResponse);
        }

    }
}
