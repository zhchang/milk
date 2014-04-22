package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InReportMessage extends InMessage {

	public int isSendReport;

	void readFromStream(MilkInputStream dis) throws IOException {
		isSendReport = dis.readInt();
	}

}
