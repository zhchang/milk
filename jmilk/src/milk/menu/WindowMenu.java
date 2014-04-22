package milk.menu;

import milk.implement.Adaptor;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MKeyEvent;
import milk.implement.mk.MArray;
import milk.implement.sv3.MilkImageUtil;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import smartview3.utils.IImageRequester;

public class WindowMenu implements IImageRequester {

	private MilkFont menuFont = Adaptor.uiFactory.getDefaultFont();

	private PopMenu popMenu;
	private static MilkImage imgLeft, imgRight1, imgRight2;
//	private String target;

	public void clearMem() {
		MilkImageUtil.getInstance().removeReceiver(this);
	}

	public WindowMenu(String target) {
//		this.target = target;
		popMenu = new PopMenu(target);
		if (imgLeft == null) {
			MilkImageUtil.getInstance().loadImageAsync("menu-left", this, 0, 0);
		}
		if (imgRight1 == null) {
			MilkImageUtil.getInstance().loadImageAsync("menu-right1", this, 0,
					0);
		}
		if (imgRight2 == null) {
			MilkImageUtil.getInstance().loadImageAsync("menu-right2", this, 0,
					0);
		}
	}

	public boolean isOpen() {
		return popMenu.isShown();
	}

	public void setMenu(MArray menus) {
		int keyHeight = 0;
		if (imgLeft != null) {
			keyHeight = imgLeft.getHeight();
		}
		popMenu.setMenuItems(menus, menuFont, 2, Adaptor.milk.getCanvasHeight()
				- keyHeight - 2);
	}

	public void show() {
		popMenu.show();
	}

	public void trigger() {
		if (popMenu.isShown()) {
			popMenu.trigger();
		}
	}

	public void hide() {
		popMenu.hide();
	}

	public void onKeyEvent(MKeyEvent keyEvent) {
		if (keyEvent.getType() == Adaptor.KEYSTATE_RELEASED) {
			int keyCode = keyEvent.getCode();
			switch (keyCode) {
			case Adaptor.KEY_FIRE: {
				if (popMenu.isShown()) {
					trigger();
				}
				break;
			}
			case Adaptor.KEY_DOWN: {
				if (popMenu.isShown()) {
					popMenu.moveFocusDown();
				}
				break;
			}
			case Adaptor.KEY_UP:
				if (popMenu.isShown()) {
					popMenu.moveFocusUp();
				}
				break;
			default:
				if (keyCode >= Adaptor.KEY_NUM1 && keyCode <= Adaptor.KEY_NUM9) {
					int newFocus = keyCode - Adaptor.KEY_NUM1;// 0-8
					if (newFocus < popMenu.getItemCount()) {
						popMenu.setFocus(newFocus);
						trigger();
					}
				}
				break;
			}
		}

	}

	public static boolean clickOnMenubar(int x, int y) {
		int width = Adaptor.milk.getCanvasWidth();
		int height = Adaptor.milk.getCanvasHeight();
		if (imgLeft != null && imgRight1 != null) {
			if (pointInRect(x, y, 0, height - imgLeft.getHeight(),
					imgLeft.getWidth(), imgLeft.getHeight())) {// left
				return true;

			} else if (pointInRect(x, y, width - imgRight1.getWidth(), height
					- imgRight1.getHeight(), imgRight1.getWidth(),
					imgRight1.getHeight())) {
				return true;

			}
		}
		return false;
	}

	public boolean onFingerEvent(MFingerEvent fingerEvent) {

		if (fingerEvent.getType() == Adaptor.POINTER_PRESSED) {
			int x = fingerEvent.getX();
			int y = fingerEvent.getY();

			int width = Adaptor.milk.getCanvasWidth();
			int height = Adaptor.milk.getCanvasHeight();
			if (imgLeft != null && imgRight1 != null) {
				if (pointInRect(x, y, 0, height - imgLeft.getHeight(),
						imgLeft.getWidth(), imgLeft.getHeight())) {// left
					Adaptor.getInstance().onLeftSoftKey();
					return true;

				} else if (pointInRect(x, y, width - imgRight1.getWidth(),
						height - imgRight1.getHeight(), imgRight1.getWidth(),
						imgRight1.getHeight())) {
					Adaptor.getInstance().onRightSoftKey();
					return true;

				} else {
					if (popMenu.isShown()) {
						return popMenu.pointerPressed(x, y);
					}
				}
			}
		}
		return false;
	}

