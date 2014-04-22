package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InLoginMessage extends InMessage {

	public int result = -1;

	void readFromStream(MilkInputStream dis) throws IOException {
		result = dis.readInt();
	}

}
