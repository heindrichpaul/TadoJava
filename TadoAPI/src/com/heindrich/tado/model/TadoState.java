package com.heindrich.tado.model;

import com.heindrich.tado.model.TadoException;
import lombok.*;

@Getter
@ToString
@EqualsAndHashCode
public class TadoState {
	private final String presence;

	public String getPresence() {
		return presence;
	}

	public TadoState(String presence) throws TadoException {
		super();
		if (presence == null || (!presence.equals("AWAY") && !presence.equals("HOME")))
			throw new TadoException("error", "Presence value can only be HOME or AWAY.");
		this.presence = presence;
	}
}
