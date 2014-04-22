package milk.net;

import java.io.IOException;

import milk.implement.Adaptor;
import milk.implement.MilkInputStream;

public class InNewGameMessage extends InMessage {

	public String value;

	void readFromStream(MilkInputStream dis) throws IOException {

		value = Adaptor.readIntStr(dis);
	}
}
