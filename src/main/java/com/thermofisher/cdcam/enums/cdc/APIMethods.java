package com.thermofisher.cdcam.enums.cdc;

public enum APIMethods {
    GET("accounts.getAccountInfo"),
    GET_IDP_INFORMATION("fidm.saml.getRegisteredIdP"),
    GET_JWT_PUBLIC_KEY("accounts.getJWTPublicKey"),
    INIT_REGISTRATION("accounts.initRegistration"),
    IS_AVAILABLE_LOGINID("accounts.isAvailableLoginID"),
    REGISTER("accounts.register"),
    RESET_PASSWORD("accounts.resetPassword"),
    SEARCH("accounts.search"),
    SEND_VERIFICATION_EMAIL("accounts.resendVerificationCode"),
    SET_ACCOUNT_INFO("accounts.setAccountInfo"),
    GET_RP("fidm.oidc.op.getRP"),
    FINALIZE_REGISTRATION("accounts.finalizeRegistration");

    private String value;

    APIMethods(String value) { this.value = value; }

    public String getValue() { return this.value; }
}
