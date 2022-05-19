package com.heindrich.tado.model;

import lombok.*;

import java.util.Date;
import java.util.List;
@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoZoneState {
	private final String tadoMode;
	private final boolean geolocationOverride;
	private final Date geolocationOverrideDisableTime;
	private final TadoSetting setting;
	private final TadoScheduleChange nextScheduleChange;
	private final String linkState;
	private final List<TadoDataPoint> activityDataPoints;
	private final List<TadoDataPoint> sensorDataPoints;
}
