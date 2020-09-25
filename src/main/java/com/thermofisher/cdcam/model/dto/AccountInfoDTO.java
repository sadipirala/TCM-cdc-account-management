package com.thermofisher.cdcam.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Builder
@Getter
@Setter
public class AccountInfoDTO {
    @NotBlank
    private String username;
    @NotBlank
    @Size(max = 30)
    private String firstName;
    @NotBlank
    @Size(max = 30)
    private String lastName;
    @NotBlank
    @Size(max = 50)
    private String emailAddress;
    @NotBlank
    @Size(max = 20)
    private String password;
    private String reCaptchaToken;
    private String localeName;
    @Size(max = 50)
    private String company;
    @Size(max = 50)
    private String department;
    @Size(max = 30)
    private String city;
    private String country;
    private String member;
    private String registrationType;
    private String timezone;
    private String hiraganaName;
}
