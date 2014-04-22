package milk.ui2;

public interface MilkFont {

	int SIZE_SMALL = 1;
	int SIZE_MEDIUM = 2;
	int SIZE_LARGE = 3;
	
	int STYLE_PLAIN = 4;
	int STYLE_UNDERLINED = 5;
	int STYLE_BOLD = 6;
	int STYLE_ITALIC = 7;
	
	int stringWidth(String str);
	int getHeight() ;
	int substringWidth(String str, int offset, int len);
	int charWidth(char a);
	
}
