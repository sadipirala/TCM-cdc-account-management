package com.thermofisher.cdcam.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LiteAccountDTO {
    @NotBlank
    @Size(max = 50)
    private String email;
    @Size(max = 30)
    private String firstName;
    @Size(max = 30)
    private String lastName;
    @Size(max = 50)
    private String inviterEmail;
    @Size(max = 2)
    private String location;
    private String clientId;
}
