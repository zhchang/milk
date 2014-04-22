package milk.ui;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

public class MilkManager extends Manager {

	public static MilkManager instance = new MilkManager(0);

	MilkManager(long style) {
		super(style);
	}

	public void doLayoutChild(Field child, int x, int y, int width, int height) {
		this.layoutChild(child, width, height);
		setPositionChild(child, x, y);

	}

	public void doPaintChild(Graphics g, Field child) {
		paintChild(g, child);
	}

	protected void sublayout(int width, int height) {
		// TODO Auto-generated method stub

	}
}
