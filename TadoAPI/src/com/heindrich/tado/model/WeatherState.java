package com.heindrich.tado.model;

import lombok.*;

import java.util.Date;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class WeatherState {
	private final String type;
	private final String value;
	private final Date timestamp;
}
