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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Slf4j
@RequestMapping("/notifications")
public class NotificationController {
    @Autowired
    NotificationService notificationService;

    @Autowired
    GigyaService gigyaService;

    @PostMapping("/emailVerification")
    @Operation(description = "Call sns of type accountUpdated")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "400", description = "Bad Request.")
    })
    public ResponseEntity<String> sendEmailVerificationSNS(@RequestBody EmailVerificationDTO emailVerificationDTO) {
        try {
            String uid = emailVerificationDTO.getUid();
            String previousEmail = emailVerificationDTO.getPreviousEmail();
            log.info("previousEmail: " + previousEmail);
            log.info(String.format("Email verification process for %s started.", uid));
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            log.info("Building AccountUpdatedNotification object.");
            AccountUpdatedNotification accountUpdatedNotification = AccountUpdatedNotification.build(accountInfo);
            if (null != previousEmail && !previousEmail.equalsIgnoreCase(accountInfo.getEmailAddress())) {
                accountUpdatedNotification.setPreviousEmail(emailVerificationDTO.getPreviousEmail());
            }
            log.info("email ~~~~"+accountUpdatedNotification.getEmailAddress());
            log.info("Sending accountUpdated notification.");

            notificationService.sendPublicAccountUpdatedNotification(accountUpdatedNotification);
            notificationService.sendPrivateAccountUpdatedNotification(accountUpdatedNotification);
            log.info("accountUpdated notification sent.");
            return new ResponseEntity<String>("The notification was sent successfully!", HttpStatus.OK);
        } catch (CustomGigyaErrorException ex) {
            log.error(String.format("Bad Request : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/update/marketing/consent")
    @Operation(description = "Notifies downstream systems about a change in a user's marketing consent status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK."),
            @ApiResponse(responseCode = "400", description = "Bad Request.")
    })
    public ResponseEntity<String> notifyMarketingConsentUpdated(@RequestBody UpdateMarketingConsentDTO updateMarketingConsentDTO) {
        try {
            String uid = updateMarketingConsentDTO.getUid();
            log.info(String.format("Marketing consent updated notification started for: %s", uid));
            AccountInfo accountInfo = gigyaService.getAccountInfo(uid);
            log.info("Building marketingConsentUpdated object.");
            MarketingConsentUpdatedNotification marketingConsentUpdatedNotification = MarketingConsentUpdatedNotification.build(accountInfo);
            log.info("Sending marketingConsentUpdated notification.");
            notificationService.sendPublicMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);
            notificationService.sendPrivateMarketingConsentUpdatedNotification(marketingConsentUpdatedNotification);
            log.info("marketingConsentUpdated notification sent.");
            return new ResponseEntity<String>("The notification was sent successfully!", HttpStatus.OK);
        } catch (CustomGigyaErrorException ex) {
            if (ex.getErrorCode() == GigyaCodes.UID_NOT_FOUND.getValue()) {
                log.error(String.format("UID not found : %s", ex.getMessage()));
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            log.error(String.format("Internal server error : %s", ex.getMessage()));
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
