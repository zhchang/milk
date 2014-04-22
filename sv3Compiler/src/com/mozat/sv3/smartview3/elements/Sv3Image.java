package com.mozat.sv3.smartview3.elements;

import java.io.IOException;

import com.mozat.sv3.mobon.MobonException;
import com.mozat.sv3.mobon.MobonReader;
import com.mozat.sv3.smartview3.layout.LayoutContext;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.render.IRenderContext;
import com.mozat.sv3.smartview3.utils.IFontUtil;
import com.mozat.sv3.smartview3.utils.IImageRequester;

public class Sv3Image extends Sv3Element implements IImageRequester {
	protected static final byte TAG_Src = 64, TAG_Alt = 65; // TAG_LocalResId =
															// 66;

	// attributes defined by SmartView3
	protected String src;
	protected String alt;
	// private String localResId; // resource id

	// ======
	protected Object image;
	protected boolean reloadNeeded = true;

	public Sv3Image(String id) {
		super(id);
	}

	public Sv3Image(String id, Sv3Image in, Sv3Page page) {
		super(id, in, page);
		this.src = in.src;
		this.alt = in.alt;
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3Image(id, this, p);
	}

	public byte getSv3Type() {
		return TYPE_IMAGE;
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx, IFontUtil fu) {
		int imgw = 0, imgh = 0;
		if (image != null) {
			imgw = imageUtil.getWidth(image);
			imgh = imageUtil.getHeight(image);
		}

		int w = newRect.width;
		int h = newRect.height;
		if (w >= 0) {
			if (h >= 0) {
				// no change
			} else {
				if (imgw > 0) {
					h = (w - resolvedPadding[1] - resolvedPadding[3]) * imgh
							/ imgw + resolvedPadding[0] + resolvedPadding[2];
				} else {
					h = 0 + resolvedPadding[1] + resolvedPadding[3];
				}
			}
		} else {
			if (h >= 0) {
				if (imgh > 0) {
					w = (h - resolvedPadding[0] - resolvedPadding[2]) * imgw
							/ imgh + resolvedPadding[1] + resolvedPadding[3];
				} else {
					w = 0 + resolvedPadding[0] + resolvedPadding[2];
				}
			} else {
				w = (short) (imgw + resolvedPadding[1] + resolvedPadding[3]);
				h = (short) (imgh + resolvedPadding[0] + resolvedPadding[2]);
			}
		}

		newRect.width = (short) w;
		newRect.height = h;

		if (reloadNeeded && src != null && src.length() > 0
				&& imageUtil != null) {
			reloadNeeded = false;
			imageUtil.loadImageAsync(src, this, w - resolvedPadding[1]
					- resolvedPadding[3], // desired image width,
											// could be 0
					h - resolvedPadding[0] - resolvedPadding[2]); // desired
																	// image
																	// height,
																	// could be
																	// 0
		}
	}

	public void render(IRenderContext ctx) {
		if (!hidden && rect != null) {
			Rect bounding = ctx.getBounding();
			Rect viewport = ctx.getViewPort();
			Rect absRect = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (absRect.overlaps(viewport)) {
				this.renderBg(ctx, bounding, rect, true);
				this.renderBorder(ctx, bounding, rect);
				if (image != null) {
					Rect absPadded = absRect.toRectWithPadding(resolvedPadding,
							ctx.getPaddedRect());
					ctx.drawImage(image, absPadded.x, absPadded.y,
							absPadded.width, absPadded.height);
				}
			}
		}
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		if (key > MAX_ELEMENT_TAG) {
			switch (key) {
			case TAG_Src:
				setSrc(r.readStringOrNull());
				break;
			case TAG_Alt:
				alt = r.readStringOrNull();
				break;
			// case TAG_LocalResId:
			// setLocalResId(r.readStringOrNull());
			// break;
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
		if ("src".equals(key)) {
			setSrc(value);
			needsRelayout = true;
		} else if ("alt".equals(key)) {
			setAlt(value);
		} else {
			return super.setStrAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	public boolean setIntAttrib(String key, int value) {
		// key = key.toLowerCase();
		// if ("selected".equals(key)) {
		// setSelected(value);
		// } else {
		// return false;
		// }
		// return true;
		// return false;
		return super.setIntAttrib(key, value);
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("src".equals(key)) {
			return src;
		} else if ("alt".equals(key)) {
			return alt;
		}
		return super.getAttrib(key);
	}

	public String getSrc() {
		return src;
	}

	public void setSrc(String src) {
		if (src == null || !src.equals(this.src)) {
			this.src = src;
			setReloadNeeded(true);

		}
	}

	public void didReceiveImage(Object image, String uri) {
		if (this.image != image) {
			this.image = image;
			boolean needsRelayout = !hasManualSizeInPixels();
			this.fireAttribEvent("image", true, needsRelayout);
		}
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public Object getImage() {
		return image;
	}

	public void setImage(Object image) {
		this.image = image;
	}

	public boolean isReloadNeeded() {
		return reloadNeeded;
	}

	public void setReloadNeeded(boolean reloadNeeded) {
		this.reloadNeeded = reloadNeeded;
	}

	// public String getLocalResId() {
	// return localResId;
	// }
	//
	// public void setLocalResId(String localResId) {
	// this.localResId = localResId;
	// }

}
