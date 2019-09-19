package com.thermofisher.cdcam.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class EmailList {
    private List<String> emails;
}
