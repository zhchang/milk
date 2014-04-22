package mobon;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import milk.implement.MilkInputStream;
import smartview3.elements.Sv3Factory;
import smartview3.elements.Sv3Page;

public class MobonReader {
	MilkInputStream in;
	Sv3Factory factory;

	// public MobonReader(DataInputStream input) {
	// this.in = input;
	// this.factory = new Sv3Factory();
	// }

	public MobonReader(InputStream input, Sv3Factory factory) {
		this.in = new MilkInputStream(input);
		this.factory = factory;
	}

	public MobonReader(InputStream input) {
		this(input, new Sv3Factory());
	}

	private byte[] readBytes(int size) throws IOException {
		byte[] result = new byte[size];
		int count = 0;
		while (count < size) {
			count += in.read(result, count, size - count);
		}
		return result;
	}

	private String readRawString(int size) throws IOException {
		byte[] bytes = this.readBytes(size);
		return new String(bytes, "UTF-8");
	}

	private byte[] readRawBytes(int size) throws IOException {
		byte[] bytes = this.readBytes(size);
		return bytes;
	}

	private Object[] readArray(int size) throws IOException, MobonException {
		Object[] result = new Object[size];
		for (int i = 0; i < size; ++i) {
			result[i] = this.read();
		}
		return result;
	}

	private String[] readStringArray(int size) throws IOException,
			MobonException {
		String[] result = new String[size];
		for (int i = 0; i < size; ++i) {
			result[i] = this.readStringOrNull();
		}
		return result;
	}

	private Vector readVector(int size) throws IOException, MobonException {
		Vector result = new Vector(size);
		for (int i = 0; i < size; ++i) {
			result.addElement(this.read());
		}
		return result;
	}

	private MobonPair[] readMap(int size) throws IOException, MobonException {
		MobonPair[] result = new MobonPair[size];
		for (int i = 0; i < size; ++i) {
			result[i] = new MobonPair(this.read(), this.read());
		}
		return result;
	}

	private int readInt(int tag) throws IOException, MobonException {
		int result = 0;

		if ((tag & ~Mobon.UINT7_MASK) == Mobon.UINT7_FLAG) {
			result = tag;
		} else if (tag == Mobon.MINUS1) {
			return -1;
		} else if (tag == Mobon.INT8) {
			result = in.readByte();
		} else if (tag == Mobon.INT16) {
			result = in.readShort();
		} else if (tag == Mobon.INT32) {
			result = in.readInt();
		} else {
			throw new MobonException("invalid tag");
		}

		return result;
	}

	private long readLong(int tag) throws IOException, MobonException {
		long result;

		if (tag == Mobon.INT64) {
			result = in.readLong();
		} else {
			result = readInt(tag);
		}
		return result;
	}

	public int readInt() throws IOException, MobonException {
		int tag = in.read();
		return readInt(tag);
	}

	public long readLong() throws IOException, MobonException {
		int tag = in.read();
		return readLong(tag);
	}

