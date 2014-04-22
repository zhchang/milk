package com.mozat.sv3.moml3.tags;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Element;

public class Spring extends Element {

	public static final String tag = "spring";

	public static final Set<String> suppressedAttr = new HashSet<String>(
			Arrays.asList("color", "fontstyle", "fontsize", "width", "height"));

	public Spring(String tagName) {
		super(tagName);
		this.setWidth(Sv3Element.SIZE_FIT);
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromSv3Attributes(this, map, pos, suppressedAttr, handler);
	}

}
