package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Address {
	private final String addressLine1;
	private final String addressLine2;
	private final String zipCode;
	private final String city;
	private final String state;
	private final String country;
}
