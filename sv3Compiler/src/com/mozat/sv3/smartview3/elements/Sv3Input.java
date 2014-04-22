package com.mozat.sv3.smartview3.elements;

import java.io.IOException;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;

public class Sv3Input extends Sv3Element {
	protected static final byte TAG_Text = 64, TAG_TextOnEmpty = 65,
			TAG_Length = 66, TAG_Popup = 67, TAG_MaxLines = 68,
			TAG_Keyboard = 69, TAG_Secure = 70, TAG_Capitalization = 71,
			TAG_ReturnKey = 72;

	public static final byte KEYBOARD_NORMAL = 0, KEYBOARD_EMAIL = 1,
			KEYBOARD_NUMERIC = 2, KEYBOARD_PHONE = 3, KEYBOARD_URL = 4;
	// public static final boolean KEYBOARD_PASSWORD = 0x10000,
	// KEYBOARD_READONLY = 0x20000,
	// KEYBOARD_NONPREDICTIVE = 0x80000;
	public static final byte RETURN_KEY_DONE = 0, RETURN_KEY_NEXT = 1,
			RETURN_KEY_SEARCH = 2, RETURN_KEY_SEND = 3;
	public static final byte CAPITALIZATION_NONE = 0, CAPITALIZATION_WORDS = 1,
			CAPITALIZATION_SENTENCES = 2, CAPITALIZATION_ALL = 3;

	// attributes defined by SmartView3
	private String text;
	private String textOnEmpty;
	private short length = Short.MAX_VALUE;
	private boolean popup;
	private short maxLines = 1;
	private byte keyboard = KEYBOARD_NORMAL;
	private boolean secure;
	private byte capitalization = CAPITALIZATION_NONE;
	private byte returnKey = RETURN_KEY_DONE;

	// private final Sv3Div innerDiv = null;
	private Sv3Text innerText = null;
	// private boolean cursorVisible;
	private boolean lastCharVisible;

	public Sv3Input(String id) {
		super(id);
	}

	public Sv3Input(String id, Sv3Input in, Sv3Page page) {
		super(id, in, page);
		this.text = in.text;
		this.textOnEmpty = in.textOnEmpty;
		this.length = in.length;
		this.popup = in.popup;
		this.maxLines = in.maxLines;
		this.keyboard = in.keyboard;
		this.secure = in.secure;
		this.capitalization = in.capitalization;
		this.returnKey = in.returnKey;
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new Sv3Input(id, this, page);
	}

	public byte getSv3Type() {
		return TYPE_INPUT;
	}

	public boolean canFocus() {
		return true;
	}

	short titleWidth;

