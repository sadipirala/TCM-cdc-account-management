package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AccountInfo {
    private String username;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private String password;
    private String localeName;
    private String company;
    private String department;
    private String city;
    private String country;
    private String member;
    private String loginProvider;
    private int regAttepmts;
}
