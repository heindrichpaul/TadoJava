package com.heindrich.tado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

public class ManualTermination extends Termination {

	public ManualTermination(String typeSkillBasedApp, LocalDateTime projectedExpiry) {
		super("MANUAL", typeSkillBasedApp, projectedExpiry);
	}

	public ManualTermination() {
		super("MANUAL");
	}

	@Override
	public JSONObject toJSONObject() {
		JSONObject root = new JSONObject();
		root.put("type", this.getType());
		if (this.getTypeSkillBasedApp() != null)
			root.put("typeSkillBasedApp", this.getTypeSkillBasedApp());
		if (this.getProjectedExpiry() != null)
			root.put("projectedExpiry", getProjectedExpiry().format(getFormatter()));
		return root;
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
