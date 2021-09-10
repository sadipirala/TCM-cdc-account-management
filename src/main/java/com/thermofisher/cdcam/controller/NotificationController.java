package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.EmailVerificationDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.services.NotificationService;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Autowired
    NotificationService notificationService;

    @Autowired
    CDCResponseHandler cdcResponseHandler;

    @PostMapping("/emailVerification")
    @ApiOperation(value = "Call sns of type accountUpdated")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 400, message = "Bad Request.")
    })
    public ResponseEntity<String> sendEmailVerificationSNS(@RequestBody EmailVerificationDTO emailVerificationDTO) {
        try {
            String uid = emailVerificationDTO.getUid();
            logger.info(String.format("Email verification process for %s started.", uid));
            AccountInfo accountInfo = cdcResponseHandler.getAccountInfo(uid);
            logger.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            logger.info("Sending accountUpdated notification.");
            notificationService.sendAccountUpdatedNotification(accountUpdatedNotification);
            logger.info("accountUpdated notification sent.");
            return new ResponseEntity<String>("The notification was sent successfully!",HttpStatus.OK);
        }
        catch (CustomGigyaErrorException ex) {
            logger.error(String.format("Bad Request : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
