package milk.chat.core;

import java.util.Vector;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class FaceCore implements FaceCoreHandler {

	private String nameloadTable[] = { 
			"face_bj", "face_dx","face_wx", "face_fn", "face_hx", 
			"face_jk", "face_js", "face_jy","face_wq", "face_zm" ,
			
//			"face_bj", "face_dx","face_wx", "face_fn", "face_hx", 
//			"face_jk", "face_js", "face_jy","face_wq", "face_zm" ,
	};
	private String nameShowTable[] = { 
			"[bj]", "[dx]", "[wx]","[fn]", "[hx]", 
			"[jk]", "[js]", "[jy]","[wq]", "[zm]",
			
//			"[bj]", "[dx]", "[wx]","[fn]", "[hx]", 
//			"[jk]", "[js]", "[jy]","[wq]", "[zm]",
	};
	
	private int FACE_DRAW_SIZE;
	private final int FACE_STR_LENGTH = 4;
	private MilkImage face[];

	public FaceCore() {
		loadFaceImage();
	}
	
	public void setFaceArray(MilkImage imgFace[],String showName[]){
		this.face=imgFace;
		nameShowTable=showName;
	}
	
	private void loadFaceImage(){
		face = new MilkImage[nameloadTable.length];
		for (int i = 0; i < face.length; i++) {
			face[i] = Utils.getImage(getFaceImageLoadName(i));
		}
	}
	
	public String getFaceImageLoadName(int index){
		return "chat-" + nameloadTable[index];
	}
	
	public MilkImage[] getFaceImageList(){
		return face;
	}

	public void setFaceHeight(int fontHeight){
		FACE_DRAW_SIZE = fontHeight - 2;
//		Utils.info("----------------fontHeight:" + fontHeight + "/FACE_DRAW_SIZE:"
//				+ FACE_DRAW_SIZE);
	}

	public int getImageListNum() {
		return nameloadTable.length;
	}
	
	public String getFaceString(int index){
		return nameShowTable[index];
	}

	public int getDrawFaceSize() {
		return FACE_DRAW_SIZE;
	}

	public void drawMessageByLanguage(MilkGraphics g, String message, int x,
			int y, int w, boolean isLanguageAr) {
		int drawX = x;
		if (isLanguageAr)
			drawX = x + w - getMessageDrawWidth(g.getFont(), message);
		drawMessage(g, message, drawX, y,0);
	}

	public void drawMessageWithOutLanguage(MilkGraphics g, String string,
			int x, int y, int w) {
		drawMessage(g, string, x, y,0);
	}

	public final int getMessageDrawWidth(MilkFont font, String messageOri) {
		String message = String.valueOf(messageOri);
		int width = 0;
		int start = message.indexOf('[');
		if (start == -1) {
			width = font.stringWidth(message);
		} else {
			if (start != 0) {
				String temp = message.substring(0, start);
				width += font.stringWidth(temp);
				message = message.substring(start);
			}
			int next = message.indexOf(']');
			if (next != -1) {
				String first4String = message.substring(0, FACE_STR_LENGTH);
				if (isFace(first4String)) {
					MilkImage imgface = getFaceImage(first4String);
					if (imgface != null) {
						width += FACE_DRAW_SIZE;
					} else {
//						Utils.info("-imgface=null-emotion:" + first4String);
						width += font.stringWidth(first4String);
					}
					if (next + 1 <= message.length() - 1) {
						message = message.substring(next + 1);
						width += getMessageDrawWidth(font, message);
					}
				} else {
					width += font.stringWidth(first4String);
					if (message.length() > 4) {
						message = message.substring(4);
						width += getMessageDrawWidth(font, message);
					}
				}
			} else {
				width += font.stringWidth(message);
			}
		}
		return width;
	}
	
	private final Vector splitQueue = new Vector();
	public MessageLine[] splitFaceMessage(final String message, MilkFont font,
			int areaWidth) {
		if (message == null || message.length() == 0) {
			throw new NullPointerException("FaceMessage=null");
		}
		int totalWidth=getRealLineWidth( message,  font);
		if (totalWidth <= areaWidth) {
			MessageLine line[]=new MessageLine[]{new MessageLine(message,totalWidth)};
			return line;
		}
		Utils.info("------------splitFaceMessage message:"+message);
		int lineWidth[]=new int[10],line=0;
		splitQueue.removeAllElements();
		int drawTotalW = 0, lineStart = 0,lastCanBreakIndex=0;
		for (int i = 0; i < message.length(); i++) {	
			final char currChar=message.charAt(i);
			if (message.charAt(i) == '['
					&& i + FACE_STR_LENGTH - 1 < message.length()
					&& message.charAt(i + FACE_STR_LENGTH - 1) == ']'
					&& isFace(message.substring(i, i + FACE_STR_LENGTH))) {//face
				if (drawTotalW + FACE_DRAW_SIZE > areaWidth) {
//					vector.addElement(message.substring(lineStart, i));
//					lineWidth[line]=drawTotalW;
					String msg=message.substring(lineStart, i);
					splitQueue.addElement(msg);
					lineWidth[line]=getRealLineWidth(msg,font);
					line++;
					lineStart = i;
					drawTotalW = 0;
				} else {
					i += FACE_STR_LENGTH - 1;
					lastCanBreakIndex = i + 1;
					drawTotalW += FACE_DRAW_SIZE;
					if (i >= message.length() - 1) {//end by face
						String msg=message.substring(lineStart);
						splitQueue.addElement(msg);
						lineWidth[line]=getRealLineWidth(msg,font);
						line++;
//						Utils.info(i+"------------msg:"+drawTotalW);
						break;
					}
				}
			}
			else
			if (i >= message.length() - 1) {//end
				String msg=message.substring(lineStart);
				splitQueue.addElement(msg);
				lineWidth[line]=getRealLineWidth(msg,font);
				line++;
				break;
			} 
			else if(currChar=='\n'){//must break
				String msg = message.substring(lineStart, i);
				splitQueue.addElement(msg);
				lineWidth[line] = getRealLineWidth(msg, font);
				line++;
				lineStart = i+1;
				if (lineStart > message.length() - 1) {
					break;
				}
				else
				drawTotalW = font.charWidth(message.charAt(i +1+1));
			}
			else {
				if (isCanBreak(currChar)) {
					lastCanBreakIndex = i + 1;
				}
				drawTotalW += font.charWidth(message.charAt(i));
				int nextCharW = font.charWidth(message.charAt(i + 1));// ��һ���ַ�Ŀ��
				if (drawTotalW + nextCharW > areaWidth) {
					if (!isCanBreak(currChar)&&lastCanBreakIndex!=0&&lastCanBreakIndex>lineStart) {
						String msg = message.substring(lineStart, lastCanBreakIndex);
						splitQueue.addElement(msg);
						lineWidth[line] = getRealLineWidth(msg, font);
						line++;
						lineStart = lastCanBreakIndex;
						i = lastCanBreakIndex;
						drawTotalW = font.charWidth(message.charAt(i + 1));
					} else {
						String msg = message.substring(lineStart, i);
						splitQueue.addElement(msg);
						lineWidth[line] = getRealLineWidth(msg, font);
						line++;
						lineStart = i;
						drawTotalW = font.charWidth(message.charAt(i + 1));
					}
				}
			}
		}
		MessageLine[] ret = new MessageLine[splitQueue.size()];
		
		for (int i = 0; i < ret.length; i++) {
			String msg=(String) splitQueue.elementAt(i);
			ret[i] = new MessageLine(msg,lineWidth[i]);
//			Utils.info(i+"------------msg:"+msg+" / len:"+lineWidth[i]);
		}
		return ret;
	}
	
	private final char splitCharList[]={' ', '\r', '\n', ',', '.', '!','?',';',':','<','>','{','}','[',']',//en
			                            ' ',            
			                            };
	
	private boolean isCanBreak(char c) {
		for (int i = 0; i <splitCharList.length; i++) {
			if (c == splitCharList[i]) {
				return true;
			}
		}
		return false;
	}

	public String subMessageByLength(String message, MilkFont font, int subLen) {
		if (message == null || message.length() == 0) {
			throw new NullPointerException("FaceMessage=null");
		}
		if (font.stringWidth(message) <= subLen) {
			return message;
		}
		int drawTotalW = 0;
		for (int i = 0; i < message.length(); i++) {
			boolean isFace = false;
			if (message.charAt(i) == '['
					&& i + FACE_STR_LENGTH-1 < message.length()) {
				String faceString = message.substring(i, i + FACE_STR_LENGTH);
				if (isFace(faceString)) {
					isFace = true;
				}
			}
			if (i >= message.length() - 1) {
				return message;
			}
			if (!isFace)
				drawTotalW += font.charWidth(message.charAt(i));
			int nextCharW = font.charWidth(message.charAt(i + 1));// ��һ���ַ�Ŀ��
			if (isFace) {
				if (drawTotalW + FACE_DRAW_SIZE > subLen) {
					return message.substring(0, i);
				} else {
					i += FACE_STR_LENGTH - 1;
					drawTotalW += FACE_DRAW_SIZE;
				}
			} else if (drawTotalW + nextCharW > subLen) {
				return message.substring(0, i);
			}
		}

		return message;
	}

	private final boolean isFace(String faceName) {
		for (int i = 0; i < nameShowTable.length; i++) {
			if (faceName.equals(nameShowTable[i])) {
				return true;
			}
		}
		return false;
	}

	private MilkImage getFaceImage(String faceName) {
		for (int i = 0; i < nameShowTable.length; i++) {
			if (faceName.equals(nameShowTable[i])) {
				return face[i];
			}
		}
		return null;
	}

	private void drawMessage(MilkGraphics g, final String msg, int x,int y,int index) {
		MilkFont font = g.getFont();
		int start = msg.indexOf('[',index);
		int end = msg.indexOf(']',index);
		if (start == -1 || end == -1) {
			g.drawString(msg.substring(index), x, y, MilkGraphics.TOP | MilkGraphics.LEFT);
			return;
		}
		if (end != start + 3 || !isFace(msg.substring(start, end + 1))) {
			String temp = msg.substring(index, end + 1);
			g.drawString(temp, x, y, MilkGraphics.TOP | MilkGraphics.LEFT);
			x += font.stringWidth(temp);
			if (end + 1 < msg.length()) {
				drawMessage(g, msg, x, y, end + 1);
			}
			return;
		}
		//face string 
		if (start != index) {
			String temp = msg.substring(index, start);
			g.drawString(temp, x, y, MilkGraphics.TOP | MilkGraphics.LEFT);
			x += font.stringWidth(temp);
		}
		MilkImage imgface = getFaceImage(msg.substring(start, end + 1));
		int dy = 0;
		if ((System.currentTimeMillis() / 200) % 2 > 0) {
			dy = 1;
		}
		Utils.drawScaleImage(g, imgface, x, y + dy, FACE_DRAW_SIZE);
		x += FACE_DRAW_SIZE;
		if (end + 1 < msg.length()) {
			drawMessage(g, msg, x, y, end + 1);
		}
	}
	
	private int getRealLineWidth(String message, MilkFont font) {
		int width = 0;
		for (int i = 0; i < message.length(); i++) {
//			Utils.info(message.charAt(i)+"");
			if (message.charAt(i) == '['
					&& i + FACE_STR_LENGTH - 1 < message.length()
					&& message.charAt(i + FACE_STR_LENGTH - 1) == ']'
					&& isFace(message.substring(i, i + FACE_STR_LENGTH))) {
				width += (font.stringWidth(message.substring(i, i + FACE_STR_LENGTH))-FACE_DRAW_SIZE);
				i += FACE_STR_LENGTH - 1;
			} 
//			else {
//				width += font.charWidth(message.charAt(i));
//			}
		}
		return font.stringWidth(message)-width;
	}
}
