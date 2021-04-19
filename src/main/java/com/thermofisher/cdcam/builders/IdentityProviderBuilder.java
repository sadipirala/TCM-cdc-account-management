package com.thermofisher.cdcam.builders;

import java.util.HashMap;
import com.gigya.socialize.GSKeyNotFoundException;
import com.gigya.socialize.GSObject;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IdentityProviderBuilder {
    private Logger logger = LogManager.getLogger(this.getClass());
    
    public IdentityProviderResponse getIdPInformation(GSObject obj) {
        try {
        GSObject config = (GSObject) obj.get("config");
        GSObject attributeMap = config.containsKey("attributeMap") ? (GSObject) config.get("attributeMap") : null;

        return IdentityProviderResponse.builder()
            .name(config.containsKey("name") ? config.getString("name") : "")
            .entityID(config.containsKey("entityID") ? config.getString("entityID") : "")
            .singleSignOnServiceUrl(config.containsKey("singleSignOnServiceUrl") ? config.getString("singleSignOnServiceUrl") : "")
            .singleSignOnServiceBinding(config.containsKey("singleSignOnServiceBinding") ? config.getString("singleSignOnServiceBinding") : "")
            .singleLogoutServiceUrl(config.containsKey("singleLogoutServiceUrl") ? config.getString("singleLogoutServiceUrl") : "")
            .singleLogoutServiceBinding(config.containsKey("singleLogoutServiceBinding") ? config.getString("singleLogoutServiceBinding") : "")
            .nameIDFormat(config.containsKey("nameIDFormat") ? config.getString("nameIDFormat") : "")
            .attributeMap(getArrayNode(attributeMap))
            .certificate(config.containsKey("certificate") ? config.getString("certificate") : "")
            .spSigningAlgorithm(config.containsKey("spSigningAlgorithm") ? config.getString("spSigningAlgorithm") : "")
            .signAuthnRequest(config.containsKey("signAuthnRequest") ? config.getBool("signAuthnRequest") : false)
            .requireSAMLResponseSigned(config.containsKey("requireSAMLResponseSigned") ? config.getBool("requireSAMLResponseSigned") : false)
            .requireAssertionSigned(config.containsKey("requireAssertionSigned") ? config.getBool("requireAssertionSigned") : false)
            .requireAssertionEncrypted(config.containsKey("requireAssertionEncrypted") ? config.getBool("requireAssertionEncrypted") : false)
            .useSessionNotOnOrAfter(config.containsKey("useSessionNotOnOrAfter") ? config.getBool("useSessionNotOnOrAfter") : false)
            .build();

        }catch(Exception e) {
            logger.error(String.format("Error building IdP info object. Message: %s", e.getMessage()));
            return null;
        }
    }

    private HashMap<String, String> getArrayNode(GSObject object) throws GSKeyNotFoundException {
        String[] keys = object.getKeys();
        HashMap<String, String> container = new HashMap<String, String>();

        for (String key : keys) {
            container.put(key, object.containsKey(key) ? object.getString(key) : "");
        }

        return container;
    }
}
