package milk.ui;

import java.util.Hashtable;


import javax.microedition.lcdui.Font;

import milk.ui2.MilkFont;

public class MilkFontImpl implements MilkFont{
	Font font;
	static Hashtable register = new Hashtable();

	public static MilkFont getFont(int style, int size) {
		String key = size + "-" + style;
		if (register.containsKey(key)) {
			return (MilkFont) register.get(key);
		} else {
			return new MilkFontImpl(style, size);
		}
	}
	
	private static int toNativeStyle(int style){
		int nativeStyle=Font.STYLE_PLAIN;
		switch(style){
		case STYLE_PLAIN:
			nativeStyle=Font.STYLE_PLAIN;
			break;
		case STYLE_UNDERLINED:
			nativeStyle=Font.STYLE_UNDERLINED;
			break;
		case STYLE_BOLD:
			nativeStyle=Font.STYLE_BOLD;
			break;
		case STYLE_ITALIC:
			nativeStyle=Font.STYLE_ITALIC;
			break;
		}
		return nativeStyle;
	}
	
	private static int toNativeSize(int size){
		int nativeSize=Font.SIZE_MEDIUM;
		switch(size){
		case SIZE_MEDIUM:
			nativeSize=Font.SIZE_MEDIUM;
			break;
		case SIZE_SMALL:
			nativeSize=Font.SIZE_SMALL;
			break;
		case SIZE_LARGE:
			nativeSize=Font.SIZE_LARGE;
			break;
		}
		return nativeSize;
	}

	public static MilkFont getDefaultFont() {
		return getFont(STYLE_PLAIN, SIZE_MEDIUM);
	}

	private MilkFontImpl(int style, int size) {
		String key = size + "-" + style;
		style=toNativeStyle(style);
		size=toNativeSize(size);
		font = Font.getFont(Font.FACE_SYSTEM, style, size);
		register.put(key, this);
	}

	public int getHeight() {
		return font.getHeight();
	}

	public int stringWidth(String input) {
		return font.stringWidth(input);
	}

	public int substringWidth(String str, int offset, int len) {
		return font.substringWidth(str, offset, len);
	}

	public int charWidth(char a) {
		return font.charWidth(a);
	}
}
