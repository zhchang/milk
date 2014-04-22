package com.mozat.sv3.moml3.tokens;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public abstract class BaseToken {
	int start;
	int end;
	String body;

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public void setBody(String body, boolean isPreformatted, ErrorHandler handler) throws Moml3Exception {
		this.body = body;
	}

	public String getBody() {
		return body;
	}

	public boolean hasBody() {
		return body != null && body.length() > 0;
	}

}
