package milk.ui.j2mechat;


import milk.chat.core.Def;
import milk.chat.core.HallAccess;
import milk.chat.core.Utils;
import milk.ui2.MilkApp;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;

public class J2meInputBox {

	private static final int infoColor = 0xb39f8c;
	private static final int infoFocusColor = 0x4a2c03;
	private static int rectX, rectY, rectW, rectH;
	private static MilkApp factory;
	private boolean isFocus = true;
	private static MilkImage imgInputPen;

	public J2meInputBox(MilkApp factory, byte chatType) {
		J2meInputBox.factory = factory;
		if (imgInputPen == null)
			imgInputPen = Utils.getImage("chat-inputpen");
	}

	public void setFocus(boolean focus) {
		isFocus = focus;
	}

	public void initInputBox(int x, int y, int w, int h) {
		rectX = x + imgInputPen.getWidth() + 6;
		rectY = y;
		rectW = w - imgInputPen.getWidth() * 2 - 6;
		rectH = h;
	}

	public void draw(MilkGraphics g) {
		RoundRect inputRect;
		g.setClip(0, rectY, factory.getCanvasWidth(), rectH);
		if (isFocus) {
			inputRect = J2meResourceManager.inputFrameFocus;
		} else {
			inputRect = J2meResourceManager.inputFrame;
		}
		inputRect.drawRoundRect(g, rectX, rectY, rectW, rectH);
		g.setClip(0, 0, factory.getCanvasWidth(), factory.getCanvasHeight());
		int size = 0;
		if ((System.currentTimeMillis() / 300) % 2 > 0) {
			size = 2;
		}
		Utils.drawScaleImage(g, imgInputPen, rectX + -imgInputPen.getWidth()
				- 3 + size / 2, rectY + size / 2, rectH - size);

		if (isFocus) {
			g.setColor(infoFocusColor);
		} else {
			g.setColor(infoColor);
		}
		String showInfo = Def.chatRoomInputInfo;
		if (HallAccess.isLanguageAr) {
			g.drawString(showInfo,
					rectX + rectW - 4 - g.getFont().stringWidth(showInfo),
					rectY, 0);
		} else {
			g.drawString(showInfo, rectX + 4, rectY, 0);
		}
	}

	public static void drawBack(MilkGraphics g, MilkImage back) {
		int size = 0;
		if ((System.currentTimeMillis() / 300) % 2 > 0) {
			size = 2;
		}
		g.setClip(0, rectY, factory.getCanvasWidth(), rectH);
		Utils.drawScaleImage(g, back, rectX + rectW + 4 - 3 + size / 2, rectY
				+ size / 2, rectH - size);
	}

	public boolean isTouch(int x, int y) {
		return Utils.pointInRect(x, y, rectX, rectY, rectW, rectH);
	}

//	public void setTarget(int id, String name) {
//
//	}

//	public void showPopMenu() {
//	}

}
