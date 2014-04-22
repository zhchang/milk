package com.mozat.sv3.moml3.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Select;

public class Select extends Sv3Select implements ISv3ElementTag {

	public static final String tag = "select";
	String tagName;
	List<Option> optionTags = new ArrayList<Option>();
	int position;

	// implemented for unit test purpose
	private Select(Select prototype) {
		super(prototype.getId(), prototype, prototype.getPage());
	}

	// implemented for unit test purpose
	public ISv3ElementTag copySv3() {
		return new Select(this);
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public Select(String tagName) {
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
		if ("selected".equals(lowkey)) {
			super.setSelected(TagUtil.strTrimmedToInteger(value));
		} else if ("options".equals(lowkey)) {
			super.setOptions(value);
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
		// handler.raise(Moml3Exception.Level.IgnorableError,
		// "child element not allowed in " + tagName, pos);
		if (tag instanceof Option) {
			optionTags.add((Option) tag);
		} else {
			handler.raise(ErrorLevel.FatalError,
					"child element " + tag.getTagName() + " not allowed in "
							+ tagName, pos);
		}
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
		if (getSelected() != 0)
			map.put(TAG_Selected, getSelected());
		if (getOptions() != null && getOptions().length > 0)
			map.put(TAG_Options, getOptions());
		return map;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		// map.put("type", getSv3Type());
		TagUtil.toStrMap(map, this, options);
		if (getSelected() != -1)
			map.put("selected", getSelected());
		if (getOptions() != null && getOptions().length > 0)
			map.put("options", getOptions());
		return map;
	}

	@Override
	public void closeTag() {
		if (optionTags.size() > 0) {
			String[] options = new String[optionTags.size() * 2];
			int i = 0;
			for (Option o : optionTags) {
				options[i * 2] = o.getValue();
				options[i * 2 + 1] = o.getText();
				++i;
			}
			super.setOptions(options, false);
		}
	}

	@Override
	public String toString() {
		return toStrMap(null).toString();
	}
}
