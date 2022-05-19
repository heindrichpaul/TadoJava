package com.heindrich.tado.model;

import lombok.*;

import java.util.List;
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoDevice {
	private final String deviceType;
	private final String serialNo;
	private final String shortSerialNo;
	private final String currentFwVersion;
	private final TadoConnectionState connectionState;
	private final List<String> capabilities;
	private final Boolean inPairingMode;
	private final String batteryState;
	private final List<String> duties;
}
