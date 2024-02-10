package com.thermofisher.cdcam.model.cdc;

import com.thermofisher.cdcam.enums.cdc.DataCenter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SearchResponse {
    private CDCSearchResponse cdcSearchResponse;
    private DataCenter dataCenter;
}
