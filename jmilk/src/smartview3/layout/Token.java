package smartview3.layout;

class Token {

	public int start;
	public short length;
	public boolean lineAfter;
	public IEmoticon emoticon;

	public Token() {
	}

	public Token(int start, int length, boolean lineAfter) {
		this.start = start;
		this.length = (short) length;
		this.lineAfter = lineAfter;
	}

	public Token(Token t) {
		this(t.start, t.length, t.lineAfter);
	}

	public void set(int start, int length, boolean lineAfter) {
		this.set(start, length, lineAfter, null);
	}

	public void set(int start, int length, boolean lineAfter, IEmoticon emoticon) {
		this.start = start;
		this.length = (short) length;
		this.lineAfter = lineAfter;
		this.emoticon = emoticon;
	}

	public TextSegment toTextSegment(int line, int width) {
		if (emoticon == null) {
			return new TextSegment(start, length, line, width);
		} else {
			return new EmoticonSegment(start, length, line, width, emoticon);
		}
	}
}
