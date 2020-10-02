package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Builder
@Getter
@Setter
public class AccountInfo {
    private String uid;
    @NotBlank
    private String username;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String emailAddress;
    @NotBlank
    private String password;
    private String jobRole;
    private String interest;
    private String localeName;
    private String company;
    private String department;
    private String city;
    private String country;
    private String phoneNumber;
    private String member;
    private String loginProvider;
    private int regAttempts;
    private String duplicatedAccountUid;
    private String registrationType;
    private String timezone;
    private String hiraganaName;
}
