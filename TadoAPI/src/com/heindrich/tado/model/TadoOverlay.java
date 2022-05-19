package com.heindrich.tado.model;

import com.heindrich.tado.Termination;
import lombok.*;
import org.json.JSONObject;

@Getter
@RequiredArgsConstructor
@ToString
@EqualsAndHashCode
public class TadoOverlay {
	private final String type;
	private final TadoSetting setting;
	private final Termination termination;

	public JSONObject toJSONObject() {
		JSONObject root = new JSONObject();
		if (this.type != null)
			root.put("type", this.type);
		root.put("setting", this.setting.toJSONObject());
		root.put("termination", this.termination.toJSONObject());
		return root;
	}
}
