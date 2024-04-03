package com.example.Angle.Controllers.Auth;


import com.example.Angle.Config.Responses.SimpleResponse;
import com.example.Angle.Config.SecServices.AccountService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(value = {"http://localhost:4200"})
@RequestMapping("/auth")
public class AccountInfoController {

    @Autowired
    AccountService accountService;

    private final Logger logger = LogManager.getLogger(AccountInfoController.class);


    @RequestMapping(value = "/getMyId",method = RequestMethod.GET)
    public ResponseEntity<SimpleResponse> getUserId(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Sending "+username+" id");
        return ResponseEntity.ok(new SimpleResponse(""));
    }
}
