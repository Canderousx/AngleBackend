package com.example.Angle.Config.Exceptions;


import com.example.Angle.Config.Responses.SimpleResponse;
import org.apache.coyote.BadRequestException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.http.fileupload.InvalidFileNameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class ExceptionHandler {

    private final Logger logger = LogManager.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<SimpleResponse>badRequestHandler(BadRequestException e){
        return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(value = {UsernameNotFoundException.class, BadCredentialsException.class})
    public ResponseEntity<SimpleResponse>userNotFoundHandler(Exception e){
        return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = {InvalidFileNameException.class})
    public ResponseEntity<SimpleResponse>invalidFileFormat(InvalidFileNameException ex){
        return ResponseEntity.badRequest().body(new SimpleResponse(ex.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = EmailExistsException.class)
    public ResponseEntity<SimpleResponse>EmailExistsHandler(EmailExistsException e){
        return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = CredentialExistsException.class)
    public ResponseEntity<SimpleResponse>UsernameExists(CredentialExistsException e){
        return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<SimpleResponse>TokenExpired(TokenExpiredException e){
        System.out.println("TokenExpiredException thrown!");
        return ResponseEntity.status(666).body(new SimpleResponse(e.getMessage()));
    }


    @org.springframework.web.bind.annotation.ExceptionHandler(value = MediaNotFoundException.class)
    public ResponseEntity<SimpleResponse>MediaNotFound(MediaNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new SimpleResponse(e.getMessage()));
    }
    @org.springframework.web.bind.annotation.ExceptionHandler(value = FileServiceException.class)
    public ResponseEntity<SimpleResponse>filesException(FileServiceException e){
        return ResponseEntity.badRequest().body(new SimpleResponse(e.getMessage()));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ResponseEntity<SimpleResponse>tooLargeFileException(Exception e){
        return ResponseEntity.badRequest().body(new SimpleResponse("File too large!"));
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<SimpleResponse>internalServerError(RuntimeException e){
        logger.error(e);
        return ResponseEntity.internalServerError().body(new SimpleResponse("Internal Server Error"));
    }






}
