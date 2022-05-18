package com.heindrich.tado.model;

import lombok.*;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class MobileLocation {
	private final boolean stale;
	private final boolean atHome;
	private final double degreesBearingFromHome;
	private final double radiansBearingFromHome;
	private final double relativeDistanceFromHomeFence;
}
