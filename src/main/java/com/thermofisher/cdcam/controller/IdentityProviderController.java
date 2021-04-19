package com.thermofisher.cdcam.controller;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import com.thermofisher.cdcam.model.identityProvider.IdentityProviderResponse;
import com.thermofisher.cdcam.utils.Utils;
import com.thermofisher.cdcam.utils.cdc.CDCResponseHandler;
import org.apache.logging.log4j.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import io.swagger.annotations.*;

@RestController
@RequestMapping("/identity-provider")
@ConditionalOnProperty(prefix = "cdc.main.apiKey", name = "federation")
public class IdentityProviderController {
    private Logger logger = LogManager.getLogger(this.getClass());
    
    @Autowired
    CDCResponseHandler cdcResponseHandler;

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
        IdentityProviderResponse idpInformation = cdcResponseHandler.getIdPInformation(identityProvider);
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("error-details", String.format("Registered idp name %s not found.", identityProvider));
        
        return Objects.nonNull(idpInformation) 
            ? new ResponseEntity<>(idpInformation, HttpStatus.OK)  
            : new ResponseEntity<>(headers, HttpStatus.BAD_REQUEST);
    }

}