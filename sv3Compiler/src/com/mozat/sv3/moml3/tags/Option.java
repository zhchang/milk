package com.mozat.sv3.moml3.tags;

import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class Option implements IMoml3Tag {
	public static final String tag = "option";

	String tagName;
	String value;
	String text;
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Option(String tagName) {
		this.tagName = tagName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromAttributes(this, map, pos, handler);
	}

	@Override
	public boolean setAttribute(String key, String value, int pos,
			ErrorHandler handler) throws Moml3Exception {
		String lowkey = key.toLowerCase();
		if ("value".equals(lowkey)) {
			setValue(value);
		} else if ("text".equals(lowkey)) {
			setText(value);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		setText(text);
	}

	@Override
	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
		handler.raise(ErrorLevel.FatalError, "missing close tag " + tagName,
				pos);
	}

	@Override
	public Map<Byte, Object> toIntMap() {
		return null;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		return null;
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public void closeTag() {
	}

	@Override
	public String toString() {
		return toStrMap(null).toString();
	}
}
