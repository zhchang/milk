package com.mozat.sv3.moml3.tags;

import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class Title implements IMoml3Tag {
	public static final String tag = "title";

	Page page;
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Title(Page page) {
		this.page = page;
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromAttributes(this, map, pos, handler);
	}

	@Override
	public boolean setAttribute(String key, String value, int pos,
			ErrorHandler handler) throws Moml3Exception {
		return false;
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		page.setTitle(text);
	}

	@Override
	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
		handler.raise(ErrorLevel.FatalError, "missing close tag "
				+ getTagName(), pos);
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
		return tag;
	}

	@Override
	public void closeTag() {
	}

	@Override
	public String toString() {
		return toStrMap(null).toString();
	}
}
