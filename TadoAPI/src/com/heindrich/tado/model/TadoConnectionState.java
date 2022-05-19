package com.heindrich.tado.model;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoConnectionState {
	private final boolean value;
	private final LocalDateTime timestamp;
}
