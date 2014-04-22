package milk.ui.bbchat;

import milk.chat.core.Def;


import milk.chat.core.FaceCoreHandler;
import milk.chat.core.Utils;
import milk.implement.Adaptor;
//import milk.implement.Core;
//import milk.implement.IMEvent.MFingerEvent;
//import milk.implement.IMEvent.MKeyEvent;
//import milk.implement.IMEvent.MRightKeyEvent;
//import milk.implement.Scene;
import milk.ui2.MilkApp;
//import milk.ui2.MilkDisplayable;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;
import milk.ui2.ShrinkRect;

public class BBFaceScreen {

	private static final int FACE_NUM_LINE = 5;
	private static MilkFont font = BBHallScreen.font;
	private static int FREE_FACE_TAB_Y = 70 - font.getHeight() * 2;
	private static final int space = 18;
	private int drawFaceX;
	private int drawFaceY;
	private int drawFaceSize;
	private int width, height;

//	private MilkApp factory;
//	private MilkDisplayable backScreen;
	private BBFaceListener faceListener;
	private int focus;
	private RoundRect focusFrame, unfocusFrame;
	private ShrinkRect shrinkTab;
	private  int FACE_IMAGE_SIZE;
	private MilkImage face[];
	private FaceCoreHandler faceHandler;

	public BBFaceScreen(MilkApp factory) {
//		this.factory = factory;
		width = factory.getCanvasWidth();
		height = factory.getCanvasHeight();
		unfocusFrame = Adaptor.uiFactory.createRoundRect("chat-faceunfocus", 5,
				0x35250E);
		focusFrame = Adaptor.uiFactory.createRoundRect("chat-facefocus", 5,
				0xFF8A00);
		shrinkTab = Adaptor.uiFactory.createShrinkRect("chat-shrinktab", 2,
				0xca974f);
	}
	
	public void setFaceCoreHandler(FaceCoreHandler faceCore){
		this.faceHandler = faceCore;
		this.faceHandler = faceCore;
		face = faceCore.getFaceImageList();
		FACE_IMAGE_SIZE=face[0].getHeight();
		drawFaceSize = FACE_IMAGE_SIZE + space;
		drawFaceX = (width - FACE_NUM_LINE * drawFaceSize + space) / 2;
		drawFaceY = FREE_FACE_TAB_Y + 2 * font.getHeight();
	}

	private boolean isShowPopScreenForBB = false;

	private int bbX, bbY, bbW, bbH;

	public void setPopScreenSizeForBB(int w, int h) {
		bbW = FACE_NUM_LINE * drawFaceSize + space;
		bbH = (FACE_IMAGE_SIZE + 20) * 3;
		bbX = (w - bbW) / 2 + (width - w)/2;
		bbY = (h - bbH) / 2 + (height - h) - 20;
		FREE_FACE_TAB_Y = bbY + 4;
		drawFaceX = bbX + space;
		drawFaceY = bbY + 4 + FACE_IMAGE_SIZE + 10;
	}

	protected void drawBBFace(MilkGraphics g) {
		g.setFont(font);
		shrinkTab.drawRoundRect(g, bbX, bbY, bbW, bbH);
		String freeface = Def.chatfreeface;
		g.setColor(0x000000);
		g.drawString(freeface, (width-font.stringWidth(freeface))/2, FREE_FACE_TAB_Y, 0);

		for (int i = 0; i < face.length; i++) {
			int col = i % FACE_NUM_LINE;
			int row = i / FACE_NUM_LINE;
			int faceX = drawFaceX + col * drawFaceSize;
			int faceY = drawFaceY + row * drawFaceSize;
			int faceFrameBorder = 6;
			int facefocusFrameBorder = 10;
			if (i == focus) {
				focusFrame.drawRoundRect(g, faceX - facefocusFrameBorder, faceY
						- facefocusFrameBorder, FACE_IMAGE_SIZE + 2
						* facefocusFrameBorder, FACE_IMAGE_SIZE + 2
						* facefocusFrameBorder);
			}
			unfocusFrame.drawRoundRect(g, faceX - faceFrameBorder, faceY
					- faceFrameBorder, FACE_IMAGE_SIZE + 2 * faceFrameBorder,
					FACE_IMAGE_SIZE + 2 * faceFrameBorder);
			g.drawImage(face[i], faceX, faceY, 0);
		}
	}

	public boolean isPopup() {
		return isShowPopScreenForBB;
	}

	public void popupFaceScreen() {
		this.focus = 0;
		isShowPopScreenForBB = true;
	}

	public void hideFaceScreen() {
		isShowPopScreenForBB = false;
	}

	public void setFaceInputListener(BBFaceListener l) {
		faceListener = l;
	}

//	public static boolean inFaceScreen = false;
//
//	public void showNotify() {
//		this.focus = 0;
//		inFaceScreen = true;
//	}
//
//	public void hideNotify() {
//		inFaceScreen = false;
//	}

	public String getFace(int index) {
		return faceHandler.getFaceString(index);
	}

