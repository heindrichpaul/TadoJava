package com.heindrich.tado.model;

import com.heindrich.tado.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoHome {
	private final int id;
	private final String name;
	private final String dateTimeZone;
	private final Date dateCreated;
	private final String temperatureUnit;
	private final boolean installationCompleted;
	private final boolean simpleSmartScheduleEnabled;
	private final double awayRadiusInMeters;
	private final boolean usePreSkillsApps;
	private final boolean christmasModeEnabled;
	private final ContactDetails contactDetails;
	private final Address address;
	private final Geolocation geolocation;
	private final boolean consentGrantSkippable;

	public List<TadoZone> getZones(TadoConnector connector) throws TadoException {
		return connector.getZones(this.id);
	}

	public TadoState getState(TadoConnector connector) throws TadoException {
		return connector.getHomeState(this.id);
	}

	public TadoWeather getWeather(TadoConnector connector) throws TadoException {
		return connector.getWeather(this.id);
	}

	public List<TadoDevice> getDevices(TadoConnector connector) throws TadoException {
		return connector.getDevices(this.id);
	}

	public List<TadoInstallation> getInstallations(TadoConnector connector) throws TadoException {
		return connector.getInstallations(this.id);
	}

	public List<User> getUsers(TadoConnector connector) throws TadoException {
		return connector.getUsers(this.id);
	}

	public List<MobileDevice> getMobileDevices(TadoConnector connector) throws TadoException {
		return connector.getMobileDevices(this.id);
	}

	public MobileDevice getMobileDevice(int id, TadoConnector connector) throws TadoException {
		return connector.getMobileDevice(id, this.id);
	}

	public Map<String, Object> getMobileDeviceSettings(int deviceId, TadoConnector connector) throws TadoException {
		return connector.getMobileDeviceSettings(deviceId, this.id);
	}

	public boolean setGeoTracking(int deviceId, boolean enabled, TadoConnector connector) throws TadoException {
		return connector.setGeoTracking(this.id, deviceId, enabled);
	}

	public boolean setState(TadoState state, TadoConnector connector) throws TadoException {
		return connector.setHomeState(this.id, state.getPresence());
	}
}
