package milk.implement;

class MInstruction {

	static int[] staticOprands = new int[20];

	static byte[] parseIndexes = new byte[2];

	static byte[] getInstruction(MilkInputStream dis) throws Exception {
		int opCode = dis.readByte();
		int temp = dis.readByte();
		byte[] bytes = new byte[temp + 1];
		bytes[0] = (byte) opCode;
		dis.readFully(bytes, 1, temp);
		return bytes;
	}

	/*
	 * MInstruction(DataInputStream dis) throws Exception { opCode = (byte)
	 * dis.readByte(); int temp = dis.readByte(); bytes = new byte[temp];
	 * dis.readFully(bytes); }
	 */

	static int[] getOprands(byte[] bytes) {

		parseIndexes[0] = 1;
		parseIndexes[1] = 0;
		int count = 0;
		try {

			while (bytes.length > parseIndexes[0]) {
				Adaptor.readVarInt(bytes, staticOprands, parseIndexes);
				count++;
			}

		} catch (Exception e) {
		}

		staticOprands[staticOprands.length - 1] = count;
		return staticOprands;
	}
}
