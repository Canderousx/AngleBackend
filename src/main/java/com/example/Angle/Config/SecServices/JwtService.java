package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Models.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
public class JwtService {

    private final Logger logger = LogManager.getLogger(JwtService.class);

    private final Set<String>invalidatedTokens = new HashSet<>();

    public static final String SECRET = "357638792F423F4428472B4B6250655368566D597133743677397A2443264629";

    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(SECRET)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    private String createToken(Map<String,Object> claims, String username){
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(getSignKey())
                .compact();
    }

    public String generateToken(String username,String userIP){
        Map<String,Object> claims = new HashMap<>();
        claims.put("IP",userIP);
        return createToken(claims,username);
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
        boolean valid = username.equals(userDetails.getEmail()) && !isTokenExpired(token) && (savedIP.equals(userIP)) && !this.invalidatedTokens.contains(token);
        return valid;
    }

    public void invalidateToken(String token){
        this.invalidatedTokens.add(token);
    }








}
