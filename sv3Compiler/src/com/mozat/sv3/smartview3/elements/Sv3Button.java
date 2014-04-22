package com.mozat.sv3.smartview3.elements;

import java.io.IOException;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;

public class Sv3Button extends Sv3Element {
	protected final byte TAG_TEXT = 64;

	private static final short MARGIN_X = 10, MARGIN_Y = 3, MIN_WIDTH = 40;
	private static final short[] radius = { 8, 8, 8, 8 };

	// attributes

	private String text;

	// =======
	protected short titleWidth;

	public Sv3Button(String id) {
		super(id);
		bgColor = 0xbbbbbb;
	}

	public Sv3Button(String id, Sv3Button b, Sv3Page p) {
		super(id, b, p);
		this.text = b.text;
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3Button(id, this, p);
	}

	public byte getSv3Type() {
		return TYPE_BUTTON;
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_TEXT:
				text = r.readStringOrNull();
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
		} else {
			return super.setStrAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	public boolean setIntAttrib(String key, int value) {
		// key = key.toLowerCase();
		// boolean needsRepaint = true;
		// boolean needsRelayout = false;
		// if ("selected".equals(key)) {
		// setSelected(value);
		// } else {
		// return false;
		// }
		// return true;
		// return false;
		return super.setIntAttrib(key, value);
	}

	public Object getAttrib(String key) {
		key = key.toLowerCase();
		// Integer intResult = null;

		if ("text".equals(key)) {
			return text;
		}
		return super.getAttrib(key);
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx, IFontUtil fu) {
		Object font = this.getFont(fu);
		titleWidth = (short) layoutSingleLineText(newRect, text, ctx, fu, font,
				resolvedPadding, MARGIN_X, MARGIN_Y, MIN_WIDTH);
	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				IFontUtil fu = ctx.getFu();
				Rect absPadded = absRect.toRectWithPadding(resolvedPadding,
						ctx.getPaddedRect());

				this.renderBg(ctx, bounding, radius, 0xffffff);

				// Rect textBound = new Rect(absPadded.x + MARGIN_X, absPadded.y
				// + MARGIN_Y, absPadded.width - MARGIN_X
				// * 2, absPadded.height - MARGIN_Y * 2);
				Rect textBound = ctx.getTextBoundRect();
				textBound.copy(absPadded.x + MARGIN_X, absPadded.y + MARGIN_Y,
						absPadded.width - MARGIN_X * 2, absPadded.height
								- MARGIN_Y * 2);

				Object font = this.getFont(fu);
				renderSingleLineText(ctx, text, titleWidth, textBound, fu,
						font, getResolvedColor(), Sv3Div.ALIGN_C);
			}
		}
	}

	// public byte getResolvedAlign() {
	// Sv3Div p = getParent();
	// if (p == null) {
	// return Sv3Div.ALIGN_L;
	// } else {
	// return p.getResolvedAlign();
	// }
	// }

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
