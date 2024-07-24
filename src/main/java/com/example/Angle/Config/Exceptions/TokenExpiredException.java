package com.example.Angle.Config.Exceptions;

public class TokenExpiredException extends Exception{

    public TokenExpiredException(){

    }

    public TokenExpiredException(String message){
        super(message);
    }
}
