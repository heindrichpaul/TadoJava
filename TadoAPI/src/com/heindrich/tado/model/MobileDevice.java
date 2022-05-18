package com.heindrich.tado.model;

import com.heindrich.tado.TadoConnector;
import lombok.*;

import java.util.Map;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class MobileDevice {
	private final int homeId;
	private final String name;
	private final int id;
	private final Map<String, Object> settings;
	private final MobileLocation location;
	private final DeviceMetadata deviceMetadata;

	public Map<String, Object> getSettings(TadoConnector connector) throws TadoException {
		return connector.getMobileDeviceSettings(this.homeId, this.id);
	}

	public boolean setGeoTracking(boolean enabled, TadoConnector connector) throws TadoException {
		return connector.setGeoTracking(this.homeId, this.id, enabled);
	}
}
