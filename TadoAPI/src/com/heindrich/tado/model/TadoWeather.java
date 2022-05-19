package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoWeather {
	private final SolarIntensity solarIntensity;
	private final OutsideTemperature outsideTemperature;
	private final WeatherState weatherState;
}
