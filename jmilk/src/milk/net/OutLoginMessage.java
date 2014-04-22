package milk.net;

import java.io.IOException;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;

public class OutLoginMessage extends OutMessage {

	private String userName, passWord;

	public OutLoginMessage(String userName, String password) {
		super(0, OutMessage.ID_LoginMessage, null);
		this.userName = userName;
		this.passWord = password;
	}

	void writeToStream(MilkOutputStream dos) throws IOException {

		dos.write(getMd5Bytes(passWord));
		dos.writeInt(1);
		dos.writeByte(0);
		dos.writeByte(0);
		dos.writeByte(0);
		dos.writeByte(0);
		byte[] name = userName.getBytes("UTF-8");
		dos.write(name);
		for (int i = name.length; i < 104; i++) {
			dos.write(0);
		}
	}

	private byte[] getMd5Bytes(String md5) {
		try {
			int len = md5.length();
			byte[] toRet = new byte[len / 2];
			for (int i = 0, j = 0; i < len - 1; i += 2, j++) {
				String s = md5.substring(i, i + 2);
				toRet[j] = (byte) (Integer.parseInt(s, 16));
			}
			return toRet;
		} catch (Exception e) {
			Adaptor.exception(e);
		}
		return null;
	}
}
