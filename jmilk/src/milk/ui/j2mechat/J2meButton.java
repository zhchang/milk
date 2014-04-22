package milk.ui.j2mechat;

import milk.ui.MilkCustomItem;
import milk.ui2.MilkCanvas;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class J2meButton extends MilkCustomItem {

	private int x = 0;
	private int y = 0;

	private String name;
	private RoundRect inputFrame;
	private static J2meButtonListener buttonListener;

	private J2meButtonManager buttonManager;

	public J2meButton(String showName, J2meButtonManager manager) {
		super(null);
		this.name = showName;
		this.buttonManager = manager;
	}

	public static void setButtonListener(J2meButtonListener l) {
		buttonListener = l;
	}

	protected int getMinContentHeight() {
		return J2meButtonManager.btnHeight;
	}

	protected int getMinContentWidth() {
		return J2meButtonManager.btnWidth;
	}

	protected int getPrefContentHeight(int arg0) {
		return getMinContentHeight();
	}

	protected int getPrefContentWidth(int arg0) {
		return getMinContentWidth();
	}

	protected void keyPressed(int keyCode) {
		if (buttonManager.getGameAction(keyCode) == MilkCanvas.FIRE) {
			buttonListener.handleClick(this);
		}
	}

	protected void pointerPressed(int x, int y) {
		buttonListener.handleClick(this);
	}

	protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
			int[] visRect_inout) {
		if (buttonManager.getFocusButton() != this) {
			buttonManager.setFocusButton(this);
			return true;
		}
		return false;
	}

	protected void traverseOut() {
		buttonManager.traverseOut(this);
	}

	private static final int infoColor = 0xb39f8c;
	private static final int infoFocusColor = 0x4a2c03;

	protected void paint(MilkGraphics g, int w, int h) {
		g.setFont(J2meButtonManager.font);
		MilkFont font = g.getFont();
		int focusAddX = 0;
		if (buttonManager.getFocusButton() == this) {
			inputFrame = J2meResourceManager.inputFrameFocus;

		} else {
			inputFrame = J2meResourceManager.inputFrame;
			focusAddX = 5;
		}
		inputFrame.drawRoundRect(g, x + focusAddX, y, w - 2 * focusAddX, h);
		if (buttonManager.getFocusButton() == this) {
			g.setColor(infoFocusColor);
		} else {
			g.setColor(infoColor);
		}
		g.drawString(name, x + (w - font.stringWidth(name)) / 2,
				y + (h - font.getHeight()) / 2, MilkGraphics.TOP
						| MilkGraphics.LEFT);

	}

	protected void hideNotify() {
		buttonManager.setFocusButton(null);
	}

	public void buttonClick() {
		buttonListener.handleClick(this);
		// TaskThread.getInstance().addTask(this);
	}

	public String getName() {
		return name;
	}

}
