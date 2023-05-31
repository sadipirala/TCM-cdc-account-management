package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.properties.EmailVerificationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.thermofisher.cdcam.enums.cdc.GigyaCodes;
import com.thermofisher.cdcam.model.cdc.CDCResponseData;
import com.thermofisher.cdcam.utils.Utils;

import static com.thermofisher.cdcam.properties.EmailVerificationProperties.*;

@Service
public class EmailVerificationService {
    private final static Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);

    @Autowired
    GigyaService gigyaService;

    /**
     * Retrieve value for {@link EmailVerificationProperties#VERIFICATION_PENDING_FIELD} based on the configured properties
     * in {@link EmailVerificationProperties}.
     * @param countryCode   The country code the new account selected during account creation.
     * @return              The value used for {@link EmailVerificationProperties#VERIFICATION_PENDING_FIELD}. Can be either
     *                      {@link EmailVerificationProperties#DEFAULT_VERIFIED_DATE} or
     *                      {@link EmailVerificationProperties#ENFORCE_EMAIL_VERIFICATION_DATE}
     */
    public static String getDefaultVerifiedDate(String countryCode) {
        // Return default value when email verification flag is disabled globally
        if (!EmailVerificationProperties.isEnabled()) {
            logger.info("Email verification feature is disabled globally. Setting default value: {}", DEFAULT_VERIFIED_DATE);
            return DEFAULT_VERIFIED_DATE;
        }

        // Return ENFORCE_EMAIL_VERIFICATION_DATE regardless of country since feature is set to global
        if (EmailVerificationProperties.isGlobal()) {
            logger.info("Email verification feature is enabled globally. Setting value to enforce feature.");
            return ENFORCE_EMAIL_VERIFICATION_DATE;
        }

        // Return default value when country exists in exclusion list
        if (EmailVerificationProperties.getExcludedCountries().contains(countryCode)) {
            logger.info("Email verification is excluded for provided country code '{}'. Setting default value: {}", countryCode, DEFAULT_VERIFIED_DATE);
            return DEFAULT_VERIFIED_DATE;
        }

        // Return ENFORCE_EMAIL_VERIFICATION_DATE when country listed as required verification flow
        if (EmailVerificationProperties.getIncludedCountries().contains(countryCode)) {
            logger.info("Email verification is enforced for provided country code '{}'. Setting value to enforce feature.", countryCode);
            return ENFORCE_EMAIL_VERIFICATION_DATE;
        }

        // Return default value given country is not listed on any list, yet feature is not global.
        logger.info("Email verification is not enforced for provided country code '{}' but feature is not global. Setting default value: {}", countryCode, DEFAULT_VERIFIED_DATE);
        return DEFAULT_VERIFIED_DATE;
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
