package milk.implement.mk;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.Core;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.VectorPool;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;

public class MPlayer extends MDraw {

	protected MRect position;
	protected MRect posDup;
	protected MRect clip = null;
	protected int drawX = 0, drawY = 0, state = -1;
	protected MState curState = null;
	protected String curResId = null;
	protected boolean focusable = false;
	protected String data;
	protected int borderColor = 0;

	public boolean onScreen = true;

	protected int offsetX = 0, offsetY = 0;

	public Hashtable states = null;

	public MRect getClip() {
		return clip;
	}

	public void setClip(MRect value) {
		clip = new MRect(value);
	}

	public void cancelClip() {
		clip = null;
	}

	public MPlayer(MPlayer player) {
		if (player != null) {
			if (player.position != null) {
				this.position = new MRect(player.position);
			}

			if (player.states != null) {
				this.states = new Hashtable();
				Enumeration keys = player.states.keys();
				while (keys.hasMoreElements()) {
					Integer key = (Integer) keys.nextElement();
					this.states.put(key,
							new MState((MState) player.states.get(key)));
				}
			}

			this.data = player.data;
			this.drawX = player.drawX;
			this.drawY = player.drawY;
			this.zIndex = player.zIndex;
			this.setState(player.state);
			this.onFingerDown = player.onFingerDown;
			this.onFingerMove = player.onFingerMove;
			this.onFingerUp = player.onFingerUp;
			this.curResId = player.curResId;
			this.focusable = player.focusable;
			this.visible = player.visible;
			this.bgColor = player.bgColor;
			if (focusable) {
				Core.getInstance().getCurrentScene().getFocusablePlayers()
						.addElement(this);
			}
		}
	}

	public void setVisible(boolean value) {
		if (this.visible != value) {
			super.setVisible(value);
			this.visible = value;
			if (this.visible && this.states != null) {
				MState state = (MState) this.states.get(new Integer(this
						.getState()));
				if (state != null) {
					state.start();
				}
			}
		}
	}

	public MPlayer(int x, int y, int width, int height) {
		this(new MRect(x, y, width, height));
	}

	public MPlayer(MRect rect) {
		position = rect;
	}

	public void defineState(int id, MState state) {
		if (states == null) {
			states = new Hashtable();
		}
		states.put(new Integer(id), state);
	}

	public MRect getPosition() {
		if (posDup == null) {
			posDup = new MRect();
		}
		posDup.x = position.x;
		posDup.y = position.y;
		posDup.width = position.width;
		posDup.height = position.height;
		return posDup;
	}

