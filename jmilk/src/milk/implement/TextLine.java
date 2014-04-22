/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package milk.implement;

/**
 * 
 * @author livec
 */

public class TextLine {
	public TextLine(int start, int end, int width) {
		this.start = start;
		this.end = end;
		this.width = width;
	}

	int start;
	int end;
	int width;

	public int getStart() {
		return start;
	}

	public void setStart(int value) {
		start = value;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int value) {
		end = value;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int value) {
		width = value;
	}
}
