package com.heindrich.tado.model;

import lombok.*;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class OutsideTemperature {
	private final double celsius;
	private final double fahrenheit;
	private final Date timestamp;
	private final String type;
	private final double celsiusPrecision;
	private final double fahrenheitPrecision;
}
