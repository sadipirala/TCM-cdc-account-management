package com.thermofisher.cdcam.model.cdc;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Consent {
	private boolean isConsentGranted;
}
