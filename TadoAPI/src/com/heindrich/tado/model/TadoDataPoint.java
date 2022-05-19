package com.heindrich.tado.model;

import lombok.*;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoDataPoint {
	private final String name;
	private final JSONObject datapoint;
}
