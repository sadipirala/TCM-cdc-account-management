package com.thermofisher.cdcam.controller;

import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.EmailUpdatedNotification;
import com.thermofisher.cdcam.model.MarketingConsentUpdatedNotification;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.dto.EmailVerificationDTO;
import com.thermofisher.cdcam.model.dto.UpdateMarketingConsentDTO;
import com.thermofisher.cdcam.model.notifications.AccountUpdatedNotification;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.NotificationService;

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
    GigyaService gigyaService;

    @PostMapping("/emailVerification")
    @ApiOperation(value = "Call sns of type accountUpdated")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 400, message = "Bad Request.")
    })
    public ResponseEntity<String> sendEmailVerificationSNS(@RequestBody EmailVerificationDTO emailVerificationDTO) {
        try {
            String uid = emailVerificationDTO.getUid();
            String previousEmail = emailVerificationDTO.getPreviousEmail();
            logger.info("previousEmail: " + previousEmail);
            logger.info(String.format("Email verification process for %s started.", uid));
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            logger.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            if (null != previousEmail && !previousEmail.equalsIgnoreCase(accountInfo.getEmailAddress())) {
                accountUpdatedNotification.setPreviousEmail(emailVerificationDTO.getPreviousEmail());
            }
            logger.info("Sending accountUpdated notification.");
            notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
            notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
            logger.info("accountUpdated notification sent.");
            return new ResponseEntity<String>("The notification was sent successfully!", HttpStatus.OK);
        } catch (CustomGigyaErrorException ex) {
            logger.error(String.format("Bad Request : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update/marketing/consent")
    @ApiOperation(value = "Notifies downstream systems about a change in a user's marketing consent status")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK."),
            @ApiResponse(code = 400, message = "Bad Request.")
    })
    public ResponseEntity<String> notifyMarketingConsentUpdated(@RequestBody UpdateMarketingConsentDTO updateMarketingConsentDTO) {
        try {
            String uid = updateMarketingConsentDTO.getUid();
            logger.info(String.format("Marketing consent updated notification started for: %s", uid));
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            logger.info("Building marketingConsentUpdated object.");
            MarketingConsentUpdatedNotification marketingConsentUpdatedNotification = MarketingConsentUpdatedNotification.build(accountInfo);
            logger.info("Sending marketingConsentUpdated notification.");
            notificationService.sendPublicMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);
            notificationService.sendPrivateMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);
            logger.info("marketingConsentUpdated notification sent.");
            return new ResponseEntity<String>("The notification was sent successfully!", HttpStatus.OK);
        } catch (CustomGigyaErrorException ex) {
            if (ex.getErrorCode() == GigyaCodes.UID_NOT_FOUND.getValue()) {
                logger.error(String.format("UID not found : %s", ex.getMessage()));
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            logger.error(String.format("Internal server error : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
