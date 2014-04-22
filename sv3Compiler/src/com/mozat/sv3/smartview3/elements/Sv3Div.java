package com.mozat.sv3.smartview3.elements;

import java.io.IOException;
import java.util.Vector;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;
import com.mozat.sv3.smartview3.utils.StringUtil;

public class Sv3Div extends Sv3Element {
	protected static final byte TAG_Children = 64, TAG_Flow = 65,
			TAG_Align = 66, TAG_Noclip = 67, TAG_Radius = 68,
			TAG_ImageUrl = 69, TAG_Marker = 70, TAG_FillColor = 71,
			TAG_FocusImageUrl = 72;

	public static final byte FLOW_INHERIT = 0, FLOW_LTR = 1, FLOW_RTL = 2;
	public static final byte ALIGN_INHERIT = 0, ALIGN_L = 1, ALIGN_C = 2,
			ALIGN_R = 3;
	public static final byte CORNER_TL = 0, CORNER_TR = 1, CORNER_BR = 2,
			CORNER_BL = 3;

	// =======

	// attributes defined by SmartView3
	private Vector children = new Vector();
	private byte flow; // default, left, right
	private byte align; // default, left, center/centre, right
	private boolean noclip;
	private final short[] radius = { 0, 0, 0, 0 }; // corner radius is ignored
													// on j2me

	public Sv3Div(String id) {
		super(id);
	}

	public void _setPage(Sv3Page p) {
		Sv3Page page = getPage();
		if (page != p) {
			super._setPage(p);
			if (children == null) {
				// ignore
				// this happens only if _setPage is called by constructor
			} else {
				int count = children.size();
				for (int i = 0; i < count; ++i) {
					Sv3Element e = (Sv3Element) children.elementAt(i);
					e._setPage(p);
				}
			}
		}
	}

	public byte getSv3Type() {
		return TYPE_DIV;
	}

	public Sv3Div(String id, Sv3Div d, Sv3Page page) {
		super(id, d, page);
		this.flow = d.flow;
		this.align = d.align;
		this.noclip = d.noclip;
		this.radius[0] = d.radius[0];
		this.radius[1] = d.radius[1];
		this.radius[2] = d.radius[2];
		this.radius[3] = d.radius[3];
		int size = d.children.size();
		for (int i = 0; i < size; ++i) {
			Sv3Element child = (Sv3Element) d.children.elementAt(i);
			String childId = child.getId();
			String newChildId = null;
			if (id != null && childId != null) {
				newChildId = id + "." + childId;
			}
			Sv3Element childClone = child.clone(newChildId, page);
			childClone._setParent(this);
			children.addElement(childClone);
		}
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new Sv3Div(id, this, page);
	}

