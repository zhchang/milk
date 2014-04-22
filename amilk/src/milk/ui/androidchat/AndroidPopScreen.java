package milk.ui.androidchat;

import java.util.Vector;


import milk.chat.core.Def;
import milk.chat.core.FaceCoreHandler;
import milk.chat.core.SelectBoxCore;
import milk.chat.core.UserInfo;
import milk.chat.core.Utils;
import milk.implement.Adaptor;
import milk.ui.MilkImageImpl;
import milk.ui.R;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;

public class AndroidPopScreen {

	private static final int FACE_NUM_LINE = 7;
	private static MilkFont font = AndroidHallScreen.font;
	private static RoundRect frame;
	private static MilkImage face[],back;
	private static AndroidFaceListener faceListener;
	private static FaceCoreHandler faceHandler;
	
	private int screenRectTopY;
	private final int space;
	private int drawFaceX;
	private int drawFaceY;
	private final int drawFaceSize;
	private final int screenWidth, screenHeight;
	private int faceFrameBorder = 8;
	
	private int focus;
	private final int faceImageSize;
	private boolean isShowFaceScreen = false;
	private int frameX, frameY,frameW,frameH;
	
	public AndroidPopScreen(MilkApp factory) {
		screenWidth = factory.getCanvasWidth();
		screenHeight = factory.getCanvasHeight();
		if (frame == null)
			frame = Adaptor.uiFactory.createRoundRect("chat-faceunfocus", 5,
					0x35250E);

		if (screenWidth >= 480) {
			faceImageSize = 40;
			space = 24;
			itemFrameH=16;
			faceFrameBorder = 10;
		} else if (screenWidth >= 320) {
			faceImageSize = 24;
			space = 18;
			itemFrameH=12;
			faceFrameBorder = 8;
		} else {
			faceImageSize = 20;
			itemFrameH=6;
			space = 12;
			faceFrameBorder = 6;
		}
		drawFaceSize = faceImageSize + space;
		
		if (back == null) {
			int backWidth=drawFaceSize*FACE_NUM_LINE+space;
			back = MilkImageImpl.createImage(R.drawable.popupface);
			int newHeight = backWidth * back.getHeight() / back.getWidth();
			resizeImage(back, newHeight);
		}
	}

//	private String nameloadTable[] = { 
//			"face_bj", "face_dx","face_wx", "face_fn", "face_hx", 
//			"face_jk", "face_js", "face_jy","face_wq", "face_zm" ,
//			
////			"face_bj", "face_dx","face_wx", "face_fn", "face_hx", 
////			"face_jk", "face_js", "face_jy","face_wq", "face_zm" ,
//	};
	
	private static final int faceResId[]={
			R.drawable.face_bj, R.drawable.face_dx, R.drawable.face_wx, R.drawable.face_fn, R.drawable.face_hx,
			R.drawable.face_jk,R.drawable.face_js,R.drawable.face_jy,R.drawable.face_wq,R.drawable.face_zm,
			R.drawable.face_11,R.drawable.face_12,R.drawable.face_13,R.drawable.face_14,R.drawable.face_15,
			R.drawable.face_16,R.drawable.face_17,R.drawable.face_18,R.drawable.face_19,R.drawable.face_20,
			R.drawable.face_21,R.drawable.face_22,R.drawable.face_23,R.drawable.face_24,R.drawable.face_25,
			R.drawable.face_26,R.drawable.face_27,R.drawable.face_28,
	};
	
	private static final String nameShowTable[] = { 
			"[bj]", "[dx]", "[wx]","[fn]", "[hx]", 
			"[jk]", "[js]", "[jy]","[wq]", "[zm]",
			
			"[11]", "[12]", "[13]","[14]", "[15]", 
			"[16]", "[17]", "[18]","[19]", "[20]", 
			"[21]", "[22]", "[23]","[24]", "[25]", 
			"[26]", "[27]", "[28]", 
	};
	
	public void setFaceCoreHandler(FaceCoreHandler faceCore) {
		faceHandler = faceCore;
		if (face == null) {
			int len = faceResId.length;
			face = new MilkImage[len];
			for (int i = 0; i < len; i++) {
				face[i] = MilkImageImpl.createImage(faceResId[i]);
			}
			resizeFace(face, faceImageSize);
			faceHandler.setFaceArray(face, nameShowTable);
		}

		initFaceScreenFrame();
	}
	
	private void resizeFace(MilkImage face[],int newHeight){
		for(int i=0;i<face.length;i++){
			resizeImage(face[i],newHeight);
		}
	}
	
	private void resizeImage(MilkImage image,int newHeight){
		int oldHeight=image.getHeight();
		int newWidth=newHeight*image.getWidth()/oldHeight;
		MilkImageImpl.resizeImage(image, newWidth, newHeight);
	}

