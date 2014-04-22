package com.mozat.sv3.moml3.tags;

import java.util.ArrayList;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.Moml3Exception;
import com.mozat.sv3.smartview3.elements.Sv3Div;
import com.mozat.sv3.smartview3.elements.Sv3Element;

public class Anchor extends Div {

	public static final String tag = "a";

	String href;

	public Anchor() {
		super(null);
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
		if ("href".equals(lowkey)) {
			href = value;
		} else {
			return false;
		}
		return true;
	}

	@Override
	public String getTagName() {
		return tag;
	}

	// @Override
	// public void addChild(IMoml3Tag tag, int pos, ErrorHandler handler) throws
	// Moml3Exception {
	// handler.raise(Moml3Exception.Level.IgnorableError,
	// "child element not allowed in " + getTagName(), pos);
	// }

	// @Override
	// public void setText(String text, int pos, ErrorHandler handler) throws
	// Moml3Exception {
	// handler.raise(Moml3Exception.Level.IgnorableError, "text not allowed in "
	// + getTagName(), pos);
	// }

	@Override
	public Map<Byte, Object> toIntMap() {
		return null;
	}

	@Override
	public Map<String, Object> toStrMap(ToJsonOptions options) {
		return null;
	}

	@Override
	public void closeTag() {
		Sv3Div parent = getParent();
		if (parent != null) {
			removeFromParent();
			@SuppressWarnings("unchecked")
			ArrayList<Sv3Element> children = new ArrayList<Sv3Element>(
					getChildren());
			for (Object o : children) {
				Sv3Element e = (Sv3Element) o;
				e.setUrl(href);
				parent.addChild(e);
			}
		} else {
			if (parentSpan != null) {
				@SuppressWarnings("unchecked")
				ArrayList<Sv3Element> children = new ArrayList<Sv3Element>(
						getChildren());
				if (children.size() == 1) {
					parentSpan.setUrl(href);
					parentSpan.setText(((Span) children.get(0)).getText());
				}
			} else {
				// do nothing
			}
		}
	}

	private Span parentSpan;

	public void setParentSpan(Span span) {
		parentSpan = span;
	}

}
