package milk.ui.j2mechat;


import milk.chat.core.Def;

import milk.ui.MilkCustomItem;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;


public class J2meButtonManager {

	public static int btnWidth = 120;
	public static int btnHeight = 20;

	static MilkFont font;

	private J2meButton focus;
	private J2meButton lastFocus;

	private J2meButton normalSend;
	private J2meButton topSend;
	private J2meButton addFace;
	private J2meButton cancel;

	private J2meButtonListener buttonListener;
	private MilkApp factory;

	
	public J2meButtonManager(MilkApp f, J2meButtonListener input) {
		buttonListener = input;
		font = J2meHallScreen.font;
		factory = f;
		btnWidth = factory.getCanvasWidth() - 10;
		btnHeight = font.getHeight() + 8;
		normalSend = new J2meButton(Def.cmdNormalSend, this);
		normalSend.setLayout(MilkCustomItem.LAYOUT_CENTER);
		topSend = new J2meButton(Def.cmdTopSend, this);
		topSend.setLayout(MilkCustomItem.LAYOUT_CENTER);
		addFace = new J2meButton(Def.cmdAddEmotion, this);
		addFace.setLayout(MilkCustomItem.LAYOUT_CENTER);
		cancel = new J2meButton(Def.cmdCancel, this);
		cancel.setLayout(MilkCustomItem.LAYOUT_CENTER);
		J2meButton.setButtonListener(buttonListener);
	}

	private byte chatType;
	private J2meButton buttonList[]=null;
	public J2meButton[] getInputButton(byte type) {
		chatType=type;
		buttonList=null;
//		if (texLength == 0) {
//			form.append(addFace);
//			form.append(cancel);
//		} else if (texLength + 4 > NativeInput.MAX_INPUT_CONTENT_LENGTH) {
//			if (type == Def.CHAT_TYPE_WORLD) {
//				form.append(normalSend);
//				form.append(topSend);
//				form.append(cancel);
//			} else {
//				form.append(normalSend);
//				form.append(cancel);
//			}
//		} else {
		
		if (type == Def.CHAT_TYPE_WORLD_TOP) {
			buttonList= new J2meButton[]{
					topSend,addFace,cancel
			};
		}
		else if (type == Def.CHAT_TYPE_WORLD) {
			buttonList= new J2meButton[]{
						normalSend,topSend,addFace,cancel
				};
			} else {
				buttonList= new J2meButton[]{
						normalSend,addFace,cancel
				};
			}
//		}
		return buttonList;
	}
	

	public J2meButton getFocusButton() {
		return focus;
	}

	J2meButton getLastFocus() {
		return lastFocus;
	}

	public void setFocusButton(J2meButton newFocus) {
		focus = newFocus;
	}

	public void traverseOut(J2meButton lastFocus) {
		if (lastFocus == normalSend || lastFocus == cancel) {
			if (lastFocus == focus) {
				focus = null;
				lastFocus = null;
			}
		}
		else
		if (chatType == Def.CHAT_TYPE_WORLD_TOP) {
			if (lastFocus == topSend) {
				if (lastFocus == focus) {
					focus = null;
					lastFocus = null;
				}
			}
		}
	}
	
	public void focusButtonExecute() {
		if (focus != null) {
			focus.buttonClick();
		}
	}

	public int getGameAction(int keyCode) {
		return factory.getGameAction(keyCode);
	}

}
