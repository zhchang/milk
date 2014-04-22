package com.mozat.sv3.moml3.tags;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mozat.sv3.extension.Sv3EmoticonText;
import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class Span extends Sv3EmoticonText implements ISv3ElementTag {
	public static final String tag = "span";

	public static final Set<String> altTags = new HashSet<String>(
			Arrays.asList("label", "h1", "h2", "h3", "h4", "h5"));
	public static final Set<String> suppressedAttr = new HashSet<String>(
			Arrays.asList("padding", "x", "y", "width", "height"));

	String tagName;
	int position;

	// implemented for unit test purpose
	private Span(Span prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Span(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Span(String tagName) {
		super(null);
		this.tagName = tagName;
		if (tagName != null && tagName.startsWith("h")) {
			setLineWrap(LINE_WRAP_BOTH);
			setFontStyle(FONT_STYLE_B);
		}
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
		if ("text".equals(lowkey)) {
			super.setText(value);
		} else if ("maxlines".equals(lowkey)) {
			super.setMaxLines(TagUtil.strTrimmedToShort(value));
		} else if ("spacing".equals(lowkey)) {
			super.setSpacing(TagUtil.strTrimmedToShort(value));
		} else if ("truncate".equals(lowkey)) {
			super.setTruncate(value);
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
		if (tag instanceof Anchor) {
			((Anchor) tag).setParentSpan(this);
		} else {
			handler.raise(ErrorLevel.FatalError,
					"missing close tag " + tagName, pos);
		}
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
		if (getMaxLines() != Short.MAX_VALUE)
			map.put(TAG_MaxLines, getMaxLines());
		if (getSpacing() != 0)
			map.put(TAG_Spacing, getSpacing());
		if (getTruncate() != TRUNCATE_NONE)
			map.put(TAG_Truncate, getTruncate());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		if (getText() != null)
			map.put("text", getText());
		if (getMaxLines() != Short.MAX_VALUE)
			map.put("maxLines", getMaxLines());
		if (getSpacing() != 0)
			map.put("spacing", getSpacing());
		if (getTruncate() != TRUNCATE_NONE)
			map.put("truncate", getTruncate());
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
