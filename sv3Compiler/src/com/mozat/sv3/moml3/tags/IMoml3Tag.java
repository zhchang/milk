package com.mozat.sv3.moml3.tags;

import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public interface IMoml3Tag {

	public void closeTag();

	public String getTagName();

	public void fromAttributeMap(Map<String, String> map, int pos,
			ErrorHandler handler) throws Moml3Exception;

	public boolean setAttribute(String key, String value, int pos,
			ErrorHandler handler) throws Moml3Exception;

	public void setText(String text, int pos, ErrorHandler handler)
			throws Moml3Exception;

	public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler)
			throws Moml3Exception;

	public Map<Byte, Object> toIntMap();

	public Map<String, Object> toStrMap(ToJsonOptions options);

	public int getPosition();

	public void setPosition(int pos);
}
