package com.example.Angle.Config.SecServices;


import com.example.Angle.Config.Models.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    private static final String SECRET_KEY = "ACC0C21FACDFE09A10C33618B291BB599ECB1A3FEDC6B1BF55D9FC7B489A4C7C";

    private Key getSignedKey(){
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public Boolean isTokenValid(String token, Account account){
        String email = getEmail(token);
        return (account.getEmail().equals(email) && !isTokenExpired(token));
    }


    private Boolean isTokenExpired(String token){
        return getExpirationDate(token).before(new Date(System.currentTimeMillis()));
    }


    public String getEmail(String token){
        return extractClaim(token,Claims::getSubject);
    }


    public Date getExpirationDate(String token){
        return extractClaim(token,Claims::getExpiration);
    }



    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignedKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String generateToken(String email){
        Map<String,Object> claims = new HashMap<>();
        return createToken(claims,email);
    }

    private String createToken(Map<String,Object> claims, String email){
        return Jwts.builder()
                .setSubject(email)
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000))
                .signWith(getSignedKey(), SignatureAlgorithm.HS256)
                .compact();
    }








}
