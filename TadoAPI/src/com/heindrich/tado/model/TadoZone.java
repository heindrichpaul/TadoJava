package com.heindrich.tado.model;

import com.heindrich.tado.TadoConnector;
import lombok.*;

import java.util.Date;
import java.util.List;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoZone {
	private final int homeId;
	private final int id;
	private final String name;
	private final String type;
	private final Date dateCreated;
	private final List<String> deviceTypes;
	private final List<TadoDevice> devices;
	private final boolean reportAvailable;
	private final boolean supportsDazzle;
	private final boolean dazzleEnabled;
	private final TadoDazzleMode dazzleMode;
	private final OpenWindowDetection openWindowDetection;

	public TadoZoneState getState(TadoConnector connector) throws TadoException {
		return connector.getZoneState(this.homeId, this.id);
	}

	public Capability getCapabilities(TadoConnector connector) throws TadoException {
		return connector.getZoneCapabilities(this.homeId, this.id);
	}

	public boolean getEarlyStart(TadoConnector connector) throws TadoException {
		return connector.getZoneEarlyStart(this.homeId, this.id);
	}

	public boolean setEarlyStart(boolean enabled, TadoConnector connector) throws TadoException {
		return connector.setZoneEarlyStart(this.homeId, this.id, enabled);
	}

	public TadoOverlay getOverlay(TadoConnector connector) throws TadoException {
		return connector.getZoneOverlay(this.homeId, this.id);
	}

	public TadoOverlay setOverlay(TadoOverlay overlay, TadoConnector connector) throws TadoException {
		return connector.setZoneOverlay(this.homeId, this.id, overlay);
	}

	public void deleteOverlay(TadoConnector connector) throws TadoException {
		connector.deleteZoneOverlay(this.homeId, this.id);
	}
}
