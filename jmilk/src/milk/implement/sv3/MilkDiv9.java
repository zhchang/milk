package milk.implement.sv3;

import milk.ui2.MilkImage;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Div9Patch;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Page;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;

public class MilkDiv9 extends Sv3Div9Patch {
	boolean fillColorDetermined;
	int determinedFillColor = 0;

	boolean focusFillColorDetermined;
	int determinedFocusFillColor = 0;

	public MilkDiv9(String id) {
		super(id);
	}

	public MilkDiv9(String id, Sv3Div prototype, Sv3Page page) {
		super(id, prototype, page);
		if (prototype instanceof MilkDiv9) {
			MilkDiv9 thing = (MilkDiv9) prototype;
			determinedFillColor = thing.determinedFillColor;
			fillColorDetermined = thing.fillColorDetermined;
			determinedFocusFillColor = thing.determinedFocusFillColor;
			focusFillColorDetermined = thing.focusFillColorDetermined;
		}
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new MilkDiv9(id, this, page);
	}

	public int getFillColor() {
		if (this.isFocused()) {
			if (focusFillColorDetermined) {
				return determinedFocusFillColor;
			} else {
				if (fillColorDetermined) {
					return determinedFillColor;
				} else {
					return super.getFillColor();
				}
			}
		} else {
			if (fillColorDetermined) {
				return determinedFillColor;
			} else {
				return super.getFillColor();
			}
		}
	}

	protected void renderBg(IRenderContext ctx, Rect bounding, Rect rect,
			boolean gradientFocus) {

		if (image == null) {
			super.renderBg(ctx, bounding, rect, gradientFocus);
		} else {
			renderImgBg(ctx, bounding, rect, gradientFocus);
		}
	}

	public void didReceiveImage(Object image, String src) {
		super.didReceiveImage(image, src);
		if (image != null && image instanceof MilkImage) {
			MilkImage temp = (MilkImage) image;
			int rgbData[] = new int[1];
			int imgw = temp.getWidth();
			int imgh = temp.getHeight();
			int w2 = marker[SIDE_RIGHT];
			int h2 = marker[SIDE_BOTTOM];
			int w0 = marker[SIDE_LEFT];
			int h0 = marker[SIDE_TOP];
			temp.getRGB(rgbData, 0, 1, (w0 + imgw - w2) / 2,
					(h0 + imgh - h2) / 2, 1, 1);
			if (src.equals(imageUrl)) {
				determinedFillColor = rgbData[0];
				fillColorDetermined = true;
			} else if (src.equals(focusImageUrl)) {
				determinedFocusFillColor = rgbData[0];
				focusFillColorDetermined = true;
			}

		}
		if (getPage() != null && getPage() instanceof MilkPage) {
			MilkPage mp = (MilkPage) getPage();
			mp.registerImageReference(src);
		}
	}

}
