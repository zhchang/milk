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

import com.mozat.sv3.io.TransUnit;
import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Button;
import com.mozat.sv3.smartview3.elements.Sv3Div;
import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.elements.Sv3Image;
import com.mozat.sv3.smartview3.elements.Sv3Input;
import com.mozat.sv3.smartview3.elements.Sv3Page;
import com.mozat.sv3.smartview3.elements.Sv3Select;
import com.mozat.sv3.smartview3.elements.Sv3Text;

public class Page extends Sv3Page implements IMoml3Tag {

	public static final String tag = "mopage";
	private final ArrayList<Moscript> scripts = new ArrayList<Moscript>();
	int position;

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	@Override
	public void closeTag() {
	}

	public void addScript(Moscript script) {
		scripts.add(script);
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
		if ("title".equals(lowkey)) {
			this.setTitle(value);
		} else if ("cachelevel".equals(lowkey)) {
			this.setCacheLevel(Byte.parseByte(value.trim()));
		} else if ("cacheimages".equals(lowkey)) {
			this.setCacheImages(Byte.parseByte(value.trim()) != 0);
		} else if ("debug".equals(lowkey)) {
			this.setDebug(Byte.parseByte(value.trim()) != 0);
		} else if ("disablescrolling".equals(lowkey)) {
			this.setDisableScrolling(Byte.parseByte(value.trim()) != 0);
		} else if ("resourceintensive".equals(lowkey)) {
			this.setResourceIntensive(Byte.parseByte(value.trim()) != 0);
		} else if ("bgcolor".equals(lowkey)) {
			this.setBgColor(Sv3Element.strToColor(value));
		} else if ("focuscolor".equals(lowkey)) {
			this.setFocusColor(Sv3Element.strToColor(value));
		} else if ("focusbgcolor".equals(lowkey)) {
			this.setFocusBgColor(Sv3Element.strToColor(value));
		} else if ("focusbgcolor2".equals(lowkey)) {
			this.setFocusBgColor2(Sv3Element.strToColor(value));
		} else if ("focusbdcolor".equals(lowkey)) {
			this.setFocusBdColor(Sv3Element.strToColor(value));
		} else if ("focusbdsize".equals(lowkey)) {
			this.setFocusBdSize(Short.parseShort(value));
		} else {
			return false;
		}
		return true;
	}

	@Override
	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception {

	}

	@Override
	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception {
	}

	// private Sv3Div root;
	// private byte[] byteCode;
	// private byte[] pageHash;
	// private String url;
	// private String title;
	// private byte cacheLevel;
	// private boolean cacheImages;
	// private boolean debug;
	// private boolean disableScrolling;
	// private boolean resourceIntensive;

	@Override
	public Map<Byte, Object> toIntMap() {
		Map<Byte, Object> map = new HashMap<Byte, Object>();
		if (getRoot() != null)
			map.put(TAG_Root, getRoot());
		if (getByteCode() != null)
			map.put(TAG_ByteCode, getByteCode());
		if (getPageHash() != null)
			map.put(TAG_PageHash, getPageHash());
		if (getUrl() != null)
			map.put(TAG_Url, getUrl());
		if (getTitle() != null)
			map.put(TAG_Title, getTitle());
		if (getCacheLevel() != CACHE_ON_DISK)
			map.put(TAG_CacheLevel, getCacheLevel());
		if (isCacheImages() != true)
			map.put(TAG_CacheImages, isCacheImages());
		if (isDebug() != false)
			map.put(TAG_Debug, isDebug());
		if (isDisableScrolling() != false)
			map.put(TAG_DisableScrolling, isDisableScrolling());
		if (isResourceIntensive() != false)
			map.put(TAG_ResourceIntensive, isResourceIntensive());
		if (getBgColor() != DEFAULT_BG_COLOR)
			map.put(TAG_BgColor, getBgColor());
		if (getFocusColor() != -1)
			map.put(TAG_FocusColor, getFocusColor());
		if (getFocusBgColor() != -1)
			map.put(TAG_FocusBgColor, getFocusBgColor());
		if (getFocusBgColor2() != -1)
			map.put(TAG_FocusBgColor2, getFocusBgColor2());
		if (getFocusBdColor() != -1)
			map.put(TAG_FocusBdColor, getFocusBdColor());
		if (getFocusBdSize() != 0)
			map.put(TAG_FocusBdSize, getFocusBdSize());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("tag", mopage);

		// if (getByteCode() != null)
		// map.put("byteCode", getByteCode());
		if (getPageHash() != null)
			map.put("pageHash", getPageHash());
		if (getUrl() != null)
			map.put("url", getUrl());
		if (getTitle() != null)
			map.put("title", getTitle());
		if (getCacheLevel() != CACHE_ON_DISK)
			map.put("cacheLevel", getCacheLevel());
		if (isCacheImages() != true)
			map.put("cacheImages", isCacheImages());
		if (isDebug() != false)
			map.put("debug", isDebug());
		if (isDisableScrolling() != false)
			map.put("disableScrolling", isDisableScrolling());
		if (isResourceIntensive() != false)
			map.put("resourceIntensive", isResourceIntensive());
		if (getRoot() != null)
			map.put("root", ((Div) getRoot()).toStrMap(options));
		if (scripts.size() > 0)
			map.put("script", Arrays.toString(scripts.toArray()));
		if (getBgColor() != DEFAULT_BG_COLOR)
			map.put("bgColor", getBgColor());
		if (getFocusColor() != -1)
			map.put("focusColor", getFocusColor());
		if (getFocusBgColor() != -1)
			map.put("focusBgColor", getFocusBgColor());
		if (getFocusBgColor2() != -1)
			map.put("focusBgColor2", getFocusBgColor2());
		if (getFocusBdColor() != -1)
			map.put("focusBdColor", getFocusBdColor());
		if (getFocusBdSize() != 0)
			map.put("focusBdSize", getFocusBdSize());

		return map;
	}

