package milk.chat.core;

import java.util.Vector;
import milk.implement.Adaptor;

import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class Utils {

	public static boolean pointInRect(int x, int y, int rectX, int rectY,
			int w, int h) {
		return x >= rectX && x <= rectX + w && y >= rectY && y <= rectY + h;
	}

	static int getMaxLineWidth(String str[], MilkFont f) {
		int len = 0, maxLen = 0;
		for (int i = 0; i < str.length; i++) {
			len = f.stringWidth(str[i]);
			if (len > maxLen)
				maxLen = len;
		}
		return maxLen;
	}
	
	static int getMaxLineWidth(int lines[]) {
		int maxLen = 0;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i] > maxLen)
				maxLen = lines[i];
		}
		return maxLen;
	}

	public static MilkImage getImage(String resId) {
		Adaptor.getInstance().grabImageResource(resId);
		MilkImage image = Adaptor.getInstance().loadImageResource(resId);
		if (image == null) {
			Adaptor.debug("failed to loading image : " + resId);
		}
		return image;
	}
	
    static String[] getMessageLines(MessageLine line[]){
		String ret[]=new String[line.length];
		for(int i = 0; i < line.length; i++){
			ret[i]=line[i].msg;
		}
		return ret;
	}
    
	static int[] getMessageLineWidth(MessageLine lines[]) {
		int[] ret = new int[lines.length];
		for (int i = 0; i < lines.length; i++) {
			ret[i]=lines[i].lineLen;
		}
		return ret;
	}

	public static String[] autoNewLine(String content, MilkFont font, int areaWidth,
			char SEPERATOR) {
		if (content == null || content.length() == 0) {
			return new String[] { "" };
		}
		Vector vector = new Vector();
		int totalW = 0, index = 0, no = 0;
		for (int i = 0; i < content.length(); i++) {
			totalW += font.charWidth(content.charAt(i));
			no++;
			if (i >= content.length() - 1) {
				vector.addElement(content.substring(index));
				break;
			} else if (content.charAt(i) == SEPERATOR) {
				vector.addElement(content.substring(index, index + no));
				index += no;
				no = 0;
				totalW = 0;
				continue;
			}
			int cnW = font.charWidth(content.charAt(i + 1));// ��һ���ַ�Ŀ��
			if (totalW + cnW > areaWidth) {
				vector.addElement(content.substring(index, index + no));
				index += no;
				no = 0;
				totalW = 0;
			}
		}
		String[] s = new String[vector.size()];
		for (int i = 0; i < s.length; i++) {
			s[i] = (String) vector.elementAt(i);
		}
		vector = null;
		return s;
	}

	static String substringByLen(String content, MilkFont font, int len) {
		int totalW = 0, no = 0;
		for (int i = 0; i < content.length(); i++) {
			if (totalW + font.charWidth(content.charAt(i)) >= len) {
				return content.substring(0, no);
			}
			totalW += font.charWidth(content.charAt(i));
			no++;
		}
		return content;
	}

	public static void info(String info) {
		 System.out.println(info);
	}

	public static void drawScaleImage(MilkGraphics g, MilkImage source, int x,
			int y, int height) {
		int sourceWidth = source.getWidth();
		int sourceHeight = source.getHeight();
		int width = height * sourceWidth / sourceHeight;

		if (sourceWidth != width || sourceHeight != height) {
			int[] rgbData = new int[sourceWidth * sourceHeight];
			source.getRGB(rgbData, 0, sourceWidth, 0, 0, sourceWidth,
					sourceHeight);
			int[] newRgbData = new int[width * height];
			scale(rgbData, width, height, sourceWidth, sourceHeight, newRgbData);
			g.drawRGB(newRgbData, 0, width, x, y, width, height, true);
			// return Image.createRGBImage(newRgbData, width, height, true);
		} else {
			g.drawImage(source, x, y, 0);
		}
		// return source;
	}
	
