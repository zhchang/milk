package com.mozat.sv3.smartview3.layout;

public class TextSegment extends Rect {

	public int location; // this is the index of starting character
	public int length; // number of characters this segment is corresponding to
	public short line; // line number

	public TextSegment(int location, int length, int line, int width) {
		this.location = location;
		this.length = length;
		this.line = (short) line;
		super.width = (short) width;
	}

	public TextSegment(TextSegment seg) {
		this(seg.location, seg.length, seg.line, seg.width);
	}

	public void mergeSegment(TextSegment segment) {
		length = segment.length + segment.location - location;
		width += segment.width;
	}
}
