package milk.utils;

import java.io.IOException;

import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;

public class IOUtil
{
	public static byte[] readBytes(MilkInputStream bin) throws IOException
	{
		int len = bin.readInt();
		if (len <= 0)
		{
			return null;
		}
		byte[] bytes = new byte[len];
		bin.readFully(bytes, 0, len);
		return bytes;
	}
	
	public static void writeBytes(MilkOutputStream bout, byte[] bytes) throws IOException
	{
		bout.writeInt(bytes.length);
		bout.write(bytes);
	}
}
