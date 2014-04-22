package milk.ui.j2mechat;

import milk.chat.core.Def;


import milk.chat.core.Message;
import milk.ui2.MilkApp;

public class J2meRoomManager {

	private static J2meRoom systemRoom;
	private static J2meRoom privateRoom;
	private static J2meRoom familyRoom;
	private static J2meRoom worldRoom;
	private static byte chatType=0;
	private static MilkApp factory;

	public static void init(MilkApp f) {
		factory = f;
		J2meRoomFactory.init(factory);
		privateRoom = J2meRoomFactory.getPrivateRoom();
		worldRoom = J2meRoomFactory.getWorldRoom();
		familyRoom = J2meRoomFactory.getFamilyRoom();
		systemRoom = J2meRoomFactory.getSystemRoom();
	}

	public static J2meRoom getFocusRoom() {
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

	public static J2meRoom getWorldRoom() {
		return worldRoom;
	}
	
	public static J2meRoom getPrivateRoom() {
		return privateRoom;
	}
	
	public static J2meRoom getFamilyRoom() {
		return familyRoom;
	}
	
	public static J2meRoom getSystemRoom() {
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
		J2meRoomFactory.verifyRoomType(type);
		chatType=type;
		getFocusRoom().showNotify();
	}

}