//	public static MilkImage scaleImage(MilkImage source, int width) {
//		int sourceWidth = source.getWidth();
//		int sourceHeight = source.getHeight();
//		int height = width * sourceHeight / sourceWidth;
//
//		if (sourceWidth != width || sourceHeight != height) {
//			int[] rgbData = new int[sourceWidth * sourceHeight];
//			source.getRGB(rgbData, 0, sourceWidth, 0, 0, sourceWidth,
//					sourceHeight);
//			int[] newRgbData = new int[width * height];
//			scale(rgbData, width, height, sourceWidth, sourceHeight, newRgbData);
//			return MilkImageImpl.createRGBImage(rgbData, width, height,false);
////			g.drawRGB(newRgbData, 0, width, x, y, width, height, true);
//			// return Image.createRGBImage(newRgbData, width, height, true);
//		} else {
//			return source;
//		}
//	}

	private static void scale(int[] rgbData, int newWidth, int newHeight,
			int oldWidth, int oldHeight, int[] newRgbData) {

		int x, y, dy;
		int srcOffset;
		int destOffset;

		// Calculate the pixel ratio ( << 10 )
		final int pixelRatioWidth = (1024 * oldWidth) / newWidth;
		final int pixelRatioHeight = (1024 * oldHeight) / newHeight;

		y = 0;
		destOffset = 0;
		while (y < newHeight) {
			dy = ((pixelRatioHeight * y) >> 10) * oldWidth;
			srcOffset = 0;

			x = 0;
			while (x < newWidth) {
				newRgbData[destOffset + x] = rgbData[dy + (srcOffset >> 10)];
				srcOffset += pixelRatioWidth;
				x++;
			}

			destOffset += newWidth;
			y++;
		}
	}

	public static String getPayTitle(String string, String prefix) {
		int start = string.indexOf(prefix) + 2;
		int end = string.indexOf(",") - 1;
		return string.substring(start, end);
	}

	public static String getPayOptoions(String string, String prefix) {
		int start = string.indexOf(prefix) + prefix.length();
		int end = string.indexOf("]");
		return string.substring(start, end);
	}

	public static String[] splitOptions(String string) {
		int count = 0;
		for (int i = 0; i < string.length(); i++) {
			if (string.charAt(i) == '{') {
				count++;
			}
		}
		String ret[] = new String[count];
		int index = 0;
		while (index < count) {
			int start = string.indexOf("{") + 1;
			int end = string.indexOf("}");
			ret[index] = string.substring(start, end);
			if (index < count)
				string = string.substring(end + 1);
			// info(ret[index]);
			index++;
		}
		return ret;
	}

	public static String getOptoionsItem(String string, String prefix) {
		String ret;
		int start = string.indexOf(prefix) + prefix.length() + 3;
		int end = string.indexOf(",", start);
		if (end > -1) {
			ret = string.substring(start, end - 1);
		} else {
			ret = string.substring(start, string.length() - 1);
		}
		info(prefix + "=" + ret);
		return ret;
	}

	public static String getOptoionActionItem(String string, String prefix) {
		String ret;
		int start = string.indexOf(prefix) + prefix.length() + 2;
		int end = string.indexOf(",", start);
		if (end > -1) {
			ret = string.substring(start, end);
		} else {
			ret = string.substring(start, string.length());
		}
		// info(prefix+"="+ret);
		return ret;
	}
	

	public static byte translateMessageType(byte serverType) {
		byte localType = Def.CHAT_TYPE_PRIVATE;
		switch (serverType) {
		case 1:
			localType = Def.CHAT_TYPE_PRIVATE;
			break;
		case 2:
			localType = Def.CHAT_TYPE_FAMILY;
			break;
		case 3:
			localType = Def.CHAT_TYPE_WORLD;
			break;
		case 4:
			localType = Def.CHAT_TYPE_WORLD_TOP;
			break;
		case 5:
			localType = Def.CHAT_TYPE_SYSTEM;
			break;
		default:
			throw new IllegalArgumentException(
					"serverMessageTypeToClientType msg type:" + serverType);
		}
		return localType;
	}
	
	public static void validateOutChatMessageType(byte messageType) {
		switch (messageType) {
		case Def.CHAT_TYPE_PRIVATE:
		case Def.CHAT_TYPE_FAMILY:
		case Def.CHAT_TYPE_WORLD:
		case Def.CHAT_TYPE_WORLD_TOP:
			break;
		default:
			throw new IllegalArgumentException(
					"validateOutChatMessageType msg type:" + messageType);
		}
	}
	
	public static void validateRoomType(byte roomType) {
		switch (roomType) {
		case Def.CHAT_TYPE_PRIVATE:
		case Def.CHAT_TYPE_FAMILY:
		case Def.CHAT_TYPE_WORLD:
		case Def.CHAT_TYPE_SYSTEM:
			break;
		default:
			throw new IllegalArgumentException(
					"validateOutChatMessageType roomType type:" + roomType);
		}
	}

}