	public Sv3Element hit(int pointerX, int pointerY) {
		if (rect != null && rect.contains(pointerX, pointerY)) {
			int size = children.size();

			int paddedX = pointerX - rect.x - resolvedPadding[1];
			int paddedY = pointerY - rect.y - resolvedPadding[0];
			for (int i = size - 1; i >= 0; --i) {
				Sv3Element e = (Sv3Element) children.elementAt(i);
				Sv3Element result = e.hit(paddedX, paddedY);
				if (result != null) {
					return result;
				}
			}
			return this;
		} else {
			return null;
		}
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx, IFontUtil fu) {
		int hpadding = resolvedPadding[3] + resolvedPadding[1];
		int vpadding = resolvedPadding[0] + resolvedPadding[2];

		LayoutContext subctx = new LayoutContext(ctx.resolvedBoundWidth()
				- hpadding, ctx.resolvedBoundHeight() - vpadding);

		subctx.definedWidth = (short) (newRect.width >= 0 ? newRect.width
				- hpadding : newRect.width);
		subctx.definedHeight = (short) (newRect.height >= 0 ? newRect.height
				- vpadding : newRect.height);
		if (flow == FLOW_INHERIT) {
			if (ctx.flow == FLOW_INHERIT) {
				subctx.flow = FLOW_LTR;
			} else {
				subctx.flow = ctx.flow;
			}
		} else {
			subctx.flow = flow;
		}
		if (align == ALIGN_INHERIT) {
			if (ctx.align == ALIGN_INHERIT) {
				subctx.align = ALIGN_L;
			} else {
				subctx.align = ctx.align;
			}
		} else {
			subctx.align = align;
		}
		// synchronized(subitems)
		synchronized (children) {
			int size = children.size();
			for (int i = 0; i < size; ++i) {
				Sv3Element e = (Sv3Element) children.elementAt(i);
				e.layout(subctx, fu);
			}
		}
		subctx.wrapContext();

		if (newRect.width < 0) {
			newRect.width = (short) (subctx.contentWidth() + hpadding);
		}
		if (newRect.height < 0) {
			newRect.height = (short) (subctx.contentHeight() + vpadding);
		}
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

				// Rect oldClip = null;

				if (radius[CORNER_TL] != 0 || radius[CORNER_TR] != 0
						|| radius[CORNER_BR] != 0 || radius[CORNER_BL] != 0) {
					// border is ignored
					this.renderBg(ctx, bounding, radius);
				} else {
					super.render(ctx);
				}

				int cx = 0, cy = 0, cw = 0, ch = 0;
				boolean clipped = false;
				if (!noclip) {
					// oldClip = new Rect(ctx.getClipX(), ctx.getClipY(),
					// ctx.getClipWidth(), ctx.getClipHeight());
					cx = ctx.getClipX();
					cy = ctx.getClipY();
					cw = ctx.getClipWidth();
					ch = ctx.getClipHeight();
					clipped = true;
					ctx.clipRect(absPadded.x, absPadded.y, absPadded.width,
							absPadded.height);
				}

				synchronized (children) {
					int size = children.size();
					IRenderContext subctx = ctx.getSubCtx(absPadded, viewport);
					for (int i = 0; i < size; ++i) {
						Sv3Element e = (Sv3Element) children.elementAt(i);
						e.render(subctx);
					}
				}

				// if (oldClip != null) {
				if (clipped) {
					// restore the clipping
					// ctx.setClip(oldClip.x, oldClip.y, oldClip.width,
					// oldClip.height);
					ctx.setClip(cx, cy, cw, ch);
				}
			}
		}
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_Children:
				children = r.readVector();
				{
					int size = children.size();
					for (int i = 0; i < size; ++i) {
						Sv3Element e = (Sv3Element) children.elementAt(i);
						e._setParent(this);
					}
				}
				break;
			case TAG_Flow:
				flow = (byte) r.readInt();
				break;
			case TAG_Align:
				align = (byte) r.readInt();
				break;
			case TAG_Noclip:
				noclip = r.readBoolean();
				break;
			case TAG_Radius:
				setRadius(r.readArrayOfShort());
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
		if ("flow".equals(key)) {
			setFlow(value);
			needsRelayout = true;
		} else if ("align".equals(key)) {
			setAlign(value);
			needsRelayout = true;
		} else if ("radius".equals(key)) {
			setRadius(Sv3Element.strToSides(value));
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
		if ("flow".equals(key)) {
			setFlow((byte) value);
			needsRelayout = true;
		} else if ("align".equals(key)) {
			setAlign((byte) value);
			needsRelayout = true;
		} else if ("noclip".equals(key)) {
			setNoclip(value != 0);
		} else if ("radius-tl".equals(key)) {
			setRadius((short) value, CORNER_TL);
		} else if ("radius-tr".equals(key)) {
			setRadius((short) value, CORNER_TR);
		} else if ("radius-br".equals(key)) {
			setRadius((short) value, CORNER_BR);
		} else if ("radius-bl".equals(key)) {
			setRadius((short) value, CORNER_BL);
		} else {
			return super.setIntAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("flow".equals(key)) {
			return new Integer(flow);
		} else if ("align".equals(key)) {
			return new Integer(align);
		} else if ("noclip".equals(key)) {
			return new Integer(noclip ? 1 : 0);
		} else if ("radius".equals(key)) {
			return StringUtil.join(radius, ',');
		} else if ("radius-tl".equals(key)) {
			return new Integer(radius[CORNER_TL]);
		} else if ("radius-tr".equals(key)) {
			return new Integer(radius[CORNER_TR]);
		} else if ("radius-br".equals(key)) {
			return new Integer(radius[CORNER_BR]);
		} else if ("radius-bl".equals(key)) {
			return new Integer(radius[CORNER_BL]);
		}
		return super.getAttrib(key);
	}

	public Vector getChildren() {
		return children;
	}

	public boolean hasChildren() {
		return children != null && children.size() > 0;
	}

	public Sv3Element firstChild() {
		return (Sv3Element) children.elementAt(0);
	}

	public Sv3Element lastChild() {
		return (Sv3Element) children.lastElement();
	}

	public void addChild(Sv3Element elem) {
		elem.removeFromParent();
		elem._setParent(this);
		children.addElement(elem);
	}

	public void insertChild(Sv3Element elem, int index) {
		elem.removeFromParent();
		elem._setParent(this);
		children.insertElementAt(elem, index);
	}

	public void removeChild(Sv3Element elem) {
		if (elem.getParent() == this) {
			elem._setParent(null);
			children.removeElement(elem);
		}
	}

	public void removeAllChildren() {
		int count = children.size();
		for (int i = count - 1; i >= 0; --i) {
			Sv3Element e = (Sv3Element) children.elementAt(i);
			e._setParent(null);
		}
		children.removeAllElements();
	}

	public void setChildren(Vector subitems) {
		this.children = subitems;
	}

	public byte getFlow() {
		return flow;
	}

	// public byte getResolvedFlow() {
	// Sv3Page p = getPage();
	// if (flow == FLOW_INHERIT) {
	// if (p != null && p.isRtl()) {
	// return FLOW_RTL;
	// } else {
	// return FLOW_LTR;
	// }
	// } else {
	// return flow;
	// }
	// }

	public void setFlow(byte flow) {
		this.flow = flow;
	}

	public void setFlow(String flow) {
		this.flow = strToFlow(flow);
	}

	public byte getAlign() {
		return align;
	}

	public byte getResolvedAlign() {
		Sv3Div p = getParent();
		if (align == ALIGN_INHERIT) {
			if (p != null) {
				return p.getResolvedAlign();
			} else {
				return ALIGN_L;
			}
		} else {
			return align;
		}
	}

	public void setAlign(byte align) {
		this.align = align;
	}

	public void setAlign(String align) {
		this.align = strToAlign(align);
	}

	public boolean isNoclip() {
		return noclip;
	}

	public void setNoclip(boolean clip) {
		this.noclip = clip;
	}

	public short[] getRadius() {
		return radius;
	}

	public boolean hasRadius() {
		return radius[0] != 0 || radius[1] != 0 || radius[2] != 0
				|| radius[3] != 0;
	}

	public short getRadius(short corner) {
		return radius[corner];
	}

	public void setRadius(short tl, short tr, short br, short bl) {
		radius[CORNER_TL] = tl;
		radius[CORNER_TR] = tr;
		radius[CORNER_BR] = br;
		radius[CORNER_BL] = bl;
	}

	public void setRadius(short[] radius) {
		this.radius[0] = radius[0];
		this.radius[1] = radius[1];
		this.radius[2] = radius[2];
		this.radius[3] = radius[3];
	}

	public void setRadius(short value) {
		radius[0] = radius[1] = radius[2] = radius[3] = value;
	}

	public void setRadius(short radius, short corner) {
		this.radius[corner] = radius;
	}

	public Rect getPaddedRect() {
		Rect r = new Rect(rect);
		r.x += resolvedPadding[1];
		r.y += resolvedPadding[0];
		r.width += resolvedPadding[1] + resolvedPadding[3];
		r.height += resolvedPadding[0] + resolvedPadding[2];
		return r;
	}

	// == attributes

	public static byte strToAlign(String value) {
		value = value.trim().toLowerCase();
		if ("left".equals(value) || "1".equals(value)) {
			return ALIGN_L;
		} else if ("center".equals(value) || "2".equals(value)
				|| "centre".equals(value)) {
			return ALIGN_C;
		} else if ("right".equals(value) || "3".equals(value)) {
			return ALIGN_R;
		} else {
			return ALIGN_INHERIT;
		}
	}

	public static byte strToFlow(String value) {
		value = value.trim().toLowerCase();
		if ("left".equals(value) || "1".equals(value)) {
			return FLOW_LTR;
		} else if ("right".equals(value) || "2".equals(value)) {
			return FLOW_RTL;
		} else {
			return FLOW_INHERIT;
		}
	}

	// script methods
	public void scriptAppendChild(Sv3Element e) {
		children.addElement(e);
	}

	public void scriptInsertChild(Sv3Element e, int index) {
		children.insertElementAt(e, index);
	}

	// end of script methods
}
