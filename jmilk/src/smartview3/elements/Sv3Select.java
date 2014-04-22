package smartview3.elements;

import java.io.IOException;
import mobon.MobonException;
import mobon.MobonReader;
import smartview3.layout.LayoutContext;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;
import smartview3.utils.StringUtil;

public class Sv3Select extends Sv3Element {
	protected static final byte TAG_Selected = 64, TAG_Options = 65;

	// private static final short MARGIN_Y = 0, MARGIN_X = 0, MIN_WIDTH = 30;

	// attributes defined by SmartView3
	private int selected;
	private String[] options = null;

	// === non attribute fields
	private boolean isOpen;

	public Sv3Select(String id) {
		super(id);
	}

	public Sv3Select(String id, Sv3Select s, Sv3Page p) {
		super(id, s, p);
		this.selected = s.selected;
		if (s.options != null) {
			this.setOptions(s.options, true);
		}
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3Select(id, this, p);
	}

	public boolean canFocus() {
		return true;
	}

	public byte getSv3Type() {
		return TYPE_SELECT;
	}

	// public Sv3Element hit(int pointerX, int pointerY) {
	// Sv3Element e = super.hit(pointerX, pointerY);
	// if (e != null) {
	// return this;
	// } else {
	// return null;
	// }
	// }

	protected void layoutManualSize(Rect newRect, LayoutContext ctx) {
		super.layoutManualSize(newRect, ctx);
		if (resolvedWidth < 0) {
			newRect.width = resolvedWidth = ctx.getHardBoundWidth();
		}
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx) {

	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				this.renderBg(ctx, bounding, rect, false);
				this.renderBorder(ctx, bounding, rect);

			}
		}
	}

	protected void renderBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {
		Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
		ctx.fillRoundRect(r, 3, bgColor);
	}

	protected void renderBorder(IRenderContext ctx, Rect bounding, Rect rect) {
		Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
		ctx.fillRoundRect(r, 3, bgColor);
	}

	public byte getResolvedAlign() {
		Sv3Div p = getParent();
		if (p == null) {
			return Sv3Div.ALIGN_L;
		} else {
			return p.getResolvedAlign();
		}
	}

	public String getSelectedTextOrValue() {
		String text;
		if (selected >= 0 && selected * 2 < options.length) {
			text = getOptionTextOrValue(selected);
		} else {
			text = null;
		}
		return text;
	}

	public String getTextOrValueAt(int index) {
		String text;
		if (index >= 0 && index * 2 < options.length) {
			text = getOptionTextOrValue(index);
		} else {
			text = null;
		}
		return text;
	}

	public void triggerRaw() {
		setIsOpen(!isOpen);
	}

	public void setIsOpen(boolean open) {
		if (this.isOpen != open) {
			this.isOpen = open;
		}
	}

	public String getOptionText(int i) {
		return options[i * 2 + 1];
	}

	public String getOptionValue(int i) {
		return options[i * 2];
	}

	public String getOptionTextOrValue(int i) {
		String text = getOptionText(i);
		if (text == null) {
			text = getOptionValue(i);
		}
		return text;
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_Selected:
				selected = r.readInt();
				break;
			case TAG_Options:
				setOptions(r.readStringArray(), false);
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
		if ("options".equals(key)) {
			setOptions(value);
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
		if ("selected".equals(key)) {
			setSelected(value);
			// needsRelayout = true;
		} else {
			return super.setIntAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("selected".equals(key)) {
			return new Integer(selected);
		} else if ("options".equals(key)) {
			return StringUtil.joinWithEscaping(options, ',');
		}
		return super.getAttrib(key);
	}

	public int getSelected() {
		return selected;
	}

	public void setSelected(int selected) {
		this.selected = selected;
	}

	public String[] getOptions() {
		return options;
	}

	// public void setOptions(String[] options) {
	// setOptions(options, false);
	// }

	public void setOptions(String[] options, boolean copy) {
		int originalLen = options.length;
		int destLen = originalLen;
		if (originalLen % 2 != 0) {
			++destLen;
		}
		if (copy) {
			this.options = new String[destLen];
			this.options[destLen] = "";
			for (int i = 0; i < originalLen; ++i) {
				this.options[i] = options[i];
			}
		} else {
			this.options = options;
		}
	}

	public void setOptions(String options) {
		// need to use StringUtil.split instead of String.split because the
		// latter is not available on J2ME
		String[] ops = StringUtil.splitWithEscaping(options, ',');
		setOptions(ops, false);
	}

}
