package com.mozat.sv3.smartview3.layout;

import java.util.Vector;

import com.mozat.sv3.smartview3.elements.Sv3Div;
import com.mozat.sv3.smartview3.elements.Sv3Element;

class Line {
	Vector lineRects;
	short width;
}

public class LayoutContext {
	// bounding size deduced from all parent containers

	short hardBoundWidth; // always positive
	int hardBoundHeight; // always positive
	// bounding size of immediate parent, they are used to deduce percentage
	// values
	// if not fixed, percentage values will not be deduced
	public short definedWidth = -1; // -1 => auto width
	public short definedHeight = -1; // -1 => auto height
	// line count and the start y of current line
	short lineCount;
	int lineY;
	// width and height of current line
	short lineWidth;
	int lineHeight;
	Vector lineRects; // all the rects in the current line
	Vector allLines; // all lines
	boolean lineWrapped; // if a line is wrapped, that no more elements can be
							// appended into that line
	public byte flow; // flow of container
	public byte align; // align of container
	short maxManualRight;
	short maxManualBottom;

	public LayoutContext(int boundWidth, int boundHeight) {
		this.hardBoundWidth = (short) boundWidth;
		this.hardBoundHeight = boundHeight;
		definedWidth = (short) boundWidth;
		definedHeight = -1;
		lineWrapped = true;
		lineRects = new Vector();
		allLines = new Vector();
	}

	public short resolvedBoundWidth() {
		if (definedWidth >= 0) {
			return definedWidth;
		} else {
			return hardBoundWidth;
		}

	}

	public int resolvedBoundHeight() {
		if (definedHeight >= 0) {
			return definedHeight;
		} else {
			return hardBoundHeight;
		}
	}

	short maxLineWidth = 0;

	public short contentWidth() {
		short cw;
		if (lineCount <= 0) {
			cw = 0;
		} else if (lineCount == 1) {
			cw = lineWidth;
		} else {
			cw = maxLineWidth;
		}
		if (cw < maxManualRight) {
			cw = maxManualRight;
		}
		return cw;
	}

	public int contentHeight() {
		int ch = lineY + lineHeight;
		if (ch < maxManualBottom) {
			ch = maxManualBottom;
		}
		return ch;
	}

	public void wrapContext() {
		if (!lineWrapped) {
			wrapCurrentLine();
		}
		short boundWidth = this.getDefinedWidth(); // this.resolvedBoundWidth();
		if (boundWidth < 0) {
			boundWidth = contentWidth();
			int count = allLines.size();
			for (int i = 0; i < count; ++i) {
				Line line = (Line) allLines.elementAt(i);
				short lineWidth = line.width;
				short left = 0;
				if (align == Sv3Div.ALIGN_L) {
					left = 0;
				} else if (align == Sv3Div.ALIGN_R) {
					left = (short) (boundWidth - lineWidth);
				} else {
					left = (short) ((boundWidth - lineWidth) / 2);
				}
				int rectCount = line.lineRects.size();
				for (int j = 0; j < rectCount; ++j) {
					LayoutInfo info = (LayoutInfo) line.lineRects.elementAt(j);
					info.rect.x += left;
				}
			}
		}
	}

	public void wrapCurrentLine() {
		if (!lineWrapped) {
			// if the
			short boundWidth = this.getDefinedWidth(); // this.resolvedBoundWidth();
			int infoCount = lineRects.size();

			// process the fittings
			if (boundWidth > 0 && lineWidth < boundWidth) {
				int count = 0;
				for (int i = 0; i < infoCount; ++i) {
					LayoutInfo info = (LayoutInfo) lineRects.elementAt(i);
					if (info.fitting) {
						++count;
					}
				}
				if (count > 0) {
					int offset = (boundWidth - lineWidth) / count;
					if (offset > 0) {
						for (int i = 0; i < infoCount; ++i) {
							LayoutInfo info = (LayoutInfo) lineRects
									.elementAt(i);
							if (info.fitting) {
								info.rect.width = (short) offset;
							}
						}
					}
					lineWidth = boundWidth;
				}
			}

			short left = 0;
			if (boundWidth >= 0) {
				if (align == Sv3Div.ALIGN_L) {
					left = 0;
				} else if (align == Sv3Div.ALIGN_R) {
					left = (short) (boundWidth - lineWidth);
				} else {
					left = (short) ((boundWidth - lineWidth) / 2);
				}
			}

			short x = 0;
			for (int i = 0; i < infoCount; ++i) {
				LayoutInfo info = (LayoutInfo) lineRects.elementAt(i);
				Rect rect = info.rect;

				if (flow == Sv3Div.FLOW_LTR) {
					rect.x = (short) (left + x);
				} else {
					rect.x = (short) (left + lineWidth - x - rect.width);
				}

				x += rect.width;

				int y;
				byte valign = info.valign;
				if (valign == Sv3Div.VALIGN_T) {
					y = 0;
				} else if (valign == Sv3Div.VALIGN_B) {
					y = lineHeight - rect.height;
				} else {
					y = (lineHeight - rect.height) / 2;
				}
				rect.y = lineY + y;
			}

			Line line = new Line();
			line.lineRects = lineRects;
			line.width = lineWidth;
			if (maxLineWidth < lineWidth) {
				maxLineWidth = lineWidth;
			}

			allLines.addElement(line);
			lineRects = new Vector();
			lineWrapped = true;
		}
	}

