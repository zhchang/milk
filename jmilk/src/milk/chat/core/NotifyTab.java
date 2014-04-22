package milk.chat.core;

import milk.ui2.MilkApp;


import milk.ui2.MilkGraphics;
import milk.ui2.RoundRect;

public class NotifyTab {

	private static final int FLIP_OPEN = 1;
	private static final int FLIP_CLOSE = 2;
	private static final int FLIP_COUNT = 5;
	private int flipType;
	private int flipIndex;
	private int rectH;
	private int rectX, rectY, rectW;
	private int width, height;
	private Message message;
	private MilkApp factory;
	private RoundRect rect, rectFocus;
	
	private int frameSize=0;
	
	public NotifyTab(MilkApp factory) {
		this.factory = factory;
		width = this.factory.getCanvasWidth();
		height = this.factory.getCanvasHeight();
	}
	
	public final void setFrameSize(int size){
		frameSize=size;
	}
	
	public final void initRect(RoundRect _roundRect,RoundRect _roundRectFocus){
		rect=_roundRect;
		rectFocus=_roundRectFocus;
	}

	final void hideNotifyTab() {
		showMessage(null);
	}
	
	final void hideNotifyTabImmediately() {
		message=null;
		flipType=0;
		flipIndex=0;
	}

	final void init(int x, int y, int w, int h) {
		this.rectX = x;
		this.rectY = y;
		this.rectW = w;
		this.rectH = h;
	}

	final void showMessage(Message msg) {
		if (msg == null && message != null) {
			flipType = FLIP_CLOSE;
			flipIndex = FLIP_COUNT;

		} else if (message == null && msg != null) {
			flipType = FLIP_OPEN;
			flipIndex = FLIP_COUNT;

		}
		this.message = msg;
	}
	

	final boolean isTouch(int x, int y) {
		return (message != null && Utils.pointInRect(x, y, rectX, rectY - 2,
				rectW, rectH + 4));
	}
	
	final Message getMessage(){
		return message;
	}

	final boolean isShown() {
		return message != null;
	}
	
	int getLeftX(){
		return rectX;
	}
	
	int getTopY(){
		return rectY;
	}
	
	int getRectWidth(){
		int w = 0;
		if (flipType == FLIP_CLOSE) {
			w = rectW * flipIndex / FLIP_COUNT;
//			h = rectH * flipIndex / FLIP_COUNT;
			
		} else if (flipType == FLIP_OPEN) {
			w = rectW * (FLIP_COUNT - flipIndex) / FLIP_COUNT;
//			h = rectH * (FLIP_COUNT - flipIndex) / FLIP_COUNT;	
		}
		else{
			return rectW;
		}
		return w;
	}
	
	int getRectHeight(){
		int h = 0;
		if (flipType == FLIP_CLOSE) {
			// w = rectW * flipIndex / FLIP_COUNT;
			h = rectH * flipIndex / FLIP_COUNT;

		} else if (flipType == FLIP_OPEN) {
			// w = rectW * (FLIP_COUNT - flipIndex) / FLIP_COUNT;
			h = rectH * (FLIP_COUNT - flipIndex) / FLIP_COUNT;

		} else {
			return rectH;
		}
		return h;
	}

	public void drawTopNotifyTab(MilkGraphics g,boolean isLanguageAr) {
		if (message == null && flipType == 0)
			return;
		RoundRect roundrect;
		if ((System.currentTimeMillis() / 400) % 2 > 0)
			roundrect = rect;
		else {
			roundrect = rectFocus;
		}
		// rectH = g.getFont().getHeight()*2;
		int x = 0, y = 0, w = 0, h = 0;
		if (flipType == FLIP_CLOSE) {
			w = rectW * flipIndex / FLIP_COUNT;
			h = rectH //* flipIndex / FLIP_COUNT
					;
			y = rectY + (rectH - h) / 2;
			x = rectX //+ (rectW - w) / 2
					;
		} else if (flipType == FLIP_OPEN) {
			w = rectW * (FLIP_COUNT - flipIndex) / FLIP_COUNT;
			h = rectH //* (FLIP_COUNT - flipIndex) / FLIP_COUNT
					;
			y = rectY + (rectH - h) / 2;
			x = rectX //+ (rectW - w) / 2
					;
		}

		if (flipType == FLIP_CLOSE || flipType == FLIP_OPEN) {
			roundrect.drawRoundRect(g, x, y, w, h);
		}

		if (flipType == 0 && message != null) {
			roundrect.drawRoundRect(g, rectX, rectY, rectW, rectH);
		} else {
			g.setClip(x, y, w, h);
		}
		if (message != null) {
			int lineH = g.getFont().getHeight();
			message.initTopNotifyBodyString(g.getFont(), rectW - 6);
			message.drawTopNotifyTabTitle(g, rectX + 5, rectY+frameSize, rectW-3, rectH,isLanguageAr);
			message.drawTopNotifyTabBody(g, rectX + 5, rectY + lineH+frameSize, rectW-5,
					lineH,isLanguageAr);
		}
		g.setClip(0, 0, width, height);

	}
	
	void update(){
		if (flipType == FLIP_CLOSE || flipType == FLIP_OPEN) {
			if (flipIndex > 0)
				flipIndex--;
			else {
				if (flipType == FLIP_CLOSE) {
					message = null;
				}
				flipType = 0;
				flipIndex=0;
			}
		}
		else{
			flipType = 0;
		}
	}

	void drawNormalNotifyTab(MilkGraphics g,boolean isLanguageAr) {
		if (message == null && flipType == 0)
			return;
		rectH = g.getFont().getHeight();
		int x = 0, y = 0, w = 0, h = 0;
		if (flipType == FLIP_CLOSE) {
			w = rectW * flipIndex / FLIP_COUNT;
			h = rectH * flipIndex / FLIP_COUNT;
			y = rectY + (rectH - h) / 2;
			x = rectX + (rectW - w) / 2;
		} else if (flipType == FLIP_OPEN) {
			w = rectW * (FLIP_COUNT - flipIndex) / FLIP_COUNT;
			h = rectH * (FLIP_COUNT - flipIndex) / FLIP_COUNT;
			y = rectY + (rectH - h) / 2;
			x = rectX + (rectW - w) / 2;
		}
		if (flipType == FLIP_CLOSE || flipType == FLIP_OPEN) {
			rect.drawRoundRect(g, x, y, w, h);
		}

		if (flipType == 0 && message != null) {
			rect.drawRoundRect(g, rectX, rectY, rectW, rectH);
		} else {
			g.setClip(x, y, w, h);
		}
		if (message != null) {
			message.initNormalNotifyBodyString();
			message.drawNormalNotifyTabBody(g, rectX + 3, rectY, rectW - 3,
					rectH,isLanguageAr);
		}
		g.setClip(0, 0, width, height);
	}
}
