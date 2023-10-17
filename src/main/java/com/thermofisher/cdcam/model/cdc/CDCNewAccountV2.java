package com.thermofisher.cdcam.model.cdc;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class CDCNewAccountV2 {
    private String username;
    private String email;
    private String password;
    private String data;
    private String profile;
    private String preferences;

    public static CDCNewAccountV2 build(String username, String email, String password, Data data, Profile profile, Preferences preferences) {
        String dataString = new Gson().toJson(data);
        String profileString = new Gson().toJson(profile);
        String preferencesString = new Gson().toJson(preferences);

        return CDCNewAccountV2.builder()
                .username(username)
                .email(email)
                .password(password)
                .data(dataString)
                .profile(profileString)
                .preferences(preferencesString)
                .build();
    }
}
