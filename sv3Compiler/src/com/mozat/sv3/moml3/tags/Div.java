package com.mozat.sv3.moml3.tags;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Div;
import com.mozat.sv3.smartview3.elements.Sv3Div9Patch;
import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.utils.IImageRequester;

public class Div extends Sv3Div9Patch implements ISv3ElementTag,
		IImageRequester {

	public static final String tag = "div";
	public static final Set<String> altTags = new HashSet<String>(
			Arrays.asList("body", "div9", "p", "table", "tr", "td", "th", "li"));
	public static final Set<String> suppressedAttr = new HashSet<String>(
			Arrays.asList("color", "fontstyle", "fontsize"));

	String tagName;
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	// implemented for unit test purpose
	private Div(Div prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Div(this);
	}

	public Div(String tagName) {
		super(null);
		this.tagName = tagName;
		if (tagName != null) {
			if ("body".equals(tagName)) {
				setLineWrap(LINE_WRAP_BOTH);
				setWidth("100%");
				// } else if ("div9".equals(tagName)) {
				// // for test only
				// setImage(imageUtil.loadLocalImage("bubble-green.png"));
				// setMarker((short) 7, (short) 9, (short) 23, (short) 10);
				// setPadding((short) 8, (short) 8, (short) 12, (short) 8);
				// setFillColor(0x9ac93e);
			} else if ("table".equals(tagName)) {
				setLineWrap(LINE_WRAP_BOTH);
				setWidth("100%");
			} else if ("tr".equals(tagName)) {
				setWidth("100%");
				setLineWrap(LINE_WRAP_BOTH);
			} else if ("td".equals(tagName) || "th".equals(tagName)) {
				setBorder((short) 1);
			} else if ("p".equals(tagName)) {
				setLineWrap(LINE_WRAP_BOTH);
			} else if ("li".equals(tagName)) {
				setLineWrap(LINE_WRAP_BOTH);
				// Div bullet = new Div("bullet");
				// bullet.setBgColor(0);
				// bullet.setWidth((short) 5, false);
				// bullet.setHeight((short) 5, false);
				// bullet.setValign("middle");
				// this.addChild(bullet);
			}
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
		if ("flow".equals(lowkey)) {
			setFlow(value);
		} else if ("align".equals(lowkey)) {
			setAlign(value);
		} else if ("noclip".equals(lowkey)) {
			setNoclip(Sv3Element.strToBoolean(value));
		} else if ("radius-tl".equals(lowkey)) {
			setRadius(TagUtil.strTrimmedToShort(value), CORNER_TL);
		} else if ("radius-tr".equals(lowkey)) {
			setRadius(TagUtil.strTrimmedToShort(value), CORNER_TR);
		} else if ("radius-br".equals(lowkey)) {
			setRadius(TagUtil.strTrimmedToShort(value), CORNER_BR);
		} else if ("radius-bl".equals(lowkey)) {
			setRadius(TagUtil.strTrimmedToShort(value), CORNER_BL);
		} else if ("radius".equals(lowkey)) {
			setRadius(Sv3Element.strToSides(value));
		} else if ("image".equals(lowkey)) { // extension for div9
			if ("div9".equals(tagName)) {
				// imageUtil.loadImageAsync(value, this);
				setImageUrl(value);
			} else {
				return false;
			}
		} else if ("marker".equals(lowkey)) { // extension for div9
			if ("div9".equals(tagName)) {
				setMarker(Sv3Element.strToSides(value));
			} else {
				return false;
			}
		} else if ("fillcolor".equals(lowkey)) { // extension for div9
			if ("div9".equals(tagName)) {
				setFillColor(Sv3Element.strToColor(value));
			} else {
				return false;
			}
		} else if ("focusimage".equals(lowkey)) {
			if ("div9".equals(tagName)) {
				setFocusImageUrl(value);
			}
		} else {
			return false;
		}
		return true;
	}

	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
		if (tag instanceof ISv3ElementTag) {
			boolean consumed = false;
			@SuppressWarnings("rawtypes")
			Vector children = getChildren();
			if (tag instanceof Br && children.size() > 0) {
				Sv3Element child = (Sv3Element) children.lastElement();
				if (child.wrapLineAfter()) {
					// element already wrapped, no choice but to add a new
					// element
				} else {
					// element not wrapped, we can save one element by merging
					// this Br to the previous element
					child.setLineWrap((byte) (child.getLineWrap() | LINE_WRAP_AFTER));
					consumed = true;
				}
			}

			if (!consumed) {
				this.addChild((Sv3Element) tag);
			}
		} else if (tag instanceof Title) {
			// ignore
		} else {
			handler.raise(ErrorLevel.IgnorableError, tag.getTagName()
					+ " tag not allowed in div", pos);
		}
	}

	@Override
	public String getTagName() {
		return tagName;
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {
		Span span = new Span("span");
		span.setPosition(pos);
		span.setText(text);
		addChild(span);
	}

	@Override
	public Map<Byte, Object> toIntMap() {
		Map<Byte, Object> map = new HashMap<Byte, Object>();
		TagUtil.toIntMap(map, this);
		@SuppressWarnings("rawtypes")
		Vector children = getChildren();
		if (children.size() > 0) {
			map.put(Sv3Div.TAG_Children, children);
		}
		if (getFlow() != FLOW_INHERIT)
			map.put(Sv3Div.TAG_Flow, getFlow());
		if (getAlign() != ALIGN_INHERIT)
			map.put(Sv3Div.TAG_Align, getAlign());
		if (isNoclip() != false)
			map.put(Sv3Div.TAG_Noclip, isNoclip());
		if (hasRadius())
			map.put(Sv3Div.TAG_Radius, getRadius());
		if (getImageUrl() != null) {
			map.put(Sv3Div.TAG_ImageUrl, getImageUrl());
			map.put(Sv3Div.TAG_FillColor, getFillColor());
			map.put(Sv3Div.TAG_Marker, getMarker());
		}
		if (getFocusImageUrl() != null) {
			map.put(Sv3Div.TAG_FocusImageUrl, getFocusImageUrl());
		}
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);

		if (getFlow() != FLOW_INHERIT)
			map.put("flow", getFlow());
		if (getAlign() != ALIGN_INHERIT)
			map.put("align", getAlign());
		if (isNoclip() != false)
			map.put("noclip", isNoclip());
		short[] fourValues = getRadius();
		if (fourValues[0] != 0 || fourValues[1] != 0 || fourValues[2] != 0
				|| fourValues[3] != 0) {
			map.put("radius", TagUtil.sidesToStr(fourValues));
		}

		List<Object> subMaps = new ArrayList<Object>();
		for (Object tag : getChildren()) {
			Map<String, Object> submap = ((ISv3ElementTag) tag)
					.toStrMap(options);
			subMaps.add(submap);
		}
		if (subMaps.size() > 0) {
			map.put("children", subMaps);
		}
		return map;
	}

	@Override
	public void closeTag() {
	}

	@Override
	public String toString() {
		return toStrMap(null).toString();
	}

	@Override
	public void didReceiveImage(Object image, String src) {
		setImage(image);
	}

}
