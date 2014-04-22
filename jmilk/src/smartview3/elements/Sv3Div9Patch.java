package smartview3.elements;

import java.io.IOException;

import milk.implement.sv3.MilkImageUtil;
import mobon.MobonException;
import mobon.MobonReader;
import smartview3.layout.LayoutContext;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;
import smartview3.utils.IImageRequester;
import smartview3.utils.StringUtil;

public class Sv3Div9Patch extends Sv3Div implements IImageRequester {

	protected final short[] marker = { 0, 0, 0, 0 }; // marker points of the
														// 9patch image (top,
														// right,
														// bottom, left)
	protected Object image;
	protected Object focusImage;
	private int fillColor;

	protected String imageUrl;
	protected String focusImageUrl;

	protected void updateBuffer() {

	}

	public void addChild(Sv3Element elem) {
		super.addChild(elem);
		updateBuffer();
	}

	public void insertChild(Sv3Element elem, int index) {
		super.insertChild(elem, index);
		updateBuffer();
	}

	public void removeChild(Sv3Element elem) {
		super.removeChild(elem);
		updateBuffer();
	}

	public String getFocusImageUrl() {
		return focusImageUrl;
	}

	public void setFocusImageUrl(String focusImageUrl) {
		this.focusImageUrl = focusImageUrl;
	}

	private boolean reloadNeeded = true;

	public Sv3Div9Patch(String id) {
		super(id);
	}

	protected void clearMem() {
		super.clearMem();
		MilkImageUtil.getInstance().removeReceiver(this);
	}

