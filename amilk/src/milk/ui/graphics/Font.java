package milk.ui.graphics;

import milk.implement.Adaptor;
import milk.ui.UIHelper;
import milk.ui.internal.ScreenControl;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.Log;

public class Font {

	public static final int FACE_MONOSPACE = 32;
	public static final int FACE_PROPORTIONAL = 64;
	public static final int FACE_SYSTEM = 0;

	public static final int FONT_INPUT_TEXT = 1;
	public static final int FONT_STATIC_TEXT = 0;

	public static int SIZE_LARGE;
	public static int SIZE_MEDIUM;
	public static int SIZE_SMALL;

	public static final int STYLE_PLAIN = 0;
	public static final int STYLE_BOLD = 1;
	public static final int STYLE_ITALIC = 2;
	public static final int STYLE_UNDERLINED = 4;

	public static final int DEFAULT_NUMBER = -999;

	private static final String tag = Font.class.getName();

	public int face = DEFAULT_NUMBER, style = DEFAULT_NUMBER,
			size = DEFAULT_NUMBER;

	static {
		setDefaultFontSize();
	}

	public Paint getPaint() {
		return milkPaint.getPaint(this, DEFAULT_NUMBER);
	}

	private static void setDefaultFontSize() {
		boolean hd = Adaptor.getInstance().getConfigWidth() * 100
				/ Adaptor.getInstance().getConfigHeight() < 75;
		if (hd) {
			Log.i("font", "screenHegiht"
					+ Adaptor.getInstance().getConfigHeight());
			SIZE_SMALL = (int) (Adaptor.getInstance().getConfigHeight() / 35); // 10
																				// 26
																				// 24
																				// 22
			SIZE_MEDIUM = (int) (Adaptor.getInstance().getConfigHeight() / 33);// 11
			SIZE_LARGE = (int) (Adaptor.getInstance().getConfigHeight() / 31);// 12
		} else if (UIHelper.milk.screenType == ScreenControl.PORTRAIT) {
			Log.i("font", "screenHegiht"
					+ Adaptor.getInstance().getConfigHeight());
			SIZE_SMALL = (int) (Adaptor.getInstance().getConfigHeight() / 26); // 10
																				// 26
																				// 24
																				// 22
			SIZE_MEDIUM = (int) (Adaptor.getInstance().getConfigHeight() / 24);// 11
			SIZE_LARGE = (int) (Adaptor.getInstance().getConfigHeight() / 22);// 12
		} else {
			SIZE_SMALL = (int) (Adaptor.getInstance().getConfigHeight() / 20); // 10
			SIZE_MEDIUM = (int) (Adaptor.getInstance().getConfigHeight() / 16);// 11
			SIZE_LARGE = (int) (Adaptor.getInstance().getConfigHeight() / 12);// 12
		}

	}

	public int charWidth(char ch) {
		char[] charArr = new char[1];
		charArr[0] = ch;
		return (int) getPaint().measureText(charArr, 0, charArr.length);
	}

	private Font(int face, int style, int size) {
		this.face = face;
		this.style = style;
		this.size = size;
	}

	public static Font getFont(int face, int style, int size) {
		Font font = new Font(face, style, size);
		Log.i(Font.class.getName(), "create font");
		return font;
	}

	public int getFace() {
		return face;
	}

	public int getHeight() {
		return (int) getPaint().getTextSize();
	}

	public int getSize() {
		return (int) getPaint().getTextSize();
	}

	public int getStyle() {
		Typeface tf = getPaint().getTypeface();
		return tf.getStyle();
	}

	public boolean isBold() {
		Typeface tf = getPaint().getTypeface();
		return tf.isBold();
	}

	public boolean isItalic() {
		Typeface tf = getPaint().getTypeface();
		return tf.isItalic();
	}

	public boolean isPlain() {
		Typeface tf = getPaint().getTypeface();
		return tf.isItalic();
	}

	public boolean isUnderlined() {
		return getPaint().isUnderlineText();
	}

	public int stringWidth(String str) {
		int strWidth = (int) getPaint().measureText(str);
		return strWidth;
	}

	public int substringWidth(String str, int offset, int len) {
		int strWidth = (int) getPaint().measureText(
				str.substring(offset, offset + len));
		return strWidth;
	}

}