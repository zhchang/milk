package milk.ui.androidchat;
import milk.implement.Adaptor;


import milk.ui.MilkImageImpl;
import milk.ui.R;
import milk.ui2.MilkImage;
import milk.ui2.RoundRect;

public class AndroidResourceManager {
	
	static RoundRect focusRect;
	static RoundRect btnUnFocus;
	static RoundRect menuFrame;
	static RoundRect inputFrame;
	static MilkImage send,sendFocus;
	static MilkImage iconBg,iconBgFocus;
	static MilkImage face,topMsg,selectFriend;
	
	static MilkImage tab,tabFocus;
	static MilkImage chat[],chatFocus[];
	
	static AndroidBubble9Patch bubbleMe,bubbleOther;
	static MilkImage me,meArraw,other,otherArraw;

	static void initChatImage() {
		if(tab == null){
		tab = MilkImageImpl.createImage(R.drawable.tabbg);
		tabFocus = MilkImageImpl.createImage(R.drawable.tabbgfocus);
		send = MilkImageImpl.createImage(R.drawable.sendbuttonbg);
		sendFocus = MilkImageImpl.createImage(R.drawable.sendbuttonbgfocus);
		iconBg = MilkImageImpl.createImage(R.drawable.iconbg);
		iconBgFocus = MilkImageImpl.createImage(R.drawable.iconbgfocus);
		face = MilkImageImpl.createImage(R.drawable.smiley);
		topMsg = MilkImageImpl.createImage(R.drawable.topmessagecolour);
		selectFriend = MilkImageImpl.createImage(R.drawable.selectfriend);
		chat = new MilkImage[4];
		chatFocus = new MilkImage[4];
		chat[0] = MilkImageImpl.createImage(R.drawable.world);
		chatFocus[0] = MilkImageImpl.createImage(R.drawable.worldfocus);
		chat[1] = MilkImageImpl.createImage(R.drawable.privatechat);
		chatFocus[1] = MilkImageImpl.createImage(R.drawable.privatefocus);
		chat[2] = MilkImageImpl.createImage(R.drawable.tribe);
		chatFocus[2] = MilkImageImpl.createImage(R.drawable.tribefocus);
		chat[3] = MilkImageImpl.createImage(R.drawable.system);
		chatFocus[3] = MilkImageImpl.createImage(R.drawable.systemfocus);
		me = MilkImageImpl.createImage(R.drawable.bubbleme);
		meArraw = MilkImageImpl.createImage(R.drawable.bubblemeconer);
		other = MilkImageImpl.createImage(R.drawable.bubbleother);
		otherArraw = MilkImageImpl.createImage(R.drawable.bubbleotherconer);
		}
	}
	
	static void resizeChatImage(double resizeFactor) {
		resize(send,resizeFactor);
		resize(sendFocus,resizeFactor);
		resize(iconBg,resizeFactor);
		resize(iconBgFocus,resizeFactor);
		resize(face,resizeFactor);
		resize(topMsg,resizeFactor);
		resize(selectFriend,resizeFactor);
		resize(me,resizeFactor);
		resize(meArraw,resizeFactor);
		resize(other,resizeFactor);
		resize(otherArraw,resizeFactor);
		for (int i = 0; i < 4; i++) {
			resize(chat[i], resizeFactor);
			resize(chatFocus[i], resizeFactor);
		}
	}
	
	private static void resize(MilkImage image,double resizeFactor){
		int newWidth=(int)((double)image.getWidth()*resizeFactor);
		int newHeight=(int)((double)image.getHeight()*resizeFactor);
		MilkImageImpl.resizeImage(image, newWidth, newHeight);
	}

    static void loadResource() {
		if (focusRect == null) {
			focusRect = Adaptor.uiFactory.createRoundRect("light-gray", 5,
					0xe97e00);
			menuFrame = Adaptor.uiFactory.createRoundRect("chat-popmenuback",
					13, 0x35250e);
			btnUnFocus = Adaptor.uiFactory.createRoundRect("gray", 5, 0x844d0b);

			inputFrame = Adaptor.uiFactory.createRoundRect("inputbox", 5,
					0xffeddb);
			int coner = 11 * me.getWidth() / 30;
			bubbleMe = new AndroidBubble9Patch(me, coner, 0x9ac93f);
			bubbleMe.setArraw(meArraw, false);

			bubbleOther = new AndroidBubble9Patch(other, coner, 0xd0d6d8);
			bubbleOther.setArraw(otherArraw, true);
		}
	}

	static void exit() {
		focusRect = null;
		menuFrame = null;
		btnUnFocus = null;
		inputFrame = null;
		tab = null;
		tabFocus = null;
		bubbleMe= null;
		bubbleOther= null;
		me= null;
		meArraw=null;
		other=null;
		otherArraw=null;
	}


}