	public void draw(MilkGraphics g) {
		MilkFont saveFont = g.getFont();
		int width = Adaptor.milk.getCanvasWidth();
		int height = Adaptor.milk.getCanvasHeight();
		g.setFont(menuFont);
		g.setClip(0, 0, width, height);
		if (imgLeft != null) {
			g.drawImage(imgLeft, 0, height - imgLeft.getHeight(), 0);
		}
		if (popMenu.isShown()) {
			if (imgRight2 != null) {
				g.drawImage(imgRight2, width - imgRight2.getWidth(), height
						- imgRight2.getHeight(), 0);
			}
		} else {
			if (imgRight1 != null) {
				g.drawImage(imgRight1, width - imgRight1.getWidth(), height
						- imgRight1.getHeight(), 0);
			}
		}
		if (popMenu.isShown())
			popMenu.draw(g);
		g.setFont(saveFont);
		// g.setClip(0, 0, width, height);
	}

	public boolean keyPressed(int keyCode) {
		boolean handled = false;
		switch (keyCode) {
		case Adaptor.KEY_LEFT_SOFT:
		case Adaptor.KEY_FIRE:

			handled = true;

			if (popMenu.isShown()) {
			} else {
				popMenu.show();
			}

			break;
		case Adaptor.KEY_RIGHT_SOFT:

			handled = true;
			if (popMenu.isShown()) {
				popMenu.hide();
			}

			break;
		case Adaptor.KEY_DOWN:
			if (popMenu.isShown()) {
				popMenu.moveFocusDown();
				handled = true;
			}
			break;
		case Adaptor.KEY_UP:
			if (popMenu.isShown()) {
				popMenu.moveFocusUp();
				handled = true;
			}
			break;
		default:
			if (keyCode >= Adaptor.KEY_NUM1 && keyCode <= Adaptor.KEY_NUM9) {
				int newFocus = keyCode - Adaptor.KEY_NUM1;// 0-8
				if (newFocus < popMenu.getItemCount()) {
					popMenu.setFocus(newFocus);
					keyPressed(Adaptor.KEY_FIRE);
				}
			}
			break;
		}
		return handled;
	}

	public boolean pointerPressed(int x, int y) {
		int width = Adaptor.milk.getCanvasWidth();
		int height = Adaptor.milk.getCanvasHeight();
		if (imgLeft != null && imgRight1 != null) {
			if (pointInRect(x, y, 0, height - imgLeft.getHeight(),
					imgLeft.getWidth(), imgLeft.getHeight())) {// left
				Adaptor.getInstance().onLeftSoftKey();
				return true;
			} else if (pointInRect(x, y, width - imgRight1.getWidth(), height
					- imgRight1.getHeight(), imgRight1.getWidth(),
					imgRight1.getHeight())) {
				Adaptor.getInstance().onRightSoftKey();
				return true;
			} else {
				if (popMenu.isShown()) {
					return popMenu.pointerPressed(x, y);
				}
			}
		}
		return false;
	}

	protected static boolean pointInRect(int px, int py, int x, int y, int w,
			int h) {
		return px >= x && px <= x + w && py >= y && py <= y + h;
	}

	public void clearImageReferences() {

	}

	public void didReceiveImage(Object image, String src) {
		if (src.equals("menu-left")) {
			imgLeft = (MilkImage) image;
		} else if (src.equals("menu-right1")) {
			imgRight1 = (MilkImage) image;
		} else if (src.equals("menu-right2")) {
			imgRight2 = (MilkImage) image;
		}

	}
}
