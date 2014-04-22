package com.mozat.sv3.extension;

import java.util.Hashtable;
import java.util.Vector;

import com.mozat.sv3.smartview3.layout.IEmoticon;

public class Emoticon implements IEmoticon {
	private static final int WIDTH = 21, HEIGHT = 21, X_SPACING = 0;

	// public static String SMILEY_STARTS = ":;(+|\\\u2640\u2642";
	private static String SMILEY_TAGS[] = { ":)", ":D", ":(", ":S", ":P", ";)",
			":-O", "(H)", ":'(", ":@", ":|", "(L)", "(U)", "(K)", "(F)", "+o(",
			"|-)", "(?)", "\\please", "\\emm", "\\frozen", "\u2640", "\u2642" };
	private static Hashtable emoticonTable = null;

	public synchronized static Hashtable getEmoticonTable() {
		if (emoticonTable == null) {
			initTables();
		}
		return emoticonTable;
	}

	private static void initTables() {
		emoticonTable = new Hashtable();
		int count = SMILEY_TAGS.length;
		for (int i = 0; i < count; ++i) {
			String tag = SMILEY_TAGS[i];
			Character c = new Character(tag.charAt(0));
			Vector v = null;
			if (emoticonTable.containsKey(c)) {
				v = (Vector) emoticonTable.get(c);
			} else {
				v = new Vector();
				emoticonTable.put(c, v);
			}
			IEmoticon emoticon = new Emoticon(tag, i);
			v.addElement(emoticon);
		}
	}

	String tag;
	int id;
	Object image;
	static Object sharedImage;

	public static void setSharedImage(Object image) {
		sharedImage = image;
	}

	public static Object getSharedImage() {
		return sharedImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see smartview3.layout.IEmoticon#getImage()
	 */
	public Object getImage() {
		return image;
	}

	public void setImage(Object image) {
		this.image = image;
	}

	private Emoticon(String tag, int id) {
		this.tag = tag;
		this.id = id;
	}

	// whether matches second charater onwards
	/*
	 * (non-Javadoc)
	 * 
	 * @see smartview3.layout.IEmoticon#matchTail(java.lang.String, int)
	 */
	public int matchTail(String content, int start) {
		int lenMinus1 = tag.length() - 1;
		int offset = 0;
		if (content.length() >= start + lenMinus1) {
			for (int i = 0; i < lenMinus1; ++i) {
				char c = content.charAt(start + i);
				if (c == '-' && i == 0) {
					offset = 1; // skip '-' if it's the second character
				} else {
					if (c != tag.charAt(i + 1 - offset)) {
						return -1;
					}
				}
			}
			return lenMinus1 + offset;
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see smartview3.layout.IEmoticon#getTag()
	 */
	public String getTag() {
		return tag;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see smartview3.layout.IEmoticon#getId()
	 */
	public int getId() {
		return id;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		return HEIGHT;
	}

	public int getWidth() {
		// TODO Auto-generated method stub
		return WIDTH;
	}

	public int getXSpacing() {
		// TODO Auto-generated method stub
		return X_SPACING;
	}

	public static int getCommonHeight() {
		return HEIGHT;
	}

}
