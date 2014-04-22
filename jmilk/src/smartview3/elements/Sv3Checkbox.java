/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package smartview3.elements;

import java.io.IOException;

import milk.implement.sv3.MilkImageUtil;
import mobon.MobonException;
import mobon.MobonReader;
import smartview3.layout.LayoutContext;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;
import smartview3.utils.IImageRequester;

/**
 * 
 * @author luyx
 */
public class Sv3Checkbox extends Sv3Element implements IImageRequester {
	protected static final byte TAG_CHECKED = 64;
	static Object imageChecked;
	static Object imageUnchecked;

	public static Object getImageChecked() {

		return imageChecked;
	}

	public static Object getImageUnchecked() {

		return imageUnchecked;
	}

	// attributes
	private boolean checked;

	protected void clearMem() {
		super.clearMem();
		MilkImageUtil.getInstance().removeReceiver(this);
	}

	public Sv3Checkbox(String id) {
		super(id);
		if (imageChecked == null) {
			imageUtil.loadImageAsync("chat-checked", this);
		}
		if (imageUnchecked == null) {
			imageUtil.loadImageAsync("chat-unchecked", this);
		}
	}

	public Sv3Checkbox(String id, Sv3Checkbox c, Sv3Page page) {
		super(id, c, page);
		this.checked = c.checked;
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3Checkbox(id, this, p);
	}

	public byte getSv3Type() {
		return TYPE_CHECKBOX;
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx) {
		Object image = getImageChecked();
		int imgw, imgh;
		if (image != null) {
			imgw = imageUtil.getWidth(image);
			imgh = imageUtil.getHeight(image);
		} else {
			imgw = 0;
			imgh = 0;
		}
		if (newRect.width < 0) {
			newRect.width = (short) (imgw + resolvedPadding[1] + resolvedPadding[3]);
		}
		if (newRect.height < 0) {
			newRect.height = (short) (imgh + resolvedPadding[0] + resolvedPadding[2]);
		}
	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				boolean gradient = page.getFocusBgColor2() != -1;
				this.renderBg(ctx, bounding, rect, gradient);
				this.renderBorder(ctx, bounding, rect);

				Object image = null;
				if (checked) {
					image = getImageChecked();
				} else {
					image = getImageUnchecked();
				}
				if (image != null) {
					Rect absPadded = absRect.toRectWithPadding(resolvedPadding,
							ctx.getPaddedRect());
					ctx.drawImage(image, absPadded.x, absPadded.y);
				}
			}
		}
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_CHECKED:
				checked = r.readBoolean();
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
		// key = key.toLowerCase();
		// boolean needsRepaint = true;
		// boolean needsRelayout = false;
		// if ("options".equals(key)) {
		// setOptions(value);
		// } else {
		// return false;
		// }
		// return true;
		// return false;
		return super.setStrAttrib(key, value);
	}

	public boolean setIntAttrib(String key, int value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		if ("checked".equals(key)) {
			setCheckedWithEvent(value != 0);
		} else {
			return super.setIntAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("checked".equals(key)) {
			return new Integer(checked ? 1 : 0);
		}
		return super.getAttrib(key);
	}

	public int getIntAttrib(String key) {
		return 0;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public void setCheckedWithEvent(boolean checked) {
		boolean triggerEvent = false;
		Sv3Page page = getPage();
		boolean cancelled = false;
		if (checked != this.checked && page != null) {
			triggerEvent = true;
			cancelled = page.willChangeValue(this);
		}
		if (!cancelled) {
			this.checked = checked;
			if (triggerEvent) {
				page.didChangeValue(this);
			}
		}
	}

	public void triggerRaw() {
		setCheckedWithEvent(!checked);
	}

	public void didReceiveImage(Object image, String src) {
		if (src.equals("chat-checked")) {
			imageChecked = image;
		} else if (src.equals("chat-unchecked")) {
			imageUnchecked = image;
		}
	}
}
