package com.heindrich.tado.model;

import lombok.*;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class SolarIntensity {
	private final String type;
	private final double percentage;
	private final Date timestamp;
}
