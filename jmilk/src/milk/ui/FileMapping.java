package milk.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import milk.implement.MilkOutputStream;

public class FileMapping {

	FileMapping(Hashtable files) {
		this.files = files;
	}

	Hashtable files;
	long lastRequested;
	boolean needUpdate = false;

	byte[] getBytes() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MilkOutputStream dos = new MilkOutputStream(bos);
		Enumeration keys = files.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			writeVarChar(dos, key);
			Integer value = (Integer) files.get(key);
			dos.writeInt(value.intValue());
		}
		byte[] temp = bos.toByteArray();
		return temp;
	}

	void writeVarChar(MilkOutputStream dos, String str) throws IOException {
		if (str != null && str.length() > 0) {
			byte[] bytes = str.getBytes("UTF-8");
			dos.writeByte(bytes.length);
			dos.write(bytes);
		} else {
			dos.writeByte(0);
		}
	}
}
