package milk.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;

public abstract class OutMessage extends MoPacket {

	static final int ID_LoginMessage = 3;
	static final int ID_HeartMessage = 1;
	static final int ID_GameMessage = 0;
	static final int ID_IAPMessage = 0;
	static final int ID_MoWebMessage = 0;

	public OutMessage(int type, int id, byte[] payload) {
		super(type, id, payload);
	}

	private void writeMessageBody() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MilkOutputStream dos = new MilkOutputStream(bos);
		try {
			writeToStream(dos);
			dos.close();
			dos = null;
			payload = bos.toByteArray();
			bos.close();
			bos = null;
		} catch (IOException e) {
			Adaptor.exception(e);
			payload = null;
		} catch (Exception e) {
			Adaptor.exception(e);
			payload = null;
		}
	}

	abstract void writeToStream(MilkOutputStream dos) throws IOException;

	public final byte[] toBytes() {

		writeMessageBody();

		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		MilkOutputStream dos1 = new MilkOutputStream(bos1);
		try {
			ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
			MilkOutputStream dos2 = new MilkOutputStream(bos2);
			dos2.writeInt(type);
			dos2.writeInt(id);
			dos2.writeInt(0);
			dos2.write(payload);
			byte[] thing = bos2.toByteArray();
			dos1.writeInt(thing.length);
			dos1.write(thing);
		} catch (Exception t) {
			Adaptor.exception(t);
		}
		return bos1.toByteArray();
	}

	static void writeVarChar(MilkOutputStream dos, String str)
			throws IOException {
		byte[] bytes = str.getBytes("UTF-8");
		dos.writeByte(bytes.length);
		dos.write(bytes);
	}

	static void writeIntStr(MilkOutputStream dos, String str)
			throws IOException {
		byte[] bytes = str.getBytes("UTF-8");
		dos.writeInt(bytes.length);
		dos.write(bytes);
	}
}
