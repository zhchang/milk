package com.mozat.sv3.mobon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Vector;

import com.mozat.sv3.moml3.tags.ISv3ElementTag;

public class MobonWriter {

	DataOutputStream out;

	public MobonWriter(DataOutputStream output) {
		this.out = output;
	}

	public MobonWriter(OutputStream output) {
		this.out = new DataOutputStream(output);
	}

	private void writeTagAndSize(int size, int type, int flag, int limit)
			throws IOException {
		if (size >= 0 && size <= limit) {
			out.write(flag | size);
		} else {
			out.write(type);
			writeInt(size);
		}
	}

	private void writeSv3TagAndType(int type) throws IOException {
		out.write(Mobon.SV3OBJ);
		writeInt(type);
	}

	public MobonWriter writeInt(int value) throws IOException {
		if (value == -1) {
			out.write(Mobon.MINUS1);
		} else if (value < -0x8000 || value >= 0x8000) {
			out.write(Mobon.INT32);
			out.writeInt(value);
		} else if (value < -0x80 || value >= 0x80) {
			out.write(Mobon.INT16);
			out.writeShort(value);
		} else if (value < 0 || value >= 0x80) {
			out.write(Mobon.INT8);
			out.write(value);
		} else {
			out.write(value);
		}
		return this;
	}

	public MobonWriter writeLong(long value) throws IOException {
		if (value < -0x80000000l || value >= 0x80000000l) {
			out.write(Mobon.INT64);
			out.writeLong(value);
		} else {
			this.writeInt((int) value);
		}
		return this;
	}

	public MobonWriter writeBoolean(boolean value) throws IOException {
		out.write(value ? Mobon.TRUE : Mobon.FALSE);
		return this;
	}

	public MobonWriter writeString(String value) throws IOException {
		byte[] bytes = value.getBytes("UTF-8");
		this.writeTagAndSize(bytes.length, Mobon.STRING32, Mobon.STRING6_FLAG,
				Mobon.STRING6_MASK);
		out.write(bytes);
		return this;
	}

	public MobonWriter writeBytes(byte[] value) throws IOException {
		this.writeTagAndSize(value.length, Mobon.BYTES32, Mobon.BYTES4_FLAG,
				Mobon.BYTES4_MASK);
		out.write(value);
		return this;
	}

	private MobonWriter writeSv3Obj(ISv3ElementTag tag) throws IOException,
			MobonException {
		this.writeSv3TagAndType(tag.getSv3Type());
		this.write(tag.toIntMap());
		return this;
	}

	public MobonWriter writeNull() throws IOException {
		out.write(Mobon.NULL);
		return this;
	}

	public MobonWriter write(Object obj) throws IOException, MobonException {
		if (obj == null) {
			this.writeNull();
		} else if (obj instanceof Byte) {
			this.writeInt(((Byte) obj).intValue());
		} else if (obj instanceof Short) {
			this.writeInt(((Short) obj).intValue());
		} else if (obj instanceof Integer) {
			this.writeInt(((Integer) obj).intValue());
		} else if (obj instanceof Long) {
			this.writeLong(((Long) obj).longValue());
		} else if (obj instanceof Boolean) {
			this.writeBoolean(((Boolean) obj).booleanValue());
		} else if (obj instanceof String) {
			this.writeString((String) obj);
		} else if (obj instanceof byte[]) {
			this.writeBytes((byte[]) obj);
			// } else if(obj instanceof Float) {
			// this.writeSingle((Float)obj);
			// } else if(obj instanceof Double) {
			// this.writeDouble((Double)obj);
		} else if (obj instanceof Collection<?>) {
			this.writeArray((Collection<?>) obj);
		} else if (obj instanceof Map<?, ?>) {
			this.writeMap((Map<?, ?>) obj);
		} else if (obj instanceof short[]) {
			this.writeArray((short[]) obj);
		} else if (obj instanceof int[]) {
			this.writeArray((int[]) obj);
		} else if (obj instanceof long[]) {
			this.writeArray((long[]) obj);
		} else if (obj instanceof String[]) {
			this.writeArray((String[]) obj);
		} else if (obj instanceof boolean[]) {
			this.writeArray((boolean[]) obj);
			// } else if(obj instanceof Collection<?>[]) {
			// this.writeArray((Collection<?>[])obj);
			// } else if(obj instanceof Map<?,?>[]) {
			// this.writeArray((Map<?,?>[])obj);
			// } else if(obj instanceof float[]) {
			// this.writeArray((float[])obj);
			// } else if(obj instanceof double[]) {
			// this.writeArray((double[])obj);
		} else if (obj instanceof Object[]) {
			this.writeArray((Object[]) obj);
		} else if (obj instanceof ISv3ElementTag) {
			this.writeSv3Obj((ISv3ElementTag) obj);
		} else {
			throw new MobonException("type not supported: " + obj.getClass());
		}
		return this;
	}

