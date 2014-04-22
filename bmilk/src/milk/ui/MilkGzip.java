package milk.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import net.rim.device.api.compress.GZIPInputStream;

public class MilkGzip {
	public static byte[] gunzip(byte[] input) {
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			GZIPInputStream gis = new GZIPInputStream(bis);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] temp = new byte[1024];
			int read = -1;
			while ((read = gis.read(temp)) != -1) {
				bos.write(temp, 0, read);
			}
			bis.close();
			return bos.toByteArray();
		} catch (Exception e) {
			return null;
		}
	}

}
