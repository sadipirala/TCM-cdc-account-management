package com.thermofisher.cdcam.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @PostMapping("/user")
    public ResponseEntity<String> registerUser(){
        return new ResponseEntity<>("OK", HttpStatus.OK);
    }
}