	public boolean readBoolean() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.FALSE) {
			return false;
		} else if (tag == Mobon.TRUE) {
			return true;
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public String readStringOrNull() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.STRING32) {
			int size = this.readInt();
			return this.readRawString(size);
		} else if ((tag & ~Mobon.STRING6_MASK) == Mobon.STRING6_FLAG) {
			return this.readRawString(tag & Mobon.STRING6_MASK);
		} else if (tag == Mobon.NULL) {
			return null;
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public byte[] readBytes() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.BYTES32) {
			int size = this.readInt();
			return this.readBytes(size);
		} else if ((tag & ~Mobon.BYTES4_MASK) == Mobon.BYTES4_FLAG) {
			return this.readBytes(tag & Mobon.BYTES4_MASK);
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public Object[] readArray() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.ARRAY32) {
			int size = this.readInt();
			return this.readArray(size);
		} else if ((tag & ~Mobon.ARRAY4_MASK) == Mobon.ARRAY4_FLAG) {
			return this.readArray(tag & Mobon.ARRAY4_MASK);
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public String[] readStringArray() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.ARRAY32) {
			int size = this.readInt();
			return this.readStringArray(size);
		} else if ((tag & ~Mobon.ARRAY4_MASK) == Mobon.ARRAY4_FLAG) {
			return this.readStringArray(tag & Mobon.ARRAY4_MASK);
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public Vector readVector() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.ARRAY32) {
			int size = this.readInt();
			return this.readVector(size);
		} else if ((tag & ~Mobon.ARRAY4_MASK) == Mobon.ARRAY4_FLAG) {
			return this.readVector(tag & Mobon.ARRAY4_MASK);
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public MobonPair[] readMap() throws IOException, MobonException {
		int tag = in.read();

		if (tag == Mobon.MAP32) {
			int size = this.readInt();
			return this.readMap(size);
		} else if ((tag & ~Mobon.MAP4_MASK) == Mobon.MAP4_FLAG) {
			return this.readMap(tag & Mobon.MAP4_MASK);
		} else {
			throw new MobonException("invalid tag");
		}
	}

	public int readMapSize() throws IOException, MobonException {
		int tag = in.read();
		if (tag == Mobon.MAP32) {
			return this.readInt();
		} else if ((tag & ~Mobon.MAP4_MASK) == Mobon.MAP4_FLAG) {
			return tag & Mobon.MAP4_MASK;
		} else {
			throw new MobonException("invalid map tag");
		}
	}

	public int readTag() throws IOException {
		return in.read();
	}

	public Object read() throws IOException, MobonException {
		Object result;

		int tag = in.read();
		if ((tag & 0xf0) == 0xf0) {
			if (tag == Mobon.NULL) {
				result = null;
			} else if (tag == Mobon.FALSE) {
				result = new Boolean(false);
			} else if (tag == Mobon.TRUE) {
				result = new Boolean(true);
			} else if (tag == Mobon.MINUS1) {
				result = new Integer(-1);
			} else if (tag == Mobon.INT8) {
				result = new Integer(in.read());
			} else if (tag == Mobon.INT16) {
				result = new Integer(in.readShort());
			} else if (tag == Mobon.INT32) {
				result = new Integer(in.readInt());
			} else if (tag == Mobon.INT64) {
				result = new Long(in.readLong());
			} else if (tag == Mobon.STRING32) {
				int size = this.readInt();
				result = this.readRawString(size);
			} else if (tag == Mobon.BYTES32) {
				int size = this.readInt();
				result = this.readRawBytes(size);
			} else if (tag == Mobon.ARRAY32) {
				int size = this.readInt();
				result = this.readArray(size);
			} else if (tag == Mobon.MAP32) {
				int size = this.readInt();
				result = this.readMap(size);
			} else if (tag == Mobon.SV3OBJ) {
				result = factory.elementFromMobon(this);
			} else if (tag == Mobon.SINGLE || tag == Mobon.DOUBLE) {
				throw new MobonException("float not supported by j2me");
			} else {
				throw new MobonException("invalid mobon tag" + tag);
			}
		} else if ((tag & ~Mobon.UINT7_MASK) == Mobon.UINT7_FLAG) {
			result = new Integer(tag);
		} else if ((tag & ~Mobon.STRING6_MASK) == Mobon.STRING6_FLAG) {
			result = this.readRawString(tag & Mobon.STRING6_MASK);
		} else if ((tag & ~Mobon.BYTES4_MASK) == Mobon.BYTES4_FLAG) {
			result = this.readRawBytes(tag & Mobon.BYTES4_MASK);
		} else if ((tag & ~Mobon.ARRAY4_MASK) == Mobon.ARRAY4_FLAG) {
			result = this.readArray(tag & Mobon.ARRAY4_MASK);
		} else if ((tag & ~Mobon.MAP4_MASK) == Mobon.MAP4_FLAG) {
			result = this.readMap(tag & Mobon.MAP4_MASK);
		} else {
			throw new MobonException("invalid mobon tag " + tag);
		}

		return result;
	}

	public short[] readArrayOfShort() throws IOException, MobonException {
		return convertToArrayOfShort(this.readArray());
	}

	protected static short[] convertToArrayOfShort(Object[] values) {
		short[] shortValues = new short[values.length];
		for (int i = 0; i < values.length; ++i) {
			shortValues[i] = ((Integer) values[i]).shortValue();
		}
		return shortValues;
	}

	// Sv3Page page = null;

	public Sv3Page readPage() throws IOException, MobonException {
		Sv3Page page = factory.createPage();
		int size = readMapSize(); // 1 or 5 bytes
		for (int i = 0; i < size; ++i) {
			int key = readInt();
			page.readAttrFromMobon(this, key);
		}
		return page;
	}

	// public Sv3Div readPageRoot(Sv3Page page) throws IOException,
	// MobonException {
	// int tag = in.read();
	// if (tag == Mobon.SV3OBJ) {
	// return (Sv3Div) factory.elementFromMobon(this, page);
	// } else {
	// throw new MobonException("expecting an Sv3Div object");
	// }
	// }

	// public readRoot() {
	//
	// }
}
