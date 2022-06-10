package com.thermofisher.cdcam.controller;

import java.io.UnsupportedEncodingException;

import com.gigya.socialize.GSKeyNotFoundException;
import com.google.gson.JsonParseException;
import com.thermofisher.cdcam.model.cdc.CustomGigyaErrorException;
import com.thermofisher.cdcam.model.cdc.OpenIdRelyingParty;
import com.thermofisher.cdcam.model.dto.CIPAuthDataDTO;
import com.thermofisher.cdcam.services.CookieService;
import com.thermofisher.cdcam.services.EncodeService;
import com.thermofisher.cdcam.services.GigyaService;
import com.thermofisher.cdcam.services.IdentityAuthorizationService;
import com.thermofisher.cdcam.services.URLService;
import com.thermofisher.cdcam.utils.Utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/identity/registration")
public class RegistrationController {
    private Logger logger = LogManager.getLogger(this.getClass());
    private static final String REQUEST_EXCEPTION_HEADER = "Request-Exception";

    @Value("${identity.oidc.rp.id}")
    String tfComClientId;

    @Value("${identity.registration.create-account-endpoint.path}")
    String createAccountEndpointPath;

    @Value("${identity.registration.get-login-endpoint.path}")
    String getOidcLoginEndpointPath;

    @Value("${identity.authorization.path}")
    String cipAuthdataAuthorizationPath;

    @Value("${identity.oidc.default.scope}")
    String identityScope;

    @Value("${identity.oidc.identity.authorization.redirect_uri}")
    String identityRedirectUri;

    @Value("${identity.oidc.response_type}")
    String identityResponseType;

    @Autowired
    GigyaService gigyaService;

    @Autowired
    CookieService cookieService;

    @Autowired
    EncodeService encodeService;

    @Autowired
    URLService urlService;

    @Autowired
    IdentityAuthorizationService identityAuthorizationService;

