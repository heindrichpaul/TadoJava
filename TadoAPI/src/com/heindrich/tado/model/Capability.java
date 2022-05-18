package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Capability {
	private final String type;
	private final String key;
	private final Object value;
}
