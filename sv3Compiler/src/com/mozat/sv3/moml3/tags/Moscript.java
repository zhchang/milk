package com.mozat.sv3.moml3.tags;

import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class Moscript implements IMoml3Tag {

	public static final String tag = "moscript";

	String text;
	String src;

	int start;
	int end;
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Moscript() {
	}

	@Override
	public void closeTag() {
	}

	@Override
	public String getTagName() {
		return tag;
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
		if ("src".equals(lowkey)) {
			this.src = value;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	@Override
	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
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
	public String toString() {
		if (src == null) {
			return text;
		} else {
			return '<' + src + '>';
		}
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

}
