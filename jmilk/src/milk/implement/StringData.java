package milk.implement;

public class StringData {
	String data;
	int index = 0;

	public StringData(String input) {
		data = input;
	}

	char getChar(boolean peep) {
		char thing = data.charAt(index);
		if (!peep) {
			index++;
		}
		return thing;
	}

	boolean hasMore() {
		return data.length() > index;
	}

	void skip() {
		index++;
	}

}
