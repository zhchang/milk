package milk.ui;

import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;

import milk.ui2.MilkGraphics;

public abstract class MilkCustomItem extends CustomItem {

	static MilkGraphicsImpl mg = new MilkGraphicsImpl();

	public static final int LAYOUT_CENTER = Item.LAYOUT_CENTER;

	protected MilkCustomItem(String label) {
		super(label);
	}

	protected void paint(Graphics g, int w, int h) {
		mg.setG(g);
		paint(mg, w, h);
	}

	abstract protected void paint(MilkGraphics g, int w, int h);

	public void setLayout(int layout) {

	}

}
