package milk.ui;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.EditField;

public class MilkEdit extends EditField {

	public MilkEdit(long consumeInput) {
		super(consumeInput);
	}

	protected boolean keyDown(int keyCode, int time) {
		boolean result = super.keyDown(keyCode, time);
		System.out.println("editor key event");
		return result;
	}

	protected void paint(Graphics g) {
		super.paint(g);
	}

}
