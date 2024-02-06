package com.thermofisher.cdcam.controller;

import com.gigya.socialize.GSKeyNotFoundException;
import com.google.gson.JsonParseException;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.LoginService;
import com.thermofisher.cdcam.services.URLService;
import com.thermofisher.cdcam.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;


@RestController
@Slf4j
@RequestMapping("/identity/registration")
public class RegistrationController {

    private static final String REQUEST_EXCEPTION_HEADER = "Request-Exception";

    @Value("${identity.oidc.rp.id}")
    String tfComClientId;

    @Value("${identity.registration.create-account-endpoint.path}")
    String createAccountEndpointPath;

    @Value("${identity.registration.get-login-endpoint.path}")
    String getOidcLoginEndpointPath;

    @Value("${identity.authorization.path}")
    String cipAuthdataAuthorizationPath;

    @Value("${default.login.path}")
    String loginEndpoint;

    @Autowired
    GigyaService gigyaService;

    @Autowired
    CookieService cookieService;

    @Autowired
    EncodeService enresponseCodeService;

    @Autowired
    URLService urlService;

    @Autowired
    LoginService loginService;

    @GetMapping(value = {"/oidc/rp", "/rp"})
    @Operation(description = "Validate RP Client Id and Redirect Uri")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "RP config found, will set cookie data and redirect to Registration page."),
            @ApiResponse(responseCode = "400", description = "Bad request, missing or invalid params."),
            @ApiResponse(responseCode = "404", description = "clientId not found."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameters({
            @Parameter(name = "client_id", description = "RP Client ID", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "redirect_uri", description = "URL to redirect", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "state", description = "State", schema = @Schema(type = "string"), in = ParameterIn.QUERY, required = false),
            @Parameter(name = "response_type", description = "Response Type", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY),
            @Parameter(name = "scope", description = "Scope", required = true, schema = @Schema(type = "string"), in = ParameterIn.QUERY)
    })
    public ResponseEntity<?> getRPRegistrationConfig(
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(name = "state", required = false) String state,
            @RequestParam("response_type") String responseType,
            @RequestParam("scope") String scope
    ) throws UnsupportedEncodingException {

        if (!Utils.isNullOrEmpty(state)) {
            state = enresponseCodeService.decodeUTF8(state);
        }

        CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(state)
                .responseType(responseType)
                .scope(scope)
                .build();

        log.info("Get RP Process started");
        if (cipAuthData.areClientIdAndRedirectUriInvalid()) {
            log.error("Either clientId or redirectURI missing");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            boolean uriExists = false;
            log.info("Getting RP data");
            OpenIdRelyingParty openIdRelyingParty = gigyaService.getRP(cipAuthData.getClientId());
            for (String uri : openIdRelyingParty.getRedirectUris()) {
                log.info(String.format("Find %s in OpenId redirectURIs", cipAuthData.getRedirectUri()));
                if (uri.equalsIgnoreCase(cipAuthData.getRedirectUri())) {
                    uriExists = true;
                    log.info(String.format("%s was found", cipAuthData.getRedirectUri()));
                    break;
                }
            }

            if (!uriExists) {
                String error = String.format("%s was not found in RP URIs", cipAuthData.getRedirectUri());
                log.error(error);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, error).build();
            }

            log.info("Building cip_authdata cookie to get login endpoint.");
            String cipAuthDataForLogin = cookieService.createCIPAuthDataCookie(cipAuthData, getOidcLoginEndpointPath);
            log.info("Building cip_authdata cookie for the create account endpoint.");
            String cipAuthDataForRegistration = cookieService.createCIPAuthDataCookie(cipAuthData, createAccountEndpointPath);
            log.info("cip_authdata cookies built.");

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataForLogin)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataForRegistration)
                    .header(HttpHeaders.LOCATION, "/global-registration/registration")
                    .build();
        } catch (CustomGigyaErrorException customGigyaException) {
            if (customGigyaException.getMessage().contains("404000")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, customGigyaException.getMessage()).body(null);
        } catch (GSKeyNotFoundException gsKeyNotFoundException) {
            log.error(String.format("GSKeyNotFoundException: %s", gsKeyNotFoundException.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private boolean isDefaultClientId(String clientId) {
        return clientId.equals(tfComClientId);
    }

    @GetMapping("/redirect/login")
    @Operation(description = "Redirect to the Login URL. Validates cip_authdata cookie.")
    @ApiResponses({
            @ApiResponse(responseCode = "302", description = "Redirects to Login URL if cip_authdata cookie is valid. redirectUrl query param with default tf.com config is used otherwise."),
            @ApiResponse(responseCode = "400", description = "Bad request, missing or invalid params."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameters({
            @Parameter(name = "redirectUrl", description = "URL to redirect, only used to redirect if cip_authdata cookie doesn't exist", required = false, in = ParameterIn.QUERY, schema = @Schema(type = "string"))
    })
    public ResponseEntity<?> redirectLoginAuth(
            @CookieValue(name = "cip_authdata", required = false) String cipAuthData,
            @RequestParam(required = false) String redirectUrl,
            @RequestParam(required = false) boolean isSignInUrl
    ) throws UnsupportedEncodingException {
        log.info("Validation for redirection started");
        try {
            if (ObjectUtils.isEmpty(cipAuthData) && ObjectUtils.isNotEmpty(redirectUrl) && !isSignInUrl) {
                log.info("Cookie not present.");
                log.info(String.format("Using default tf.com configuration and returning URL %s", redirectUrl));
                String loginAuthUrl = loginService.generateDefaultLoginUrl(redirectUrl);
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(loginAuthUrl);
            } else if (ObjectUtils.isNotEmpty(cipAuthData)) {
                log.info("Decoding cip_authdata.");
                CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(cipAuthData);

                // #region Custom RP implementation. Temporarily here as this endpoint was moved to the Idenity API.

                if (cipAuthDataDTO.hasOidcCustomProperties()) {
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .body(cipAuthDataDTO.getSignInRedirectUri());
                }

                // #endregion 

                log.info("Validating cip_authdata.");
                if (cipAuthDataDTO.isCipAuthDataValid()) {
                    String loginUrl;
                    String expectedReturnCookie = cookieService.createCIPAuthDataCookie(cipAuthDataDTO, cipAuthdataAuthorizationPath);
                    if (!Utils.isNullOrEmpty(cipAuthDataDTO.getState())) {
                        String state = cipAuthDataDTO.getState();
                        // TODO: remove temporary fix to redirect to thank you page while coming from login page for tfcom
                        if (isDefaultClientId(cipAuthDataDTO.getClientId()) && !isSignInUrl) {
                            log.info("Returning login url for default RP");
                            loginUrl = loginService.generateDefaultLoginUrl(redirectUrl);
                            return ResponseEntity
                                    .status(HttpStatus.OK)
                                    .body(loginUrl);
                        }
                        log.info(String.format("state: %s", state));
                        String encodedState = enresponseCodeService.encodeUTF8(state);
                        cipAuthDataDTO.setState(encodedState);
                    }

                    loginUrl = urlService.queryParamMapper(cipAuthDataDTO);
                    log.info("Returning Login URL with parameters from cookie.");
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .header(HttpHeaders.SET_COOKIE, expectedReturnCookie)
                            .body(loginUrl);
                }

                log.info("Invalid cip_authdata. Bad request.");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            } else {
                log.info("Generating default redirect Sign in URL");
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(loginEndpoint);
            }
        } catch (JsonParseException j) {
            log.error(String.format("JsonParseException: %s", Utils.stackTraceToString(j)));

            log.info("Invalid cip_authdata. Bad request.");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            log.error(String.format("An error occurred: %s", Utils.stackTraceToString(e)));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
