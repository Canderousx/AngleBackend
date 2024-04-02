package com.example.Angle.Config.Exceptions;


import com.example.Angle.Config.Responses.SimpleResponse;
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



}
