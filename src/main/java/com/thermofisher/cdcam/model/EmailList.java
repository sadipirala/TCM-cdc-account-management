package com.thermofisher.cdcam.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class EmailList {
    private List<String> emails;
}