    @GetMapping(value = {"/oidc/rp", "/rp"})
    @ApiOperation(value = "Validate RP Client Id and Redirect Uri")
    @ApiResponses({
        @ApiResponse(code = 302, message = "RP config found, will set cookie data and redirect to Registration page."),
        @ApiResponse(code = 400, message = "Bad request, missing or invalid params."),
        @ApiResponse(code = 404, message = "clientId not found."),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "client_id", value = "RP Client ID", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "redirect_uri", value = "URL to redirect", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "state", value = "State", dataType = "String", paramType = "query", required = false),
        @ApiImplicitParam(name = "response_type", value = "Response Type", required = true, dataType = "String", paramType = "query"),
        @ApiImplicitParam(name = "scope", value = "Scope", required = true, dataType = "String", paramType = "query")
    })
    public ResponseEntity<?> getRPRegistrationConfig(
        @RequestParam("client_id") String clientId,
        @RequestParam("redirect_uri") String redirectUri,
        @RequestParam(name = "state", required = false) String state,
        @RequestParam("response_type") String responseType,
        @RequestParam("scope") String scope
        ) throws UnsupportedEncodingException {

            if (!Utils.isNullOrEmpty(state)) {
                state = encodeService.decodeUTF8(state);
            }

            CIPAuthDataDTO cipAuthData = CIPAuthDataDTO.builder()
                .clientId(clientId)
                .redirectUri(redirectUri)
                .state(state)
                .responseType(responseType)
                .scope(scope)
                .build();

            logger.info("Get RP Process started");
            if (cipAuthData.areClientIdAndRedirectUriInvalid()) {
                logger.error("Either clientId or redirectURI missing");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }

            try {
                boolean uriExists = false;
                logger.info("Getting RP data");
                OpenIdRelyingParty openIdRelyingParty = gigyaService.getRP(cipAuthData.getClientId());
                for (String uri : openIdRelyingParty.getRedirectUris()) {
                    logger.info(String.format("Find %s in OpenId redirectURIs", cipAuthData.getRedirectUri()));
                    if (uri.equalsIgnoreCase(cipAuthData.getRedirectUri())) {
                        uriExists = true;
                        logger.info(String.format("%s was found", cipAuthData.getRedirectUri()));
                        break;
                    }
                }

                if (!uriExists) {
                    String error = String.format("%s was not found in RP URIs", cipAuthData.getRedirectUri());
                    logger.error(error);
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, error).build();
                }

                logger.info("Building cip_authdata cookie to get login endpoint.");
                String cipAuthDataForLogin = cookieService.createCIPAuthDataCookie(cipAuthData, getOidcLoginEndpointPath);
                logger.info("Building cip_authdata cookie for the create account endpoint.");
                String cipAuthDataForRegistration = cookieService.createCIPAuthDataCookie(cipAuthData, createAccountEndpointPath);
                logger.info("cip_authdata cookies built.");

                return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataForLogin)
                    .header(HttpHeaders.SET_COOKIE, cipAuthDataForRegistration)
                    .header(HttpHeaders.LOCATION, "/global-registration/registration")
                    .build();
            }
            catch (CustomGigyaErrorException customGigyaException) {
                if (customGigyaException.getMessage().contains("404000")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
                }
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).header(REQUEST_EXCEPTION_HEADER, customGigyaException.getMessage()).body(null);
            } catch (GSKeyNotFoundException gsKeyNotFoundException) {
                logger.error(String.format("GSKeyNotFoundException: %s", gsKeyNotFoundException.getMessage()));
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
    }

    private boolean isTfcomClientId(String clientId) {
        return clientId.equals(tfComClientId);
    }

    @GetMapping("/redirect/login")
    @ApiOperation(value = "Redirect to the Login URL. Validates cip_authdata cookie.")
    @ApiResponses({
        @ApiResponse(code = 302, message = "Redirects to Login URL if cip_authdata cookie is valid. redirectUrl query param with default tf.com config is used otherwise."),
        @ApiResponse(code = 400, message = "Bad request, missing or invalid params."),
        @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParams({
        @ApiImplicitParam(name = "redirectUrl", value = "URL to redirect, only used to redirect if cip_authdata cookie doesn't exist", required = false, dataType = "String", paramType = "query")
    })
    public ResponseEntity<?> redirectLoginAuth(@CookieValue(name = "cip_authdata", required = false) String cipAuthData, @RequestParam(required = false) String redirectUrl, @RequestParam(required = false) boolean isSignInUrl) throws UnsupportedEncodingException {
        logger.info("Validation for redirection started");
        try {
            if (ObjectUtils.isEmpty(cipAuthData) && ObjectUtils.isNotEmpty(redirectUrl) && !isSignInUrl) {
                logger.info("Cookie not present.");
                logger.info(String.format("Using default tf.com configuration and returning URL %s", redirectUrl));
                String loginAuthUrl = identityAuthorizationService.generateRedirectAuthUrl(redirectUrl);
                String state = identityAuthorizationService.buildDefaultStateProperty(redirectUrl);
                logger.info(String.format("Building cip_authdata cookie in path %s", cipAuthdataAuthorizationPath));

                CIPAuthDataDTO cipAuthDataDTO = CIPAuthDataDTO.builder()
                        .clientId(tfComClientId)
                        .redirectUri(identityRedirectUri)
                        .state(state)
                        .responseType(identityResponseType)
                        .scope(identityScope)
                        .build();

                logger.info("cip_authdata cookie built.");
                String newCipAuthData = cookieService.createCIPAuthDataCookie(cipAuthDataDTO, cipAuthdataAuthorizationPath);

                return ResponseEntity
                        .status(HttpStatus.OK)
                        .header(HttpHeaders.SET_COOKIE, newCipAuthData)
                        .body(loginAuthUrl);
            } else if (ObjectUtils.isNotEmpty(cipAuthData)) {
                logger.info("Decoding cip_authdata.");
                CIPAuthDataDTO cipAuthDataDTO = cookieService.decodeCIPAuthDataCookie(cipAuthData);

                logger.info("Validating cip_authdata.");
                if (cipAuthDataDTO.isCipAuthDataValid()) {
                    String expectedReturnCookie = cookieService.createCIPAuthDataCookie(cipAuthDataDTO, cipAuthdataAuthorizationPath);
                    if (!Utils.isNullOrEmpty(cipAuthDataDTO.getState())) {
                        String state = cipAuthDataDTO.getState();
                        // TODO: remove temporary fix to redirect to thank you page while coming from login page for tfcom
                        if (isTfcomClientId(cipAuthDataDTO.getClientId()) && !isSignInUrl) {
                            state = identityAuthorizationService.buildDefaultStateProperty(redirectUrl);
                            logger.info("Building CIP_AUTHDATA cookie with new state for tfcom");
                            cipAuthDataDTO.setState(state);
                            expectedReturnCookie = cookieService.createCIPAuthDataCookie(cipAuthDataDTO, cipAuthdataAuthorizationPath);
                            logger.info("CIP_AUTHDATA cookie built.");
                        }
                        logger.info(String.format("state: %s", state));
                        String encodedState = encodeService.encodeUTF8(state);
                        cipAuthDataDTO.setState(encodedState);
                    }

                    String authRedirectUrl = urlService.queryParamMapper(cipAuthDataDTO);
                    logger.info("Returning Login URL with parameters from cookie.");
                    return ResponseEntity
                            .status(HttpStatus.OK)
                            .header(HttpHeaders.SET_COOKIE, expectedReturnCookie)
                            .body(authRedirectUrl);
                }

                logger.info("Invalid cip_authdata. Bad request.");
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .build();
            } else {
                logger.info("Generating default redirect Sign in URL");
                String loginAuthUrl = identityAuthorizationService.generateDefaultRedirectSignInUrl();
                return ResponseEntity
                        .status(HttpStatus.OK)
                        .body(loginAuthUrl);
            }
        } catch (JsonParseException j) {
            logger.error(String.format("JsonParseException: %s", Utils.stackTraceToString(j)));

            logger.info("Invalid cip_authdata. Bad request.");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        } catch (Exception e) {
            logger.error(String.format("An error occurred: %s", Utils.stackTraceToString(e)));
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }
}
