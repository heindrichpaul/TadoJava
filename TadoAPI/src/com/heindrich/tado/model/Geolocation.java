package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Geolocation {
	private final double latitude;
	private final double longitude;
}
