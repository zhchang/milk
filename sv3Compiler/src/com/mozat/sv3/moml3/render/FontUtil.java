package com.mozat.sv3.moml3.render;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;

import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.utils.IFontUtil;


public class FontUtil implements IFontUtil {
	private final Font[] fonts = new Font[9];
	private final HashMap<Font, FontMetrics> map = new HashMap<Font, FontMetrics>();
	private final Graphics g;

	public FontUtil(Graphics g) {
		this.g = g;
	}

	private FontMetrics getOrCreateMetrics(Font font) {
		FontMetrics metrics = map.get(font);
		if (metrics == null) {
			metrics = g.getFontMetrics(font);
			map.put(font, metrics);
		}
		return metrics;
	}

	public int getAscent(Font font) {
		FontMetrics metrics = getOrCreateMetrics(font);
		return metrics.getMaxAscent();
	}

	public int getFullHeight(Font font) {
		FontMetrics metrics = getOrCreateMetrics(font);
		return metrics.getMaxAscent() + metrics.getMaxDescent();
	}

	public int getStringWidth(Font font, String str) {
		FontMetrics metrics = getOrCreateMetrics(font);
		return metrics.stringWidth(str);
	}

	public int getSubStringWidth(Font font, String str, int start, int length) {
		String sub = str.substring(start, start + length);
		FontMetrics metrics = getOrCreateMetrics(font);
		return metrics.stringWidth(sub);
	}

	@Override
	public Font getFont(byte fontStyle, byte fontSize) {
		int index = fontStyle * 3 + fontSize + 1;
		Font f = fonts[index];
		if (f == null) {
			int size;
			if (fontSize == Sv3Element.FONT_SIZE_M) {
				size = 14;
			} else if (fontSize == Sv3Element.FONT_SIZE_L) {
				size = 16;
			} else { // if(size == FONT_SIZE_S)
				size = 12;
			}

			int style = 0;
			if ((fontStyle & Sv3Element.FONT_STYLE_B) != 0) {
				style |= Font.BOLD;
			} else if ((fontStyle & Sv3Element.FONT_STYLE_I) != 0) {
				style |= Font.ITALIC;
			} else {
			}

			f = new Font(Font.DIALOG, style, size);
			fonts[index] = f;
			FontMetrics m = g.getFontMetrics(f);
			map.put(f, m);
		}

		return f;
	}

	@Override
	public int getAscent(Object font) {
		return getAscent((Font) font);
	}

	@Override
	public int getFullHeight(Object font) {
		return getFullHeight((Font) font);
	}

	@Override
	public int getStringWidth(Object font, String str) {
		return getStringWidth((Font) font, str);
	}

	@Override
	public int getSubStringWidth(Object font, String str, int start, int length) {
		return getSubStringWidth((Font) font, str, start, length);
	}

}
