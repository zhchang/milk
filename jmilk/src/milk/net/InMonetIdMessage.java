package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InMonetIdMessage extends InMessage {

	public int monetId = -1;
	public int requiredMajorVersion = 0;
	public int requiredMinorVersion = 0;
	public int requiredBuildVersion = 0;

	void readFromStream(MilkInputStream dis) throws IOException {
		monetId = dis.readInt();
		try {
			requiredMajorVersion = dis.readInt();
			requiredMinorVersion = dis.readInt();
			requiredBuildVersion = dis.readInt();
		} catch (Exception e) {
		}
	}

}
