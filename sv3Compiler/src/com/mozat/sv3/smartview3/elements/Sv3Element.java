package com.mozat.sv3.smartview3.elements;

import java.io.IOException;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.AbstractImageUtil;
import com.mozat.sv3.smartview3.utils.IFontUtil;
import com.mozat.sv3.smartview3.utils.StringUtil;

public class Sv3Element {

	public static final short[] FOUR_ZEROS = { 0, 0, 0, 0 };
	public static final byte TYPE_ELEMENT = 0, TYPE_DIV = 1, TYPE_TEXT = 2,
			TYPE_IMAGE = 3, TYPE_BUTTON = 4, TYPE_INPUT = 5, TYPE_SELECT = 6,
			TYPE_CHECKBOX = 7;

	public static final byte TAG_Id = 1, TAG_Hidden = 2, TAG_X = 3, TAG_Y = 4,
			TAG_Width = 5, TAG_Height = 6, TAG_Valign = 7, TAG_Color = 8,
			TAG_Bdcolor = 9, TAG_Bgcolor = 10, TAG_Bgcolor2 = 11, TAG_Url = 12,
			TAG_Urlmode = 13, TAG_Confirmation = 14, TAG_Tips = 15,
			TAG_Name = 16, TAG_LineWrap = 17, TAG_FontSize = 18,
			TAG_FontStyle = 19, TAG_Padding = 20, TAG_Border = 21,
			TAG_HiddenFocus = 22, TAG_Disabled = 23;

	protected static final byte MAX_ELEMENT_TAG = 63;

	public static final byte VALIGN_T = 0, VALIGN_M = 1, VALIGN_B = 2;
	public static final byte FONT_SIZE_S = -1, FONT_SIZE_M = 0,
			FONT_SIZE_L = 1;
	public static final byte FONT_STYLE_N = 0, FONT_STYLE_B = 1,
			FONT_STYLE_I = 2, FONT_STYLE_BI = 3;
	public static final byte SIDE_TOP = 0, SIDE_RIGHT = 1, SIDE_BOTTOM = 2,
			SIDE_LEFT = 3;
	// public static final byte GEOMETRY_X = 1, GEOMETRY_Y = 2, GEOMETRY_W = 4,
	// GEOMETRY_H = 8;
	public static final short PERCENT_MIN = Short.MIN_VALUE,
			PERCENT_MAX = Short.MIN_VALUE + 100;
	public static final short PERCENT_OF_W_MIN = Short.MIN_VALUE + 101,
			PERCENT_OF_W_MAX = Short.MIN_VALUE + 10101;
	public static final short XY_AUTO = Short.MIN_VALUE + 101;
	public static final short SIZE_AUTO = -1, SIZE_FIT = -2;

	public static final int DEFAULT_COLOR = 0, DEFAULT_BD_COLOR = 0,
			DEFAULT_BG_COLOR = -1;
	public static final int FOCUS_COLOR = 0xffffff, FOCUS_BG_COLOR = 0x7380e5,
			FOCUS_BG_COLOR2 = 0x7380e5;
	// FOCUS_BG_COLOR = 0x6688dd, FOCUS_BG_COLOR2 = 0xaaccff;
	public static final int PRESSED_HIGHLIGHT_COLOR = 0x555555;
	public static final byte URLMODE_OPEN = 0, URLMODE_BACK = 1,
			URLMODE_REPLACE = 2, URLMODE_SUBMIT_GET = 3,
			URLMODE_SUBMIT_POST = 4;

	public static final byte LINE_WRAP_NONE = 0, LINE_WRAP_BEFORE = 1,
			LINE_WRAP_AFTER = 2, LINE_WRAP_BOTH = 3;

	public static AbstractImageUtil imageUtil;

	// ============

	// attributes defined by SmartView3
	private String id;
	protected boolean hidden = false;
	private short x = XY_AUTO;
	private short y = XY_AUTO;
	private short width = SIZE_AUTO;
	private short height = SIZE_AUTO;
	protected byte valign = VALIGN_T; // top, middle, bottom
	protected int color = DEFAULT_COLOR;
	protected int bdColor = DEFAULT_BD_COLOR;
	protected int bgColor = DEFAULT_BG_COLOR;
	protected int bgColor2 = DEFAULT_BG_COLOR;
	private String url;
	private byte urlMode = URLMODE_OPEN; // forward, backward, replace,
											// submit-get, submit-post
	private String confirmation;
	private String tips;
	private String name;
	protected byte lineWrap = LINE_WRAP_NONE; // none, before, after, both
	private byte fontSize = FONT_SIZE_M; // small, medium, large
	private byte fontStyle = FONT_STYLE_N; // normal, bold, italic, bolditalic
	private final short[] padding = { 0, 0, 0, 0 };
	private final short[] border = { 0, 0, 0, 0 };
	private boolean hiddenFocus = false;
	private boolean disabled = false;

	// runtime fields
	private Sv3Div parent;
	private Sv3Page page;
	protected Rect rect;
	private Rect absoluteRect;
	protected short resolvedWidth = -1;
	protected short resolvedHeight = -1;
	protected short[] resolvedPadding = { 0, 0, 0, 0 };
	private Object obj; // custom object associated with this element

	public Object getObj() {
		return obj;
	}

	public void setObj(Object obj) {
		this.obj = obj;
	}

	public Sv3Div getParent() {
		return parent;
	}

	public Sv3Page getPage() {
		return page;
	}

	void _setPage(Sv3Page p) {
		if (page != p) {
			if (page != null && id != null) {
				page.deregisterElement(this);
			}
			if (p != null && id != null) {
				p.registerElement(this);
			}
			page = p;
		}
	}

	void _setParent(Sv3Div p) {
		if (parent != p) {
			if (p == null) {
				_setPage(null);
			} else {
				if (page != p.getPage()) {
					_setPage(p.getPage());
				}
			}
			parent = p;
		}
	}

	public void removeFromParent() {
		if (parent != null) {
			parent.removeChild(this);
		}
	}