	public MilkImage getMilkImage(int select) {
		return face[select];
	}

//	public void handleFingerEvent(MFingerEvent finger) {
//		int type = finger.getType();
//		if (type == Adaptor.POINTER_PRESSED) {
//			int x = finger.getX();
//			int y = finger.getY();
//			for (int i = 0; i < face.length; i++) {
//				int col = i % FACE_NUM_LINE;
//				int row = i / FACE_NUM_LINE;
//				int faceX = drawFaceX + col * drawFaceSize;
//				int faceY = drawFaceY + row * drawFaceSize;
//				int facefocusFrameBorder = 10;
//				if (Utils.pointInRect(x, y, faceX - facefocusFrameBorder, faceY
//						- facefocusFrameBorder, FACE_IMAGE_SIZE + 2
//						* facefocusFrameBorder, FACE_IMAGE_SIZE + 2
//						* facefocusFrameBorder)) {
//					this.focus = i;
//					keyFire();
//					return;
//				}
//			}
//		}
//	}

//	public void handleKeyEvent(MKeyEvent key) {
//		int type = key.getType();
//		int keyCode = key.getCode();
//		if (type == Adaptor.KEYSTATE_PRESSED) {
//			if (keyCode == Adaptor.KEY_RIGHT_SOFT) {
//				factory.switchDisplay(backScreen);
//				Core.getInstance().replaceScene(this.backScene);
//			} else if (keyCode == Adaptor.KEY_FIRE
//					|| keyCode == Adaptor.KEY_LEFT_SOFT) {
//				keyFire();
//			} else {
//				switch (keyCode) {
//				case Adaptor.KEY_LEFT:
//					if (focus > 0)
//						focus--;
//					break;
//				case Adaptor.KEY_RIGHT:
//					if (focus < face.length - 1)
//						focus++;
//					break;
//				case Adaptor.KEY_UP:
//					if (focus - FACE_NUM_LINE >= 0) {
//						focus = focus - FACE_NUM_LINE;
//					}
//					break;
//				case Adaptor.KEY_DOWN:
//					if (focus + FACE_NUM_LINE <= face.length - 1) {
//						focus = focus + FACE_NUM_LINE;
//					} else {
//						focus = face.length - 1;
//					}
//					break;
//				}
//			}
//		}
//	}

	public void keyPressed(int keyCode) {
		if (keyCode == Adaptor.KEY_RIGHT_SOFT) {
			this.hideFaceScreen();
		} else if (keyCode == Adaptor.KEY_FIRE
				|| keyCode == Adaptor.KEY_LEFT_SOFT) {
			keyFire();
			this.hideFaceScreen();
		} else {
			switch (keyCode) {
			case Adaptor.KEY_LEFT:
				if (focus > 0)
					focus--;
				break;
			case Adaptor.KEY_RIGHT:
				if (focus < face.length - 1)
					focus++;
				break;
			case Adaptor.KEY_UP:
				if (focus - FACE_NUM_LINE >= 0) {
					focus = focus - FACE_NUM_LINE;
				}
				break;
			case Adaptor.KEY_DOWN:
				if (focus + FACE_NUM_LINE <= face.length - 1) {
					focus = focus + FACE_NUM_LINE;
				} else {
					focus = face.length - 1;
				}
				break;
			}
		}
	}

	public void pointerPressed(int x, int y) {
		for (int i = 0; i < face.length; i++) {
			int col = i % FACE_NUM_LINE;
			int row = i / FACE_NUM_LINE;
			int faceX = drawFaceX + col * drawFaceSize;
			int faceY = drawFaceY + row * drawFaceSize;
			int facefocusFrameBorder = 10;
			if (Utils.pointInRect(x, y, faceX - facefocusFrameBorder, faceY
					- facefocusFrameBorder, FACE_IMAGE_SIZE + 2
					* facefocusFrameBorder, FACE_IMAGE_SIZE + 2
					* facefocusFrameBorder)) {
				this.focus = i;
				keyFire();
				this.hideFaceScreen();
				return;
			}
		}
	}

	private void keyFire() {
		if (faceListener != null) {
			faceListener.insertFace(faceHandler.getFaceString(focus));
		} else {
			throw new NullPointerException("emotionListener=null");
		}
//		if (isShowPopScreenForBB) {
			hideFaceScreen();
//		} else {
//			factory.switchDisplay(backScreen);
//			Core.getInstance().replaceScene(this.backScene);
//		}
	}

//	public void handleLeftKeyEvent(MRightKeyEvent rightKey) {
//		keyFire();
//	}


//	private BackGround backGround;

//	protected void draw(MilkGraphics g) {
//		g.setFont(font);
//		// ResourceManager.loadResource();
//		if (backGround == null) {
//			backGround = new BackGround(width, height);
//		}
//		backGround.drawBackGround(g, Def.chatface);
//		// ChatManager.drawScreenBack(g, Def.chatface);
//		shrinkTab.drawRoundRect(g, 10, FREE_FACE_TAB_Y, width - 20,
//				font.getHeight());
//		String freeface = Def.chatfreeface;
//		g.setColor(0x000000);
//		g.drawString(freeface, 20, FREE_FACE_TAB_Y, 0);
//
//		for (int i = 0; i < face.length; i++) {
//			int col = i % FACE_NUM_LINE;
//			int row = i / FACE_NUM_LINE;
//			int faceX = drawFaceX + col * drawFaceSize;
//			int faceY = drawFaceY + row * drawFaceSize;
//			int faceFrameBorder = 6;
//			int facefocusFrameBorder = 10;
//			if (i == focus) {
//				focusFrame.drawRoundRect(g, faceX - facefocusFrameBorder, faceY
//						- facefocusFrameBorder, FACE_IMAGE_SIZE + 2
//						* facefocusFrameBorder, FACE_IMAGE_SIZE + 2
//						* facefocusFrameBorder);
//			}
//			unfocusFrame.drawRoundRect(g, faceX - faceFrameBorder, faceY
//					- faceFrameBorder, FACE_IMAGE_SIZE + 2 * faceFrameBorder,
//					FACE_IMAGE_SIZE + 2 * faceFrameBorder);
//			g.drawImage(face[i], faceX, faceY, 0);
//		}
//	}

}
