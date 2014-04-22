package com.mozat.sv3.moml3.tokens;

import com.mozat.sv3.moml3.parser.Moml3Exception;

public class DummyToken extends BaseToken {
	public static DummyToken getInstance() {
		return instance;
	}

	private static DummyToken instance = new DummyToken();

	public void setBody(String body) throws Moml3Exception {
	}
}