	public Sv3Div9Patch(String id, Sv3Div prototype, Sv3Page page) {
		super(id, prototype, page);
		if (prototype instanceof Sv3Div9Patch) {
			Sv3Div9Patch thing = (Sv3Div9Patch) prototype;
			image = thing.image;
			imageUrl = thing.imageUrl;
			fillColor = thing.fillColor;
			focusImage = thing.focusImage;
			focusImageUrl = thing.focusImageUrl;
			setMarker(thing.marker);
		}
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new Sv3Div9Patch(id, this, page);
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public int getFillColor() {
		return fillColor;
	}

	public void setFillColor(int fillColor) {
		this.fillColor = fillColor;
	}

	public void setMarker(short[] values) {
		for (int i = 0; i < 4; ++i) {
			marker[i] = values[i];
		}
	}

	public void setMarker(short t, short r, short b, short l) {
		marker[SIDE_TOP] = t;
		marker[SIDE_RIGHT] = r;
		marker[SIDE_BOTTOM] = b;
		marker[SIDE_LEFT] = l;
	}

	public void setMarkerAndPadding(short[] values) {
		this.setMarker(values);
		this.setPadding(values);
	}

	public void setMarkerAndPadding(short top, short right, short bottom,
			short left) {
		this.setMarker(top, right, bottom, left);
		this.setPadding(top, right, bottom, left);
	}

	public Object getImage() {
		return image;
	}

	public void setImage(Object image) {
		this.image = image;
	}

	public short[] getMarker() {
		return marker;
	}

	protected void readAttrFromMobon(MobonReader r, int key)
			throws IOException, MobonException {
		boolean handled = false;
		switch (key) {
		case TAG_ImageUrl:
			imageUrl = r.readStringOrNull();
			handled = true;
			break;
		case TAG_FillColor:
			fillColor = r.readInt();
			handled = true;
			break;
		case TAG_Marker:
			short[] temp = r.readArrayOfShort();
			if (temp != null && temp.length == 4) {
				for (int i = 0; i < 4; i++) {
					marker[i] = temp[i];
				}
			}
			handled = true;
			break;
		case TAG_FocusImageUrl:
			focusImageUrl = r.readStringOrNull();
			handled = true;
			break;
		}

		if (!handled) {
			super.readAttrFromMobon(r, key);
		}
	}

	protected void layoutContent(Rect newRect, LayoutContext ctx) {
		super.layoutContent(newRect, ctx);
		updateBuffer();
		if (reloadNeeded) {
			reloadNeeded = false;
			if (imageUrl != null && imageUrl.length() != 0) {
				imageUtil.loadImageAsync(imageUrl, this);
			}
			if (focusImageUrl != null && focusImageUrl.length() != 0) {
				imageUtil.loadImageAsync(focusImageUrl, this);
			}
		}
	}

	protected void renderImgBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {

		Object image = this.isFocused() ? focusImage : this.image;
		if (image == null) {
			image = this.image;
		}
		if (image != null) {

			Rect r = rect.toRectWithOffset(bounding, ctx.getAbsRect());
			if (image != null) {
				// render the 9 patch background
				int imgw = imageUtil.getWidth(image);
				int imgh = imageUtil.getHeight(image);
				int w2 = marker[SIDE_RIGHT];
				int h2 = marker[SIDE_BOTTOM];
				int w0 = marker[SIDE_LEFT];
				int h0 = marker[SIDE_TOP];
				int x2 = imgw - w2;
				int y2 = imgh - h2;
				int x1 = w0;
				int y1 = h0;
				int w1 = x2 - x1;
				int h1 = y2 - y1;

				int rx = r.x;
				int ry = r.y;
				int rw = r.width;
				int rh = r.height;

				int cx = ctx.getClipX(), cy = ctx.getClipY(), cw = ctx
						.getClipWidth(), ch = ctx.getClipHeight();
				// Rect oldClip = new Rect(ctx.getClipX(), ctx.getClipY(),
				// ctx.getClipWidth(), ctx.getClipHeight());

				// center
				ctx.setColor(getFillColor());
				ctx.clipRect(rx + x1, ry + y1, rw - w0 - w2, rh - h0 - h2);
				ctx.fillRect(rx + x1, ry + y1, rw - w0 - w2, rh - h0 - h2);
				ctx.setClip(cx, cy, cw, ch);

				// top // bottom
				if (w1 > 0) {
					for (int x = x1; x < rw - w2; x += w1) {
						ctx.clipRect(rx + x, ry, Math.min(w1, rw - w2 - x), h0);
						ctx.drawImage(image, rx + x - w0, ry);
						ctx.setClip(cx, cy, cw, ch);

						ctx.clipRect(rx + x, ry + rh - h2,
								Math.min(w1, rw - w2 - x), h2);
						ctx.drawImage(image, rx + x - w0, ry + rh - imgh);
						ctx.setClip(cx, cy, cw, ch);
					}
				}

				// left // right
				if (h1 > 0) {
					for (int y = y1; y < rh - h2; y += h1) {
						ctx.clipRect(rx, ry + y, w0, Math.min(h1, rh - h2 - y));
						ctx.drawImage(image, rx, ry + y - h0);
						ctx.setClip(cx, cy, cw, ch);

						ctx.clipRect(rx + rw - w2, ry + y, w2,
								Math.min(h1, rh - h2 - y));
						ctx.drawImage(image, rx + rw - imgw, ry + y - h0);
						ctx.setClip(cx, cy, cw, ch);
					}
				}

				// top left
				ctx.clipRect(rx, ry, w0, h0);
				ctx.drawImage(image, rx, ry);
				ctx.setClip(cx, cy, cw, ch);

				// top right
				ctx.clipRect(rx + rw - w2, ry, w2, h0);
				ctx.drawImage(image, rx + rw - imgw, ry);
				ctx.setClip(cx, cy, cw, ch);

				// bottom left
				ctx.clipRect(rx, ry + rh - h2, w0, h2);
				ctx.drawImage(image, rx, ry + rh - imgh);
				ctx.setClip(cx, cy, cw, ch);

				// bottom right
				ctx.clipRect(rx + rw - w2, ry + rh - h2, w2, h2);
				ctx.drawImage(image, rx + rw - imgw, ry + rh - imgh);
				ctx.setClip(cx, cy, cw, ch);

				// restore the clip settings
				// ctx.setClip(oldClip.x, oldClip.y, oldClip.width,
				// oldClip.height);
				ctx.setClip(cx, cy, cw, ch);
			}

		}

	}

	protected void renderBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {

		super.renderBg(ctx, bounding, rect, gradientFocus);
		renderImgBg(ctx, bounding, rect, gradientFocus);
	}

	public void didReceiveImage(Object image, String src) {
		boolean fire = false;
		if (src.equals(imageUrl)) {
			if (this.image != image) {
				this.image = image;
				fire = true;
			}
		} else if (src.equals(focusImageUrl)) {
			if (this.focusImage != image) {
				this.focusImage = image;
				fire = true;
			}
		}
		if (fire) {
			this.fireAttribEvent("image", true, false);
		}

	}

	public boolean setStrAttrib(String key, String value) {
		key = key.toLowerCase();
		boolean needsRepaint = true;
		boolean needsRelayout = false;
		updateBuffer();
		if ("image".equals(key)) {
			if (this.imageUrl == null || !this.imageUrl.equals(value)) {
				imageUrl = value;
				imageUtil.loadImageAsync(imageUrl, this);
			}
		} else if ("focusimage".equals(key)) {
			if (this.focusImageUrl == null || !this.focusImageUrl.equals(value)) {
				focusImageUrl = value;
				imageUtil.loadImageAsync(focusImageUrl, this);
			}
		} else if ("marker".equals(key)) {
			this.setMarker(Sv3Element.strToSides(value));
		} else if ("fillColor".equals(key)) {
			this.setFillColor(Sv3Element.strToColor(value));
		} else {
			return super.setStrAttrib(key, value);
		}
		fireAttribEvent(key, needsRepaint, needsRelayout);
		return true;
	}

	// return either String or Integer
	public Object getAttrib(String key) {
		key = key.toLowerCase();
		if ("image".equals(key)) {
			return imageUrl;
		} else if ("marker".equals(key)) {
			return StringUtil.join(marker, ',');
		} else if ("fillcolor".equals(key)) {
			return new Integer(fillColor);
		} else if ("focusimage".equals(key)) {
			return focusImageUrl;
		}
		return super.getAttrib(key);
	}
}
