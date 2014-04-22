package milk.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class ParseUtils {
	public static final String input2String(InputStream is) {
		StringBuilder buffer = new StringBuilder();

		try {

			BufferedReader in = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while ((line = in.readLine()) != null) {
				buffer.append(line);
			}
			System.out.println(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return buffer.toString();
	}

	public static final InputStream byte2Input(byte[] buf) {
		return new ByteArrayInputStream(buf);
	}

	public static final byte[] input2byte(InputStream inStream)
			throws IOException {
		ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
		byte[] buff = new byte[100];
		int rc = 0;
		while ((rc = inStream.read(buff, 0, 100)) > 0) {
			swapStream.write(buff, 0, rc);
		}
		byte[] in2b = swapStream.toByteArray();
		swapStream.close();
		inStream.close();
		return in2b;
	}

	public static final String byte2String(byte[] bytes) {
		String str = "";
		if (bytes == null || bytes.length <= 0) {
			return str;
		}

		try {
			str = new String(bytes, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return str;

	}
}
