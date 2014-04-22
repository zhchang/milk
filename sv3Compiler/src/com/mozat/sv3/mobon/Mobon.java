package com.mozat.sv3.mobon;

public class Mobon {

	public static final short NULL = 0xf0;
	public static final short FALSE = 0xf5;
	public static final short TRUE = 0xf6;
	public static final short INT8 = 0xf1;
	public static final short INT16 = 0xf2;
	public static final short INT32 = 0xf4;
	public static final short INT64 = 0xf8;
	public static final short SINGLE = 0xf9;
	public static final short DOUBLE = 0xfa;
	public static final short STRING32 = 0xfb;
	public static final short BYTES32 = 0xfc;
	public static final short ARRAY32 = 0xfd;
	public static final short MAP32 = 0xfe;
	public static final short MINUS1 = 0xff;

	// extended
	public static final short SV3OBJ = 0xf7;

	public static final short UINT7_MASK = 0x7f;
	public static final short STRING6_MASK = 0x3f;
	public static final short BYTES4_MASK = 0x0f;
	public static final short ARRAY4_MASK = 0x0f;
	public static final short MAP4_MASK = 0x0f;

	public static final short UINT7_FLAG = 0x00;
	public static final short STRING6_FLAG = 0x80;
	public static final short BYTES4_FLAG = 0xc0;
	public static final short ARRAY4_FLAG = 0xd0;
	public static final short MAP4_FLAG = 0xe0;
}
