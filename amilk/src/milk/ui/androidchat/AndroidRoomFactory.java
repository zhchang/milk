package milk.ui.androidchat;

import milk.chat.core.Def;
import milk.ui2.MilkApp;

public class AndroidRoomFactory {

	private static MilkApp factory;

	static void init(MilkApp _app) {
		factory = _app;
	}

	static AndroidRoom getWorldRoom() {
		return new AndroidRoom(factory, Def.CHAT_TYPE_WORLD);
	}

	static AndroidRoom getPrivateRoom() {
		return new AndroidRoom(factory, Def.CHAT_TYPE_PRIVATE);
	}

	static AndroidRoom getFamilyRoom() {
		return new AndroidRoom(factory, Def.CHAT_TYPE_FAMILY);
	}

	static AndroidRoom getSystemRoom() {
		return new AndroidRoom(factory, Def.CHAT_TYPE_SYSTEM);
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
