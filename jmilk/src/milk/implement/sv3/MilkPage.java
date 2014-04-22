package milk.implement.sv3;

import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.VectorPool;
import milk.ui2.MilkFont;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Page;
import smartview3.layout.Rect;
import smartview3.layout.TextSegment;
import smartview3.render.IRenderContext;

public class MilkPage extends Sv3Page {

	protected Sv3Element currentFocus;
	private Vector imageResources = new Vector();
	protected boolean noBg = false;

	public boolean isNoBg() {
		return noBg;
	}

	public void setNoBg(boolean noBg) {
		this.noBg = noBg;
	}

	public Sv3Element getCurrentFocus() {
		return currentFocus;
	}

	boolean isElementFocusable(Sv3Element element) {
		return element != null && element.canFocus();
	}

	public void didChangeAttrib(Sv3Element elem, String name,
			boolean needsRepaint, boolean needsRelayout) {
		if (currentFocus == elem) {
			if (!isElementFocusable(elem)) {
				currentFocus.setFocusedWithEvent(false);
				currentFocus = null;
			}
		}
		super.didChangeAttrib(elem, name, needsRepaint, needsRelayout);

	}

	public boolean willFocus(Sv3Element element) {

		if (currentFocus != null && currentFocus != element) {
			currentFocus.setFocusedWithEvent(false);
		}
		return super.willFocus(element);
	}

	public void didFocus(Sv3Element element) {
		super.didFocus(element);
		currentFocus = element;
	}

	public void focusFirst() {
		if (currentFocus == null) {
			Sv3Element element = navigate((byte) -2);
			if (element != null) {
				element.setFocusedWithEvent(true);
				currentFocus = element;
			}
		}
	}

	public Rect getScrollRect(int canvasWidth, int canvasHeight) {

		Rect r = getRoot().getRect();
		scrollRect.x = (short) (canvasWidth - 5);
		scrollRect.width = 4;
		if (r != null && canvasHeight < r.height) {
			scrollRect.y = getScrollY() * canvasHeight / r.height;
			scrollRect.height = canvasHeight * canvasHeight / r.height;
			if (scrollRect.height < 3) {
				scrollRect.height = 3;
			}

		} else {
			scrollRect.y = 0;
			scrollRect.height = canvasHeight - 1;
		}
		return scrollRect;
	}

	public void render(IRenderContext ctx) {

		if (!isNoBg()) {
			ctx.setColor(getBgColor());
			ctx.fillRect(ctx.getViewPort().x, ctx.getViewPort().y,
					ctx.getViewPort().width, ctx.getViewPort().height);
		}
		super.render(ctx);
		if (currentFocus != null) {
			TextSegment tipsSegment = currentFocus.tipsSegment;
			boolean draw = tipsSegment != null;
			long now = System.currentTimeMillis();
			if (currentFocus.getTipsDrawStartTime() == 0) {
				draw &= true;
				currentFocus.setTipsDrawStartTime(now);
			} else {
				draw &= now - currentFocus.getTipsDrawStartTime() < 3000;
			}

			if (draw) {
				MilkFont tipFont = Adaptor.uiFactory.getFont(
						MilkFont.STYLE_PLAIN, MilkFont.SIZE_SMALL);
				Rect r = currentFocus.getAbsoluteRect().toRectWithOffset(
						ctx.getBounding(), ctx.getAbsRect());
				int tempH = r.height;
				r.width = (short) (tipsSegment.width + 4);
				r.height = tipFont.getHeight() + 4;

				ctx.storeClip();
				ctx.setClip(0, 0, Adaptor.milk.getCanvasWidth(),
						Adaptor.milk.getCanvasHeight());
				if (r.y < Adaptor.milk.getCanvasHeight() / 2) {
					r.y += tempH + 2;
				} else {
					r.y -= r.height + 2;
				}
				r.x += 2;

				if (r.x + r.width > Adaptor.milk.getCanvasWidth()) {
					r.x = (short) (Adaptor.milk.getCanvasWidth() - r.width - 2);
				}

				if (r.y + r.height > Adaptor.milk.getCanvasHeight()) {
					r.y = (short) (Adaptor.milk.getCanvasHeight() - r.height - 2);
				}

				ctx.setColor(0xffff00);
				ctx.fillRect(r.x, r.y, r.width, r.height);

				ctx.setColor(0);
				ctx.drawRect(r.x, r.y, r.width, r.height);
				ctx.setFont(tipFont);
				ctx.drawSubstring(currentFocus.getTips(), tipsSegment.location,
						tipsSegment.length, r.x + (r.width - tipsSegment.width)
								/ 2, r.y + (r.height - tipFont.getHeight()) / 2);
				ctx.restoreClip();

			}
		}
	}

