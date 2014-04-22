package com.mozat.sv3.moml3.tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class Br extends Element {
	public static final String tag = "br";
	public static final Set<String> suppressedAttr = new HashSet<String>(
			Arrays.asList("color", "fontstyle", "fontsize"));

	public Br(String tagName) {
		super(tagName);
		this.setLineWrap(LINE_WRAP_AFTER);
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromSv3Attributes(this, map, pos, suppressedAttr, handler);
	}

}
