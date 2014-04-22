package milk.ui.androidchat;

import milk.chat.core.Utils;

import milk.ui.MilkImageImpl;
import milk.ui.MilkSpriteImpl;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;
import milk.ui2.RoundRect;

public class AndroidBubble9Patch implements RoundRect{

	private final int fillColor;
	private final int frameTotalSize;
	private final MilkSprite lineUp,lineDown,lefLine,rightLine;
	private final MilkSprite ltCorner,lrCorner,lbCorner,rbCorner;
	private MilkSprite inside;
	private MilkImage arraw;
	private boolean isLeft;
	private static int conerSize,screenWidth=240;
	
    static void initScreenSize(int width){
    	screenWidth=width;
    }
	
	public AndroidBubble9Patch(MilkImage image, int frameSize, int fillColor) {
		frameTotalSize = frameSize;
		conerSize=frameSize;
		int size = frameSize;
		this.fillColor = fillColor;
		ltCorner = new MilkSpriteImpl(MilkImageImpl.createImage(image, 0, 0, size, size,
				MilkSpriteImpl.TRANS_NONE));
		lrCorner=new MilkSpriteImpl(MilkImageImpl.createImage(image, image.getWidth()-size, 0, size, size,
				MilkSpriteImpl.TRANS_NONE));
		lbCorner=new MilkSpriteImpl(MilkImageImpl.createImage(image, 0, image.getHeight()-size, size, size,
				MilkSpriteImpl.TRANS_NONE));
		rbCorner=new MilkSpriteImpl(MilkImageImpl.createImage(image, image.getWidth()-size, image.getHeight()-size, size, size,
				MilkSpriteImpl.TRANS_NONE));
		
		lineUp = new MilkSpriteImpl(MilkImageImpl.createImage(image, size, 0, image.getWidth()
				- size * 2, size, MilkSprite.TRANS_NONE));
		lineDown=new MilkSpriteImpl(MilkImageImpl.createImage(image, size, image.getHeight()-size, 
				image.getWidth()
				- size * 2, size, MilkSprite.TRANS_NONE));
		lefLine=new MilkSpriteImpl(MilkImageImpl.createImage(image, 0, frameSize, 
				size,image.getHeight()-2* size, MilkSprite.TRANS_NONE));
		rightLine=new MilkSpriteImpl(MilkImageImpl.createImage(image, image.getWidth()-size, frameSize, 
				size,image.getHeight()-2* size, MilkSprite.TRANS_NONE));
	
		if (fillColor == 0) {
			inside = new MilkSpriteImpl(MilkImageImpl.createImage(image, size, size, size,
					size, MilkSprite.TRANS_NONE));
		}
		image = null;
	}
	
	static int getArcSize(){
		return conerSize;
	}
	
	int getFrameSize(){
		return frameTotalSize;
	}
	
	void setArraw(MilkImage arraw,boolean isLeft){
		this.arraw=arraw;
		this.isLeft=isLeft;
	}
	
	public AndroidBubble9Patch(String path, int frameSize, int fillColor) {
		this(Utils.getImage(path),frameSize,fillColor);
	}

	public void drawRoundRect(MilkGraphics g, int x, int y, int w, int h) {
		int oldX=g.getClipX();
		int oldY=g.getClipY();
		int oldWidth = g.getClipWidth();
		int oldHeight = g.getClipHeight();
		g.clipRect(x + frameTotalSize, y, w - frameTotalSize * 2, h);
		int oriValue = x;
		while (x < w + oriValue) {// up,down border
			lineUp.setTransform(MilkSprite.TRANS_NONE);
			lineUp.setPosition(x + frameTotalSize, y);
			lineUp.paint(g);
			lineDown.setTransform(MilkSprite.TRANS_NONE);
			lineDown.setPosition(x + frameTotalSize, y + h - frameTotalSize);
			lineDown.paint(g);
			x += lineUp.getWidth();
		}
		x = oriValue;
		g.setClip(oldX, oldY, oldWidth, oldHeight);
		oriValue = y;
		g.clipRect(x, y + frameTotalSize, w, h - frameTotalSize * 2);
		while (y < h + oriValue) {// left,right border
			lefLine.setTransform(MilkSprite.TRANS_NONE);
			lefLine.setPosition(x, y + frameTotalSize);
			lefLine.paint(g);
			rightLine.setTransform(MilkSprite.TRANS_NONE);
			rightLine.setPosition(x + w - frameTotalSize, y + frameTotalSize);
			rightLine.paint(g);
			y += lefLine.getHeight();
		}
		y = oriValue;
		g.setClip(oldX, oldY, oldWidth, oldHeight);
		g.clipRect(x, y, w, h);

		// 4 corner
		ltCorner.setTransform(MilkSprite.TRANS_NONE);
		ltCorner.setPosition(x, y);
		ltCorner.paint(g);

		lrCorner.setTransform(MilkSprite.TRANS_NONE);
		lrCorner.setPosition(x + w - ltCorner.getWidth(), y);
		lrCorner.paint(g);

		rbCorner.setTransform(MilkSprite.TRANS_NONE);
		rbCorner.setPosition(x + w - ltCorner.getWidth(),
				y + h - ltCorner.getHeight());
		rbCorner.paint(g);

		lbCorner.setTransform(MilkSprite.TRANS_NONE);
		lbCorner.setPosition(x, y + h - ltCorner.getHeight());
		lbCorner.paint(g);
		if (inside != null) {
			fillInsideRect(g, x + frameTotalSize, y + frameTotalSize, w - 2
					* frameTotalSize, h - 2 * frameTotalSize);
		} else {
			g.setColor(fillColor);
			g.fillRect(x + frameTotalSize, y + frameTotalSize, w - 2
					* frameTotalSize, h - 2 * frameTotalSize);
		}
		g.setClip(oldX, oldY, oldWidth, oldHeight);
		if(this.arraw!=null){
			int frameX=2;
			if(screenWidth>=540)frameX=6;
			else
			if(screenWidth>=480)frameX=4;
			else if(screenWidth>=320){
				frameX=3;
			}
			if(isLeft){
				g.drawImage(arraw, x -arraw.getWidth()+frameX, y+(h-arraw.getHeight())/2, 0);
			}
			else{
				g.drawImage(arraw, x + w-frameX, y+(h-arraw.getHeight())/2, 0);
			}
		}
	}

	private void fillInsideRect(MilkGraphics g, int x, int y, int w, int h) {
		int size = inside.getWidth();
		
		g.clipRect(x, y, w, h);
		for (int i = 0; i * size < w; i++) {
			for (int j = 0; j * size < h; j++) {
				inside.setPosition(x + i * size, y + j * size);
				inside.paint(g);
			}
		}
	}

	@Override
	public void drawHorizonLine(MilkGraphics g, int x, int y, int w) {
		// TODO Auto-generated method stub
		
	}

}

