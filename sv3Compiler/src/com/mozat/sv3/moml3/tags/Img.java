package com.mozat.sv3.moml3.tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Image;

public class Img extends Sv3Image implements ISv3ElementTag {

	public static final String tag = "img";
	public static final Set<String> suppressedAttr = new HashSet<String>(
			Arrays.asList("color", "fontstyle", "fontsize"));
	int position;

	// implemented for unit test purpose
	private Img(Img prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Img(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Img(String tagName) {
		super(null);
		this.tagName = tagName;
	}

	@Override
	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception {
		TagUtil.fromSv3Attributes(this, map, pos, suppressedAttr, handler);
	}

	@Override
	public boolean setAttribute(String key, String value, int pos,
			ErrorHandler handler) throws Moml3Exception {
		String lowkey = key.toLowerCase();
		if ("src".equals(lowkey)) {
			super.setSrc(value);
		} else if ("alt".equals(lowkey)) {
			super.setAlt(value);
			// } else if ("localresid".equals(lowkey)) {
			// super.setLocalResId(value);
		} else {
			return false;
		}
		return true;
	}

	String tagName;

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
		if (getSrc() != null)
			map.put(TAG_Src, getSrc());
		if (getAlt() != null)
			map.put(TAG_Alt, getAlt());
		// if (getLocalResId() != null)
		// map.put(TAG_LocalResId, getLocalResId());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		if (getSrc() != null)
			map.put("src", getSrc());
		if (getAlt() != null)
			map.put("alt", getAlt());
		// if (getLocalResId() != null)
		// map.put("localResId", getLocalResId());
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