	private void initFaceScreenFrame() {
		int faceW = FACE_NUM_LINE * drawFaceSize -space;
		int faceLine=faceResId.length/FACE_NUM_LINE;
		if(faceResId.length%FACE_NUM_LINE!=0)
			faceLine++;
		frameH = drawFaceSize * faceLine + back.getHeight() / 8+space;
		frameW = back.getWidth();
		frameX = (screenWidth - back.getWidth()) / 2;
		frameY = (screenHeight - frameH) / 2;
		screenRectTopY = frameY;
		drawFaceX = frameX + (back.getWidth() - faceW) / 2;
		drawFaceY = frameY + back.getHeight() / 8 + space;
	}
	
	void initScreenByInputBoxY(int inputBoxY){
		int spaceTemp=screenHeight/40;
		if(screenHeight<=320){
			spaceTemp=5;
		}
		frameY = inputBoxY-frameH-spaceTemp;
		screenRectTopY = frameY;
		drawFaceY = frameY + back.getHeight() / 8 + space;
	}
	
	private void drawBackGroundFrame(MilkGraphics g, int x, int y, int w, int h) {
		g.setClip(x, y, w, h - 20);
		g.drawImage(back, x, y, 0);
		g.setClip(x, y + h - 20, w, 20);
		g.drawImage(back, x, y + h - back.getHeight(), 0);
		g.setClip(0, 0, screenWidth, screenHeight);
	}

	protected void drawFaceScreen(MilkGraphics g) {
		g.setFont(font);
		drawBackGroundFrame(g, frameX, frameY, frameW, frameH);
		String freeface = Def.chatfreeface;
		g.setColor(0xffffff);
		int dy = back.getHeight() / 8 - font.getHeight();
		g.drawString(freeface, (screenWidth-font.stringWidth(freeface))/2-20, screenRectTopY+dy/2, 0);
	
		for (int i = 0; i < face.length; i++) {
			int col = i % FACE_NUM_LINE;
			int row = i / FACE_NUM_LINE;
			int faceX = drawFaceX + col * drawFaceSize;
			int faceY = drawFaceY + row * drawFaceSize;
			frame.drawRoundRect(g, faceX - faceFrameBorder, faceY
					- faceFrameBorder, faceImageSize + 2 * faceFrameBorder,
					faceImageSize + 2 * faceFrameBorder);
			g.drawImage(face[i], faceX, faceY, 0);
		}
	}

	public boolean isPopup() {
		return isShowFaceScreen;
	}
	
	public void popupFaceScreen() {
		this.focus = 0;
		isShowFaceScreen = true;
	}

	public void hideScreen() {
		isShowFaceScreen = false;
	}

	public void setFaceListener(AndroidFaceListener l) {
		faceListener = l;
	}

	public String getFace(int index) {
		return faceHandler.getFaceString(index);
	}

	public MilkImage getMilkImage(int select) {
		return face[select];
	}

