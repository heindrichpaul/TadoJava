package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoDazzleMode {
	private final boolean supported;
	private final boolean enabled;
}
