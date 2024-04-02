package com.example.Angle.Config.Filters;

import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.SecServices.JwtService;
import com.example.Angle.Config.SecServices.MyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authentication");
        String email = null;
        String token = null;
        if(authHeader != null){
            if(authHeader.startsWith("Bearer ")){
                token = authHeader.substring(7);
                email = jwtService.getEmail(token);
            }
        }
        if(email !=null && SecurityContextHolder.getContext().getAuthentication() == null){
            Account account = userDetailsService.loadUserByUsername(email);
            if(jwtService.isTokenValid(token,account)){
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        account,null,account.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request,response);
    }
}
