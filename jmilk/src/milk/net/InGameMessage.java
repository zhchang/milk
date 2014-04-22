package milk.net;

import java.io.IOException;

import milk.implement.Adaptor;
import milk.implement.MilkInputStream;

public class InGameMessage extends InMessage {

	public String key;
	public String value;

	void readFromStream(MilkInputStream dis) throws IOException {

		key = Adaptor.readIntStr(dis);
		value = Adaptor.readIntStr(dis);
	}
}
