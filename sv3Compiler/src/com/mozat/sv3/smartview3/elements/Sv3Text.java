package com.mozat.sv3.smartview3.elements;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.EmoticonSegment;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.layout.TextBreaker;
import com.mozat.sv3.smartview3.layout.TextSegment;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;

public class Sv3Text extends Sv3Element {
	protected static final byte TAG_Text = 64, TAG_MaxLines = 65,
			TAG_Spacing = 66, TAG_Truncate = 67;

	public static final byte TRUNCATE_NONE = 0, TRUNCATE_HEAD = 1,
			TRUNCATE_MIDDLE = 2, TRUNCATE_TAIL = 3;
	// public static final byte LINK_STYLE_NONE = 0, LINK_STYLE_UNDERLINE = 1,
	// LINK_STYLE_BLUE = 2, LINK_STYLE_RED = 2;

	// attributes defined by SmartView3
	protected String text;
	protected short maxLines = Short.MAX_VALUE;
	protected short spacing;
	protected byte truncate = TRUNCATE_NONE;
	private boolean cursorVisible = false;

	// private byte linkStyle;

	// =======

	protected final Vector segments = new Vector();

	public Sv3Text(String id) {
		super(id);
	}

	public Sv3Text(String id, Sv3Text t, Sv3Page p) {
		super(id, t, p);
		this.text = t.text;
		this.maxLines = t.maxLines;
		this.spacing = t.spacing;
		this.truncate = t.truncate;
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3Text(id, this, p);
	}

	public byte getSv3Type() {
		return TYPE_TEXT;
	}

	public Sv3Element hit(int pointerX, int pointerY) {
		int size = segments.size();
		for (int i = 0; i < size; ++i) {
			TextSegment seg = (TextSegment) segments.elementAt(i);
			if (seg.contains(pointerX, pointerY)) {
				return this;
			}
		}
		return null;
		// } else {
		// return null;
		// }
	}

	protected void triggerRaw() {
		super.triggerRaw();
		Sv3Div p = this.getParent();
		if (p instanceof Sv3Select) {
			((Sv3Select) p).triggerChildRaw(this);
		}
	}

	public void layout(LayoutContext ctx, IFontUtil fu) {
		if (hidden) {
			segments.removeAllElements();
			setRect(null);
			return;
		}
		// rect is always null, see comments at the end of this method
		setRect(null);
		short boundWidth = ctx.resolvedBoundWidth();
		int boundHeight = ctx.resolvedBoundHeight();
		Object font = this.getFont(fu);
		short fontHeight = (short) fu.getFullHeight(font);

		int _maxlines = maxLines;
		if (maxLines <= 0) {
			// this is a conservative estimation, just so that CPU won't be
			// wasted calculating useless layout
			_maxlines = (boundHeight - ctx.getLineY())
					/ (fontHeight + spacing * 2) + 1;
		}

		if (needReBreak) {
			segments.removeAllElements();
			TextBreaker breaker = new TextBreaker();
			Vector pieces = new Vector();

			int firstLine = -1;
			if (!ctx.needToWrapLine(lineWrap)) {
				firstLine = boundWidth - ctx.getLineWidth();
			}
			if (firstLine <= 0) {
				firstLine = boundWidth;
			}

			if (text != null) {
				breaker.breakText(text, pieces, font, fu, firstLine,
						boundWidth, _maxlines, getEmoticonTable());
			}
			layoutSegs(pieces, ctx, boundHeight, fontHeight, true);

			// synchronized(segments)
			// {
			// !!! IMPORTANT:
			// 'rect' cannot be set for Sv3Text because the rect of each segment
			// will be adjusted when the line is
			// wrapped. Since the values here are not finalized yet, it wouldn't
			// be accurate to use them to
			// calculate the bounding rect of this Sv3Text element.
			// !!! IMPORTANT:

			// if (size > 0) {
			// rect = new Rect(minX, minY, maxX - minX, maxY - minY);
			// }
			// }
		} else {
			layoutSegs(segments, ctx, boundHeight, fontHeight, false);
		}
		needReBreak = false;
	}

	private void layoutSegs(Vector segs, LayoutContext ctx, int boundHeight,
			int fontHeight, boolean addToSegs) {
		synchronized (segs) {
			int size = segs.size();
			// int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX =
			// Integer.MIN_VALUE, maxY =
			// Integer.MIN_VALUE;
			for (int i = 0; i < size; ++i) {
				TextSegment seg = (TextSegment) segs.elementAt(i);
				if (ctx.getLineY() > boundHeight) {
					break;
				}

				int segHeight = fontHeight;
				if (seg instanceof EmoticonSegment) {
					segHeight = ((EmoticonSegment) seg).getEmoticon()
							.getHeight();
				}
				seg.height = segHeight + spacing * 2;
				if (addToSegs)
					segments.addElement(seg);
				byte wrap = lineWrap;
				// if (i == size - 1) {
				// wrap |= LINE_WRAP_AFTER;
				// }
				ctx.addRect(seg, valign, wrap);
			}
		}
	}