	public void startNewLine() {
		this.wrapCurrentLine();

		lineCount += 1;
		lineY += lineHeight;
		lineWidth = 0;
		lineHeight = 0;
		lineWrapped = false;
	}

	public boolean needToWrapLine(byte lineWrap) {
		return lineWrapped || (lineWrap & Sv3Element.LINE_WRAP_BEFORE) != 0;
	}

	public void addRect(Rect rect, byte valign, byte lineWrap) {
		this.addRect(rect, valign, lineWrap, false);
	}

	public void addRect(Rect rect, byte valign, byte lineWrap, boolean fitting) {
		LayoutInfo info = new LayoutInfo();
		info.rect = rect;
		info.valign = valign;
		info.fitting = fitting;

		if (needToWrapLine(lineWrap)) {
			this.startNewLine();
		}

		boolean wrapAfter = (lineWrap & Sv3Element.LINE_WRAP_AFTER) != 0;
		short boundWidth = this.resolvedBoundWidth();
		if (rect.width > boundWidth) { // rect doesn't fit in the full line
			if (lineWidth > 0) // line already contain stuff
			{
				this.startNewLine();
			}

			lineWidth = rect.width;
			lineHeight = rect.height;
			lineRects.addElement(info);
			wrapAfter = true; // need to wrap immediately
		} else { // rect can fit in full line
			short newLineWidth = (short) (rect.width + lineWidth);
			if (newLineWidth > boundWidth) { // line overflow
				this.startNewLine();

				lineWidth = rect.width;
				lineHeight = rect.height;
			} else {
				lineWidth = newLineWidth;
				if (rect.height > lineHeight) {
					lineHeight = rect.height;
				}
			}
			lineRects.addElement(info);
		}

		if (wrapAfter) {
			this.wrapCurrentLine();
		}

	}

	public short getHardBoundWidth() {
		return hardBoundWidth;
	}

	public int getHardBoundHeight() {
		return hardBoundHeight;
	}

	public short getDefinedWidth() {
		return definedWidth;
	}

	public short getDefinedHeight() {
		return definedHeight;
	}

	public short getLineCount() {
		return lineCount;
	}

	public int getLineY() {
		return lineY;
	}

	public short getLineWidth() {
		return lineWidth;
	}

	public int getLineHeight() {
		return lineHeight;
	}

	public byte getFlow() {
		return flow;
	}

	public byte getAlign() {
		return align;
	}

	public void setHardBoundWidth(short hardBoundWidth) {
		this.hardBoundWidth = hardBoundWidth;
	}

	public void setHardBoundHeight(int hardBoundHeight) {
		this.hardBoundHeight = hardBoundHeight;
	}

	public void setFixedWidth(short fixedWidth) {
		this.definedWidth = fixedWidth;
	}

	public void setFixedHeight(short fixedHeight) {
		this.definedHeight = fixedHeight;
	}

	public void setFlow(byte flow) {
		this.flow = flow;
	}

	public void setAlign(byte align) {
		this.align = align;
	}

	public void addManualRect(Rect newRect) {
		short r = (short) newRect.getRight();
		short b = (short) newRect.getBottom();
		if (maxManualRight < r) {
			maxManualRight = r;
		}
		if (maxManualBottom < b) {
			maxManualBottom = b;
		}
	}

}
