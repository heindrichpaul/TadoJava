package com.heindrich.tado.model;

import lombok.*;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class Temperature {
	private Double celsius;
	private Double fahrenheit;

	public Temperature(Double celsius, Double fahrenheit) throws TadoException {
		super();
		if (celsius == null && fahrenheit == null)
			throw new TadoException("error", "Please specify at least celsius or fahrenheit temperature.");
		if (celsius != null)
			this.celsius = celsius;
		if (fahrenheit != null)
			this.fahrenheit = fahrenheit;
	}

	public JSONObject toJSONObject() {
		JSONObject root = new JSONObject();
		if (this.celsius != null)
			root.put("celsius", this.celsius);
		if (this.fahrenheit != null)
			root.put("fahrenheit", this.fahrenheit);
		return root;
	}
}