	// ===========================

	public Sv3Element(String id) {
		this.id = id;
	}

	public Sv3Element(String id, Sv3Element e, Sv3Page page) {
		this.id = id;
		this.hidden = e.hidden;
		this.hiddenFocus = e.hiddenFocus;
		this.disabled = e.disabled;
		this.x = e.x;
		this.y = e.y;
		this.width = e.width;
		this.height = e.height;
		this.valign = e.valign;
		this.color = e.color;
		this.bdColor = e.bdColor;
		this.bgColor = e.bgColor;
		this.bgColor2 = e.bgColor2;
		this.url = e.url;
		this.urlMode = e.urlMode;
		this.confirmation = e.confirmation;
		this.tips = e.tips;
		this.name = e.name;
		this.lineWrap = e.lineWrap;
		this.fontSize = e.fontSize;
		this.fontStyle = e.fontStyle;
		this.padding[0] = e.padding[0];
		this.padding[1] = e.padding[1];
		this.padding[2] = e.padding[2];
		this.padding[3] = e.padding[3];
		this.border[0] = e.border[0];
		this.border[1] = e.border[1];
		this.border[2] = e.border[2];
		this.border[3] = e.border[3];
		this._setPage(page);
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new Sv3Element(id, this, page);
	}

	public Sv3Element clone(String id) {
		return clone(id, page);
	}

	public byte getSv3Type() {
		return TYPE_ELEMENT;
	}

	protected void layoutManualSize(Rect newRect, LayoutContext ctx) {
		int w;
		if (width >= 0) {
			w = width;
		} else if (isPercent(width)) {
			if (ctx.definedWidth >= 0) {
				w = (short) (((width - PERCENT_MIN) * ctx.definedWidth) / 100);
			} else {
				// ignore percentage value
				w = -1;
			}
		} else {
			w = -1;
		}
		newRect.width = resolvedWidth = (short) w;

		int h;
		if (height >= 0) {
			h = height;
		} else if (isPercent(height)) {
			if (ctx.definedHeight >= 0) {
				h = ((height - PERCENT_MIN) * ctx.definedHeight) / 100;
			} else {
				// ignore percentage value
				h = -1;
			}
		} else if (height >= PERCENT_OF_W_MIN && height <= PERCENT_OF_W_MAX) {
			if (resolvedWidth >= 0) { // yes, width
				h = ((height - PERCENT_OF_W_MIN) * resolvedWidth) / 100;
			} else {
				// ignore percentage value
				h = -1;
			}
		} else {
			h = -1;
		}
		newRect.height = resolvedHeight = (short) h;
		resolvePadding(resolvedPadding);
	}

	// public short getResolvedWidth() {
	// return resolvedWidth;
	// }
	//
	// public short getResolvedHeight() {
	// return resolvedHeight;
	// }
	//
	// public short[] getResolvedPadding() {
	// return resolvedPadding;
	// }

	protected void resolveRadius(short[] original, short[] result) {
		for (int i = 0; i < 4; ++i) {
			short o = original[i];
			if (o >= 0) {
				result[i] = o;
			} else {
				if (resolvedWidth >= 0) {
					result[i] = (short) ((o - PERCENT_MIN) * resolvedWidth / 100);
				} else {
					result[i] = 0;
				}
			}
		}
	}

	private void resolveBorder(short[] result) {
		if (focused && !hiddenFocus) {
			if (page != null) {
				short b = page.getFocusBdSize();
				result[0] = result[1] = result[2] = result[3] = b;
			} else {
				resolvePaddingOrBorder(border, result);
			}
		} else {
			resolvePaddingOrBorder(border, result);
		}
	}

	private void resolvePadding(short[] result) {
		resolvePaddingOrBorder(padding, result);
	}

	private void resolvePaddingOrBorder(short[] original, short[] result) {
		for (int i = 0; i < 4; ++i) {
			short o = original[i];
			if (o >= 0) {
				result[i] = o;
			} else {
				if ((i & 0x1) == 0) {
					if (resolvedHeight >= 0) {
						result[i] = (short) ((o - PERCENT_MIN) * resolvedHeight / 100);
					} else {
						result[i] = 0;
					}
				} else {
					if (resolvedWidth >= 0) {
						result[i] = (short) ((o - PERCENT_MIN) * resolvedWidth / 100);
					} else {
						result[i] = 0;
					}
				}
			}
		}
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx, IFontUtil fu) {

		if (newRect.width < 0) {
			int w = 0;
			if (isPercent(resolvedPadding[SIDE_LEFT])) {
				w += resolvedPadding[SIDE_LEFT];
			}
			if (isPercent(resolvedPadding[SIDE_RIGHT])) {
				w += resolvedPadding[SIDE_RIGHT];
			}
			newRect.width = (short) w;
		}
		if (newRect.height < 0) {
			int h = 0;
			if (isPercent(resolvedPadding[SIDE_TOP])) {
				h += resolvedPadding[SIDE_TOP];
			}
			if (isPercent(resolvedPadding[SIDE_BOTTOM])) {
				h += resolvedPadding[SIDE_BOTTOM];
			}
			newRect.height = (short) h;
		}
	}

	public final void trigger() {
		if (!disabled) {
			boolean cancelled = false;
			if (page != null) {
				cancelled = page.willTrigger(this);
			}
			if (!cancelled) {
				triggerRaw();
			}
		}
	}

	protected void triggerRaw() {
	}

	public Sv3Element hit(int pointerX, int pointerY) {
		if (rect != null && rect.contains(pointerX, pointerY)) {
			return this;
		} else {
			return null;
		}
	}

	// public static Sv3Element hit(Vector items, int pointerX, int pointerY) {
	// int size = items.size();
	// for (int i = size - 1; i >= 0; --i) {
	// Sv3Element e = (Sv3Element) items.elementAt(i);
	// if (e.hit(pointerX, pointerY)) {
	// return e;
	// }
	// }
	// return null;
	// }

