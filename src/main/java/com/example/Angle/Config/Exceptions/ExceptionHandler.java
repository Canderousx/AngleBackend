package com.example.Angle.Config.Exceptions;


import com.example.Angle.Config.Responses.SimpleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class ExceptionHandler {


    @org.springframework.web.bind.annotation.ExceptionHandler(value = {UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<SimpleResponse>userNotFoundHandler(){
        return ResponseEntity.badRequest().body(new SimpleResponse("Wrong email address or password!"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = EmailExistsException.class)
    public ResponseEntity<SimpleResponse>EmailExistsHandler(){
        return ResponseEntity.badRequest().body(new SimpleResponse("Given address already exists. Please log in"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = UsernameExistsException.class)
    public ResponseEntity<SimpleResponse>UsernameExists(){
        return ResponseEntity.badRequest().body(new SimpleResponse("Given username exists. Try different!"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<SimpleResponse>TokenExpired(){
        System.out.println("TokenExpiredException thrown!");
        return ResponseEntity.status(666).body(new SimpleResponse("Used token is EXPIRED!"));
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(value = MediaNotFoundException.class)
    public ResponseEntity<SimpleResponse>MediaNotFound(){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleResponse("Requested media not found"));
    }



}
