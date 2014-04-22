package milk.net;

import java.io.IOException;

import milk.implement.Adaptor;
import milk.implement.MilkInputStream;

public class InRawRequestMessage extends InMessage {

	public String url;
	public String response;

	public InRawRequestMessage(String msgId) {
		this.msgId = msgId;
	}

	void readFromStream(MilkInputStream dis) throws IOException {
		url = Adaptor.readShortStr(dis);
		response = Adaptor.readIntStr(dis);
	}

}
