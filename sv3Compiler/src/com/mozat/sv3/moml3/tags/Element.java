package com.mozat.sv3.moml3.tags;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Element;

public class Element extends Sv3Element implements ISv3ElementTag {

	String tagName;
	int position;

	// implemented for unit test purpose
	private Element(Element prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Element(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Element(String tagName) {
		super(null);
		this.tagName = tagName;
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromSv3Attributes(this, map, pos, null, handler);
	}

	@Override
	public boolean setAttribute(String key, String value, int pos,
			ErrorHandler handler) throws Moml3Exception {
		return false;
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
		handler.raise(ErrorLevel.FatalError, "missing close tag " + tagName,
				pos);
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		handler.raise(ErrorLevel.IgnorableError, "text not allowed in "
				+ tagName, pos);
	}

	@Override
	public Map<Byte, Object> toIntMap() {
		Map<Byte, Object> map = new HashMap<Byte, Object>();
		TagUtil.toIntMap(map, this);
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		return map;
	}

	@Override
	public void closeTag() {
	}

	@Override
	public String toString() {
		return toStrMap(null).toString();
	}
}
