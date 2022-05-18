package com.heindrich.tado;

import java.util.Date;

import org.json.JSONObject;

public abstract class Termination {
	private final String type;
	private String typeSkillBasedApp;
	private Date projectedExpiry;

	public String getType() {
		return type;
	}

	public String getTypeSkillBasedApp() {
		return typeSkillBasedApp;
	}

	public Date getprojectedExpiry() {
		return projectedExpiry;
	}

	protected Termination(String type, String typeSkillBasedApp, Date projectedExpiry) {
		super();
		this.type = type;
		this.typeSkillBasedApp = typeSkillBasedApp;
		this.projectedExpiry = projectedExpiry;
	}

	protected Termination(String type) {
		super();
		this.type = type;
	}

	public abstract JSONObject toJSONObject();

	@Override
	public String toString() {
		return "Termination [type=" + type + ", typeSkillBasedApp=" + typeSkillBasedApp + ", projectedExpiry="
				+ projectedExpiry + "]";
	}
}
