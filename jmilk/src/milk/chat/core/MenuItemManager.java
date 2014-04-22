package milk.chat.core;


import milk.chat.core.Def;

import milk.chat.core.MenuItem;

public class MenuItemManager {

	private MenuItem worldQueue[];
	private MenuItem normalQueue [];
	
	public MenuItemManager(){
		initMenuItemQueue();
	}
	
	private void initMenuItemQueue() {
		initAllPopMenuItem();
		worldQueue = new MenuItem[] { sendItem, sendTopItem,
				addFaceItem, cancelItem };
		normalQueue = new MenuItem[] { sendItem, addFaceItem,
				cancelItem };
	}
	
    MenuItem[] getInputMenuItems(byte chatType) {
		if (chatType == Def.CHAT_TYPE_WORLD_TOP
				|| chatType == Def.CHAT_TYPE_WORLD) {
			return worldQueue;
		} else {
			return normalQueue;
		}
	}
    
    MenuItem[] getAndroidInputMenuItems(byte chatType) {
		if (chatType == Def.CHAT_TYPE_WORLD_TOP
				|| chatType == Def.CHAT_TYPE_WORLD) {
			return new MenuItem[] { sendItem, sendTopItem, cancelItem };
		} else {
			return new MenuItem[] { sendItem,cancelItem };
		}
	}


	private MenuItem sendItem ;
	private MenuItem sendTopItem ;
	private MenuItem addFaceItem ;
	private MenuItem cancelItem ;
	
	private void initAllPopMenuItem(){
		sendItem = new MenuItem(Def.cmdNormalSend, Def.cmdNormalSend);
		sendTopItem = new MenuItem(Def.cmdTopSend, Def.cmdTopSend);
		addFaceItem = new MenuItem(Def.cmdAddEmotion, Def.cmdAddEmotion);
		cancelItem = new MenuItem(Def.cmdCancel, Def.cmdCancel);
	}
	
   void loadL10nString(){
		sendItem.setItem(Def.cmdNormalSend, Def.cmdNormalSend);
		sendTopItem.setItem(Def.cmdTopSend, Def.cmdTopSend);
		addFaceItem.setItem(Def.cmdAddEmotion, Def.cmdAddEmotion);
		cancelItem.setItem(Def.cmdCancel, Def.cmdCancel);
	}
	
}
