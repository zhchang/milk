package milk.ui;

import milk.ui2.MilkDisplayable;
import net.rim.device.api.ui.Screen;

public class MilkDisplayableImpl implements MilkDisplayable {

	Screen screen;

	MilkDisplayableImpl(Screen screen) {
		this.screen = screen;
	}

}
