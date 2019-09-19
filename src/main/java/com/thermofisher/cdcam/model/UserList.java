package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class UserList {
   private List<EECUser> eecUsers;
}
