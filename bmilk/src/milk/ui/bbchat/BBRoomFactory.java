package milk.ui.bbchat;

import milk.chat.core.Def;
import milk.ui2.MilkApp;

public class BBRoomFactory {

	private static MilkApp factory;

	static void init(MilkApp _app) {
		factory = _app;
	}

	static BBRoom getWorldRoom() {
		return new BBRoom(factory, Def.CHAT_TYPE_WORLD);
	}

	static BBRoom getPrivateRoom() {
		return new BBRoom(factory, Def.CHAT_TYPE_PRIVATE);
	}

	static BBRoom getFamilyRoom() {
		return new BBRoom(factory, Def.CHAT_TYPE_FAMILY);
	}

	static BBRoom getSystemRoom() {
		return new BBRoom(factory, Def.CHAT_TYPE_SYSTEM);
	}

	static void verifyRoomType(byte chatType) {
		switch (chatType) {
		case Def.CHAT_TYPE_PRIVATE:
		case Def.CHAT_TYPE_FAMILY:
		case Def.CHAT_TYPE_WORLD:
		case Def.CHAT_TYPE_SYSTEM:
			return;
		}
		throw new IllegalArgumentException("verifyRoomType chatType:"
				+ chatType);
	}
}
