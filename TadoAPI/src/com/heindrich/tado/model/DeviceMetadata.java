package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class DeviceMetadata {
	private final String platform;
	private final String osVersion;
	private final String model;
	private final String locale;
}
