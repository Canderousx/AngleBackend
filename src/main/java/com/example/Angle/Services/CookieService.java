package com.example.Angle.Services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class CookieService {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final int cookieExpiry = 7 * 24 * 60 * 60; //7 dni



    public  Cookie getCookie(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies == null){
            return null;
        }
        for(Cookie cookie : cookies){
            if(cookie.getName().equals(cookieName)){
                return cookie;
            }
        }
        return null;
    }

    public <T> T getCookieValue(HttpServletRequest request, String cookieName, TypeReference<T> tTypeReference) throws JsonProcessingException {
        Cookie cookie = getCookie(request,cookieName);
        if(cookie == null){
            return null;
        }
        return objectMapper.readValue(cookie.getValue(),tTypeReference);
    }

    public <T> void setCookie(HttpServletResponse response, String cookieName, T cookieValue ) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(cookieValue);
        byte[] jsonBytes = json.getBytes(StandardCharsets.UTF_8);
        String encodedJson = Base64.getUrlEncoder().encodeToString(jsonBytes);
        ResponseCookie cookie = ResponseCookie.from(cookieName,encodedJson)
                .path("/")
                .sameSite("None")
                .secure(false)
                .httpOnly(false)
                .maxAge(cookieExpiry)
                .build();
        response.setHeader("Set-Cookie",cookie.toString());
    }


}