	protected void renderSegment(TextSegment seg, IRenderContext ctx,
			Rect bounding, String fullText, Object font, Rect viewport,
			boolean renderCursor) {
		Rect absRect = seg.toRectWithOffset(bounding, ctx.getAbsRect());
		if (absRect.overlaps(viewport)) {
			String url = getUrl();
			if (url != null && url.length() > 0) {
				this.setBorder((short) 1, Sv3Element.SIDE_BOTTOM);
			}

			super.renderBg(ctx, bounding, seg, false);
			super.renderBorder(ctx, bounding, seg);

			renderSegmentText(seg, ctx, fullText, font, absRect,
					getResolvedColor(), spacing);
			if (renderCursor) {
				IFontUtil fu = ctx.getFu();
				renderCursor(
						ctx,
						absRect.x
								+ fu.getSubStringWidth(font, fullText,
										seg.location, seg.length), absRect.y
								+ spacing + 2, font);
			}
		}
	}

	private void renderCursor(IRenderContext ctx, int x, int y, Object font) {
		ctx.setColor(0);
		ctx.fillRect(x, y, 2, ctx.getFu().getFullHeight(font) - spacing * 2 - 4);
	}

	protected void renderSegmentText(TextSegment seg, IRenderContext ctx,
			String fullText, Object font, Rect r, int color, int spacing) {
		ctx.setFont(font);
		ctx.setColor(color);
		if (seg.location == 0 && seg.length == fullText.length())
			ctx.drawString(fullText, r.x, r.y + spacing);
		else
			ctx.drawSubstring(fullText, seg.location, seg.length, r.x, r.y
					+ spacing);
	}

	public void render(IRenderContext ctx) {
		if (!hidden) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			IFontUtil fu = ctx.getFu();
			Object font = this.getFont(fu);

			ctx.setColor(color);
			synchronized (segments) {
				int size = segments.size();
				for (int i = 0; i < size; ++i) {
					TextSegment seg = (TextSegment) segments.elementAt(i);
					this.renderSegment(seg, ctx, bounding, text, font,
							viewport, cursorVisible && i == size - 1);
				}
				if (size == 0 && cursorVisible)
					renderCursor(ctx, bounding.x, bounding.y, font);
			}
		}
	}

	protected Hashtable getEmoticonTable() {
		return null;
	}

	protected boolean isCursorVisible() {
		return cursorVisible;
	}

	public void setCursorVisible(boolean cursorVisible) {
		this.cursorVisible = cursorVisible;
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_Text:
				text = r.readStringOrNull();
				break;
			case TAG_MaxLines:
				maxLines = (short) r.readInt();
				break;
			case TAG_Spacing:
				spacing = (short) r.readInt();
				break;
			case TAG_Truncate:
				truncate = (byte) r.readInt();
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
		} else if ("truncate".equals(key)) {
			setTruncate(value);
			needsRelayout = true;
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
		if ("maxlines".equals(key)) {
			setMaxLines((byte) value);
			needsRelayout = true;
		} else if ("spacing".equals(key)) {
			setSpacing((byte) value);
			needsRelayout = true;
		} else if ("truncate".equals(key)) {
			setTruncate((byte) value);
			needsRelayout = true;
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
		} else if ("maxlines".equals(key)) {
			return new Integer(maxLines);
		} else if ("spacing".equals(key)) {
			return new Integer(spacing);
		} else if ("truncate".equals(key)) {
			return new Integer(truncate);
		}
		return super.getAttrib(key);
	}

	public String getText() {
		return text;
	}

	// FIXME: needReBreak is added by Lei Lei intending to optimize the layout
	// speed
	// however, this optimization will cause incorrect layout result when the
	// resolved size
	// of this text element changes. E.g. when the width of the element is
	// modified, or
	// when this element is in percentage width and its parent's width changes.
	// Since the speed saved is significant, this optimization is left as is,
	// until the problem it causes is proven unacceptable.
	private boolean needReBreak = true;

	public void setText(String text) {
		this.text = text;
		this.needReBreak = true;
	}

	public void setHidden(boolean v) {
		if (v != isHidden())
			this.needReBreak = true;
		super.setHidden(v);
	}

	public short getMaxLines() {
		return maxLines;
	}

	public void setMaxLines(short maxLines) {
		if (this.maxLines != maxLines)
			needReBreak = true;
		this.maxLines = maxLines;
	}

	public short getSpacing() {
		return spacing;
	}

	public void setSpacing(short spacing) {
		if (this.spacing != spacing)
			needReBreak = true;
		this.spacing = spacing;
	}

	public byte getTruncate() {
		return truncate;
	}

	public void setTruncate(byte truncate) {
		if (this.truncate != truncate)
			needReBreak = true;
		this.truncate = truncate;
	}

	public void setTruncate(String truncate) {
		setTruncate(strToTruncate(truncate));
	}

	// public int getStyles() {
	// return styles;
	// }
	//
	// public void setStyles(int styles) {
	// this.styles = styles;
	// }

	public Vector getSegments() {
		return segments;
	}

	// =====

	public static byte strToTruncate(String value) {
		value = value.trim().toLowerCase();
		if ("head".equals(value) || "1".equals(value)) {
			return TRUNCATE_HEAD;
		} else if ("middle".equals(value) || "2".equals(value)) {
			return TRUNCATE_MIDDLE;
		} else if ("tail".equals(value) || "3".equals(value)) {
			return TRUNCATE_TAIL;
		} else {
			return TRUNCATE_NONE;
		}
	}

}
