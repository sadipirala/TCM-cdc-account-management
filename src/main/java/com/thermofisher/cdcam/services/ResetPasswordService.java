package com.thermofisher.cdcam.services;

import com.thermofisher.cdcam.model.AccountInfo;
import com.thermofisher.cdcam.model.ResetPasswordConfirmation;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class ResetPasswordService {
    private Logger logger = LogManager.getLogger(this.getClass());

    @Value("${tf.home}")
    private String redirectUrl;

    @Value("${tfrn.email-notification.url}")
    private String emailNotificationUrl;

    @Value("${supported.locales}")
    private String supportedLocales;

    @Autowired
    HttpService httpService;

    @Async
    public void sendResetPasswordConfirmation(AccountInfo accountInfo) throws IOException {
        updateLocale(accountInfo);
        ResetPasswordConfirmation request = new ResetPasswordConfirmation().build(accountInfo, redirectUrl);
        JSONObject requestBody = new JSONObject(request);

        CloseableHttpResponse response = (httpService.post(emailNotificationUrl, requestBody)).getCloseableHttpResponse();
        HttpEntity responseEntity = response.getEntity();

        if (responseEntity != null) {
            int status = response.getStatusLine().getStatusCode();
            HttpStatus httpStatus = HttpStatus.valueOf(status);

            if (httpStatus.is2xxSuccessful()) {
                logger.info(String.format("Reset Password confirmation email sent to: %s", accountInfo.getEmailAddress()));
            } else {
                logger.warn(String.format("Something went wrong while sending the Reset Password confirmation email to: %s. Status: %d",
                        accountInfo.getEmailAddress(), status));
            }

        } else {
            logger.error(String.format("Something went wrong while connecting to the email notification service. UID: %s", accountInfo.getUid()));
            throw new IOException();
        }
    }

    private void updateLocale(AccountInfo account) {
        final String chinaLocale = "zh-cn";
        final String chineseTemplateMapping = "zh_CN";

        List<String> supportedLocaleList = Arrays.asList(supportedLocales.toUpperCase().split(","));

        String locale = account.getLocaleName();
        String country = account.getCountry();

        if (locale == null) return;

        if (supportedLocaleList.contains(locale.toUpperCase())) {
            String[] localeSections = locale.split("_");
            account.setLocaleName(String.format("%s_%s", localeSections[0].toLowerCase(), localeSections[1].toUpperCase()));
            return;
        }

        if (locale.equals(chinaLocale)) {
            account.setLocaleName(chineseTemplateMapping);
            return;
        }

        if (country != null) {
            account.setLocaleName(String.format("%s_%s", locale, country.toUpperCase()));
        }
    }
}
