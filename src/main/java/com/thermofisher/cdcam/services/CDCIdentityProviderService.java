package com.thermofisher.cdcam.services;

import javax.annotation.PostConstruct;
import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.aws.SecretsManager;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
    
@Service
@ConditionalOnProperty(prefix = "cdc.main.apiKey", name = "federation")
public class CDCIdentityProviderService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private String userKey;
    private String secretKey;
    private String apiMethod;
    
    @Value("${cdc.main.apiKey.federation}")
    private String apiKey;

    @Value("${cdc.main.credentials}")
    private String cdcKey;

    @Value("${cdc.main.datacenter}")
    private String cdcDataCenter;
    
    @Value("${env.name}")
    private String env;

    @Autowired
    SecretsManager secretsManager;

    @PostConstruct
    public void setCredentials() {
        try {
            if (env.equals("local") || env.equals("test")) return;

            logger.info("Setting up CDC credentials.");
            JSONObject secretProperties = new JSONObject(secretsManager.getSecret(cdcKey));
            secretKey = secretsManager.getProperty(secretProperties, "secretKey");
            userKey = secretsManager.getProperty(secretProperties, "userKey");
        } catch (Exception e) {
            logger.error(String.format("An error occurred while configuring CDC credentials. Error: %s",
                    Utils.stackTraceToString(e)));
        }
    }

    public GSResponse getIdPInformation(String idpName) {
        apiMethod = APIMethods.GET_IDP_INFORMATION.getValue();
        logger.info(String.format("%s triggered. IdP Name: %s", apiMethod, idpName));
        GSRequest request = new GSRequest(apiKey, secretKey, apiMethod, null, true, userKey);
        request.setAPIDomain(cdcDataCenter);
        request.setParam("idpName", idpName);

        return request.send();
    }
}
