package milk.ui.bbchat;

import milk.chat.core.Def;


import milk.chat.core.Message;
import milk.ui2.MilkApp;

public class BBRoomManager {

	private static BBRoom systemRoom;
	private static BBRoom privateRoom;
	private static BBRoom familyRoom;
	private static BBRoom worldRoom;
	private static byte chatType=0;
	private static MilkApp factory;

	public static void init(MilkApp f) {
		factory = f;
		BBRoomFactory.init(factory);
		privateRoom = BBRoomFactory.getPrivateRoom();
		worldRoom = BBRoomFactory.getWorldRoom();
		familyRoom = BBRoomFactory.getFamilyRoom();
		systemRoom = BBRoomFactory.getSystemRoom();
	}

	public static BBRoom getFocusRoom() {
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

	public static BBRoom getWorldRoom() {
		return worldRoom;
	}
	
	public static BBRoom getPrivateRoom() {
		return privateRoom;
	}
	
	public static BBRoom getFamilyRoom() {
		return familyRoom;
	}
	
	public static BBRoom getSystemRoom() {
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
		BBRoomFactory.verifyRoomType(type);
		chatType=type;
		getFocusRoom().showNotify();
	}

}