	final boolean layoutManualPosition(Rect newRect, LayoutContext ctx) {
		// both x and y are defined, enter manual position mode
		if (x != XY_AUTO && y != XY_AUTO) {
			if (isPercent(x)) {
				if (ctx.definedWidth >= 0) {
					newRect.x = (short) (((x - PERCENT_MIN) * ctx.definedWidth) / 100);
				} else {
					newRect.x = 0;
				}
			} else {
				newRect.x = x;
			}

			if (isPercent(y)) {
				if (ctx.definedHeight >= 0) {
					newRect.y = (y - PERCENT_MIN) * ctx.definedHeight / 100;
				} else {
					newRect.y = 0;
				}
			} else {
				newRect.y = y;
			}
			return true;
		} else { // flow layout mode
			return false;
		}
	}

	protected static int layoutSingleLineText(Rect newRect, String text,
			LayoutContext ctx, IFontUtil fu, Object font, short[] inPadding,
			int marginX, int marginY, int minTextWidth) {
		int fontHeight = fu.getFullHeight(font);

		int width = 0;
		if (text != null) {
			width = fu.getStringWidth(font, text);
		}

		if (newRect.width < 0) {
			int newWidth = width + inPadding[1] + inPadding[3] + marginX * 2;

			short boundWidth = ctx.resolvedBoundWidth();
			if (newWidth > boundWidth) {
				int minAllowed = minTextWidth + inPadding[1] + inPadding[3];
				if (boundWidth >= minAllowed) {
					newWidth = boundWidth;
				} else {
					newWidth = minAllowed;
				}
			}
			newRect.width = (short) newWidth;
		}

		if (newRect.height < 0) {
			newRect.height = fontHeight + inPadding[0] + inPadding[2] + marginY
					* 2;
		}
		return width;
	}

	protected static void renderSingleLineText(IRenderContext ctx, String text,
			short textWidth, Rect textBound, IFontUtil fu, Object font,
			int color, byte align) {
		if (text != null) {
			int fontheight = fu.getFullHeight(font);

			textBound.y += (textBound.height - fontheight) / 2;
			textBound.height = fontheight;
			if (align == Sv3Div.ALIGN_C) {
				textBound.x += (textBound.width - textWidth) / 2;
			} else if (align == Sv3Div.ALIGN_R) {
				textBound.x += (textBound.width - textWidth);
			} else {
			}

			ctx.setColor(color);
			ctx.setFont(font);
			ctx.drawString(text, textBound.x, textBound.y);
		}
	}

	boolean canSeeThroughAncestorFocus() {
		if (focused) {
			return true;
		} else if (parent != null) {
			return bgColor == DEFAULT_BG_COLOR
					&& parent.canSeeThroughAncestorFocus();
		} else {
			return false;
		}
	}

	public int getResolvedColor() {
		if (!hiddenFocus && canSeeThroughAncestorFocus()) {
			if (page != null) {
				int c = page.getFocusColor();
				return c == -1 ? FOCUS_COLOR : c;
			} else {
				return FOCUS_COLOR;
			}
		} else {
			return color;
		}
	}

	private int getResolvedFocusBgColor() {
		if (page != null) {
			int c = page.getFocusBgColor();
			return c == -1 ? FOCUS_BG_COLOR : c;
		} else {
			return FOCUS_BG_COLOR;
		}
	}

	private int getResolvedFocusBgColor2() {
		if (page != null) {
			int c = page.getFocusBgColor2();
			return c == -1 ? FOCUS_BG_COLOR2 : c;
		} else {
			return FOCUS_BG_COLOR2;
		}
	}

	public void layout(LayoutContext ctx, IFontUtil fu) {
		if (hidden) {
			setRect(null);
		} else {
			Rect newRect = new Rect(0, 0, -1, -1);
			this.layoutManualSize(newRect, ctx);
			this.layoutContent(newRect, ctx, fu);
			if (this.layoutManualPosition(newRect, ctx)) {
				// is manual
				ctx.addManualRect(newRect);
			} else {
				// is not manual
				ctx.addRect(newRect, valign, lineWrap, width == SIZE_FIT);
			}
			setRect(newRect);
		}
	}

	protected void renderBg(IRenderContext ctx, Rect bounding, short[] radius) {
		renderBg(ctx, bounding, radius, -1);
	}

	protected void renderBg(IRenderContext ctx, Rect bounding,
			short[] originalRadius, int highlightColor) {
		short[] radius = ctx.resetAndGetTempShortArray();// new short[4];
		resolveRadius(originalRadius, radius);
		Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
		int bgColor = this.bgColor;
		int bgColor2 = this.bgColor2;
		if (focused && !hiddenFocus) {
			if (highlightColor >= 0) {
				highlightColor = PRESSED_HIGHLIGHT_COLOR;
			} else {
				bgColor = getResolvedFocusBgColor();
				bgColor2 = getResolvedFocusBgColor2();
			}
		}
		ctx.fillRoundedRect(r, radius, bgColor, bgColor2, highlightColor);
	}

	protected void renderBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {
		Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
		int bgColor = this.bgColor;
		int bgColor2 = this.bgColor2;
		if (focused && !hiddenFocus) {
			bgColor = getResolvedFocusBgColor();
			if (gradientFocus) {
				bgColor2 = getResolvedFocusBgColor2();
			} else {
				bgColor2 = -1;
			}
		}
		ctx.fillGradientRect(r, bgColor, bgColor2);
	}

	private int getResolvedBdColor() {
		if (focused && !hiddenFocus) {
			if (page != null) {
				return page.getFocusBdColor();
			} else {
				return bdColor;
			}
		} else {
			return bdColor;
		}
	}