	public String getSecureText() {
		if (text == null) {
			return null;
		} else {
			String result;
			if (secure) {
				if (text.length() > 0) {
					StringBuffer sb = new StringBuffer();
					int len = text.length();
					for (int i = 0; i < len; ++i) {
						if (lastCharVisible && i == len - 1) {
							sb.append(text.charAt(i));
						} else {
							sb.append('\u2022');
						}
					}
					result = sb.toString();
				} else {
					result = "";
				}
			} else {
				result = text;
			}
			// if (cursorVisible) {
			// result = result + '|';
			// }
			return result;
		}
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx, IFontUtil fu) {
		// if (maxLines <= 1) {
		// Object font = this.getFont(fu);
		// titleWidth = (short) layoutSingleLineText(newRect, text, ctx, fu,
		// font, resolvedPadding, MARGIN_X, MARGIN_Y,
		// MIN_WIDTH);
		// } else {
		if (innerText == null) {
			innerText = new Sv3Text(null);
		}
		{
			innerText.setText(getSecureText());
			innerText.setMaxLines(maxLines);
			innerText.setColor(getColor());
			innerText.setBgColor(getBgColor());
			innerText.setBgColor2(getBgColor2());
			innerText.setFocused(isFocused());
		}

		int hpadding = resolvedPadding[3] + resolvedPadding[1];
		int vpadding = resolvedPadding[0] + resolvedPadding[2];

		LayoutContext subctx = new LayoutContext(ctx.resolvedBoundWidth()
				- hpadding, ctx.resolvedBoundHeight() - vpadding);

		subctx.definedWidth = (short) (newRect.width >= 0 ? newRect.width
				- hpadding : newRect.width);
		subctx.definedHeight = (short) (newRect.height >= 0 ? newRect.height
				- vpadding : newRect.height);
		subctx.flow = ctx.flow;
		subctx.align = ctx.align;
		innerText.layout(subctx, fu);
		subctx.wrapContext();

		if (newRect.width < 0) {
			newRect.width = (short) (subctx.contentWidth() + hpadding);
		}
		if (newRect.height < 0) {
			newRect.height = (short) (subctx.contentHeight() + vpadding);
		}
		// }
	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				// IFontUtil fu = ctx.getFu();
				Rect absPadded = absRect.toRectWithPadding(resolvedPadding,
						ctx.getPaddedRect());
				super.render(ctx);

				IRenderContext subctx = ctx.getSubCtx(absPadded, viewport);
				innerText.render(subctx);
			}
		}
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_Text:
				text = r.readStringOrNull();
				break;
			case TAG_TextOnEmpty:
				textOnEmpty = r.readStringOrNull();
				break;
			case TAG_Length:
				length = (short) r.readInt();
				break;
			case TAG_Popup:
				popup = r.readBoolean();
				break;
			case TAG_MaxLines:
				maxLines = (short) r.readInt();
				break;
			case TAG_Keyboard:
				keyboard = (byte) r.readInt();
				break;
			case TAG_Secure:
				secure = r.readBoolean();
				break;
			case TAG_Capitalization:
				capitalization = (byte) r.readInt();
				break;
			case TAG_ReturnKey:
				returnKey = (byte) r.readInt();
				break;
			default:
				r.read(); // read the value no matter what it is
				break;
			}
		} else {
			super.readAttrFromMobon(r, key);
		}
	}

	public boolean setStrAttrib(String key, String value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		if ("text".equals(key)) {
			setText(value);
			needsRelayout = true;
		} else if ("textonempty".equals(key)) {
			setTextOnEmpty(value);
			needsRelayout = true;
		} else if ("keyboard".equals(key)) {
			setKeyboard(value);
			needsRepaint = false;
		} else if ("capitalization".equals(key)) {
			setCapitalization(value);
			needsRepaint = false;
		} else if ("returnkey".equals(key)) {
			setReturnKey(value);
			needsRepaint = false;
		} else {
			return super.setStrAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	public boolean setIntAttrib(String key, int value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		if ("length".equals(key)) {
			setLength((short) value);
			needsRelayout = true;
		} else if ("popup".equals(key)) {
			setPopup(value != 0);
			needsRepaint = false;
		} else if ("maxlines".equals(key)) {
			setMaxLines((short) value);
			needsRelayout = true;
		} else if ("keyboard".equals(key)) {
			setKeyboard((byte) value);
			needsRepaint = false;
		} else if ("secure".equals(key)) {
			setSecure(value != 0);
		} else if ("capitalization".equals(key)) {
			setCapitalization((byte) value);
			needsRepaint = false;
		} else if ("returnkey".equals(key)) {
			setReturnKey((byte) value);
			needsRepaint = false;
		} else {
			return super.setIntAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("text".equals(key)) {
			return text;
		} else if ("textonempty".equals(key)) {
			return textOnEmpty;
		} else if ("length".equals(key)) {
			return new Integer(length);
		} else if ("popup".equals(key)) {
			return new Integer(popup ? 1 : 0);
		} else if ("maxlines".equals(key)) {
			return new Integer(maxLines);
		} else if ("keyboard".equals(key)) {
			return new Integer(keyboard);
		} else if ("returnkey".equals(key)) {
			return new Integer(returnKey);
		} else if ("secure".equals(key)) {
			return new Integer(secure ? 1 : 0);
		} else if ("capitalization".equals(key)) {
			return new Integer(capitalization);
		}
		return super.getAttrib(key);

	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		if (innerText != null) {
			innerText.setText(getSecureText());
		}
	}

	public String getTextOnEmpty() {
		return textOnEmpty;
	}

	public void setTextOnEmpty(String textOnEmpty) {
		this.textOnEmpty = textOnEmpty;
	}

	public short getLength() {
		return length;
	}

	public void setLength(short length) {
		this.length = length;
	}

	public boolean isPopup() {
		return popup;
	}

	public void setPopup(boolean popup) {
		this.popup = popup;
	}

	public short getMaxLines() {
		return maxLines;
	}

	public void setMaxLines(short maxLines) {
		this.maxLines = maxLines;
	}

	public byte getKeyboard() {
		return keyboard;
	}

	public void setKeyboard(byte keyboard) {
		this.keyboard = keyboard;
	}

	public boolean isSecure() {
		return secure;
	}

	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	public byte getCapitalization() {
		return capitalization;
	}

	public void setCapitalization(byte capitalization) {
		this.capitalization = capitalization;
	}

	public byte getReturnKey() {
		return returnKey;
	}

	public void setReturnKey(byte returnKey) {
		this.returnKey = returnKey;
	}

	public void setKeyboard(String keyboard) {
		this.keyboard = strToKeyboard(keyboard);
	}

	public void setCapitalization(String c) {
		this.capitalization = strToCapitalization(c);
	}

	public void setReturnKey(String returnKey) {
		this.returnKey = strToReturnKey(returnKey);
	}

	public boolean isCursorVisible() {
		return innerText.isCursorVisible();
	}

	public void setCursorVisible(boolean cursorVisible) {
		// this.cursorVisible = cursorVisible;
		this.innerText.setCursorVisible(cursorVisible);
	}

	public boolean isLastCharVisible() {
		return lastCharVisible;
	}

	public void setLastCharVisible(boolean lastCharVisible) {
		this.lastCharVisible = lastCharVisible;
	}

	// convert functions
	public static byte strToReturnKey(String value) {
		value = value.trim().toLowerCase();
		if ("next".equals(value) || "1".equals(value)) {
			return RETURN_KEY_NEXT;
		} else if ("search".equals(value) || "2".equals(value)) {
			return RETURN_KEY_SEARCH;
		} else if ("send".equals(value) || "3".equals(value)) {
			return RETURN_KEY_SEND;
		} else {
			return RETURN_KEY_DONE;
		}
	}

	public static byte strToCapitalization(String value) {
		value = value.trim().toLowerCase();
		if ("words".equals(value) || "word".equals(value) || "1".equals(value)) {
			return CAPITALIZATION_WORDS;
		} else if ("sentences".equals(value) || "sentence".equals(value)
				|| "2".equals(value)) {
			return CAPITALIZATION_SENTENCES;
		} else if ("all".equals(value) || "3".equals(value)) {
			return CAPITALIZATION_ALL;
		} else {
			return CAPITALIZATION_NONE;
		}
	}

	// public static final byte KEYBOARD_NORMAL = 0, KEYBOARD_EMAIL = 1,
	// KEYBOARD_NUMERIC = 2,
	// KEYBOARD_PHONE_NUMBER = 3, KEYBOARD_URL = 4;
	public static byte strToKeyboard(String value) {
		value = value.trim().toLowerCase();
		if ("email".equals(value) || "1".equals(value)) {
			return KEYBOARD_EMAIL;
		} else if ("numeric".equals(value) || "2".equals(value)) {
			return KEYBOARD_NUMERIC;
		} else if ("phone".equals(value) || "3".equals(value)) {
			return KEYBOARD_PHONE;
		} else if ("url".equals(value) || "4".equals(value)) {
			return KEYBOARD_URL;
		} else {
			return KEYBOARD_NORMAL;
		}
	}
}
