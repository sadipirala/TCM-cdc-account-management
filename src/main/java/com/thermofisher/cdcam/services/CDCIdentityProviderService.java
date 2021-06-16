package com.thermofisher.cdcam.services;

import javax.annotation.PostConstruct;

import com.gigya.socialize.GSRequest;
import com.gigya.socialize.GSResponse;
import com.thermofisher.cdcam.enums.aws.CdcamSecrets;
import com.thermofisher.cdcam.enums.cdc.APIMethods;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
    
@Service
@ConditionalOnProperty(prefix = "cdc.main.apiKey", name = "federation")
public class CDCIdentityProviderService {
    private Logger logger = LogManager.getLogger(this.getClass());
    private String apiMethod;
    private String cdcSecretKey;
    private final boolean useHTTPS = true;
    
    @Value("${cdc.main.apiKey.federation}")
    private String apiKey;

    @Value("${cdc.main.datacenter}")
    private String cdcDataCenter;
    
    @Value("${env.name}")
    private String env;

    @Autowired
    SecretsService secretsService;

    @PostConstruct
    public void setCredentials() {
        try {
            if (env.equals("local") || env.equals("test")) return;

            logger.info("Setting up CDC credentials.");
            cdcSecretKey = secretsService.get(CdcamSecrets.MAIN_DC.getKey());
        } catch (Exception e) {
            logger.error(String.format("An error occurred while configuring CDC credentials. Error: %s", Utils.stackTraceToString(e)));
        }
    }

    public GSResponse getIdPInformation(String idpName) {
        apiMethod = APIMethods.GET_IDP_INFORMATION.getValue();
        logger.info(String.format("%s triggered. IdP Name: %s", apiMethod, idpName));
        GSRequest request = new GSRequest(apiKey, cdcSecretKey, apiMethod, useHTTPS);
        request.setAPIDomain(cdcDataCenter);
        request.setParam("idpName", idpName);

        return request.send();
    }
}
