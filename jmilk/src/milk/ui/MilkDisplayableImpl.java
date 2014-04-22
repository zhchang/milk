package milk.ui;

import javax.microedition.lcdui.Displayable;

import milk.ui2.MilkDisplayable;

public class MilkDisplayableImpl implements MilkDisplayable {

	Displayable displayable;

	public MilkDisplayableImpl(Displayable displayable) {
		this.displayable = displayable;
	}

}
