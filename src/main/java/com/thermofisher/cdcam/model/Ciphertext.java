package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Ciphertext {
    private String firstName;
    private String lastName;
    private String email;
    private String source;
}