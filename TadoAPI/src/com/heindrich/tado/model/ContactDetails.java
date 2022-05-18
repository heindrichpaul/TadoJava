package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class ContactDetails {
	private final String name;
	private final String email;
	private final String phone;
}
