package milk.net;

import java.io.IOException;

import milk.implement.MilkOutputStream;

public class OutHeartMessage extends OutMessage {

	public OutHeartMessage() {
		super(0, OutMessage.ID_HeartMessage, null);
	}

	void writeToStream(MilkOutputStream dos) throws IOException {
		dos.write('P');
	}

}