	private MobonWriter writeArray(Collection<?> collection)
			throws IOException, MobonException {
		this.writeTagAndSize(collection.size(), Mobon.ARRAY32,
				Mobon.ARRAY4_FLAG, Mobon.ARRAY4_MASK);

		for (Object o : collection) {
			this.write(o);
		}
		return this;
	}

	public MobonWriter writeArray(long[] list) throws IOException,
			MobonException {
		this.writeTagAndSize(list.length, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);

		for (int i = 0; i < list.length; ++i) {
			long t = list[i];
			this.writeLong(t);
		}
		return this;
	}

	@SuppressWarnings("rawtypes")
	public MobonWriter writeArray(Vector list) throws IOException,
			MobonException {
		int size = list.size();

		this.writeTagAndSize(size, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);

		for (int i = 0; i < size; ++i) {
			Object t = list.elementAt(i);
			this.write(t);
		}
		return this;
	}

	public MobonWriter writeArray(Object[] list) throws IOException,
			MobonException {
		this.writeTagAndSize(list.length, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);

		for (int i = 0; i < list.length; ++i) {
			Object t = list[i];
			this.write(t);
		}
		return this;
	}

	public MobonWriter writeArray(short[] list) throws IOException,
			MobonException {
		this.writeTagAndSize(list.length, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);
		for (int i = 0; i < list.length; ++i) {
			short t = list[i];
			this.writeInt(t);
		}
		return this;
	}

	public MobonWriter writeArray(boolean[] list) throws IOException,
			MobonException {
		this.writeTagAndSize(list.length, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);
		for (int i = 0; i < list.length; ++i) {
			boolean t = list[i];
			this.writeBoolean(t);
		}
		return this;
	}

	public MobonWriter writeArray(int[] list) throws IOException,
			MobonException {
		this.writeTagAndSize(list.length, Mobon.ARRAY32, Mobon.ARRAY4_FLAG,
				Mobon.ARRAY4_MASK);
		for (int i = 0; i < list.length; ++i) {
			int t = list[i];
			this.writeInt(t);
		}
		return this;
	}

	public MobonWriter writeMap(Map<?, ?> map) throws IOException,
			MobonException {
		this.writeTagAndSize(map.size(), Mobon.MAP32, Mobon.MAP4_FLAG,
				Mobon.MAP4_MASK);
		for (Map.Entry<?, ?> e : map.entrySet()) {
			this.write(e.getKey());
			this.write(e.getValue());
		}
		return this;
	}

	public MobonWriter writeMap(MobonPair[] map) throws IOException,
			MobonException {
		this.writeTagAndSize(map.length, Mobon.MAP32, Mobon.MAP4_FLAG,
				Mobon.MAP4_MASK);
		for (int i = 0; i < map.length; ++i) {
			MobonPair p = map[i];
			this.write(p.key);
			this.write(p.value);
		}
		return this;
	}
}
