package milk.ui;

import java.util.Hashtable;

import milk.ui2.MilkFont;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.FontFamily;
import net.rim.device.api.ui.Ui;

public class MilkFontImpl implements MilkFont {

	private static FontFamily family;
	Font font;
	private static Hashtable register = new Hashtable();
	private static int SMALL = Display.getHeight() / 24;
	private static int MEDIUM = Display.getHeight() / 20;
	private static int LARGE = Display.getHeight() / 16;

	static {
		if (Display.getHeight() > Display.getWidth()) {
			SMALL = Display.getHeight() / 28;
			MEDIUM = Display.getHeight() / 26;
			LARGE = Display.getHeight() / 24;
		}
	}

	private static int toNativeStyle(int style) {
		int nativeStyle = MilkFontImpl.STYLE_PLAIN;
		switch (style) {
		case MilkFont.STYLE_PLAIN:
			nativeStyle = Font.PLAIN;
			break;
		case MilkFont.STYLE_UNDERLINED:
			nativeStyle = Font.UNDERLINED;
			break;
		case MilkFont.STYLE_BOLD:
			nativeStyle = Font.BOLD;
			break;
		case MilkFont.STYLE_ITALIC:
			nativeStyle = Font.ITALIC;
			break;
		}
		return nativeStyle;
	}

	private static int toNativeSize(int size) {
		int nativeSize = MilkFontImpl.MEDIUM;
		switch (size) {
		case MilkFont.SIZE_MEDIUM:
			nativeSize = MilkFontImpl.MEDIUM;
			break;
		case MilkFont.SIZE_SMALL:
			nativeSize = MilkFontImpl.SMALL;
			break;
		case MilkFont.SIZE_LARGE:
			nativeSize = MilkFontImpl.LARGE;
			break;
		}
		return nativeSize;
	}

	private static void load() {
		try {
			if (family == null)
				family = FontFamily.forName(FontFamily.FAMILY_SYSTEM);
		} catch (Exception e) {
			e.printStackTrace();

		}
		if (family == null) {
			family = FontFamily.getFontFamilies()[0];
		}
	}

	public static MilkFont getFont(int style, int size) {
		int bbstyle = toNativeStyle(style);
		int bbsize = toNativeSize(size);
		String key = bbsize + "-" + bbstyle;
		if (register.containsKey(key)) {
			return (MilkFont) register.get(key);
		} else {
			return new MilkFontImpl(bbstyle, bbsize);
		}
	}

	public static MilkFont getDefaultFont() {
		return getFont(Font.PLAIN, MilkFontImpl.SMALL);
	}

	public static MilkFont getFontByHeight(int fontHeight) {
		return new MilkFontImpl(Font.PLAIN, fontHeight);
	}

	private MilkFontImpl(int bbstyle, int bbsize) {
		load();
		String key = bbsize + "-" + bbstyle;
		if (!family.isStyleSupported(bbstyle)) {
			this.font = net.rim.device.api.ui.Font.getDefault();
		} else {
			for (int size = bbsize; size < 100; size++) {
				net.rim.device.api.ui.Font font = family.getFont(bbstyle, size,
						Ui.UNITS_px);
				if (font.getHeight() >= bbsize) {
					// return the found font
					this.font = font;
					break;
				}
			}
		}
		if (this.font == null) {
			this.font = net.rim.device.api.ui.Font.getDefault();
		}
		// System.out.println("--------bb font----font:"+font);
		// System.out.println("--------bb font----font height:"+font.getHeight());
		register.put(key, this);
	}

	public int getHeight() {
		return font.getHeight();
	}

	public int stringWidth(String input) {
		return font.getAdvance(input);
	}

	public int substringWidth(String str, int offset, int len) {
		return font.getAdvance(str, offset, len);
	}

	public int charWidth(char a) {
		return font.getAdvance(a);
	}
}