	public void setPosition(MRect position) {
		this.position = new MRect(position);
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {

		if (states != null) {
			this.state = state;
			curState = (MState) states.get(new Integer(this.state));
			if (curState != null) {
				if (curState.type == MState.stateTypeStatic) {
					this.setCurResId(curState.staticFrame);
				} else if (curState.type == MState.stateTypeSprite) {
					this.setCurResId(curState.spriteId);
				}
				curState.start();
			} else {
				this.setCurResId(null);
			}
		}

	}

	public String getCurResId() {
		return curResId;
	}

	public void setCurResId(String curResId) {
		if (this.curResId == null || !this.curResId.equals(curResId)) {
			Adaptor.getInstance().releaseImageResource(this.curResId);
			this.curResId = curResId;
			Adaptor.getInstance().grabImageResource(this.curResId);
		}
	}

	public boolean isFocusable() {
		return focusable;
	}

	public void setFocusable(boolean focusable) {
		if (this.focusable != focusable) {

			if (this.focusable) {
				Core.getInstance().getCurrentScene().getFocusablePlayers()
						.removeElement(this);
			} else {
				try {
					Core.getInstance().getCurrentScene().getFocusablePlayers()
							.addElement(this);
				} catch (Exception e) {
				}
			}

			this.focusable = focusable;
		}
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	protected boolean canDraw(MRect screen, int xOffset, int yOffset) {
		return isVisible() && position.intersacts(screen, xOffset, yOffset);
	}

	public boolean draw(MilkGraphics g, MRect viewPort, int xOffset, int yOffset) {
		offsetX = xOffset;
		offsetY = yOffset;
		boolean refresh = false;
		boolean restoreClip = false;
		int clipX = 0;
		int clipY = 0;
		int clipW = 0;
		int clipH = 0;
		if (isVisible()) {

			if (position.intersacts(viewPort, xOffset, yOffset)) {
				if (clip != null) {
					restoreClip = true;
					clipX = g.getClipX();
					clipY = g.getClipY();
					clipW = g.getClipWidth();
					clipH = g.getClipHeight();
					g.setClip(clip.getX() + position.getX() + xOffset,
							clip.getY() + position.getY() + yOffset,
							clip.getWidth(), clip.getHeight());
				}
				onScreen = true;

				if (bgColor != 0) {
					g.setColor(bgColor);
					g.fillRect(position.getX() + xOffset, position.getY()
							+ yOffset, position.getWidth(),
							position.getHeight());
				}
				if (borderColor != 0) {
					g.setColor(borderColor);
					g.drawRect(position.getX() + xOffset - 1, position.getY()
							+ yOffset - 1, position.getWidth() + 1,
							position.getHeight() + 1);
				}
				Object image = Adaptor.getInstance().getImageResource(
						getCurResId());
				if (image != null) {
					if (image instanceof MilkImage) {
						g.drawImage((MilkImage) image, position.getX()
								+ xOffset, position.getY() + yOffset,
								MilkGraphics.TOP | MilkGraphics.LEFT);
					} else if (image instanceof MilkSprite) {
						MilkSprite sprite = (MilkSprite) image;
						sprite.setPosition(position.getX() + xOffset,
								position.getY() + yOffset);
						sprite.setFrame(curState.spriteIndex);
						sprite.paint(g);
					}
				}

				if (restoreClip) {
					g.setClip(clipX, clipY, clipW, clipH);
				}
			} else {
				onScreen = false;
			}
			refresh = moveState != null;

			if (curState != null && curState.type != MState.stateTypeStatic) {
				refresh |= true;
			} else {
				refresh |= false;
			}

		}
		return refresh;

	}

	public void processAnimation(int xOffset, int yOffset) {
		if (states != null && isVisible()) {
			if (curState != null && curState.type != MState.stateTypeStatic) {
				long now = System.currentTimeMillis();
				int next = ((int) ((now - curState.startTime) / curState.frameDelay) % curState.frames
						.size());
				int totalDelay = curState.frameDelay * curState.frames.size();
				if (curState.type == MState.stateTypeDynamic) {
					this.setCurResId((String) curState.frames.elementAt(next));
				} else {
					curState.spriteIndex = (byte) ((Integer) curState.frames
							.elementAt(next)).intValue();
				}
				if (curState.finishCallback != null
						&& now - curState.lastCallback >= totalDelay) {
					curState.lastCallback += totalDelay;
					try {
						Core.getInstance().getCurrentScene().prepareCallParams();
						Core.getInstance().getCurrentScene().addCallParam(this);

						Core.getInstance().getCurrentScene()
								.execute(curState.finishCallback);
					} catch (Exception t) {
						Adaptor.exception(t);
					}
				}

			}
		}
	}

	public MDraw matchFinger(int x, int y, int xOffset, int yOffset) {
		if (isVisible() && onFingerDown != null) {
			MRect pos = this.getPosition();
			pos.move(xOffset, yOffset);
			if (pos.contains(x, y)) {
				return this;
			}
		}
		return null;
	}

	public int getMyX() {
		return getPosition().getX();
	}

	public int getMyY() {
		return getPosition().getY();
	}

	public int getX() {
		if (position != null) {
			return position.getX();
		} else {
			return 0;
		}
	}

	public void setX(int value) {
		if (position == null) {
			position = new MRect();
		}
		position.setX(value);
	}

	public int getY() {
		if (position != null) {
			return position.getY();
		} else {
			return 0;
		}
	}

	public void setY(int value) {
		if (position == null) {
			position = new MRect();
		}
		position.setY(value);
	}

	public int getWidth() {
		if (position != null) {
			return position.getWidth();
		} else {
			return 0;
		}
	}

	public void setWidth(int value) {
		if (position == null) {
			position = new MRect();
		}
		position.setWidth(value);
	}

	public int getHeight() {
		if (position != null) {
			return position.height;
		} else {
			return 0;
		}
	}

	public void setHeight(int value) {
		if (position == null) {
			position = new MRect();
		}
		position.setHeight(value);
	}

	public int getCenterX() {
		return offsetX + position.x + position.width / 2;
	}

	public int getCenterY() {
		return offsetY + position.y + position.height / 2;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	public void transform(int mode, int param1, int param2, int param3,
			int param4) {
		// to be implemented
	}
}
