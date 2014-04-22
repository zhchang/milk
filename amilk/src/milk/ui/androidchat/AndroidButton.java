package milk.ui.androidchat;

import milk.chat.core.Utils;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class AndroidButton {

	private boolean focus;
	private int x,y,w,h;
	private MilkImage bg,bgFocus,icon;
	private String name;
	
	AndroidButton(MilkImage bgImage, MilkImage bgFocusImage, MilkImage iconImage) {
		bg = bgImage;
		bgFocus = bgFocusImage;
		icon = iconImage;
		w = bg.getWidth();
		h = bg.getHeight();
	}
	
	AndroidButton(MilkImage bgName, MilkImage bgFocusName, String iconName) {
		bg = bgName;
		bgFocus = bgFocusName;
		this.name = iconName;
		w = bg.getWidth();
		h = bg.getHeight();
	}
	
	int getWidth(){
		return bg.getWidth();
	}
	
	void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	void draw(MilkGraphics g) {
		MilkImage back;
		if (focus) {
			back = bgFocus;
		} else {
			back = bg;
		}
		g.drawImage(back, x, y, 0);
		if (icon != null) {
			g.drawImage(icon, x + (back.getWidth() - icon.getWidth()) / 2, y
					+ (back.getHeight() - icon.getHeight()) / 2, 0);
		} else if (name != null) {
			g.setColor(0xffffff);
			g.drawString(name,
					x + (back.getWidth() - g.getFont().stringWidth(name))/ 2, 
					y+ (back.getHeight() - g.getFont().getHeight()) / 2,
					0);
		}
	}
	
	boolean isTouched(int x,int y){
		return Utils.pointInRect(x, y, this.x, this.y, w, h);
	}
	
}
