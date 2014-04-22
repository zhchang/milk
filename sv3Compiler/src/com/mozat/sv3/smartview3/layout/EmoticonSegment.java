package com.mozat.sv3.smartview3.layout;

public class EmoticonSegment extends TextSegment {

	IEmoticon emoticon;

	public IEmoticon getEmoticon() {
		return emoticon;
	}

	public void setEmoticon(IEmoticon emoticon) {
		this.emoticon = emoticon;
	}

	// public EmoticonSegment(int location, int length, int line, int width) {
	// }

	public EmoticonSegment(int location, short length, int line, int width,
			IEmoticon emoticon) {
		super(location, length, line, width);
		this.emoticon = emoticon;
	}

}