	public void keyPressed(int keyCode) {
		if (keyCode == Adaptor.KEY_RIGHT_SOFT) {
			this.hideScreen();
		} else if (keyCode == Adaptor.KEY_FIRE
				|| keyCode == Adaptor.KEY_LEFT_SOFT) {
			keyFire();
			this.hideScreen();
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

	public boolean pointerPressedFaceScreen(int x, int y) {
		int closeRectH=back.getHeight() / 8;
		int closeRectW=back.getWidth()*closeRectH/back.getHeight();
		int rectX=frameX+back.getWidth()-closeRectW;
		int rectY=frameY;
		if (Utils.pointInRect(x, y, rectX, rectY, closeRectW, closeRectH)) {
			this.hideScreen();
			return true;
		}
		for (int i = 0; i < face.length; i++) {
			int col = i % FACE_NUM_LINE;
			int row = i / FACE_NUM_LINE;
			int faceX = drawFaceX + col * drawFaceSize;
			int faceY = drawFaceY + row * drawFaceSize;
			int facefocusFrameBorder = 10;
			if (Utils.pointInRect(x, y, faceX - facefocusFrameBorder, faceY
					- facefocusFrameBorder, faceImageSize + 2
					* facefocusFrameBorder, faceImageSize + 2
					* facefocusFrameBorder)) {
				this.focus = i;
				keyFire();
				this.hideScreen();
				return true;
			}
		}
		return false;
	}
	
	public boolean pointerPressedFriendScreen(int x, int y) {
		int closeRectH=back.getHeight() / 8;
		int closeRectW=back.getWidth()*closeRectH/back.getHeight();
		int rectX=frameX+back.getWidth()-closeRectW;
		int rectY=frameY;
		if (Utils.pointInRect(x, y, rectX, rectY, closeRectW, closeRectH)) {
			this.hideScreen();
			return true;
		}
		final int nameWidth=(frameW-60)/2;
		int nameX ;
		int nameY ;
		final int maxRow=boxCore.getMaxLength()/2;
		for (int i = 0; i < popQueue.size(); i++) {
			int row = i;
			if (i >= maxRow) {
				row = i - maxRow;
				nameX = frameX + 20 + nameWidth + 20;
			}
			else{
				row = i;
				nameX = frameX + 20;
			}
			nameY = friendY + row * (friendItemH + friendSpaceY);
			if (Utils.pointInRect(x,y, nameX , nameY, nameWidth ,friendItemH)){
				UserInfo info =(UserInfo)popQueue.elementAt(i);
//				Utils.info("------------pointerPressedFriendScreen--------info.name-"+info.name);
				selectBoxListener.focusItemChange(info.id, info.name);
				this.hideScreen();
				return true;
			}
		}
		return false;
	}
	
	void drawFriendScreen(MilkGraphics g){
		g.setFont(font);
		drawBackGroundFrame(g, frameX, frameY, frameW, frameH);
		String freeface = Def.chatchoosefriend;
		g.setColor(0xffffff);
		int dy = back.getHeight() / 8 - font.getHeight();
		g.drawString(freeface, (screenWidth-font.stringWidth(freeface))/2-20, frameY+dy/2, 0);
		int nameX ;
		int nameY ;
		final int distance=5;
		final int nameWidth=(frameW-distance*3)/2;
	
		final int maxRow=boxCore.getMaxLength()/2;
		for (int i = 0; i < popQueue.size(); i++) {
			UserInfo info =(UserInfo)popQueue.elementAt(i);
			int row = i;
			if (i >= maxRow) {
				row = i - maxRow;
				nameX = frameX + distance + nameWidth + distance;
			}
			else{
				row = i;
				nameX = frameX + distance;
			}
			nameY = friendY + row * (friendItemH + friendSpaceY);
			frame.drawRoundRect(g, nameX , nameY, nameWidth ,friendItemH);
			
			g.setClip(nameX , nameY, nameWidth,friendItemH);
			g.setColor(0xffffff);
			int dx=0;
			if (font.stringWidth(info.name) < nameWidth - 4) {
				dx = (nameWidth - 4 - font.stringWidth(info.name)) / 2;
			}
			g.drawString(info.name, nameX+2+dx, nameY+itemFrameH/2, 0);
		}
		g.setClip(0, 0, screenWidth, screenHeight);
	}
	
	private static Vector popQueue=new Vector();
	private static SelectBoxCore boxCore= new SelectBoxCore();;
	private static int friendY,friendItemH;
	private static AndroidSelectBoxListener selectBoxListener;
	private static int friendSpaceY=2,itemFrameH=14;
	
	void setSelectListener(AndroidSelectBoxListener l) {
		selectBoxListener = l;
	}
	
	
	void initUserQueue(Vector messageQueue) {
		boxCore.initUserQueue(messageQueue);
		UserInfo last = boxCore.getTopUserInfo();
		if (last != null) {
			if (selectBoxListener != null) {
				selectBoxListener.focusItemChange(last.id, last.name);
			} else {
				throw new NullPointerException(
						"initSelectBoxByMessageQueue(),selectBoxListener=null");
			}
		}
	}


	void addItem(String name, int id) {
		boxCore.addItem(name, id);
	}
	
	void popFriendScreen(int inputBoxY) {
		popQueue.removeAllElements();
		Vector userQueue = boxCore.getUserInfoQueue();
		for (int i = 0; i < userQueue.size(); i++) {
			UserInfo user = (UserInfo) userQueue.elementAt(i);
			popQueue.addElement(user);
//			Utils.info("------------popFriendScreen---------"+user.name);
		}
		friendItemH = font.getHeight() + itemFrameH;
		frameH = (friendItemH +friendSpaceY)* boxCore.getMaxLength()/2+itemFrameH+ back.getHeight() / 8;
		frameW = back.getWidth();
		frameX = (screenWidth - back.getWidth()) / 2;
		frameY = (screenHeight - frameH) / 2;
//		friendY = frameY + back.getHeight() / 8 + font.getHeight();
		initScreenByInputBoxY(inputBoxY);
		friendY = frameY + back.getHeight() / 8 + itemFrameH/2;
		popupFaceScreen();
	}
	
	boolean hasFriendList(){
//		Utils.info("------------hasFriendList----------size--"+boxCore.getUserInfoQueue().size());
		return boxCore.getUserInfoQueue().size()>0;
	}

	private void keyFire() {
		if (faceListener != null) {
			faceListener.insertFace(faceHandler.getFaceString(focus));
		} else {
			throw new NullPointerException("emotionListener=null");
		}
		hideScreen();
	}

}
