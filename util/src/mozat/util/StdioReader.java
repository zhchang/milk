package mozat.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

public class StdioReader implements Runnable {
	InputStream in;
	String output;
	BufferWriter writer;

	public StdioReader(InputStream in, BufferWriter writer) {
		this.in = in;
		this.writer = writer;

		if (this.in != null) {
			new Thread(this).start();
		}
	}

	@Override
	public void run() {
		int b = 0;
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			while ((b = in.read()) != -1) {
				bos.write(b);
			}
			output = new String(bos.toByteArray(), Charset.forName("cp936"));
			synchronized (writer) {
				writer.write(output);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			synchronized (writer) {
				writer.finish();
				writer.notifyAll();
			}
			try {
				in.close();
			} catch (Exception e) {
			}
		}

	}
}
