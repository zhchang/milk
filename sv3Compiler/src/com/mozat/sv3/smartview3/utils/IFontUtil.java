package com.mozat.sv3.smartview3.utils;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * 
 * @author luyx
 */
public interface IFontUtil {

	int getAscent(Object font);

	Object getFont(byte fontStyle, byte fontSize);

	int getFullHeight(Object font);

	int getStringWidth(Object font, String str);

	int getSubStringWidth(Object font, String str, int start, int length);

}
