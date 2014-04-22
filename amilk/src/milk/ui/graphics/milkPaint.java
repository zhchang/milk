package milk.ui.graphics;

import java.util.Hashtable;

import android.R.integer;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Style;
import android.util.Log;

public class milkPaint {
	static Hashtable paintTable = new Hashtable();

	private static String getKey(Font f, int color) {
		return f.face + "-" + f.size + "-" + f.style + "-" + color;
	}

	public static Paint getPaint(Font f, int color) {
		String key = getKey(f, color);
		if (paintTable.containsKey(key)) {
			return (Paint) paintTable.get(key);
		}
		Paint p = new Paint();
		p.setStyle(Style.STROKE);
		setPaintAtt(p, f, color);
		paintTable.put(key, p);
		return p;
	}

	public static void setPaint(Paint p, Font f, int color) {
		setPaintAtt(p, f, color);
	}

	private static void setPaintAtt(Paint paint, Font f, int color) {
		if (f == null) {
			Log.i("font", "font is null");
			return;
		}
		int face = f.face;
		int style = f.style;
		int size = f.size;

		if (paint == null) {
			paint = new Paint();
		}

		if (face != Font.DEFAULT_NUMBER) {

			switch (face) {
			case Font.FACE_MONOSPACE:
				paint.setTypeface(Typeface.MONOSPACE);
				break;
			case Font.FACE_PROPORTIONAL:
				break;
			case Font.FACE_SYSTEM:
				paint.setTypeface(Typeface.DEFAULT);
				break;
			}
		}

		paint.setAntiAlias(true);
		if (style != Font.DEFAULT_NUMBER) {
			switch (style) {
			case Font.STYLE_BOLD:
				paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
				break;
			case Font.STYLE_ITALIC:
				paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
				break;
			case Font.STYLE_PLAIN:
				paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				break;
			case Font.STYLE_UNDERLINED:
				paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
				break;
			}
		}

		if (size != Font.DEFAULT_NUMBER) {
			paint.setTextSize(size);
				
		}

		if (color != Font.DEFAULT_NUMBER) {
			paint.setColor(Color.rgb(Color.red(color), Color.green(color),
					Color.blue(color)));
		}
	}

}
