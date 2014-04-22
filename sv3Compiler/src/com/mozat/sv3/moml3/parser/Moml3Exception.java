package com.mozat.sv3.moml3.parser;

import com.mozat.sv3.moml3.tokens.CharPosition;

public class Moml3Exception extends Exception {
	private static final long serialVersionUID = 1L;

	private final CharPosition pos;
	private final ErrorLevel level;

	public CharPosition getPosition() {
		return pos;
	}

	protected Moml3Exception(ErrorLevel level, String string, CharPosition pos) {
		super(string);
		this.level = level;
		this.pos = pos;
	}

	@Override
	public String toString() {
		return pos + " " + level.getShortName() + ": " + getMessage();
	}

}
