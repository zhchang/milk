package milk.ui;

import java.util.Hashtable;
import smartview3.elements.Sv3Text;
import android.os.Build;
import milk.implement.Adaptor;
import milk.ui.graphics.Font;
import milk.ui2.MilkFont;

public final class MilkFontImpl implements MilkFont
{
	public Font font;
	static Hashtable register = new Hashtable();

	public static MilkFont getFont(int style, int size)
	{
		String key = size + "-" + style ;
		if (register.containsKey(key))
		{
			return (MilkFontImpl) register.get(key);
		} else
		{
			MilkFontImpl mfi = new MilkFontImpl(style, size);
			register.put(key, mfi);
			return mfi;
		}
		
	}

	public static MilkFont getDefaultFont()
	{
		return getFont(STYLE_PLAIN, SIZE_SMALL);
	}

	private MilkFontImpl(int style, int size)
	{
		style = toNativeStyle(style);
		size = toNativeSize(size);
		font = Font.getFont(Font.FACE_SYSTEM, style, size);
	}


	private static int toNativeStyle(int style)
	{
		int nativeStyle = Font.STYLE_PLAIN;
		switch (style)
		{
		case STYLE_PLAIN:
			nativeStyle = Font.STYLE_PLAIN;
			break;
		case STYLE_UNDERLINED:
			nativeStyle = Font.STYLE_UNDERLINED;
			break;
		case STYLE_BOLD:
			nativeStyle = Font.STYLE_BOLD;
			break;
		case STYLE_ITALIC:
			nativeStyle = Font.STYLE_ITALIC;
			break;
		}
		return nativeStyle;
	}

	private static int toNativeSize(int size)
	{
		int nativeSize = Font.SIZE_MEDIUM;
		switch (size)
		{
		case SIZE_MEDIUM:
			nativeSize = Font.SIZE_MEDIUM;
			break;
		case SIZE_SMALL:
			nativeSize = Font.SIZE_SMALL;
			break;
		case SIZE_LARGE:
			nativeSize = Font.SIZE_LARGE;
			break;
		}
		return nativeSize;
	}

	public int getHeight() {
		initForSpecialDevice();
		return font.getHeight() + offsetYFor_MOTOX702_HTCG7;
	}

	public int stringWidth(String input)
	{
		return font.stringWidth(input);
	}

	public int substringWidth(String str, int offset, int len)
	{
		return font.substringWidth(str, offset, len);
	}

	public int charWidth(char a)
	{
		return font.charWidth(a);
	}
	
	private static int offsetYFor_MOTOX702_HTCG7=0;
	private static void initForSpecialDevice(){
		if("cn".equalsIgnoreCase(Adaptor.getInstance().language)){
			 if("HTC EVO 3D X515m".equalsIgnoreCase(Build.MODEL)
					 ||"Milestone".equalsIgnoreCase(Build.MODEL)){
				 offsetYFor_MOTOX702_HTCG7=4;
				 Sv3Text.setOffsetYForAndroidDevice(offsetYFor_MOTOX702_HTCG7);
			 }
		}
	}

}
