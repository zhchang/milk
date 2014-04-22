package com.mozat.sv3.moml3.tokens;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class ContentToken extends BaseToken {

	String trimmedBody;

	@Override
	public String toString() {
		return "ContentToken [start=" + start + ", end=" + end + ", body="
				+ body + "]";
	}

	public void setBody(String body, boolean isPreformatted,
			ErrorHandler handler) throws Moml3Exception {
		if (isPreformatted) {
			super.setBody(body, isPreformatted, handler);
		} else {
			String[] lines = body.split("\r?\n");
			StringBuffer sb = new StringBuffer();
			boolean first = true;
			for (String line : lines) {
				if (!first)
					sb.append(' ');
				else
					first = false;
				line = processLine(line);
				sb.append(line);
			}
			super.setBody(sb.toString(), isPreformatted, handler);
		}
	}

	protected String processLine(String line) {
		line = line.trim(); // must trim FIRST
		line = line.replace("&nbsp;", " ");
		line = line.replace("&lt;", "<");
		line = line.replace("&gt;", ">");
		line = line.replace("&amp;", "&"); // must be the LAST
		return line;
	}
}
