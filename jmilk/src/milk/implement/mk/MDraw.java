package milk.implement.mk;

import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.Core;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.MilkCallback;
import milk.implement.VectorPool;
import milk.ui2.MilkGraphics;

public abstract class MDraw {

	protected MGroup parent;
	protected int zIndex;
	protected boolean visible = true;

	protected MoveState moveState = null;

	abstract protected boolean canDraw(MRect screen, int xOffset, int yOffset);

	public int getzIndex() {
		return zIndex;
	}

	public void setzIndex(int zIndex) {
		this.zIndex = zIndex;
		if (parent != null) {
			MArray temp = parent.getChildren().clone();
			temp.remove(this);
			int i = 0;
			if (!temp.array.contains(this)) {
				int size = temp.array.size();
				for (; i < size; i++) {
					MDraw thing = temp.getDraw(i);
					if (thing.getzIndex() > this.getzIndex()) {
						break;
					}
				}
			}
			temp.insert(i, this);
			parent.setChildren(temp);
		}
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean value) {
		if (value != this.visible) {
			this.visible = value;
			if (!this.visible) {
				moveState = null;
			}
		}
	}

	public MGroup getParent() {
		return parent;
	}

	public void setParent(MGroup thing) {
		if (parent != null) {
			parent.removeChild(this);
		}
		parent = thing;

	}

	public int resolveX(int input, boolean abs) {
		if (abs) {
			input += getX();
		} else {
			input -= getX();
		}
		if (parent != null) {
			return parent.resolveX(input, abs);
		}
		return input;
	}

	public int resolveY(int input, boolean abs) {
		if (abs) {
			input += getY();
		} else {
			input -= getY();
		}
		if (parent != null) {
			return parent.resolveY(input, abs);
		}
		return input;
	}

	public abstract boolean draw(MilkGraphics g, MRect viewPort, int xOffset,
			int yOffset);

	public abstract void processAnimation(int xOffset, int yOffset);

	public abstract MDraw matchFinger(int x, int y, int xOffset, int yOffset);

	public void calcFingerUsingViewPort(MFingerEvent finger) {
		if (this.parent != null) {
			if (parent.viewPort != null) {
				parent.calcFinger(finger);
			} else {
				parent.calcFingerUsingViewPort(finger);
			}
		}
	}

	protected MilkCallback onFingerDown = null, onFingerMove = null,
			onFingerUp = null;
	protected int bgColor = 0;

	public MilkCallback getOnFingerDown() {
		return onFingerDown;
	}

	public void setOnFingerDown(MilkCallback onFingerDown) {
		this.onFingerDown = onFingerDown;
	}

	public MilkCallback getOnFingerMove() {
		return onFingerMove;
	}

	public void setOnFingerMove(MilkCallback onFingerMove) {
		this.onFingerMove = onFingerMove;
	}

	public MilkCallback getOnFingerUp() {
		return onFingerUp;
	}

	public void setOnFingerUp(MilkCallback onFingerUp) {
		this.onFingerUp = onFingerUp;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int color) {
		this.bgColor = color;
	}

	public void setMoveState(MoveState state) {
		moveState = state;
	}

	public abstract void setX(int value);

	public abstract void setY(int value);

	public abstract int getX();

	public abstract int getY();

	public void processMove() {
		if (moveState != null && isVisible()) {
			long now = System.currentTimeMillis();
			long elapsed = now - moveState.startTime;
			switch (moveState.mode) {
			case MoveState.ConstantSpeed: {
				if (elapsed >= moveState.time) {
					MoveState temp = moveState;
					moveState = null;
					setX(temp.destX);
					setY(temp.destY);
					if (temp.callback != null) {

						Vector input = VectorPool.produce();
						try {
							Core.getInstance().getCurrentScene()
									.prepareCallParams();
							Core.getInstance().getCurrentScene()
									.addCallParam(this);
							Core.getInstance().getCurrentScene()
									.execute(temp.callback);
						} catch (Exception t) {
							Adaptor.exception(t);
						}
						VectorPool.recycle(input);
					}

				} else {
					long distanceX = (now - moveState.startTime)
							* (moveState.destX - moveState.startX)
							/ moveState.time;
					long distanceY = (now - moveState.startTime)
							* (moveState.destY - moveState.startY)
							/ moveState.time;
					// int percent = (int) ((now - moveState.startTime) * 10000
					// / moveState.time);
					// setX(moveState.startX
					// + (moveState.destX - moveState.startX) * percent
					// / 10000);
					// setY(moveState.startY
					// + (moveState.destY - moveState.startY) * percent
					// / 10000);
					setX(moveState.startX + (int) distanceX);
					setY(moveState.startY + (int) distanceY);
				}
				break;
			}
			case MoveState.ConstantStep: {
				if (elapsed >= moveState.time) {
					int times = (int) ((now - moveState.startTime) / moveState.time);
					setX(getX() + times * moveState.destX);
					setY(getY() + times * moveState.destY);
					moveState.startTime = moveState.startTime + times
							* moveState.time;
				}
				break;
			}
			case MoveState.VariableSpeed: {
				if (elapsed >= moveState.time) {
					MoveState temp = moveState;
					moveState = null;
					setX(temp.destX);
					setY(temp.destY);
					if (temp.callback != null) {
						Vector input = VectorPool.produce();
						try {
							Core.getInstance().getCurrentScene()
									.prepareCallParams();
							Core.getInstance().getCurrentScene()
									.addCallParam(this);
							Core.getInstance().getCurrentScene()
									.execute(temp.callback);
						} catch (Exception t) {
							Adaptor.exception(t);
						}
						VectorPool.recycle(input);
					}

				} else {
					long delta = elapsed;
					if (elapsed > moveState.time / 2) {
						delta = moveState.time - elapsed;
					}
					int xTotal = (moveState.destX - moveState.startX);
					int yTotal = (moveState.destY - moveState.startY);
					long distanceX = 2 * xTotal * delta * delta
							/ moveState.time / moveState.time;
					long distanceY = 2 * yTotal * delta * delta
							/ moveState.time / moveState.time;
					if (elapsed > moveState.time / 2) {
						distanceX = xTotal - distanceX;
						distanceY = yTotal - distanceY;
					}
					// System.out.println("<" + elapsed + "> [" + distanceX +
					// "]");
					setX(moveState.startX + (int) distanceX);
					setY(moveState.startY + (int) distanceY);
				}
				break;
			}
			}

		}
	}
}
