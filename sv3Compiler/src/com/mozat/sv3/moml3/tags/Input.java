package com.mozat.sv3.moml3.tags;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.elements.Sv3Input;

public class Input extends Sv3Input implements ISv3ElementTag {

	public static final String tag = "input";

	String tagName;
	int position;

	// implemented for unit test purpose
	private Input(Input prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Input(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Input(String tagName) {
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
		} else if ("textonempty".equals(lowkey)) {
			super.setTextOnEmpty(value);
		} else if ("length".equals(lowkey)) {
			super.setLength(TagUtil.strTrimmedToShort(value));
		} else if ("popup".equals(lowkey)) {
			super.setPopup(Byte.parseByte(value) != 0);
		} else if ("maxlines".equals(lowkey)) {
			super.setMaxLines(TagUtil.strTrimmedToShort(value));
		} else if ("keyboard".equals(lowkey)) {
			super.setKeyboard(value);
		} else if ("secure".equals(lowkey)) {
			super.setSecure(Sv3Element.strToBoolean(value));
		} else if ("capitalization".equals(lowkey)) {
			super.setCapitalization(value);
		} else if ("returnkey".equals(lowkey)) {
			super.setReturnKey(value);
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
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		super.setText(text);
	}

	@Override
	public Map<Byte, Object> toIntMap() {
		Map<Byte, Object> map = new HashMap<Byte, Object>();
		TagUtil.toIntMap(map, this);
		if (getText() != null)
			map.put(TAG_Text, getText());
		if (getTextOnEmpty() != null)
			map.put(TAG_TextOnEmpty, getTextOnEmpty());
		if (getLength() != Short.MAX_VALUE)
			map.put(TAG_Length, getLength());
		if (isPopup() != false)
			map.put(TAG_Popup, isPopup());
		if (getMaxLines() != 1)
			map.put(TAG_MaxLines, getMaxLines());
		if (getKeyboard() != KEYBOARD_NORMAL)
			map.put(TAG_Keyboard, getKeyboard());
		if (isSecure() != false)
			map.put(TAG_Secure, isSecure());
		if (getCapitalization() != CAPITALIZATION_NONE)
			map.put(TAG_Capitalization, getCapitalization());
		if (getReturnKey() != RETURN_KEY_DONE)
			map.put(TAG_ReturnKey, getReturnKey());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		if (getText() != null)
			map.put("text", getText());
		if (getTextOnEmpty() != null)
			map.put("textOnEmpty", getTextOnEmpty());
		if (getLength() != Short.MAX_VALUE)
			map.put("length", getLength());
		if (isPopup() != false)
			map.put("popup", isPopup());
		if (getMaxLines() != 1)
			map.put("maxLines", getMaxLines());
		if (getKeyboard() != KEYBOARD_NORMAL)
			map.put("keyboard", getKeyboard());
		if (isSecure() != false)
			map.put("secure", isSecure());
		if (getCapitalization() != CAPITALIZATION_NONE)
			map.put("capitalization", getCapitalization());
		if (getReturnKey() != RETURN_KEY_DONE)
			map.put("returnKey", getReturnKey());
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