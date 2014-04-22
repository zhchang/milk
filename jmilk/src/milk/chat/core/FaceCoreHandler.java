package milk.chat.core;

import milk.ui2.MilkFont;
import milk.ui2.MilkImage;

import milk.ui2.MilkGraphics;
//import milk.ui2.MilkImage;

public interface FaceCoreHandler {
	
	MessageLine[] splitFaceMessage(String string, MilkFont font, int areaWidth);
	
	void drawMessageByLanguage(MilkGraphics g, String string, int x, int y,int w,boolean isLanguageAr);
	
	int getDrawFaceSize();
	
	String subMessageByLength(String message, MilkFont font, int subLen);
	
	int getMessageDrawWidth(MilkFont font,String message);
	
	void drawMessageWithOutLanguage(MilkGraphics g, String string, int x, int y,int w);
	
	MilkImage[] getFaceImageList();
	
	int getImageListNum();
	
	String getFaceImageLoadName(int index);
	
	String getFaceString(int index);
	
	void setFaceHeight(int fontHeight);
	
	void setFaceArray(MilkImage imgFace[],String showName[]);
	
}
