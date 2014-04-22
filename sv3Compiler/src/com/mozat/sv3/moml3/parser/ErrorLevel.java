package com.mozat.sv3.moml3.parser;

public enum ErrorLevel {
	Warning("warning"), // a minor error that is somehow expected
	IgnorableError("error"), // an error that can be ignored (without affecting the rest of of parsing process
	FatalError("fatal"); // a fatal error that cannot be neglected or recovered

	private final String shortName;

	ErrorLevel(String shortName) {
		this.shortName = shortName;
	}

	public String getShortName() {
		return shortName;
	}

}
