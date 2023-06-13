package com.thermofisher.cdcam.services;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.utils.Utils;

@Service
public class EmailVerificationService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static String VERIFICATION_PENDING_FIELD = "data.verifiedEmailDate";

    @Autowired
    GigyaService gigyaService;

    public static String getDefaultVerifiedDate(String countryCode) {
        String DEFAULT_VERIFIED_DATE = "0001-01-01";
        List<String> EMAIL_VERIFICATION_COUNTRIES = Arrays.asList("ca", "ar", "bo", "br", "cl", "co", "cr", "ec", "sv", "gt", "hn", "mx", "ni", "pa", "py", "pe", "do", "uy", "at", "be", "cz", "dk", "fi", "fr", "de", "hu", "it", "lu", "nl", "no", "pl", "sk", "es", "se", "ch", "uk", "ru");
        return EMAIL_VERIFICATION_COUNTRIES.contains(countryCode) ? null : DEFAULT_VERIFIED_DATE;
    }
    
    public static boolean isVerificationPending(CDCResponseData cdcResponseData) {
        return cdcResponseData.getErrorCode() == GigyaCodes.ACCOUNT_PENDING_REGISTRATION.getValue()
            && cdcResponseData.getErrorDetails().contains(VERIFICATION_PENDING_FIELD);
    }

    @Async
    public void sendVerificationByLinkEmail(String uid) {
        sendVerificationEmail(uid);
    }

    public CDCResponseData sendVerificationByLinkEmailSync(String uid) {
        return sendVerificationEmail(uid);
    }

    private CDCResponseData sendVerificationEmail(String uid) {
        CDCResponseData response = new CDCResponseData();

        try {
            response = gigyaService.sendVerificationEmail(uid);
            HttpStatus status = HttpStatus.valueOf(response.getStatusCode());

            if (status.is2xxSuccessful()) {
                logger.info("Verification email sent successfully to UID: {}", uid);
            } else {
                logger.info("Something went wrong while sending the verification email. UID: {}. Status: {}. Error: {}", uid, status.value(), response.getErrorDetails());
            }
        } catch (Exception e) {
            logger.error("An exception occurred while sending the verification email to the user. UID: {}. Exception: {}", uid, Utils.stackTraceToString(e));
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return response;
    }
}
