package milk.ui.store;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;

public class FileMapping {

	FileMapping(Hashtable files) {
		this.files = files;
	}

	public Hashtable files;
	long lastRequested;
	boolean needUpdate = false;

	byte[] getBytes() throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MilkOutputStream dos = new MilkOutputStream(bos);
		Enumeration keys = files.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Adaptor.writeVarChar(dos, key);
			Integer value = (Integer) files.get(key);
			dos.writeInt(value.intValue());
		}
		byte[] temp = bos.toByteArray();
		return temp;
	}

	public void addfileMapping(String fileName,int id)
	{
		if(files==null)
		{
			files=new Hashtable();
		}
		files.put(fileName, id);
	}
	
}
