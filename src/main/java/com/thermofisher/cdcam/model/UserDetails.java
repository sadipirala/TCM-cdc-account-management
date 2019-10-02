package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class UserDetails {
    private String uid;
    private String email;
    private String firstName;
    private String lastName;
    private int associatedAccounts;
}
