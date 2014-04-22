package milk.implement;

import java.io.IOException;
import java.io.InputStream;

public class MilkInputStream extends InputStream {

	protected InputStream in;

	public MilkInputStream(InputStream in) {
		this.in = in;
	}

	public final int read(byte b[]) throws IOException {

		return in.read(b, 0, b.length);
	}

	public int available() throws IOException {
		return in.available();
	}

	public final byte readByte() throws IOException {
		int ch = in.read();
		if (ch < 0)
			throw new IOException();
		return (byte) (ch);
	}

	public int read() throws IOException {
		return in.read();
	}

	public final short readShort() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		if ((ch1 | ch2) < 0)
			throw new IOException();
		return (short) ((ch1 << 8) + (ch2 << 0));
	}

	public final int readInt() throws IOException {
		int ch1 = in.read();
		int ch2 = in.read();
		int ch3 = in.read();
		int ch4 = in.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0)
			throw new IOException();
		return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
	}

	private byte readBuffer[] = new byte[8];

	public final long readLong() throws IOException {
		readFully(readBuffer, 0, 8);
		return (((long) readBuffer[0] << 56)
				+ ((long) (readBuffer[1] & 255) << 48)
				+ ((long) (readBuffer[2] & 255) << 40)
				+ ((long) (readBuffer[3] & 255) << 32)
				+ ((long) (readBuffer[4] & 255) << 24)
				+ ((readBuffer[5] & 255) << 16) + ((readBuffer[6] & 255) << 8) + ((readBuffer[7] & 255) << 0));
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
		int n = 0;
		while (n < len) {
			int count = in.read(b, off + n, len - n);
			if (count < 0)
				throw new IOException();
			n += count;
		}
	}

	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}
}
