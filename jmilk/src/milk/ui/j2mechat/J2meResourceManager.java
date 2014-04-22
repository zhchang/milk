package milk.ui.j2mechat;

import milk.implement.Adaptor;
import milk.ui2.RoundRect;

public class J2meResourceManager {
	
	static RoundRect focusRect;
	static RoundRect unfocusRect;
	static RoundRect btnUnFocus;
	static RoundRect menuFrame;
	public static RoundRect inputFrame;
	public static RoundRect inputFrameFocus;
	
	static J2meTab roomTab, roomTabFocus;
	
	static void init() {
		roomTab = new J2meTab("chat-tabroom", 17);
		roomTabFocus = new J2meTab("chat-tabroomfocus", 17);
	}

    static void loadResource() {
		if (focusRect == null) {
			focusRect = Adaptor.uiFactory.createRoundRect("light-gray", 5,
					0xe97e00);
			unfocusRect = Adaptor.uiFactory.createRoundRect("tab-gray", 5,
					0xca974f);
			menuFrame = Adaptor.uiFactory.createRoundRect("chat-popmenuback",
					13, 0x35250e);
			btnUnFocus = Adaptor.uiFactory.createRoundRect("gray", 5, 0x844d0b);

			inputFrame = Adaptor.uiFactory.createRoundRect("inputbox", 5,
					0xffeddb);
			inputFrameFocus = Adaptor.uiFactory.createRoundRect("inputbox1", 5,
					0xfffea3);
	
		}
	}

	static void exit() {
		focusRect = null;
		unfocusRect = null;
		menuFrame = null;
		btnUnFocus = null;
		inputFrame = null;
		inputFrameFocus = null;
		roomTab = null;
		roomTabFocus = null;
	}


}
