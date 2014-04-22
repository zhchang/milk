package milk.implement.mk;

import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.TextLine;
import milk.implement.VectorPool;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;

public class MText extends MDraw {
	private MRect position;
	private int userWidth, userHeight, fontSize, fontModifier, bgColor,
			borderColor, textColor = 0xffffff, maxLines, align, vAlign;

	public int getFontModifier() {
		return fontModifier;
	}

	public void setFontModifier(int fontModifier) {
		this.fontModifier = fontModifier;
	}

	private boolean bgTransparent = true;
	private String text;
	Vector lines;

	public MText() {

	}

	public MText(String text, int x, int y) {
		position = new MRect(x, y, 0, 0);
		this.text = text;
	}

	public MText(MText thing) {
		if (thing != null) {
			if (thing.position != null) {
				this.position = new MRect(thing.position);
			}
			this.userWidth = thing.userWidth;
			this.userHeight = thing.userHeight;
			this.zIndex = thing.zIndex;
			this.fontSize = thing.fontSize;
			this.fontModifier = thing.fontModifier;
			this.bgColor = thing.bgColor;
			this.borderColor = thing.borderColor;
			this.textColor = thing.textColor;
			this.maxLines = thing.maxLines;
			this.align = thing.align;
			this.vAlign = thing.vAlign;
			this.visible = thing.visible;
			this.bgTransparent = thing.bgTransparent;
			this.text = thing.text;
			System.out.println("--------------MText align "+align);
		}
	}

	public MilkFont getFont() {
		int style = MilkFont.STYLE_PLAIN;
		switch (this.fontModifier) {
		case 1: {
			style = MilkFont.STYLE_ITALIC;
			break;
		}
		case 2: {
			style = MilkFont.STYLE_BOLD;
			break;
		}
		case 3: {
			style = MilkFont.STYLE_BOLD | MilkFont.STYLE_ITALIC;
		}
		}
		int size = MilkFont.SIZE_SMALL;
		switch (this.fontSize) {
		case -1: {
			size = MilkFont.SIZE_SMALL;
			break;
		}
		case 0: {
			size = MilkFont.SIZE_MEDIUM;
			break;
		}
		case 1: {
			size = MilkFont.SIZE_LARGE;
			break;
		}
		}
		return Adaptor.uiFactory.getFont(style, size);
	}

	public void layout() {
		VectorPool.recycle(lines);
		if (text != null && text.length() > 0) {
			MilkFont font = getFont();
			lines = Adaptor.getInstance().contentsToLines(text,
					userWidth < 1 ? Integer.MAX_VALUE : userWidth, font,
					maxLines);
			int size = lines.size();
			int layoutWidth = 0;
			for (int i = 0; i < size; i++) {
				TextLine line = (TextLine) lines.elementAt(i);
				layoutWidth = Math.max(layoutWidth, line.getWidth());
			}
			int layoutHeight = font.getHeight() * size;
			if (layoutHeight > userHeight && userHeight != 0) {
				layoutHeight = userHeight;
			}
			position.setWidth(layoutWidth);
			position.setHeight(layoutHeight);
		} else {
			lines = null;
		}
	}

	public int getUserWidth() {
		return userWidth;
	}

	public void setUserWidth(int userWidth) {
		this.userWidth = userWidth;
	}

	public int getUserHeight() {
		return userHeight;
	}

	public void setUserHeight(int userHeight) {
		this.userHeight = userHeight;
	}

	public int getFontSize() {
		return fontSize;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(int borderColor) {
		this.borderColor = borderColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public int getMaxLines() {
		return maxLines;
	}

	public void setMaxLines(int maxLines) {
		this.maxLines = maxLines;
	}

	public int getAlign() {
		return align;
	}

	public void setAlign(int align) {
		this.align = align;
		System.out.println("--------------setAlign align "+align);
	}

	public int getvAlign() {
		return vAlign;
	}

	public void setvAlign(int vAlign) {
		this.vAlign = vAlign;
	}

	public boolean isBgTransparent() {
		return bgTransparent;
	}

	public void setBgTransparent(boolean bgTransparent) {
		this.bgTransparent = bgTransparent;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public int getX() {
		return position.getX();
	}

	public void setX(int value) {
		position.setX(value);
	}

	public int getY() {
		return position.getY();
	}

	public void setY(int value) {
		position.setY(value);
	}

	public int getLayoutWidth() {
		return position.getWidth();
	}

	public int getLayoutHeight() {
		return position.getHeight();
	}

	protected boolean canDraw(MRect screen, int xOffset, int yOffset) {
		return isVisible() && position.intersacts(screen, xOffset, yOffset);
	}

	public boolean draw(MilkGraphics g, MRect screen, int xOffset, int yOffset) {
		if (isVisible()) {

			if (position.intersacts(screen, xOffset, yOffset)) {

				if (bgColor != 0) {
					g.setColor(bgColor);
					g.fillRect(position.getX() - 2 + xOffset, position.getY()
							+ yOffset, position.getWidth() + 4,
							position.getHeight());
				}
				if (borderColor != 0) {
					g.setColor(borderColor);
					g.drawRect(position.getX() - 2 + xOffset, position.getY()
							+ yOffset, position.getWidth() + 4,
							position.getHeight());
				}
				if (lines != null) {

					g.setColor(textColor);
					int size = lines.size();
					int runY = position.getY() + yOffset;
					MilkFont font = getFont();
					g.setFont(font);
					int runX = 0;
					for (int i = 0; i < size; i++) {
						TextLine line = (TextLine) lines.elementAt(i);
						int lineWidth = line.getWidth();	
						switch (align) {
						case 1: {//middle
							runX += (position.getWidth() - lineWidth) / 2;
							break;
						}
						case 2: {//right top
							runX = position.getX() + xOffset
									+ position.getWidth() - lineWidth;
							break;
						}
						default: {//left
							runX = position.getX() + xOffset;
							break;
						}
						}
						// String toDraw = text.substring(line.getStart(),
						// line.getEnd());
						g.drawSubstring(text, line.getStart(), line.getEnd()
								- line.getStart(), runX, runY, MilkGraphics.TOP
								| MilkGraphics.LEFT);
						// g.drawString(text, runX, runY, Graphics.TOP
						// | Graphics.LEFT);
						runY += font.getHeight();

					}

				}

			}
		}

		return moveState != null;

	}

	public void processAnimation(int xOffset, int yOffset) {

	}

	public MDraw matchFinger(int x, int y, int xOffset, int yOffset) {
		if (isVisible() && onFingerDown != null) {
			MRect pos = position;
			pos.move(xOffset, yOffset);
			if (pos.contains(x, y)) {
				return this;
			}
		}
		return null;
	}

	public int getMyX() {
		return getX();
	}

	public int getMyY() {
		return getY();
	}

}
