package com.thermofisher.cdcam.model.cdc;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CDCNewAccount {
    private String username;
    private String email;
    private String password;
    private String data;
    private String profile;

    public static CDCNewAccount build(String username, String email, String password, Data data, Profile profile) {
        String dataString = new Gson().toJson(data);
        String profileString = new Gson().toJson(profile);

        return CDCNewAccount.builder()
                .username(username)
                .email(email)
                .password(password)
                .data(dataString)
                .profile(profileString)
                .build();
    }
}
