package milk.ui.bbchat;


import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;

public class BBBackGround {
	
	private static final int hallBackColor = 0xd5af78;
	private int screenWidth;
	private int screenHeight;
	
	BBBackGround(int screenWidth,int screenHeight){
		this.screenWidth=screenWidth;
		this.screenHeight=screenHeight;
	}
	
	public void drawBackGround(MilkGraphics g,String title) {
		MilkFont font=g.getFont();
		if (screenHeight > screenWidth) {
			int TitleBarH=font.getHeight();
			g.setClip(0, 0, screenWidth, screenHeight);
			g.setColor(hallBackColor);
			g.fillRect(0, 0, screenWidth, screenHeight);
			// hall title
			g.setClip(0, 0, screenWidth, TitleBarH);
			g.setColor(0x382100);
			g.fillRect(0, 0, screenWidth, TitleBarH);
		
			g.setFont(font);
			g.setColor(0xffffff);
			g.drawString(title, (screenWidth - font.stringWidth(title)) / 2,
					0, 0);
			g.setClip(0, 0, screenWidth, screenHeight);
		} else {
			g.setClip(0, 0, screenWidth, screenHeight);
			g.setColor(hallBackColor);
			g.fillRect(0, 0, screenWidth, screenHeight);
			g.setFont(font);
		}
	}

}
