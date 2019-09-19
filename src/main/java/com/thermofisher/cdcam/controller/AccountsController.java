package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.model.EECUser;
import com.thermofisher.cdcam.model.EmailList;
import com.thermofisher.cdcam.utils.cdc.LiteRegHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountsController {
    static final Logger logger = LogManager.getLogger("CdcamApp");

    @Autowired
    LiteRegHandler handler;

    @PostMapping("/eec/emails")
    public ResponseEntity<List<EECUser>> emailOnlyRegistration(@Valid @NotNull @RequestBody EmailList emailList) {
        if (emailList.getEmails() != null && emailList.getEmails().size() > 0) {
            try {
                List<EECUser> response = handler.process(emailList);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } catch (IOException e) {
                logger.fatal(String.format("An error occurred during EEC email only registration process... [%s]", e.toString()));
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.error("No users requested.");
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class, NullPointerException.class})
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }
}
