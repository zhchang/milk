package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InIapResultMessage extends InMessage{

	public String ticket;
	public int result=1;
	
	void readFromStream(MilkInputStream dis) throws IOException {
		readVarChar(dis);
		readVarChar(dis);
		
		ticket = readIntStr(dis);
		result = dis.readInt();
	}

	
}
