package com.thermofisher.cdcam.controller;

import java.util.Objects;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotBlank;

import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.services.GigyaService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
@RequestMapping("/identity-provider")
@ConditionalOnProperty(prefix = "cdc.main.apiKey", name = "federation")
public class IdentityProviderController {

    @Autowired
    GigyaService gigyaService;

    @GetMapping("/{identityProvider}")
    @Operation(description = "Gets information about an Identity Provider.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "500", description = "Internal server error.")
    })
    @Parameter(name = "identityProvider", description = "IdP (Identity Provider Name)", required = true)
    public ResponseEntity<IdentityProviderResponse> getIdentityProviderInformation(@PathVariable @NotBlank String identityProvider) {
        log.info("Identity Provider information requested.");
        IdentityProviderResponse idpInformation = gigyaService.getIdPInformation(identityProvider);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("error-details", String.format("Registered idp name %s not found.", identityProvider));
        
        return Objects.nonNull(idpInformation) 
            ? new ResponseEntity<>(idpInformation, HttpStatus.OK)  
            : new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
    }
}