package com.heindrich.tado.model;

import lombok.*;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoInstallation {
	private final int id;
	private final String type;
	private final int revision;
	private final String state;
	private final List<TadoDevice> devices;
}
