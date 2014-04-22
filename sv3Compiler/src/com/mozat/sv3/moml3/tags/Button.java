package com.mozat.sv3.moml3.tags;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Button;

public class Button extends Sv3Button implements ISv3ElementTag {

	public static final String tag = "button";

	String tagName;

	int position;

	// implemented for unit test purpose
	private Button(Button prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Button(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Button(String tagName) {
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
		String lowkey = key.toLowerCase();
		if ("text".equals(lowkey)) {
			super.setText(value);
		} else {
			return false;
		}
		return true;
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
	public void setText(String text, int pos, ErrorHandler handler) {
		super.setText(text);
	}

	@Override
	public Map<Byte, Object> toIntMap() {
		Map<Byte, Object> map = new HashMap<Byte, Object>();
		TagUtil.toIntMap(map, this);
		if (getText() != null)
			map.put(TAG_TEXT, getText());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		if (getText() != null)
			map.put("text", getText());
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
