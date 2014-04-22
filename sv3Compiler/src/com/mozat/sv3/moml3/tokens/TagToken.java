package com.mozat.sv3.moml3.tokens;

import java.util.HashMap;
import java.util.Map;

import com.mozat.sv3.moml3.parser.ErrorHandler;
import com.mozat.sv3.moml3.parser.ErrorLevel;
import com.mozat.sv3.moml3.parser.Moml3Exception;

public class TagToken extends BaseToken {
	public enum TagType {
		Unknown, // <a>
		OpenTag, // <a>
		SelfClosedOpenTag, // <input />
		CloseTag, // </a>
		DocTypeTag, // <!-- comment -->
	}

	TagType type = TagType.Unknown;
	String name; // open, close
	Map<String, String> attributes; // open

	public TagType getTagType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	@Override
	public void setBody(String body, boolean isPreformatted,
			ErrorHandler handler) throws Moml3Exception {
		super.setBody(body, isPreformatted, handler);

		assert (body.length() > 0);

		int col = getStart();

		// find first none white space char
		int len = body.length();
		char c = 0;
		int i;
		for (i = 0; i < len; ++i) {
			c = body.charAt(i);
			++col;
			if (!Character.isWhitespace(c)) {
				break;
			}
		}
		if (c == 0) {
			handler.raise(ErrorLevel.IgnorableError, "empty tag", col);
		} else {
			if (c == '/') {
				type = TagType.CloseTag;
				name = body.substring(i + 1, body.length()).trim()
						.toLowerCase();
			} else if (c == '!') {
				type = TagType.DocTypeTag;
			} else {
				if (body.charAt(body.length() - 1) == '/') {
					type = TagType.SelfClosedOpenTag;
					getNameAndAttributes(i, body.length() - 1, col, handler);
				} else {
					type = TagType.OpenTag;
					getNameAndAttributes(i, body.length(), col, handler);
				}
			}
		}
	}

	private enum State {
		WaitingForKey, InKey, WaitingForEqual, WaitingForValue, InQuotedValue, InUnquotedValue,
	};

	private void getNameAndAttributes(int first, int last, int pos,
			ErrorHandler handler) throws Moml3Exception {
		name = null;
		int s;
		for (s = first; s < last; ++s) {
			char c = body.charAt(s);
			++pos;
			if (Character.isWhitespace(c)) {
				break;
			}
		}
		name = body.substring(first, s).trim();
		first = s + 1;
		attributes = new HashMap<String, String>();

		State state = State.WaitingForKey;
		int start = first;
		String attribName = null;
		char openQuote = 0;
		for (int i = first; i < last + 1; ++i) {
			char c;
			if (i == last) { // the last NULL character is to terminate whatever
								// that is not completed
				c = 0;
			} else {
				c = body.charAt(i);
			}
			++pos;

			if (c == 0) {
				if (state == State.WaitingForKey) {
					// ok
				} else if (state == State.InKey) {
					handler.raise(ErrorLevel.FatalError, "expecting =", pos);
				} else if (state == State.WaitingForEqual) {
					handler.raise(ErrorLevel.FatalError, "expecting =", pos);
				} else if (state == State.WaitingForValue) {
					handler.raise(ErrorLevel.FatalError,
							"expecting value after =", pos);
				} else if (state == State.InUnquotedValue) {
					state = State.WaitingForKey;
					String attribValue = body.substring(start, i);
					putAttribute(attribName, attribValue, handler, pos);
					attribName = null;
				} else { // if state == State.InStringValue
					handler.raise(ErrorLevel.FatalError, "expecting "
							+ openQuote, pos);
				}
			} else if (Character.isWhitespace(c)) {
				if (state == State.WaitingForKey) {
					// ok
				} else if (state == State.InKey) {
					state = State.WaitingForEqual;
					attribName = body.substring(start, i).trim();
				} else if (state == State.WaitingForEqual) {
					// ok
				} else if (state == State.WaitingForValue) {
					// ok
				} else if (state == State.InUnquotedValue) {
					state = State.WaitingForKey;
					String attribValue = body.substring(start, i);
					putAttribute(attribName, attribValue, handler, pos);
					attribName = null;
				} else { // if state == State.InStringValue
					// ok
				}
			} else if (c == '"' || c == '\'') {
				if (state == State.WaitingForKey) {
					handler.raise(ErrorLevel.FatalError, "unexpected " + c, pos);
				} else if (state == State.InKey) {
					handler.raise(ErrorLevel.FatalError, "unexpected " + c, pos);
				} else if (state == State.WaitingForEqual) {
					handler.raise(ErrorLevel.FatalError, "expecting =", pos);
				} else if (state == State.WaitingForValue) {
					state = State.InQuotedValue;
					openQuote = c;
					start = i;
				} else if (state == State.InUnquotedValue) {
					handler.raise(ErrorLevel.FatalError, "unexpected " + c, pos);
				} else { // if state == State.InStringValue
					if (c == openQuote) {
						state = State.WaitingForKey;
						String attribValue = body.substring(start + 1, i);
						putAttribute(attribName, attribValue, handler, pos);
						attribName = null;
					} else {
						// ok
					}
				}
			} else if (c == '=') {
				if (state == State.WaitingForKey) {
					handler.raise(ErrorLevel.FatalError, "unexpected =", pos);
				} else if (state == State.InKey) {
					state = State.WaitingForValue;
					attribName = body.substring(start, i).trim();
				} else if (state == State.WaitingForEqual) {
					state = State.WaitingForValue;
				} else if (state == State.WaitingForValue) {
					handler.raise(ErrorLevel.FatalError, "unexpected =", pos);
				} else if (state == State.InUnquotedValue) {
					handler.raise(ErrorLevel.FatalError, "unexpected =", pos);
				} else { // if state == State.InStringValue
					// ok
				}
			} else {
				if (state == State.WaitingForKey) {
					state = State.InKey;
					start = i;
				} else if (state == State.InKey) {
					// ok
				} else if (state == State.WaitingForEqual) {
					handler.raise(ErrorLevel.IgnorableError,
							"expecting value for attribute " + attribName, pos);
					state = State.InKey;
					start = i;
				} else if (state == State.WaitingForValue) {
					state = State.InUnquotedValue;
					start = i;
				} else if (state == State.InUnquotedValue) {
					// ok
				} else { // if state == State.InStringValue
					// ok
				}
			}
		}
	}

	private void putAttribute(String attribName, String attribValue,
			ErrorHandler handler, int pos) throws Moml3Exception {
		attribName = attribName.toLowerCase();
		if (attributes.containsKey(attribName)) {
			handler.raise(ErrorLevel.Warning,
					"overwriting duplicate attribute " + attribName, pos);
		}
		attributes.put(attribName, attribValue);
	}

	@Override
	public String toString() {
		return "TagToken [type=" + type + ", name=" + name + ", attributes="
				+ attributes + ", start=" + start + ", end=" + end + ", body="
				+ body + "]";
	}

}
