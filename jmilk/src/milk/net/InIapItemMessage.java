package milk.net;

import java.io.IOException;

import milk.implement.MilkInputStream;

public class InIapItemMessage extends InMessage {
	
	public int count;
    public String id[];
    public String title[];
    public String info[];

	void readFromStream(MilkInputStream dis) throws IOException {
		readVarChar(dis);
		readVarChar(dis);
		count = dis.readInt();
		id = new String[count];
		title = new String[count];
		info = new String[count];
		for (int i = 0; i < count; i++) {
			id[i] = readIntStr(dis);
			title[i] = readIntStr(dis);
			info[i] = readIntStr(dis);
			// System.out.println("InIapItemMessage :"+i+":" + id[i]);
		}
	}

}