	protected void renderBorder(IRenderContext ctx, Rect bounding, Rect rect) {
		int bdc = getResolvedBdColor();
		if (bdc >= 0) {
			short[] border = ctx.resetAndGetTempShortArray();// new short[4];
			resolveBorder(border);

			short top = border[SIDE_TOP];
			short right = border[SIDE_RIGHT];
			short bottom = border[SIDE_BOTTOM];
			short left = border[SIDE_LEFT];

			if (top > 0 || right > 0 || bottom > 0 || left > 0) {
				// Rect bordered = rect.toRectWithOffsetAndBorder(bounding,
				// border);
				Rect bordered = rect.toRectWithOffset(bounding,
						ctx.getAbsRect());
				ctx.setColor(bdc);
				// if (top == right && right == bottom && bottom == left) //
				// uniform border
				// {
				// CGContextSetLineWidth(ctx, top);
				// ctx.drawRect(bordered.x, bordered.y, bordered.width,
				// bordered.height);
				// } else {
				int x1, y1, x2, y2;
				if (top > 0) {
					x1 = bordered.x;
					y1 = bordered.y;
					x2 = bordered.getRight();
					if (top > 1) {
						ctx.fillRect(x1, y1, x2 - x1, top);
					} else {
						ctx.drawLine(x1, y1, x2 - 1, y1);
					}
				}
				if (right > 0) {
					x1 = bordered.getRight();
					y1 = bordered.y;
					y2 = bordered.getBottom();
					if (right > 1) {
						ctx.fillRect(x1 - right, y1, right, y2 - y1);
					} else {
						ctx.drawLine(x1 - 1, y1, x1 - 1, y2 - 1);
					}
				}
				if (bottom > 0) {
					x1 = bordered.x;
					y1 = bordered.getBottom();
					x2 = bordered.getRight();
					if (bottom > 1) {
						ctx.fillRect(x1, y1 - bottom, x2 - x1, bottom);
					} else {
						ctx.drawLine(x1, y1 - 1, x2 - 1, y1 - 1);
					}
				}
				if (left > 0) {
					x1 = bordered.x;
					y1 = bordered.y;
					y2 = bordered.getBottom();
					if (left > 1) {
						ctx.fillRect(x1, y1, left, y2 - y1);
					} else {
						ctx.drawLine(x1, y1, x1, y2 - 1);
					}
				}
				// }
			}
		}
	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				// IFontUtil fu = ctx.getFu();
				// Rect absPadded = absRect.toRectWithPadding(padding);
				this.renderBg(ctx, bounding, rect, true);
				this.renderBorder(ctx, bounding, rect);
			}
		}
	}

	protected Object lastFont = null;

	public Object getFont(IFontUtil fu) {
		if (lastFont == null)
			lastFont = fu.getFont(fontStyle, fontSize);
		return lastFont;
	}

	public boolean canFocus() {
		return url != null && !isDisabledByParent();
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		switch (key) {
		case TAG_Id:
			id = r.readStringOrNull();
			if (id != null && page != null) {
				page.registerElement(this);
			}
			break;
		case TAG_Hidden:
			hidden = r.readBoolean();
			break;
		case TAG_X:
			x = (short) r.readInt();
			break;
		case TAG_Y:
			y = (short) r.readInt();
			break;
		case TAG_Width:
			width = (short) r.readInt();
			break;
		case TAG_Height:
			height = (short) r.readInt();
			break;
		case TAG_Valign:
			valign = (byte) r.readInt();
			break;
		case TAG_Color:
			color = r.readInt();
			break;
		case TAG_Bdcolor:
			bdColor = r.readInt();
			break;
		case TAG_Bgcolor:
			bgColor = r.readInt();
			break;
		case TAG_Bgcolor2:
			bgColor2 = r.readInt();
			break;
		case TAG_Url:
			url = r.readStringOrNull();
			break;
		case TAG_Urlmode:
			urlMode = (byte) r.readInt();
			break;
		case TAG_Confirmation:
			confirmation = r.readStringOrNull();
			break;
		case TAG_Tips:
			tips = r.readStringOrNull();
			break;
		case TAG_Name:
			name = r.readStringOrNull();
			break;
		case TAG_LineWrap:
			lineWrap = (byte) r.readInt();
			break;
		case TAG_FontSize:
			fontSize = (byte) r.readInt();
			break;
		case TAG_FontStyle:
			fontStyle = (byte) r.readInt();
			break;
		case TAG_Padding:
			setPadding(r.readArrayOfShort());
			break;
		case TAG_Border:
			setBorder(r.readArrayOfShort());
			break;
		case TAG_HiddenFocus:
			hiddenFocus = r.readBoolean();
			break;
		case TAG_Disabled:
			disabled = r.readBoolean();
			break;
		default:
			// forwar compatibility
			r.read(); // read the value no matter what it is
			break;
		}
	}

	public boolean setStrAttrib(String key, String value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		// note that id is not included in this list (id is immutable once set)
		if ("bdcolor".equals(key)) {
			setBdColor(value);
		} else if ("bgcolor".equals(key)) {
			setBgColor(value);
		} else if ("bgcolor2".equals(key)) {
			setBgColor2(value);
		} else if ("color".equals(key)) {
			setColor(value);
		} else if ("border".equals(key)) {
			setBorder(Sv3Element.strToSides(value));
		} else if ("confirmation".equals(key)) {
			setConfirmation(value);
			needsRepaint = false;
		} else if ("name".equals(key)) {
			setName(value);
			needsRepaint = false;
		} else if ("padding".equals(key)) {
			setPadding(Sv3Element.strToSides(value));
			needsRelayout = true;
		} else if ("tips".equals(key)) {
			setTips(value);
			needsRepaint = false;
		} else if ("url".equals(key)) {
			setUrl(value);
			needsRepaint = false;
		} else if ("valign".equals(key)) {
			setValign(value);
			needsRelayout = true;
		} else if ("urlmode".equals(key)) {
			setUrlMode(value);
			needsRepaint = false;
		} else if ("linewrap".equals(key)) {
			setLineWrap(value);
			needsRelayout = true;
		} else if ("fontsize".equals(key)) {
			setFontSize(value);
			needsRelayout = true;
		} else if ("fontstyle".equals(key)) {
			setFontStyle(value);
			needsRelayout = true;
		} else if ("x".equals(key)) {
			setX(value);
			needsRelayout = true;
		} else if ("y".equals(key)) {
			setY(value);
			needsRelayout = true;
		} else if ("width".equals(key)) {
			setWidth(value);
			needsRelayout = true;
		} else if ("height".equals(key)) {
			setHeight(value);
			needsRelayout = true;
		} else {
			return false;
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	public boolean setIntAttrib(String key, int value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		if ("border".equals(key)) {
			setBorder((short) value);
		} else if ("fontsize".equals(key)) {
			setFontSize((byte) value);
			needsRelayout = true;
		} else if ("fontstyle".equals(key)) {
			setFontStyle((byte) value);
			needsRelayout = true;
		} else if ("height".equals(key)) {
			setHeight((short) value);
			needsRelayout = true;
		} else if ("linewrap".equals(key)) {
			setLineWrap((byte) value);
			needsRelayout = true;
		} else if ("padding".equals(key)) {
			setPadding((short) value);
			needsRelayout = true;
		} else if ("urlmode".equals(key)) {
			setUrlMode((byte) value);
			needsRepaint = false;
		} else if ("valign".equals(key)) {
			setValign((byte) value);
			needsRelayout = true;
		} else if ("hidden".equals(key)) {
			setHidden(value != 0);
			needsRelayout = true;
		} else if ("width".equals(key)) {
			setWidth((short) value);
			needsRelayout = true;
		} else if ("x".equals(key)) {
			setX((short) value);
			needsRelayout = true;
		} else if ("y".equals(key)) {
			setY((short) value);
			needsRelayout = true;
		} else if ("color".equals(key)) {
			setColor(value);
		} else if ("bdcolor".equals(key)) {
			setBdColor(value);
		} else if ("bgcolor".equals(key)) {
			setBgColor(value);
		} else if ("bgcolor2".equals(key)) {
			setBgColor2(value);
		} else if ("hiddenfocus".equals(key)) {
			setHiddenFocus(value != 0);
			needsRepaint = false;
		} else if ("disabled".equals(key)) {
			setDisabled(value != 0);
		} else if ("border-b".equals(key) || "border-bottom".equals(key)) {
			setBorder((short) value, Sv3Element.SIDE_BOTTOM);
		} else if ("border-l".equals(key) || "border-left".equals(key)) {
			setBorder((short) value, Sv3Element.SIDE_LEFT);
		} else if ("border-r".equals(key) || "border-right".equals(key)) {
			setBorder((short) value, Sv3Element.SIDE_RIGHT);
		} else if ("border-t".equals(key) || "border-top".equals(key)) {
			setBorder((short) value, Sv3Element.SIDE_TOP);
		} else if ("padding-b".equals(key) || "padding-bottom".equals(key)) {
			setPadding((short) value, Sv3Element.SIDE_BOTTOM);
			needsRelayout = true;
		} else if ("padding-l".equals(key) || "padding-left".equals(key)) {
			setPadding((short) value, Sv3Element.SIDE_LEFT);
			needsRelayout = true;
		} else if ("padding-r".equals(key) || "padding-right".equals(key)) {
			setPadding((short) value, Sv3Element.SIDE_RIGHT);
			needsRelayout = true;
		} else if ("padding-t".equals(key) || "padding-top".equals(key)) {
			setPadding((short) value, Sv3Element.SIDE_TOP);
			needsRelayout = true;
		} else {
			return false;
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	protected void fireAttribEvent(String key, boolean needsRepaint,
			boolean needsRelayout) {
		if (page != null) {
			page.didChangeAttrib(this, key, needsRepaint, needsRelayout);
		}
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("id".equals(key)) {
			return id;
		} else if ("hidden".equals(key)) {
			return new Integer(hidden ? 1 : 0);
		} else if ("x".equals(key)) {
			return new Integer(x);
		} else if ("y".equals(key)) {
			return new Integer(y);
		} else if ("width".equals(key)) {
			return new Integer(width);
		} else if ("height".equals(key)) {
			return new Integer(height);
		} else if ("valign".equals(key)) {
			return new Integer(valign);
		} else if ("color".equals(key)) {
			return new Integer(color);
		} else if ("bdcolor".equals(key)) {
			return new Integer(bdColor);
		} else if ("bgcolor".equals(key)) {
			return new Integer(bgColor);
		} else if ("bgcolor2".equals(key)) {
			return new Integer(bgColor2);
		} else if ("url".equals(key)) {
			return url;
		} else if ("urlmode".equals(key)) {
			return new Integer(urlMode);
		} else if ("confirmation".equals(key)) {
			return confirmation;
		} else if ("tips".equals(key)) {
			return tips;
		} else if ("name".equals(key)) {
			return name;
		} else if ("linewrap".equals(key)) {
			return new Integer(lineWrap);
		} else if ("fontsize".equals(key)) {
			return new Integer(fontSize);
		} else if ("fontstyle".equals(key)) {
			return new Integer(fontStyle);
		} else if ("hiddenfocus".equals(key)) {
			return new Integer(hiddenFocus ? 1 : 0);
		} else if ("disabled".equals(key)) {
			return new Integer(disabled ? 1 : 0);
		} else if ("border".equals(key)) {
			return StringUtil.join(border, ',');
		} else if ("padding".equals(key)) {
			return StringUtil.join(padding, ',');
		} else if ("border-b".equals(key) || "border-bottom".equals(key)) {
			return new Integer(border[SIDE_BOTTOM]);
		} else if ("border-l".equals(key) || "border-left".equals(key)) {
			return new Integer(border[SIDE_LEFT]);
		} else if ("border-r".equals(key) || "border-right".equals(key)) {
			return new Integer(border[SIDE_RIGHT]);
		} else if ("border-t".equals(key) || "border-top".equals(key)) {
			return new Integer(border[SIDE_TOP]);
		} else if ("padding-b".equals(key) || "padding-bottom".equals(key)) {
			return new Integer(padding[SIDE_BOTTOM]);
		} else if ("padding-l".equals(key) || "padding-left".equals(key)) {
			return new Integer(padding[SIDE_LEFT]);
		} else if ("padding-r".equals(key) || "padding-right".equals(key)) {
			return new Integer(padding[SIDE_RIGHT]);
		} else if ("padding-t".equals(key) || "padding-top".equals(key)) {
			return new Integer(padding[SIDE_TOP]);
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public void setIdRestricted(String id) {
		this.id = id;
	}

	// public boolean isVisible() {
	// return !hidden;
	// }

	// public void setVisible(boolean visible) {
	// this.hidden = visible;
	// }

	public boolean isHiddenByParent() {
		if (hidden) {
			return true;
		} else {
			if (parent != null) {
				return parent.isHiddenByParent();
			} else {
				return false;
			}
		}
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * applicable to x/y/width/height and padding, border and radius
	 */
	public static boolean isPercent(short value) {
		return value >= Sv3Element.PERCENT_MIN
				&& value <= Sv3Element.PERCENT_MAX;
	}

	public short getX() {
		return x;
	}

	public void setX(short x) {
		this.x = x;
	}

	public void setPercentX(int x) {
		if (x >= 0 && x <= 100) {
			this.x = (short) (PERCENT_MIN + x);
		} else {
			throw new IllegalArgumentException("out of valid range [0, 100]");
		}
	}

	public short getY() {
		return y;
	}

	public void setY(short y) {
		this.y = y;
	}

	public void setPercentY(int y) {
		if (y >= 0 && y <= 100) {
			this.y = (short) (PERCENT_MIN + y);
		} else {
			throw new IllegalArgumentException("out of valid range [0, 100]");
		}
	}

	public short getWidth() {
		return width;
	}

	public boolean hasManualSizeInPixels() {
		return width >= 0 && height >= 0;
	}

	public void setWidth(short width) {
		this.width = width;
	}

	public void setPercentWidth(int width) {
		if (width >= 0 && width <= 100) {
			this.width = (short) (PERCENT_MIN + width);
		} else {
			throw new IllegalArgumentException("out of valid range [0, 100]");
		}
	}

	public short getHeight() {
		return height;
	}

	public void setHeight(short height) {
		this.height = height;
	}

	public void setPercentHeight(int height) {
		if (height >= 0 && height <= 100) {
			this.height = (short) (PERCENT_MIN + height);
		} else {
			throw new IllegalArgumentException("out of valid range [0, 100]");
		}
	}

	public void setHeightRelativeToWidth(int height) {
		if (height >= 0 && height <= 10000) {
			this.height = (short) (PERCENT_OF_W_MIN + height);
		} else {
			throw new IllegalArgumentException("out of valid range [0, 10000]");
		}
	}

	// public void setGeometry(int paramId, String value) throws
	// RuntimeException {
	// value = value.trim();
	//
	// short shortValue;
	// if (value.endsWith("%")) {
	// String subValue = value.substring(0, value.length() - 1);
	// shortValue = Short.parseShort(subValue);
	// percent |= paramId;
	// } else {
	// shortValue = Short.parseShort(value);
	// percent &= ~paramId;
	// }
	// if (paramId == GEOMETRY_X) {
	// x = shortValue;
	// } else if (paramId == GEOMETRY_Y) {
	// y = shortValue;
	// } else if (paramId == GEOMETRY_W) {
	// width = shortValue;
	// } else if (paramId == GEOMETRY_H) {
	// height = shortValue;
	// } else {
	// throw new RuntimeException("invalid argument 'paramId'");
	// }
	// }

	public void setX(String value) {
		value = value.trim();
		this.x = possiblePercentStrToValue(value, false);
	}

	public void setY(String value) {
		value = value.trim();
		this.y = possiblePercentStrToValue(value, false);
	}

	public void setWidth(String value) {
		value = value.trim().toLowerCase();
		if ("auto".equals(value)) {
			this.width = SIZE_AUTO;
		} else {
			this.width = possiblePercentStrToValue(value, false);
		}
	}

	public void setHeight(String value) {
		value = value.trim().toLowerCase();
		if ("auto".equals(value)) {
			this.height = SIZE_AUTO;
		} else if ("width".equals(value)) {
			this.height = PERCENT_OF_W_MIN + 100; // "width*100%"
		} else {
			this.height = possiblePercentStrToValue(value, true);
		}
	}

	private static short possiblePercentStrToValue(String trimmedValue,
			boolean supportPercentOfWidth) {
		short shortValue;
		boolean normalPercent = false;
		if (trimmedValue.endsWith("%")) {
			if (supportPercentOfWidth && trimmedValue.startsWith("width*")) {
				String subValue = trimmedValue.substring(6,
						trimmedValue.length() - 1);
				shortValue = Short.parseShort(subValue);
				if (shortValue >= 0 && shortValue <= 10000) {
					shortValue += PERCENT_OF_W_MIN;
				} else {
					shortValue = 0;
				}
			} else {
				normalPercent = true;
				String subValue = trimmedValue.substring(0,
						trimmedValue.length() - 1);
				shortValue = Short.parseShort(subValue);
			}
		} else {
			shortValue = Short.parseShort(trimmedValue);
		}
		if (normalPercent) {
			if (shortValue >= 0 && shortValue <= 100) {
				return (short) (shortValue + PERCENT_MIN);
			} else {
				return 0;
			}
		} else {
			return shortValue;
		}
	}

	public short[] getPadding() {
		return padding;
	}

	public void setPadding(short value) {
		padding[0] = padding[1] = padding[2] = padding[3] = value;
	}

	public void setPadding(short[] padding) {
		setPadding(padding, padding.length);
	}

	public void setPadding(short[] padding, int effectiveCount) {
		for (int i = 0; i < effectiveCount; ++i) {
			this.padding[i] = padding[i];
		}
		propagate(this.padding, effectiveCount);
	}

	public short getPadding(short side) {
		return padding[side];
	}

	public void setPadding(short padding, short side) {
		this.padding[side] = padding;
	}

	public void setPadding(short t, short r, short b, short l) {
		padding[SIDE_TOP] = t;
		padding[SIDE_RIGHT] = r;
		padding[SIDE_BOTTOM] = b;
		padding[SIDE_LEFT] = l;
	}

	public boolean hasPadding() {
		return padding[0] != 0 || padding[1] != 0 || padding[2] != 0
				|| padding[3] != 0;
	}

	public void setBorder(short t, short r, short b, short l) {
		border[SIDE_TOP] = t;
		border[SIDE_RIGHT] = r;
		border[SIDE_BOTTOM] = b;
		border[SIDE_LEFT] = l;
	}

	public boolean hasBorder() {
		return border[0] != 0 || border[1] != 0 || border[2] != 0
				|| border[3] != 0;
	}

	public short[] getBorder() {
		return border;
	}

	public short getBorder(short side) {
		return border[side];
	}

	public void setBorder(short[] border) {
		setBorder(border, border.length);
	}

	public void setBorder(short[] border, int effectiveCount) {
		for (int i = 0; i < effectiveCount; ++i) {
			this.border[i] = border[i];
		}
		propagate(this.border, effectiveCount);
	}

	public void setBorder(short value) {
		border[0] = border[1] = border[2] = border[3] = value;
	}

	public void setBorder(short border, short side) {
		this.border[side] = border;
	}

	public byte getValign() {
		return valign;
	}

	public void setValign(String valign) {
		this.valign = strToValign(valign);
	}

	public void setValign(byte valign) {
		this.valign = valign;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public void setBdColor(String string) {
		bdColor = strToColor(string);
	}

	public void setColor(String string) {
		color = strToColor(string);
	}

	public void setBgColor(String string) {
		bgColor = strToColor(string);
	}

	public void setBgColor2(String string) {
		bgColor2 = strToColor(string);
	}

	public int getBdColor() {
		return bdColor;
	}

	public void setBdColor(int bdColor) {
		this.bdColor = bdColor;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getBgColor2() {
		return bgColor2;
	}

	public void setBgColor2(int bgColor2) {
		this.bgColor2 = bgColor2;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte getUrlMode() {
		return urlMode;
	}

	public void setUrlMode(byte urlMode) {
		this.urlMode = urlMode;
	}

	public void setUrlMode(String urlMode) {
		this.urlMode = strToUrlMode(urlMode);
	}

	public String getConfirmation() {
		return confirmation;
	}

	public void setConfirmation(String confirmation) {
		this.confirmation = confirmation;
	}

	public String getTips() {
		return tips;
	}

	public void setTips(String tips) {
		this.tips = tips;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte getLineWrap() {
		return lineWrap;
	}

	public boolean wrapLineAfter() {
		return (lineWrap & LINE_WRAP_AFTER) != 0;
	}

	public boolean wrapLineBefore() {
		return (lineWrap & LINE_WRAP_BEFORE) != 0;
	}

	public void setLineWrap(byte lineWrap) {
		this.lineWrap = lineWrap;
	}

	public void setLineWrap(String lineWrap) {
		this.lineWrap = strToLineWrap(lineWrap);
	}

	public byte getFontSize() {
		return fontSize;
	}

	public void setFontSize(byte fontSize) {
		if (this.fontSize != fontSize) {
			this.fontSize = fontSize;
			lastFont = null;
		}
	}

	public void setFontSize(String fontSize) {
		setFontSize(strToFontSize(fontSize));
	}

	public byte getFontStyle() {
		return fontStyle;
	}

	public void setFontStyle(byte fontStyle) {
		if (this.fontStyle != fontStyle) {
			this.fontStyle = fontStyle;
			lastFont = null;
		}
	}

	public void setFontStyle(String fontStyle) {
		setFontStyle(strToFontStyle(fontStyle));
	}

	public boolean isHiddenFocus() {
		return hiddenFocus;
	}

	public void setHiddenFocus(boolean hiddenFocus) {
		this.hiddenFocus = hiddenFocus;
	}

	public boolean isDisabledByParent() {
		if (disabled) {
			return true;
		} else {
			if (parent != null) {
				return parent.isDisabledByParent();
			} else {
				return false;
			}
		}
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public void setDisabledWithEvent(boolean disabled) {
		boolean triggerEvent = false;
		boolean cancelled = false;
		if (disabled != this.disabled && page != null) {
			triggerEvent = true;
			if (disabled) {
				cancelled = page.willFocus(this);
			} else {
				cancelled = page.willUnfocus(this);
			}
		}
		if (!cancelled) {
			this.disabled = disabled;
			if (triggerEvent) {
				if (disabled) {
					page.didFocus(this);
				} else {
					page.didUnfocus(this);
				}
			}
		}
	}

	public Rect getRect() {
		return rect;
	}

	private static final Rect ZERO_RECT = new Rect(0, 0, 0, 0);

	public Rect getRectSafely() {
		return rect == null ? ZERO_RECT : rect;
	}

	protected void setRect(Rect rect) {
		this.rect = rect;
		this.absoluteRect = null;
	}

	public boolean hasRect() {
		return rect != null;
	}

	public Rect getAbsoluteRectSafely() {
		Rect absRect = this.getAbsoluteRect();
		return absRect == null ? ZERO_RECT : absRect;
	}

	public Rect getAbsoluteRect() {
		if (absoluteRect == null && rect != null) {
			Rect r = new Rect(rect);
			for (Sv3Div p = parent; p != null; p = p.getParent()) {
				Rect pr = p.getPaddedRect();
				r.x += pr.x;
				r.y += pr.y;
			}
			absoluteRect = r;
		}
		return absoluteRect;
	}

	// == attributes ===
	// script functions
	// public void executeFuction(String function) {
	// function = function.toLowerCase();
	// if("sv3.moveItem")
	// }

	// end of script functions

	// convert functions

	public static boolean strToBoolean(String str) {
		int b = Integer.parseInt(str);
		if (b == 0) {
			return false;
		} else {
			return true;
		}
	}

	public static int strToColor(String str) {
		str = str.trim().toLowerCase();
		if (str.length() > 0) {
			if (str.charAt(0) == '#') {
				str = str.replace('#', ' ').trim();
				if (str.length() != 6) {
					throw new NumberFormatException();
				}
				int color = Integer.parseInt(str, 16);
				return color;
			} else {
				if ("red".equals(str)) {
					return 0xff0000;
				} else if ("white".equals(str)) {
					return 0xffffff;
				} else if ("cyan".equals(str)) {
					return 0x00ffff;
				} else if ("silver".equals(str)) {
					return 0xc0c0c0;
				} else if ("blue".equals(str)) {
					return 0x0000ff;
				} else if ("grey".equals(str) || "gray".equals(str)) {
					return 0x808080;
				} else if ("darkblue".equals(str)) {
					return 0x0000a0;
				} else if ("black".equals(str)) {
					return 0x000000;
				} else if ("lightblue".equals(str)) {
					return 0xadd8e6;
				} else if ("orange".equals(str)) {
					return 0xffa500;
				} else if ("purple".equals(str)) {
					return 0x800080;
				} else if ("brown".equals(str)) {
					return 0xa52a2a;
				} else if ("yellow".equals(str)) {
					return 0xffff00;
				} else if ("maroon".equals(str)) {
					return 0x800000;
				} else if ("lime".equals(str)) {
					return 0x00ff00;
				} else if ("green".equals(str)) {
					return 0x008000;
				} else if ("fuchsia".equals(str)) {
					return 0xff00ff;
				} else if ("olive".equals(str)) {
					return 0x808000;
				} else {
					if (str.length() != 6) {
						throw new NumberFormatException();
					}
					int color = Integer.parseInt(str, 16);
					return color;
				}
			}
		} else {
			return -1;
		}
	}

	private byte strToUrlMode(String value) {
		value = value.trim().toLowerCase();
		if ("backward".equals(value) || "1".equals(value)) {
			return URLMODE_BACK;
		} else if ("replace".equals(value) || "2".equals(value)) {
			return URLMODE_REPLACE;
		} else if ("submit-get".equals(value) || "3".equals(value)) {
			return URLMODE_SUBMIT_GET;
		} else if ("submit-post".equals(value) || "4".equals(value)) {
			return URLMODE_SUBMIT_POST;
		} else {
			return URLMODE_OPEN;
		}
	}

	public static byte strToValign(String value) {
		value = value.trim().toLowerCase();
		if ("middle".equals(value) || "1".equals(value)) {
			return VALIGN_M;
		} else if ("bottom".equals(value) || "2".equals(value)) {
			return VALIGN_B;
		} else {
			return VALIGN_T;
		}
	}

	public static byte strToLineWrap(String value) {
		value = value.trim().toLowerCase();
		if ("before".equals(value) || "1".equals(value)) {
			return LINE_WRAP_BEFORE;
		} else if ("after".equals(value) || "2".equals(value)) {
			return LINE_WRAP_AFTER;
		} else if ("both".equals(value) || "3".equals(value)) {
			return LINE_WRAP_BOTH;
		} else {
			return LINE_WRAP_NONE;
		}
	}

	public static byte strToFontSize(String value) {
		value = value.trim().toLowerCase();
		if ("small".equals(value) || "-1".equals(value)) {
			return FONT_SIZE_S;
		} else if ("large".equals(value) || "1".equals(value)) {
			return FONT_SIZE_L;
		} else {
			return FONT_SIZE_M;
		}
	}

	public static byte strToFontStyle(String value) {
		value = value.trim().toLowerCase();
		if ("bold".equals(value) || "1".equals(value)) {
			return FONT_STYLE_B;
		} else if ("italic".equals(value) || "2".equals(value)) {
			return FONT_STYLE_I;
		} else if ("bold-italic".equals(value) || "3".equals(value)) {
			return FONT_STYLE_BI;
		} else {
			return FONT_STYLE_N;
		}
	}

	public static short[] strToSides(String str) {
		short[] values = new short[4];
		int valueCount = 0;
		int last = 0;
		int index = str.indexOf(',');
		while (index >= 0) {
			String sub = str.substring(last, index).trim();
			values[valueCount] = possiblePercentStrToValue(sub, false);
			++valueCount;
			last = index + 1;
			index = str.indexOf(',', last);
			if (valueCount >= 4)
				break;
		}
		if (valueCount < 4) {
			String sub = str.substring(last);
			values[valueCount] = possiblePercentStrToValue(sub, false);
			++valueCount;
		}

		propagate(values, valueCount);
		return values;
	}

	private static void propagate(short[] values, int effectiveCount) {
		if (effectiveCount == 0) {
			values[0] = values[1] = values[2] = values[3] = 0;
		} else if (effectiveCount == 1) {
			values[1] = values[2] = values[3] = values[0];
		} else if (effectiveCount == 2) {
			values[2] = values[0]; // bottom same as top
			values[3] = values[1]; // left same as right
		} else if (effectiveCount == 3) {
			values[3] = values[1]; // left same as right
		} else {
			// ok
		}
	}

	// end of convert functions

	public static String strFromColor(int color) {
		if (color < 0) {
			return null;
		} else {
			String number = Integer.toString(color, 16);
			StringBuffer sb = new StringBuffer();
			sb.append("#");
			for (int i = 0; i < 6 - number.length(); ++i) {
				sb.append("0");
			}
			sb.append(number);
			return sb.toString();
		}
	}

	private boolean focused = false;

	public boolean isFocused() {
		return focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public void setFocusedWithEvent(boolean focused) {
		boolean triggerEvent = false;
		boolean cancelled = false;
		if (focused != this.focused && page != null) {
			triggerEvent = true;
			if (focused) {
				cancelled = page.willFocus(this);
			} else {
				cancelled = page.willUnfocus(this);
			}
		}
		if (!cancelled) {
			this.focused = focused;
			if (triggerEvent) {
				if (focused) {
					page.didFocus(this);
				} else {
					page.didUnfocus(this);
				}
			}
		}
	}

}