	public List<Moscript> getScripts() {
		return scripts;
	}

	public void copyPageAttrib(Page page) {
		if (page.getTitle() != null) {
			this.setTitle(page.getTitle());
		}
		this.setCacheLevel(page.getCacheLevel());
		this.setCacheImages(page.isCacheImages());
		this.setDebug(page.isDebug());
		this.setDisableScrolling(page.isDisableScrolling());
		this.setResourceIntensive(page.isResourceIntensive());
		this.setBgColor(page.getBgColor());
		this.setFocusColor(page.getFocusColor());
		this.setFocusBgColor(page.getFocusBgColor());
		this.setFocusBgColor2(page.getFocusBgColor2());
		this.setFocusBdColor(page.getFocusBdColor());
		this.setFocusBdSize(page.getFocusBdSize());
	}

	public String extractResource() {
		Set<String> resSet = new HashSet<String>();
		addResource(resSet, getTitle());
		Sv3Div root = getRoot();
		if (root != null) {
			extractResource(resSet, root);
		}
		StringBuilder sb = new StringBuilder();
		for (String text : resSet) {
			sb.append(text).append('=').append(text).append('\n');
		}
		return sb.toString();
	}

	private void addResource(Set<String> sb, String text) {
		if (text != null && text.length() > 0) {
			text = text.replace("\n", "\\n");
			text = text.replace("\r", "");
			sb.add(text);
		}
	}

	private void extractResource(Set<String> sb, Sv3Element elem) {
		if (elem instanceof Sv3Text) {
			addResource(sb, ((Sv3Text) elem).getText());
		} else if (elem instanceof Sv3Select) {
			String[] options = ((Sv3Select) elem).getOptions();
			// only translate text (odd index) but not value (even index)
			for (int i = 1; i < options.length; i += 2) {
				addResource(sb, options[i]);
			}
		} else if (elem instanceof Sv3Button) {
			addResource(sb, ((Sv3Button) elem).getText());
		} else if (elem instanceof Sv3Input) {
			addResource(sb, ((Sv3Input) elem).getText());
			addResource(sb, ((Sv3Input) elem).getTextOnEmpty());
		} else if (elem instanceof Sv3Image) {
			addResource(sb, ((Sv3Image) elem).getAlt());
		}
		addResource(sb, elem.getTips());
		addResource(sb, elem.getConfirmation());

		if (elem instanceof Sv3Div) {
			Vector<?> children = ((Sv3Div) elem).getChildren();
			for (Object c : children) {
				extractResource(sb, (Sv3Element) c);
			}
		}
	}

	public void translate(LinkedHashMap<String, TransUnit> units) {
		Sv3Div root = getRoot();
		if (root != null) {
			translate(root, units);
		}
	}

	private void translate(Sv3Element elem,
			LinkedHashMap<String, TransUnit> units) {
		if (elem instanceof Sv3Text) {
			String translated = getTranslatedText(units,
					((Sv3Text) elem).getText());
			((Sv3Text) elem).setText(translated);
		} else if (elem instanceof Sv3Select) {
			String[] options = ((Sv3Select) elem).getOptions();
			// only translate text (odd index) but not value (even index)
			for (int i = 1; i < options.length; i += 2) {
				options[i] = getTranslatedText(units, options[i]);
			}
		} else if (elem instanceof Sv3Button) {
			String translated = getTranslatedText(units,
					((Sv3Button) elem).getText());
			((Sv3Button) elem).setText(translated);
		} else if (elem instanceof Sv3Input) {
			String translated = getTranslatedText(units,
					((Sv3Input) elem).getText());
			((Sv3Input) elem).setText(translated);
			translated = getTranslatedText(units,
					((Sv3Input) elem).getTextOnEmpty());
			((Sv3Input) elem).setTextOnEmpty(translated);
		} else if (elem instanceof Sv3Image) {
			String translated = getTranslatedText(units,
					((Sv3Image) elem).getAlt());
			((Sv3Image) elem).setAlt(translated);
		}
		String translated = getTranslatedText(units, elem.getTips());
		elem.setTips(translated);
		translated = getTranslatedText(units, elem.getConfirmation());
		elem.setConfirmation(translated);

		if (elem instanceof Sv3Div) {
			Vector<?> children = ((Sv3Div) elem).getChildren();
			for (Object c : children) {
				translate((Sv3Element) c, units);
			}
		}
	}

	private String getTranslatedText(LinkedHashMap<String, TransUnit> units,
			String pkey) {
		if (pkey == null) {
			return null;
		} else {
			TransUnit unit = units.get(pkey);
			String translated = unit.getTransSafely();
			return translated;
		}
	}

	public void convertToRightToLeft() {
		Sv3Div root = getRoot();
		root.setAlign(Sv3Div.ALIGN_R);
		root.setFlow(Sv3Div.FLOW_RTL);
	}
}
