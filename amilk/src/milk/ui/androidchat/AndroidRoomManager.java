package milk.ui.androidchat;

import milk.chat.core.Def;


import milk.chat.core.Message;
import milk.ui2.MilkApp;

public class AndroidRoomManager {

	private static AndroidRoom systemRoom;
	private static AndroidRoom privateRoom;
	private static AndroidRoom familyRoom;
	private static AndroidRoom worldRoom;
	private static byte chatType=0;
	private static MilkApp factory;

	public static void init(MilkApp f) {
		factory = f;
		AndroidRoomFactory.init(factory);
		privateRoom = AndroidRoomFactory.getPrivateRoom();
		worldRoom = AndroidRoomFactory.getWorldRoom();
		familyRoom = AndroidRoomFactory.getFamilyRoom();
		systemRoom = AndroidRoomFactory.getSystemRoom();
	}

	public static AndroidRoom getFocusRoom() {
		switch (chatType) {
		case Def.CHAT_TYPE_PRIVATE:
			return privateRoom;
		case Def.CHAT_TYPE_FAMILY:
			return familyRoom;
		case Def.CHAT_TYPE_WORLD:
			return worldRoom;
		case Def.CHAT_TYPE_SYSTEM:
			return systemRoom;
		}
		throw new IllegalArgumentException("RoomManager getFocusRoom chatType:"
				+ chatType);
	}

	static void exit() {
		systemRoom = null;
		privateRoom = null;
		familyRoom = null;
		worldRoom = null;
	}

	public static AndroidRoom getWorldRoom() {
		return worldRoom;
	}
	
	public static AndroidRoom getPrivateRoom() {
		return privateRoom;
	}
	
	public static AndroidRoom getFamilyRoom() {
		return familyRoom;
	}
	
	public static AndroidRoom getSystemRoom() {
		return systemRoom;
	}

	public static void initRoom(int roomX, int roomY, int roomW, int roomH,int lineH) {
		getWorldRoom().initRoom(roomX, roomY, roomW, roomH, lineH);
		getPrivateRoom().initRoom(roomX, roomY, roomW, roomH, lineH);
		getFamilyRoom().initRoom(roomX, roomY, roomW, roomH, lineH);
		getSystemRoom().initRoom(roomX, roomY, roomW, roomH, lineH);
	}

	public static Message removeMessageById(String msgId) {
		Message remove = getFamilyRoom().removeMessage(msgId);
		if (remove == null)
			remove = getWorldRoom().removeMessage(msgId);
		if (remove == null)
			remove = getPrivateRoom().removeMessage(msgId);
		return remove;
	}

	public static void switchChatRoom(byte type) {
		if (chatType != 0)
			getFocusRoom().hideNotify();
		AndroidRoomFactory.verifyRoomType(type);
		chatType=type;
		getFocusRoom().showNotify();
	}

}
