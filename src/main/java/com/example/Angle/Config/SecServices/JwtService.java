package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Models.Account;
import com.example.Angle.Config.Models.EnvironmentVariables;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    @Autowired
    private EnvironmentVariables environmentVariables;

    private final Logger logger = LogManager.getLogger(JwtService.class);

    private final Set<String>invalidatedTokens = new HashSet<>();


    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(this.environmentVariables.getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(this.environmentVariables.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }


    private String createToken(Map<String,Object> claims, String username,long time){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .signWith(getSignKey())
                .compact();
    }

    public String generateToken(String username,String userIP){
        Map<String,Object> claims = new HashMap<>();
        claims.put("IP",userIP);
        return createToken(claims,username,60 * 60 * 1000);
    }


    public String generatePasswordRecoveryToken(String username,String userIP){
        Map<String,Object>claims = new HashMap<>();
        claims.put("src","passwordRecovery");
        claims.put("IP",userIP);
        return createToken(claims,username,15 * 60 * 1000);
    }

    public String generateEmailConfirmationToken(String username){
        Map<String,Object>claims = new HashMap<>();
        claims.put("src","emailConfirmation");
        return createToken(claims,username,15 * 60 * 1000);
    }



    public <T> T extractClaim(String token, Function<Claims,T>claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private String extractIP(String token){
        return extractClaim(token, claims -> {
            return (String) claims.get("IP");
        });
    }


    public String extractUsername(String token){
        return extractClaim(token,Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token,Claims::getExpiration);
    }

    public Boolean isTokenExpired(String token){
        boolean expired = extractExpiration(token).before(new Date(System.currentTimeMillis()));
        if(expired){
            logger.info("Received token is expired!");
        }else{
            logger.info("Received token time is valid");
        }
        return expired;
    }
    public Boolean validateToken(String token, Account userDetails,String userIP){
        final String username = extractUsername(token);
        final String savedIP = extractIP(token);
        logger.info("USERNAME: "+username);
        logger.info("UserDetailsName: "+userDetails.getEmail());
        logger.info("Requested from: "+userIP);
        return username.equals(userDetails.getEmail()) && !isTokenExpired(token) && (savedIP.equals(userIP)) && !this.invalidatedTokens.contains(token)
                && !extractAllClaims(token).containsKey("src");
    }

    public Boolean validatePasswordRecoveryToken(String token,String userIP){
        final String username = extractUsername(token);
        final String savedIP = extractIP(token);
        logger.info("USERNAME: "+username);
        logger.info("Requested from: "+userIP);
        return savedIP.equals(userIP) && !this.invalidatedTokens.contains(token) && !isTokenExpired(token)
                && extractAllClaims(token).containsKey("src");
    }

    public Boolean validateEmailConfirmationToken(String token){
        return !this.invalidatedTokens.contains(token) && !isTokenExpired(token);
    }





    public void invalidateToken(String token){
        this.invalidatedTokens.add(token);
    }








}
