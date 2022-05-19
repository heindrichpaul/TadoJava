package com.heindrich.tado.model;

import lombok.*;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoSetting {
	private final String type;
	private final boolean powered;
	private final Temperature temperature;


	public String getPowered() {
		return (this.powered) ? "ON" : "OFF";
	}

	public Temperature getTemperature() {
		return temperature;
	}

	public JSONObject toJSONObject() {
		JSONObject root = new JSONObject();
		root.put("type", this.type);
		root.put("power", this.getPowered());
		root.put("temperature", this.temperature.toJSONObject());
		return root;
	}
}
