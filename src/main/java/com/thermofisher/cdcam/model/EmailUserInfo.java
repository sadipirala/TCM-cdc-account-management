package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailUserInfo {
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String redirectUrl;
}
