package milk.ui.j2mechat;

import milk.chat.core.Def;
import milk.ui2.MilkApp;

public class J2meRoomFactory {

	private static MilkApp factory;

	static void init(MilkApp _app) {
		factory = _app;
	}

	static J2meRoom getWorldRoom() {
		return new J2meRoom(factory, Def.CHAT_TYPE_WORLD);
	}

	static J2meRoom getPrivateRoom() {
		return new J2meRoom(factory, Def.CHAT_TYPE_PRIVATE);
	}

	static J2meRoom getFamilyRoom() {
		return new J2meRoom(factory, Def.CHAT_TYPE_FAMILY);
	}

	static J2meRoom getSystemRoom() {
		return new J2meRoom(factory, Def.CHAT_TYPE_SYSTEM);
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
