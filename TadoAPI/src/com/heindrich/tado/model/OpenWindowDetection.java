package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class OpenWindowDetection {
	private final boolean supported;
	private final boolean enabled;
	private final int timeoutInSeconds;
}
