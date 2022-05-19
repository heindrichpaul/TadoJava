package com.heindrich.tado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

public class TadoModeTermination extends Termination {

	public TadoModeTermination(String typeSkillBasedApp, LocalDateTime projectedExpiry) {
		super("TADO_MODE", typeSkillBasedApp, projectedExpiry);
	}

	public TadoModeTermination() {
		super("TADO_MODE");
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject root = new JSONObject();
		root.put("type", this.getType());
		if (this.getTypeSkillBasedApp() != null)
			root.put("typeSkillBasedApp", this.getTypeSkillBasedApp());
		if (this.getProjectedExpiry() != null)
			root.put("projectedExpiry", this.getProjectedExpiry().format(this.getFormatter()));
		return root;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
