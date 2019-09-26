package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccountInfo {
    private String uid;
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
    private int regAttempts;
}
