package milk.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import milk.chat.core.Def;
import milk.implement.Adaptor;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;

public abstract class InMessage {

	private static final int ID_LoginMessage = 4;
	private static final int ID_HeartMessage = 1;
	private static final int ID_CloseMessage = 5;

	public String msgId;

	abstract void readFromStream(MilkInputStream dis) throws IOException;

	public static InReportMessage parseInReportPacket(MilkInputStream dis)
			throws IOException {
		InReportMessage msg = new InReportMessage();
		msg.readFromStream(dis);
		return msg;
	}

	public static InRawRequestMessage parseInRawRequestPacket(
			MilkInputStream dis, String msgId) throws IOException {
		InRawRequestMessage msg = new InRawRequestMessage(msgId);
		msg.readFromStream(dis);
		return msg;
	}

	public static InMessage parseMServerPacket(MilkInputStream dis,
			String msgId, int action) throws IOException {
		InMessage result = null;
		switch (action) {
		case 0: {

			Adaptor.infor("InMonetIdMessage received.");
			InMonetIdMessage monet = new InMonetIdMessage();
			monet.readFromStream(dis);
			result = monet;
			break;
		}
		case 1: {

			Adaptor.infor("InManifestMessage received.");
			InManifestMessage manifest = new InManifestMessage();
			manifest.readFromStream(dis);
			result = manifest;
			break;
		}
		case 2: {

			Adaptor.infor("InOneResourceMessage received.");
			InOneResourceMessage oneResource = new InOneResourceMessage(msgId);
			oneResource.readFromStream(dis);
			result = oneResource;
			break;
		}
		case 3: {

			Adaptor.infor("InDataEventMessage received");
			InDataEventMessage dataEvent = new InDataEventMessage();
			dataEvent.readFromStream(dis);
			result = dataEvent;
			break;
		}
		case 4: {

			InMultiResourceMessage multiResource = new InMultiResourceMessage();
			multiResource.readFromStream(dis);
			Adaptor.infor("InMultiResourceMessage received num:"
					+ multiResource.resCount);
			result = multiResource;
			break;
		}
		case 5: {
			InIapItemMessage iapMessage = new InIapItemMessage();
			iapMessage.readFromStream(dis);
			Adaptor.debug("InIapItemMessage received count:" + iapMessage.count);
			result = iapMessage;
			break;
		}
		case 6: {
			InIapResultMessage iapResult = new InIapResultMessage();
			iapResult.readFromStream(dis);
			Adaptor.debug("InIapResultMessage received reslut:"
					+ iapResult.result);
			result = iapResult;
			break;
		}
		}
		if (result != null) {
			result.msgId = msgId;
		}
		return result;
	}

	public static InMessage parse(MilkInputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.readFully(bytes);

		int len = new MilkInputStream(new ByteArrayInputStream(bytes))
				.readInt();

		bytes = new byte[len];
		is.readFully(bytes);

		ByteArrayInputStream bis1 = new ByteArrayInputStream(bytes);
		MilkInputStream dis1 = new MilkInputStream(bis1);

		int type = dis1.readInt();
		int id = dis1.readInt();
		dis1.readInt();
		byte[] payload = new byte[len - 12];

		dis1.readFully(payload);

		MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(
				payload));

		if (type == 0) {
			switch (id) {
			case ID_LoginMessage:
				Adaptor.infor("InLoginMessage received.");
				InLoginMessage login = new InLoginMessage();
				login.readFromStream(dis);
				return login;
			case ID_HeartMessage:// heart msg,do nothing.
				Adaptor.infor("InHeartMessage from server received.");
				return null;
			case ID_CloseMessage: {
				int reason = dis.readInt();
				throw new KickedException(reason);
			}
			default: {
				Adaptor.debug("system message : " + id);
				break;
			}
			}

		} else if (type == Adaptor.getInstance().chatServiceId) {
			String msgId = readVarChar(dis);
			byte action = dis.readByte();
			Adaptor.infor("ChatMessage msgId:" + msgId + "/action:" + action);
			switch (action) {
			case Def.CHAT_ACTION_RECEIVE:
				InChatMessage chat = new InChatMessage(msgId, action);
				chat.readFromStream(dis);
				Adaptor.debug("InChatMessage received.msgId:" + msgId);
				return chat;
			case Def.CHAT_ACTION_RESPONSE:
				InChatResponseMessage chatResponse = new InChatResponseMessage(
						msgId);
				chatResponse.readFromStream(dis);
				Adaptor.debug("InChatResponseMessage received. result:"
						+ chatResponse.result);
				return chatResponse;
			default:
				InDynamicPayMessage dynamic = new InDynamicPayMessage(msgId,
						action);
				dynamic.readFromStream(dis);
				Adaptor.debug("InDynamicPayMessage received. result:"
						+ dynamic.result);
				return dynamic;

			}
		} else if (Adaptor.getInstance().mgServerServiceId == type) {
			String msgId = readVarChar(dis);
			int action = dis.readByte();
			int isGzipped = dis.readByte();
			if (isGzipped != 0) {
				byte[] left = new byte[dis.available()];
				dis.readFully(left);
				dis.close();
				left = Adaptor.milk.gunzip(left);
				dis = new MilkInputStream(new ByteArrayInputStream(left));
			}
			return parseMServerPacket(dis, msgId, action);
		} else if (Adaptor.getInstance().browserServiceId == type) {
			String msgId = readVarChar(dis);
			int action = dis.readByte();
			if (action == 11) {
				// raw request
				return parseInRawRequestPacket(dis, msgId);
			}
		} else if (Adaptor.getInstance().reportServiceId == type) {
			readVarChar(dis);
			dis.readByte();
			return parseInReportPacket(dis);
		} else if (Adaptor.getInstance().gameServiceId == type) {
			String msgId = readVarChar(dis);
			int isGzipped = dis.readByte();
			if (isGzipped != 0) {
				byte[] left = new byte[dis.available()];
				dis.readFully(left);
				dis.close();
				left = Adaptor.milk.gunzip(left);
				dis = new MilkInputStream(new ByteArrayInputStream(left));
			}
			InGameMessage msg = new InGameMessage();
			msg.msgId = msgId;
			msg.readFromStream(dis);
			return msg;
		} else if (Adaptor.getInstance().newGameServiceId == type) {
			String msgId = readVarChar(dis);
			int isGzipped = dis.readByte();
			if (isGzipped != 0) {
				byte[] left = new byte[dis.available()];
				dis.readFully(left);
				dis.close();
				left = Adaptor.milk.gunzip(left);
				dis = new MilkInputStream(new ByteArrayInputStream(left));
			}
			InNewGameMessage msg = new InNewGameMessage();
			msg.msgId = msgId;
			msg.readFromStream(dis);
			return msg;
		}

		Adaptor.infor("InMessage parse().unknow InMessage type:" + type
				+ "/id:" + id);
		dis.close();
		dis = null;
		return null;
	}

	static String readVarChar(MilkInputStream dis) throws IOException {
		int count = dis.readByte();
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

	static void writeIntStr(MilkOutputStream dos, String str)
			throws IOException {
		byte[] bytes = str.getBytes("UTF-8");
		dos.writeInt(bytes.length);
		dos.write(bytes);
	}

	static String readIntStr(MilkInputStream dis) throws IOException {
		int count = dis.readInt();
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

}
