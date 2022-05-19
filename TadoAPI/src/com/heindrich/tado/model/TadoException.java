package com.heindrich.tado.model;

import lombok.*;

@Getter
public class TadoException extends Exception{
	private final String code;
	private final String title;

	public TadoException(String code, String title) {
		super(code + ": " + title);
		this.code = code;
		this.title = title;
	}
}