	void getAllChildren(Sv3Element element, Vector children) {
		if (element instanceof Sv3Div) {
			Vector temp = ((Sv3Div) element).getChildren();
			int count = temp.size();
			for (int i = 0; i < count; i++) {
				Sv3Element thing = (Sv3Element) temp.elementAt(i);

				if (isElementFocusable(thing) && thing != currentFocus) {
					children.addElement(thing);
				}
				if (thing instanceof Sv3Div) {
					getAllChildren(thing, children);
				}
			}
		}
	}

	private int getNearestElementY(Vector children, int currentY, int direction) {
		int findDownElementsY = 10000000;
		int findUpElementsY = -1000000;
		for (int i = 0; i < children.size(); i++) {
			Sv3Element element = (Sv3Element) children.elementAt(i);
			if (isElementFocusable(element)) {
				int ey = element.getAbsoluteRect().y;
				switch (direction) {
				case Adaptor.KEY_UP: { // up
					if (currentY > ey && ey > findUpElementsY) {
						findUpElementsY = ey;
					}
					break;
				}
				case Adaptor.KEY_DOWN: {// down
					if (currentY < ey && findDownElementsY > ey) {
						findDownElementsY = ey;
					}
					break;
				}
				}
			}
		}
		if (direction == -2)
			return findDownElementsY;
		else
			return findUpElementsY;
	}

	public Sv3Element navigate(byte direction) {
		Vector children = VectorPool.produce();
		getAllChildren(getRoot(), children);

		int lastX = -1;
		int lastY = -1;

		if (currentFocus != null) {
			lastX = currentFocus.getAbsoluteRect().x;
			lastY = currentFocus.getAbsoluteRect().y;
		}
		int count = children.size();
		Sv3Element nextFocus = null;
		switch (direction) {
		case Adaptor.KEY_UP: {// up
			int minUpElementY = getNearestElementY(children, lastY, direction);
			// System.out.println("navigate()up minY:"+minY);
			int minX = 100000;
			for (int i = 0; i < count; i++) {
				Sv3Element element = (Sv3Element) children.elementAt(i);
				int ey = element.getAbsoluteRect().y;
				if (minUpElementY == ey) {//
					int diffX = Math.abs(lastX - element.getAbsoluteRect().x);
					if (minX > diffX) {
						minX = diffX;
						nextFocus = element;
					}
				}
			}
			break;
		}
		case Adaptor.KEY_DOWN: {// down
			int minDownElementY = getNearestElementY(children, lastY, direction);
			// System.out.println("navigate()down minY:"+minY);
			int minX = 100000;
			for (int i = 0; i < count; i++) {
				Sv3Element element = (Sv3Element) children.elementAt(i);

				int ey = element.getAbsoluteRect().y;
				if (minDownElementY == ey) {//
					int diffX = Math.abs(lastX - element.getAbsoluteRect().x);
					if (minX > diffX) {
						minX = diffX;
						nextFocus = element;
					}
				}
			}
			break;
		}
		case Adaptor.KEY_LEFT: {// left
			int min = 0x7ffff;
			for (int i = 0; i < count; i++) {
				Sv3Element element = (Sv3Element) children.elementAt(i);
				int ey = element.getAbsoluteRect().y;
				int ex = element.getAbsoluteRect().x;
				int diff = lastX - ex;
				int diffY = Math.abs(ey - lastY);
				if (diffY < 10 && diff > 0 && diff < min) {
					min = diff;
					nextFocus = element;
				}
			}
			break;
		}
		case Adaptor.KEY_RIGHT: {// right
			int min = 10000;
			for (int i = 0; i < count; i++) {
				Sv3Element element = (Sv3Element) children.elementAt(i);
				int ey = element.getAbsoluteRect().y;
				int ex = element.getAbsoluteRect().x;
				int diffX = ex - lastX;
				int diffY = Math.abs(ey - lastY);
				if (diffY < 10 && diffX > 0 && diffX < min) {
					min = diffX;
					nextFocus = element;
				}
			}
			break;
		}
		}
		VectorPool.recycle(children);
		return nextFocus;
	}

	public void registerImageReference(String resourceId) {
		if (!imageResources.contains(resourceId)) {
			Adaptor.getInstance().grabImageResource(resourceId);
			imageResources.addElement(resourceId);
		}
	}

	public void clearImageReference() {
		int count = imageResources.size();
		for (int i = 0; i < count; i++) {
			String resourceId = (String) imageResources.elementAt(i);
			Adaptor.getInstance().releaseImageResource(resourceId);
		}
	}

}
