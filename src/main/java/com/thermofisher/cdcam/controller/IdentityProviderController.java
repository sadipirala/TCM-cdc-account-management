package com.thermofisher.cdcam.controller;

import java.util.Objects;

import javax.validation.constraints.NotBlank;

import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.GigyaService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/identity-provider")
@ConditionalOnProperty(prefix = "cdc.main.apiKey", name = "federation")
public class IdentityProviderController {
    private Logger logger = LogManager.getLogger(this.getClass());
    
    @Autowired
    GigyaService gigyaService;

    @GetMapping("/{identityProvider}")
    @ApiOperation(value = "Gets information about an Identity Provider.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request."),
            @ApiResponse(code = 500, message = "Internal server error.")
    })
    @ApiImplicitParam(name = "identityProvider", value = "IdP (Identity Provider Name)", required = true)
    public ResponseEntity<IdentityProviderResponse> getIdentityProviderInformation(@PathVariable @NotBlank String identityProvider) {
        logger.info("Identity Provider information requested.");
        IdentityProviderResponse idpInformation = gigyaService.getIdPInformation(identityProvider);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("error-details", String.format("Registered idp name %s not found.", identityProvider));
        
        return Objects.nonNull(idpInformation) 
            ? new ResponseEntity<>(idpInformation, HttpStatus.OK)  
            : new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
    }
}