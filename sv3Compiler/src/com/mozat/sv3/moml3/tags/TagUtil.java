package com.mozat.sv3.moml3.tags;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonWriter;
import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Element;

public class TagUtil {
	public static Gson getGson(boolean pretty) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.serializeNulls(); // it's better to include the null values,
										// so the
		if (pretty) {
			gsonBuilder.setPrettyPrinting();
		}
		return gsonBuilder.create();
	}

	public static String toJson(ISv3ElementTag tag) {
		return toJson(tag, null);
	}

	public static String toJson(ISv3ElementTag tag, ToJsonOptions options) {
		if (options == null) {
			options = new ToJsonOptions();
		}
		Gson gson = getGson(options.pretty);
		Map<String, Object> map = tag.toStrMap(options);
		return gson.toJson(map).toString();
	}

	public static String toJson(Page page) {
		return toJson(page, null);
	}

	public static String toJson(Page page, ToJsonOptions options) {
		if (options == null) {
			options = new ToJsonOptions();
		}
		Gson gson = getGson(options.pretty);
		Map<String, Object> map = page.toStrMap(options);
		return gson.toJson(map).toString();
	}

	public static void toMobon(Page page, OutputStream out) throws IOException,
			MobonException {
		MobonWriter writer = new MobonWriter(out);
		writer.write(page.toIntMap());
		out.flush();
	}

	public static void toMobon(ISv3ElementTag tag, OutputStream out)
			throws IOException, MobonException {
		MobonWriter writer = new MobonWriter(out);
		writer.write(tag);
		out.flush();
	}

	public static Set<String> tolerantTags = new HashSet<String>(Arrays.asList(
			"br", "hr", "mopage", "link", "meta", "img"));

	public static boolean tolerantPartial(String name) {
		name = name.toLowerCase();
		return tolerantTags.contains(name);
	}

	public static void toIntMap(Map<Byte, Object> map, Sv3Element e) {
		if (e.getId() != null)
			map.put(Sv3Element.TAG_Id, e.getId());
		if (e.isHidden() != false)
			map.put(Sv3Element.TAG_Hidden, e.isHidden());
		if (e.getX() != Short.MIN_VALUE)
			map.put(Sv3Element.TAG_X, e.getX());
		if (e.getY() != Short.MIN_VALUE)
			map.put(Sv3Element.TAG_Y, e.getY());
		if (e.getWidth() != -1)
			map.put(Sv3Element.TAG_Width, e.getWidth());
		if (e.getHeight() != -1)
			map.put(Sv3Element.TAG_Height, e.getHeight());
		if (e.hasPadding())
			map.put(Sv3Element.TAG_Padding, e.getPadding());
		if (e.hasBorder())
			map.put(Sv3Element.TAG_Border, e.getBorder());
		if (e.getValign() != Sv3Element.VALIGN_T)
			map.put(Sv3Element.TAG_Valign, e.getValign());
		if (e.getColor() != Sv3Element.DEFAULT_COLOR)
			map.put(Sv3Element.TAG_Color, e.getColor());
		if (e.getBdColor() != Sv3Element.DEFAULT_BD_COLOR)
			map.put(Sv3Element.TAG_Bdcolor, e.getBdColor());
		if (e.getBgColor() != Sv3Element.DEFAULT_BG_COLOR)
			map.put(Sv3Element.TAG_Bgcolor, e.getBgColor());
		if (e.getBgColor2() != Sv3Element.DEFAULT_BG_COLOR)
			map.put(Sv3Element.TAG_Bgcolor2, e.getBgColor2());
		if (e.getUrl() != null)
			map.put(Sv3Element.TAG_Url, e.getUrl());
		if (e.getUrlMode() != Sv3Element.URLMODE_OPEN)
			map.put(Sv3Element.TAG_Urlmode, e.getUrlMode());
		if (e.getConfirmation() != null)
			map.put(Sv3Element.TAG_Confirmation, e.getConfirmation());
		if (e.getTips() != null)
			map.put(Sv3Element.TAG_Tips, e.getTips());
		if (e.getName() != null)
			map.put(Sv3Element.TAG_Name, e.getName());
		if (e.getLineWrap() != Sv3Element.LINE_WRAP_NONE)
			map.put(Sv3Element.TAG_LineWrap, e.getLineWrap());
		if (e.getFontSize() != Sv3Element.FONT_SIZE_M)
			map.put(Sv3Element.TAG_FontSize, e.getFontSize());
		if (e.getFontStyle() != Sv3Element.FONT_STYLE_N)
			map.put(Sv3Element.TAG_FontStyle, e.getFontStyle());
		if (e.isHiddenFocus() != false)
			map.put(Sv3Element.TAG_HiddenFocus, e.isHiddenFocus());
		if (e.isDisabled() != false)
			map.put(Sv3Element.TAG_Disabled, e.isDisabled());
	}

	public static void toStrMap(Map<String, Object> map, Sv3Element e,
			ToJsonOptions options) {
		if (options == null) {
			options = new ToJsonOptions();
		}
		if (options.includeTag) {
			if (e instanceof IMoml3Tag) {
				String tagName = ((IMoml3Tag) e).getTagName();
				if (tagName != null)
					map.put("tag", tagName);
			}
		}
		if (options.includeType) {
			map.put("type", e.getSv3Type());
		}
		if (e.getId() != null)
			map.put("id", e.getId());
		if (e.hasRect()) {
			map.put("rect-abs", e.getAbsoluteRect());
			map.put("rect-rel", e.getRect());
		}
		if (e.isHidden() != false)
			map.put("hidden", e.isHidden());
		if (e.getX() != Sv3Element.XY_AUTO) {
			short x = e.getX();
			if (Sv3Element.isPercent(x))
				map.put("x", x - Sv3Element.PERCENT_MIN + "%");
			else
				map.put("x", e.getX());
		}
		if (e.getY() != Sv3Element.XY_AUTO) {
			short y = e.getY();
			if (Sv3Element.isPercent(y))
				map.put("y", y - Sv3Element.PERCENT_MIN + "%");
			else
				map.put("y", e.getY());
		}
		if (e.getWidth() != Sv3Element.SIZE_AUTO) {
			short w = e.getWidth();
			if (Sv3Element.isPercent(w))
				map.put("width", w - Sv3Element.PERCENT_MIN + "%");
			else
				map.put("width", e.getWidth());
		}
		if (e.getHeight() != Sv3Element.SIZE_AUTO) {
			short h = e.getHeight();
			if (Sv3Element.isPercent(h))
				map.put("height", h - Sv3Element.PERCENT_MIN + "%");
			else
				map.put("height", e.getHeight());
		}
		short[] fourValues = e.getPadding();
		if (fourValues[0] != 0 || fourValues[1] != 0 || fourValues[2] != 0
				|| fourValues[3] != 0) {
			map.put("padding", sidesToStr(fourValues));
		}
		fourValues = e.getBorder();
		if (fourValues[0] != 0 || fourValues[1] != 0 || fourValues[2] != 0
				|| fourValues[3] != 0) {
			map.put("border", sidesToStr(fourValues));
		}
		if (e.getValign() != Sv3Element.VALIGN_T)
			map.put("valign", e.getValign());
		if (e.getColor() != Sv3Element.DEFAULT_COLOR)
			map.put("color", Sv3Element.strFromColor(e.getColor()));
		if (e.getBdColor() != Sv3Element.DEFAULT_BD_COLOR)
			map.put("bdColor", Sv3Element.strFromColor(e.getBdColor()));
		if (e.getBgColor() != Sv3Element.DEFAULT_BG_COLOR)
			map.put("bgColor", Sv3Element.strFromColor(e.getBgColor()));
		if (e.getBgColor2() != Sv3Element.DEFAULT_BG_COLOR)
			map.put("bgColor2", Sv3Element.strFromColor(e.getBgColor2()));
		if (e.getUrl() != null)
			map.put("url", e.getUrl());
		if (e.getUrlMode() != Sv3Element.URLMODE_OPEN)
			map.put("urlMode", e.getUrlMode());
		if (e.getConfirmation() != null)
			map.put("confirmation", e.getConfirmation());
		if (e.getTips() != null)
			map.put("tips", e.getTips());
		if (e.getName() != null)
			map.put("name", e.getName());
		if (e.getLineWrap() != Sv3Element.LINE_WRAP_NONE)
			map.put("lineWrap", e.getLineWrap());
		if (e.getFontSize() != Sv3Element.FONT_SIZE_M)
			map.put("fontSize", e.getFontSize());
		if (e.getFontStyle() != Sv3Element.FONT_STYLE_N)
			map.put("fontStyle", e.getFontStyle());
		if (e.isHiddenFocus() != false)
			map.put("hiddenFocus", e.isHiddenFocus());
		if (e.isDisabled() != false)
			map.put("disabled", e.isDisabled());
	}

	public static String sidesToStr(short[] fourValues) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fourValues.length; ++i) {
			if (i > 0) {
				sb.append(',');
			}
			short v = fourValues[i];
			if (Sv3Element.isPercent(v)) {
				sb.append(v - Sv3Element.PERCENT_MIN).append('%');
			} else {
				sb.append(v);
			}
		}
		return sb.toString();
	}

	public static boolean setAttribute(Sv3Element elem, final String key,
			String value, int pos, ErrorHandler handler) throws Moml3Exception {
		String lowkey = key.toLowerCase();
		if ("bdcolor".equals(lowkey)) {
			elem.setBdColor(Sv3Element.strToColor(value));
		} else if ("bgcolor".equals(lowkey)) {
			elem.setBgColor(Sv3Element.strToColor(value));
		} else if ("bgcolor2".equals(lowkey)) {
			elem.setBgColor2(Sv3Element.strToColor(value));
		} else if ("border".equals(lowkey)) {
			elem.setBorder(Sv3Element.strToSides(value));
		} else if ("color".equals(lowkey)) {
			elem.setColor(Sv3Element.strToColor(value));
		} else if ("confirmation".equals(lowkey)) {
			elem.setConfirmation(value);
		} else if ("fontsize".equals(lowkey)) {
			elem.setFontSize(value);
		} else if ("fontstyle".equals(lowkey)) {
			elem.setFontStyle(value);
		} else if ("height".equals(lowkey)) {
			elem.setHeight(value);
		} else if ("id".equals(lowkey)) {
			elem.setIdRestricted(value);
		} else if ("linewrap".equals(lowkey)) {
			elem.setLineWrap(value);
		} else if ("name".equals(lowkey)) {
			elem.setName(value);
		} else if ("padding".equals(lowkey)) {
			elem.setPadding(Sv3Element.strToSides(value));
		} else if ("tips".equals(lowkey)) {
			elem.setTips(value);
		} else if ("url".equals(lowkey)) {
			elem.setUrl(value);
		} else if ("urlmode".equals(lowkey)) {
			elem.setUrlMode(value);
		} else if ("valign".equals(lowkey)) {
			elem.setValign(value);
		} else if ("hidden".equals(lowkey)) {
			elem.setHidden(Sv3Element.strToBoolean(value));
		} else if ("width".equals(lowkey)) {
			elem.setWidth(value);
		} else if ("x".equals(lowkey)) {
			elem.setX(value);
		} else if ("y".equals(lowkey)) {
			elem.setY(value);
		} else if ("hiddenfocus".equals(lowkey)) {
			elem.setHiddenFocus(Sv3Element.strToBoolean(value));
		} else if ("disabled".equals(lowkey)) {
			elem.setDisabled(Sv3Element.strToBoolean(value));
		} else if ("border-b".equals(lowkey) || "border-bottom".equals(lowkey)) {
			elem.setBorder(strTrimmedToShort(value), Sv3Element.SIDE_BOTTOM);
		} else if ("border-l".equals(lowkey) || "border-left".equals(lowkey)) {
			elem.setBorder(strTrimmedToShort(value), Sv3Element.SIDE_LEFT);
		} else if ("border-r".equals(lowkey) || "border-right".equals(lowkey)) {
			elem.setBorder(strTrimmedToShort(value), Sv3Element.SIDE_RIGHT);
		} else if ("border-t".equals(lowkey) || "border-top".equals(lowkey)) {
			elem.setBorder(strTrimmedToShort(value), Sv3Element.SIDE_TOP);
		} else if ("padding-b".equals(lowkey)
				|| "padding-bottom".equals(lowkey)) {
			elem.setPadding(strTrimmedToShort(value), Sv3Element.SIDE_BOTTOM);
		} else if ("padding-l".equals(lowkey) || "padding-left".equals(lowkey)) {
			elem.setPadding(strTrimmedToShort(value), Sv3Element.SIDE_LEFT);
		} else if ("padding-r".equals(lowkey) || "padding-right".equals(lowkey)) {
			elem.setPadding(strTrimmedToShort(value), Sv3Element.SIDE_RIGHT);
		} else if ("padding-t".equals(lowkey) || "padding-top".equals(lowkey)) {
			elem.setPadding(strTrimmedToShort(value), Sv3Element.SIDE_TOP);
		} else {
			return false;
		}
		return true;
	}

	public static short strTrimmedToShort(String value) {
		return Short.parseShort(value.trim());
	}

	public static int strTrimmedToInteger(String value) {
		return Integer.parseInt(value.trim());
	}

	public static void fromSv3Attributes(ISv3ElementTag elem,
			Map<String, String> map, int pos, Set<String> suppressedAttr,
			ErrorHandler handler) throws Moml3Exception {
		for (Map.Entry<String, String> e : map.entrySet()) {
			String key = e.getKey().toLowerCase();
			String value = e.getValue();
			if (suppressedAttr != null && suppressedAttr.contains(key)) {
				handler.raise(ErrorLevel.IgnorableError, "attribute " + key
						+ " not supported by tag " + elem.getTagName(), pos);
			} else {
				try {
					if (!TagUtil.setAttribute((Sv3Element) elem, key, value,
							pos, handler)) {
						if (!elem.setAttribute(key, value, pos, handler)) {
							handler.raise(
									ErrorLevel.IgnorableError,
									"attribute " + key
											+ " not supported by tag "
											+ elem.getTagName(), pos);
						}
					}
				} catch (NumberFormatException ex) {
					handler.raise(ErrorLevel.IgnorableError,
							"invalid value format for attribute " + key + ": "
									+ value, pos);
				}
			}
		}
	}

	public static void fromAttributes(IMoml3Tag tag, Map<String, String> map,
			int pos, ErrorHandler handler) throws Moml3Exception {
		for (Map.Entry<String, String> e : map.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			try {
				if (!tag.setAttribute(key, value, pos, handler)) {
					handler.raise(ErrorLevel.IgnorableError, "attribute " + key
							+ " not supported by tag " + tag.getTagName(), pos);
				}
			} catch (NumberFormatException ex) {
				handler.raise(ErrorLevel.IgnorableError,
						"invalid value format for attribute " + key + ": "
								+ value, pos);
			}
		}
	}
}
