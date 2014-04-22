package milk.implement.mk;

import milk.implement.Adaptor;
import milk.implement.IMEvent.MFingerEvent;
import milk.ui2.MilkGraphics;

public class MGroup extends MDraw {

	private int x, y;
	private MArray children;
	private String data;
	MRect viewPort = null;

	public MGroup() {

	}

	public MGroup(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void setViewPort(MRect rect) {

		if (rect != null) {
			if (viewPort == null) {
				viewPort = new MRect(rect);
			} else {
				viewPort.x = rect.x;
				viewPort.y = rect.y;
				viewPort.width = rect.width;
				viewPort.height = rect.height;
			}
		}
	}

	public MRect getViewPort() {
		return new MRect(viewPort);
	}

	public boolean calcFinger(MFingerEvent finger) {
		if (viewPort != null) {
			int x = finger.getX();
			int y = finger.getY();

			finger.setX(viewPort.x + x * viewPort.width
					/ Adaptor.milk.getCanvasWidth());
			finger.setY(viewPort.y + y * viewPort.height
					/ Adaptor.milk.getCanvasHeight());
			// finger.setX(viewPort.x + x);
			// finger.setY(viewPort.y + y);
			return true;
		}
		return false;
	}

	public boolean calcFingerRecursive(MFingerEvent finger) {
		boolean thing = calcFinger(finger);
		if (!thing) {
			if (children != null) {
				int count = children.size();
				for (int i = 0; i < count; i++) {
					MDraw draw = children.getDraw(i);
					if (draw instanceof MGroup) {
						thing = ((MGroup) draw).calcFinger(finger);
						if (thing) {
							break;
						}
					}
				}
			}
		}
		return thing;

	}

	public MGroup(MGroup group) {
		if (group != null) {
			this.x = group.x;
			this.y = group.y;
			this.zIndex = group.zIndex;
			this.data = group.data;
			this.visible = group.visible;
			if (group.children != null) {
				children = new MArray(group.children);
			}
		}
	}

	public void clearChildren() {
		children = null;
	}

	public void clearImageReferences() {
		if (children != null) {
			int count = children.size();
			for (int i = 0; i < count; i++) {
				MDraw draw = children.getDraw(i);
				if (draw instanceof MPlayer) {
					((MPlayer) draw).setCurResId(null);
				} else if (draw instanceof MGroup) {
					((MGroup) draw).clearImageReferences();
				}
			}
		}

	}

	public void addChild(MDraw child) {
		if (children == null) {
			children = new MArray(true);
		}
		if (children.array.contains(child)) {
			return;
		}
		child.setParent(this);

		int i = 0;
		if (!children.array.contains(child)) {
			int size = children.size();
			for (; i < size; i++) {
				MDraw thing = children.getDraw(i);
				if (thing.getzIndex() > child.getzIndex()) {
					break;
				}
			}
		}
		children.insert(i, child);
	}

	public void removeChild(MDraw child) {
		if (children != null) {
			child.parent = null;
			children.remove(child);
		}
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public MArray getChildren() {
		if (children != null) {
			return new MArray(children);
		} else {
			return new MArray();
		}
	}

	public void setChildren(MArray children) {
		this.children = children;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public boolean draw(MilkGraphics g, MRect screen, int xOffset, int yOffset) {
		boolean anim = false;

		if (canDraw(screen, xOffset, yOffset)) {
			if (viewPort != null) {
				g.save();
				g.resize(viewPort.width * 1000 / screen.width);
				viewPort.height = viewPort.width * 1000 / screen.width
						* screen.height / 1000;
				g.translate(0 - viewPort.x - g.getTranslateX(), 0 - viewPort.y
						- g.getTranslateY());
			}

			if (children != null) {
				int size = children.size();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						MDraw draw = children.getDraw(i);
						anim |= draw
								.draw(g,
										(viewPort == null ? screen : viewPort),
										xOffset + getX(), yOffset + getY());
					}
				}
			}
			if (viewPort != null) {
				g.restore();
			}
		}
		return (moveState != null) | anim;
	}

	public void processAnimation(int xOffset, int yOffset) {
		processMove();
		if (children != null && isVisible()) {
			int size = children.size();
			if (size > 0) {
				xOffset += getX();
				yOffset += getY();
				for (int i = 0; i < size; i++) {
					MDraw draw = children.getDraw(i);
					draw.processAnimation(xOffset, yOffset);
					draw.processMove();
				}
			}
		}
	}

	public MDraw matchFinger(int x, int y, int xOffset, int yOffset) {
		MDraw match = null;
		if (viewPort == null && isVisible() && children != null) {

			int size = children.size();
			if (size > 0) {
				xOffset += getX();
				yOffset += getY();
				for (int i = size - 1; i >= 0; i--) {
					MDraw draw = children.getDraw(i);
					match = draw.matchFinger(x, y, xOffset, yOffset);
					if (match != null) {
						break;
					}
				}
			}
		}
		return match;
	}

	public int getMyX() {
		return getX();
	}

	public int getMyY() {
		return getY();
	}

	protected boolean canDraw(MRect screen, int x, int y) {
		return isVisible();
	}

}
