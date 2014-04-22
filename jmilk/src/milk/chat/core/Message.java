package milk.chat.core;

import milk.chat.port.CoreListener;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class Message {

	private static final int WORLD_TOP_MESSAGE_LINE = 5;
	private static int nameColor = 0xfff694;
	private static int youToColor = 0x00e360;
	private static int lineColor = 0xd5af78;
	private static MilkImage topMessageArrow;

	private static long lastTopMessageValidTime;
	private byte type;
	private String content;
	private String contentLines[];
	private String notifyLines[];
	private String fromName;
	private String toName;
	private String privateChatShowName;
	
	private int fromId;
	private int toId;
	private boolean sendByMyself;
	private String messageId;
	private String strTypeTitle;
	private int titleColor;
	private int contentColor = 0x000000;
	private long createTime;
	private static FaceCoreHandler faceHandler;
	private static int lineH;
	private static String topMessageNotify[];
	
	private static boolean paintBubble=false;
	private static int bubbleArcSize=5;
	
	public Message() {
		resetTopMessageNotifyInfo();
	}
	
	
	public void destoryMessage(){
		content=null;
		contentLines=null;
		notifyLines=null;
		fromName=null;
		toName=null;
		privateChatShowName=null;
		messageId=null;
		strTypeTitle=null;
	}
	
	public static void setPaintBubble(int arcSize){
//		paintBubble=true;
//		nameColor = 0x000000;
//	    youToColor = 0x000000;
	    bubbleArcSize=arcSize;
	}
	
	public static void initAndroidMessage(){
		paintBubble=true;
		nameColor = 0x000000;
	    youToColor = 0x000000;
	}
	
	static void initFaceHandler(FaceCoreHandler face){
		faceHandler=face;
	}

	public static void setLineHeight(int h) {
		lineH = h;
	}
	
	public int getTitleHeight(){
		return lineH;
	}
	
	public static void initL10nString(){
		topMessageNotify = HallCore.getTopMessageNotify();
	}
	
	public void resetTopMessageNotifyInfo() {
		topMessageNotify = HallCore.getTopMessageNotify();
		type = Def.CHAT_TYPE_WORLD_TOP;
		contentColor = titleColor = 0xfff000;
		if (paintBubble) {
			contentColor = titleColor = 0x000000;
		}
		createTime = 0;
		messageId = "";
		sendByMyself = true;
	}

	public static int getTopMessageValidTimeSecond() {
		return (int) (lastTopMessageValidTime - System.currentTimeMillis()) / 1000;
	}

	public static void setTopMessageValidTime(long durationTime) {
		lastTopMessageValidTime = System.currentTimeMillis() + durationTime;
	}

	private int lineWidth[];
	public Message(byte type, int senderId, String senderName, int toId,
			String toName, String contentLine[], boolean sendByMyself,
			String messageId, String oriContent, int duration,int lineWidth[]) {

		this.lineWidth=lineWidth;
		this.type = type;
		this.fromName = senderName;
		this.fromId = senderId;
		this.toId = toId;
		this.toName = toName;
		this.contentLines = contentLine;
		this.sendByMyself = sendByMyself;
		this.messageId = messageId;
		this.content = oriContent;
		if (type == Def.CHAT_TYPE_PRIVATE) {
			if(this.isSendByMyself()){
				this.privateChatShowName=HallAccess.getL10nString(Def.toWho, toName);
			}
			else{
				this.privateChatShowName=HallAccess.getL10nString(Def.whoSaid, fromName);
			}
		}
		switch (this.type) {
		case Def.CHAT_TYPE_PRIVATE:
			strTypeTitle = Def.msgTitlePrivate;
			titleColor = 0x00e360;
			break;
		case Def.CHAT_TYPE_FAMILY:
			strTypeTitle = Def.msgTitleTribe;
			titleColor = 0x00dae8;
			break;
		case Def.CHAT_TYPE_WORLD:
			strTypeTitle = Def.msgTitleWorld;
			titleColor = 0xfff000;
			break;
		case Def.CHAT_TYPE_SYSTEM:
			strTypeTitle = Def.msgTitleSystem;
			contentColor = titleColor = 0xbb0000;
			break;
		case Def.CHAT_TYPE_WORLD_TOP:
			strTypeTitle = Def.msgTitleTop;
			contentColor = titleColor = 0xfff000;
			setTopMessageValidTime(duration);
			break;
		default:
			throw new IllegalArgumentException("ChatMessage type:" + type);
		}
		if (paintBubble) {
			contentColor = titleColor = 0x000000;
		}
		createTime = System.currentTimeMillis();
	}

	public void initTopNotifyBodyString(MilkFont font, int width) {
		if (notifyLines == null){
			MessageLine lines[]=faceHandler.splitFaceMessage(content, font, width);
			notifyLines = Utils.getMessageLines(lines);
//					faceHandler.splitFaceMessage(content, font, width);
		}
	}

	public void initNormalNotifyBodyString() {
		if (notifyLines == null) {
			notifyLines = new String[1];
			notifyLines[0] = String.valueOf(contentLines[0]);
		}
	}

	public void setNextNotifyMessage(Message next) {
		this.nextNotify = next;
	}

	public String getMessageId() {
		return messageId;
	}

	public long getCreateTime() {
		return createTime;
	}

	void updateTime() {
		createTime = System.currentTimeMillis();
	}


	public void drawTopNotifyTabTitle(MilkGraphics g, int x, int y, int w, int h,
			boolean isLanguageAr) {
		int time = getTopMessageValidTimeSecond();
		int drawStartX = x;
		if (isLanguageAr) {
			MilkFont font = g.getFont();
			int titleWidth = getTitleDrawWidth(font);
			if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
				if (time > 0) {
					String info = "("+ HallAccess.getL10nString(Def.timeLeft, time + "") + ")";
					titleWidth += font.stringWidth(info);
				}
			}
			drawStartX = x + w - titleWidth;
		}
		if(paintBubble){//android
			drawTopTabMessageTitle(g, drawStartX, y, w, h, time,true);
		}
		else
		   drawTopTabMessageTitle(g, drawStartX, y, w, h, time,false);
	}
	
	private void drawTopTabMessageTitle(MilkGraphics g, int x, int y, int w, int h,
			int validSecond, boolean isTabInAndroid) {
		int drawAddX = 0;
		if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
			int iconSize = faceHandler.getDrawFaceSize();
			int offfsetY = 0;
			if (validSecond > 0 && (System.currentTimeMillis() / 200) % 2 > 0) {
				offfsetY = 1;
			}
			if (!isTabInAndroid) {
				if (topMessageArrow == null)
					topMessageArrow = Utils.getImage("chat-topmessagearrow");
				Utils.drawScaleImage(g, topMessageArrow, x, y + offfsetY,
						iconSize);
				drawAddX += iconSize;
			}
		}

		drawAddX += drawTitle(g, x + drawAddX, y);
		g.setColor(0xdd0b0b);
		String name = getSenderName();
		g.drawString(name, x + drawAddX, y, 0);
		drawAddX += g.getFont().stringWidth(name);

		drawAddX += drawColon(g, x + drawAddX, y);
		if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
			if (validSecond > 0) {
				String info = "("+ HallAccess.getL10nString(Def.timeLeft,validSecond + "") + ")";
				g.drawString(info, x + drawAddX, y, 0);
			}
		}
	}

//	private static final MArray array = new MArray();

	public void drawTopNotifyTabBody(MilkGraphics g, int x, int y, int w, int h,
			boolean isLanguageAr) {
		g.clipRect(x, y, w, h);
		String body[] = notifyLines;
		if (faceHandler != null) {
			g.setColor(0x4d0e0e);
			if (isLanguageAr) {
				faceHandler.drawMessageByLanguage(g, body[notifyRollMsgIndex], x, y
						- notifyRollY, w, isLanguageAr);
			} else
				faceHandler.drawMessageByLanguage(g, body[notifyRollMsgIndex], x, y
						- notifyRollY, w, isLanguageAr);
			if (isRollingMsg) {
				if (notifyRollMsgIndex + 1 > body.length - 1) {
					faceHandler.drawMessageByLanguage(g, body[0], x, y - notifyRollY
							+ h, w, isLanguageAr);
				} else {
					faceHandler.drawMessageByLanguage(g,
							body[notifyRollMsgIndex + 1], x, y - notifyRollY
									+ h, w, isLanguageAr);
				}
			}
			if (body.length > 1)
				updateTopNotifyRollY(h, body.length);
		} else {
			throw new NullPointerException(
					"drawRollTab(),faceMessageHandler=null");
		}
	}

	private int notifyRollMsgIndex = 0;
	private static final int NOTIFY_ROLL_INTERVAL_TIME = 4000;
	private long lastNotifyRollTime = System.currentTimeMillis();
	private int notifyRollY = 0;
	private boolean isRollingMsg = false;

	private void updateTopNotifyRollY(int lineH, int strArrayLen) {
		if (isRollingMsg) {
			if (notifyRollY > lineH) {
				notifyRollY = 0;
				isRollingMsg = false;
				notifyRollMsgIndex++;
				if (notifyRollMsgIndex >= strArrayLen) {
					notifyRollMsgIndex = 0;
				}
				lastNotifyRollTime = System.currentTimeMillis();
			} else {
				notifyRollY += 2;
			}
		} else if (System.currentTimeMillis() - lastNotifyRollTime > NOTIFY_ROLL_INTERVAL_TIME) {
			lastNotifyRollTime = System.currentTimeMillis();
			isRollingMsg = true;
		}
	}
	
	private int getTitleDrawWidth(MilkFont font) {
		int drawLen = 0;
		if (type == Def.CHAT_TYPE_WORLD_TOP) {
			drawLen += faceHandler.getDrawFaceSize();// icon
		}
		if (type != Def.CHAT_TYPE_SYSTEM) {
			drawLen += getNameDrawWidth(font);
			drawLen += font.stringWidth(":");
		}
		return drawLen;
	}
	
	private int getNameDrawWidth(MilkFont font) {
		String name = getSenderName();
		int nameWidth = font.stringWidth(name);
		if (type == Def.CHAT_TYPE_PRIVATE) {
			if (isSendByMyself())
				nameWidth = font.stringWidth(this.privateChatShowName);
			else
				nameWidth = font.stringWidth(this.privateChatShowName);

		}
//		if (type == Def.CHAT_TYPE_PRIVATE) {
//			if (isSendByMyself()) {
//				return font.stringWidth(Def.chatTo) + nameWidth;
//			} else {
//				return font.stringWidth(Def.chatSaid) + nameWidth;
//			}
//		} else {
//			return nameWidth;
//		}
		return nameWidth;
	}

	private int drawMessageTitle(MilkGraphics g, int x, int y) {
		int drawLength = 0;
		drawLength += drawTitle(g, x + drawLength, y);
		if (type != Def.CHAT_TYPE_SYSTEM) {
			drawLength += drawName(g, x + drawLength, y);
		}
		drawLength += drawColon(g, x + drawLength, y);
		return drawLength;
	}

	private Message nextNotify;

	public void drawNormalNotifyTabBody(MilkGraphics g, int x, int y, int w, int h,boolean isLanguageAr) {
		MilkFont font = g.getFont();
		String nailStr = "...";
		int nailLen = font.stringWidth(nailStr);
		g.clipRect(x, y, w, h);
		drawNextMessage(g, x, y, w, h);

		int drawLength = drawMessageTitle(g, x, y - notifyRollY);
		g.setColor(0x4d0e0e);
		String current = notifyLines[0];
		int msgBodyLen = w - drawLength - nailLen / 2;
		String msgString = null;
		if (font.stringWidth(current) <= msgBodyLen) {
			msgString = current;
		} else if (msgBodyLen > 8) {
			msgString = faceHandler.subMessageByLength(current, font,msgBodyLen);
			if (msgString != null && msgString.length() > 0) {
				msgString = msgString + nailStr;
			} else {
				msgString = nailStr;
			}
		} else {
			msgString = nailStr;
		}

		faceHandler.drawMessageWithOutLanguage(g, msgString, x + drawLength + 2, y
				- notifyRollY,w);

	}

	private void drawNextMessage(MilkGraphics g, int x, int y, int w, int h) {
		if (nextNotify != null) {// needs roll
			if (notifyRollY + 4 > h) {
				notifyRollY = 0;
				notifyLines[0] = String.valueOf(nextNotify.notifyLines[0]);
				nextNotify = null;
			} else {
				int temp = nextNotify.drawMessageTitle(g, x, y - notifyRollY+ h);
				g.setColor(0x4d0e0e);
				faceHandler.drawMessageWithOutLanguage(g, nextNotify.notifyLines[0], x
						+ temp + 2, y - notifyRollY + h,w);
				notifyRollY += 4;
			}
		}
	}
	
	public void drawBubbleMessage(MilkGraphics g, final int msgRectX, final int msgRectY, final int msgRectW, 
			boolean focus,boolean isLanguageAr,
			int bubbleTopY,int bubbleLeftX,int bubbleRightX,int bubbleArc) {
		int time = getTopMessageValidTimeSecond();
		int msgTitleDrawX = msgRectX;
		String topMessageTime =null;
		MilkFont font = g.getFont();
		if (isLanguageAr||isSendByMyself()) {
			int titleWidth = getTitleDrawWidth(font);
			if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
				if (time > 0) {
					topMessageTime = "("+ HallAccess.getL10nString(Def.timeLeft, time + "") + ")";
					titleWidth += font.stringWidth(topMessageTime);
				}
			}
			msgTitleDrawX = msgRectX + msgRectW - titleWidth;
			if (isSendByMyself()) {
				msgTitleDrawX = bubbleRightX - titleWidth - bubbleArc;
			}
		}
		
		final int topY=msgRectY;
		int titleRealDrawX=0;
		if (isLanguageAr){
			titleRealDrawX = msgTitleDrawX;
		}
		
		// android
		if (isSendByMyself())
			titleRealDrawX = msgTitleDrawX;
		else
			titleRealDrawX = bubbleLeftX;
		
		final int titleRealDrawY = bubbleTopY - font.getHeight();
		
		int drawAddX = 0;
		if (type == Def.CHAT_TYPE_WORLD_TOP){
			drawAddX = drawTopMessageTitle(g, titleRealDrawX, titleRealDrawY, time,drawAddX);
		} else {
			if (type != Def.CHAT_TYPE_SYSTEM) {
				if (isLanguageAr) {
					drawAddX += drawColon(g, titleRealDrawX + drawAddX, titleRealDrawY);
				}
				drawAddX += drawName(g, titleRealDrawX + drawAddX, titleRealDrawY);
				if (!isLanguageAr){
					drawAddX += drawColon(g, titleRealDrawX + drawAddX, titleRealDrawY);
				}
			}
		}

		if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
			if (time > 0) {
				if(topMessageTime!=null){
				   g.drawString(topMessageTime, titleRealDrawX + drawAddX, titleRealDrawY, 0);
				}
			}
		}

		boolean ifRightAlign=false;
//		if (isSendByMyself()) {
//			ifRightAlign=false;
//		}
//		else 
		if(isLanguageAr){
			ifRightAlign=true;
		}
	   
		int contentRealDrawY=topY+lineH;
		g.setColor(contentColor);
		String body[] = getContent();
		for (int i = 0; i < body.length; i++) {
			int lineRealDrawX=bubbleLeftX+bubbleArc;
			if(ifRightAlign){
				lineRealDrawX=bubbleRightX-bubbleArc-lineWidth[i];
			}
			faceHandler.drawMessageWithOutLanguage(g, body[i], lineRealDrawX, contentRealDrawY,msgRectW);
			contentRealDrawY += lineH;
		}
	}
	
	public void draw(MilkGraphics g, int x, int y, int w, boolean focus,boolean isLanguageAr) {
		int time = getTopMessageValidTimeSecond();
		int drawStartX = x;
		String topMessageTime =null;
		MilkFont font = g.getFont();
		if (isLanguageAr||(paintBubble&&this.isSendByMyself())) {
			
			int titleWidth = getTitleDrawWidth(font);
			if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
				if (time > 0) {
					topMessageTime = "("+ HallAccess.getL10nString(Def.timeLeft, time + "") + ")";
					titleWidth += font.stringWidth(topMessageTime);
				}
			}
			drawStartX = x + w - titleWidth;
		}
		final int leftX = x;
		if (isLanguageAr)
			x = drawStartX;
		
		int drawAddX = 0;
		if (type == Def.CHAT_TYPE_WORLD_TOP){
			drawAddX = drawTopMessageTitle(g, x, y, time,drawAddX);
		} else {
			if (type != Def.CHAT_TYPE_SYSTEM) {
				if (isLanguageAr) {
					drawAddX += drawColon(g, x + drawAddX, y);
				}
				drawAddX += drawName(g, x + drawAddX, y);
				if (!isLanguageAr){
					drawAddX += drawColon(g, x + drawAddX, y);
				}
			}
		}

		if (type == Def.CHAT_TYPE_WORLD_TOP && getCreateTime() != 0) {
			if (time > 0) {
				if(topMessageTime!=null){
				   g.drawString(topMessageTime, x + drawAddX, y, 0);
				}
			}
		}

		x=leftX;
		y += lineH;
		g.setColor(contentColor);
		String body[] = getContent();
		for (int i = 0; i < body.length; i++) {
			faceHandler.drawMessageByLanguage(g, body[i], x, y,w,isLanguageAr);
			y += lineH;
		}
//		if (type == Def.CHAT_TYPE_WORLD_TOP||paintBubble)
//			return;
//		
		g.setColor(lineColor);
		g.drawLine(x, y, x + w, y);
	}

	private int drawTopMessageTitle(MilkGraphics g, int x, int y,int validSecond, int drawAddX) {
		if (getCreateTime() != 0) {
			int iconSize = faceHandler.getDrawFaceSize();
			int offfsetY = 0;
			if (validSecond > 0
					&& (System.currentTimeMillis() / 200) % 2 > 0) {
				offfsetY = 1;
			}
			if (topMessageArrow == null)
				topMessageArrow = Utils.getImage("chat-topmessagearrow");
			Utils.drawScaleImage(g, topMessageArrow, x, y + offfsetY, iconSize);
			drawAddX += iconSize;
		}

		if (type != Def.CHAT_TYPE_SYSTEM) {
			drawAddX += drawName(g, x + drawAddX, y);
		}
		drawAddX += drawColon(g, x + drawAddX, y);
		
		if (getCreateTime() != 0) {
			if (validSecond > 0) {
				String info = "("+ HallAccess.getL10nString(Def.timeLeft,validSecond + "") + ")";
				g.drawString(info, x + drawAddX, y, 0);
			}
		}
		return drawAddX;
	}

	public void drawTopMessageNotifyInfo(MilkGraphics g, int x, int y, int rectW,
			int rectH) {
		lineH = g.getFont().getHeight();
		g.setColor(contentColor);
		String body[] = topMessageNotify;
		y += (rectH - body.length * lineH) / 2;
		for (int i = 0; i < body.length; i++) {
			g.drawString(body[i], x
					+ (rectW - g.getFont().stringWidth(body[i])) / 2, y,
					MilkGraphics.TOP | MilkGraphics.LEFT);
			y += lineH;
		}
	}
	
	public static void drawNoMessageNotifyInfo(MilkGraphics g, String info, int x, int y,int color) {
		g.setColor(color);
		g.drawString(info, x, y, MilkGraphics.TOP | MilkGraphics.LEFT);
	}

	private int drawTitle(MilkGraphics g, int x, int y) {
		if (strTypeTitle == null || strTypeTitle.length() == 0)
			return 0;
		g.setColor(titleColor);
		g.drawString(strTypeTitle, x, y, 0);
		MilkFont font = g.getFont();
		return font.stringWidth(strTypeTitle);
	}

	private int drawName(MilkGraphics g, int x, int y) {
		MilkFont font = g.getFont();
		String name = getSenderName();
		int nameWidth = font.stringWidth(name);
		if (type == Def.CHAT_TYPE_PRIVATE) {
			if (isSendByMyself()) {
				g.setColor(youToColor);
				g.drawString(privateChatShowName, x, y, 0);
//				g.setColor(nameColor);
//				g.drawString(name, x + font.stringWidth(Def.chatTo), y, 0);
//				return font.stringWidth(Def.chatTo) + nameWidth;
				nameWidth = font.stringWidth(privateChatShowName);
				return nameWidth;
			} else {
				g.setColor(nameColor);
				g.drawString(privateChatShowName, x, y, 0);
//				g.setColor(youToColor);
//				g.drawString(Def.chatSaid, x + font.stringWidth(name), y, 0);
//				return font.stringWidth(Def.chatSaid) + nameWidth;
				nameWidth = font.stringWidth(privateChatShowName);
				return nameWidth;
			}
		} else {
			g.setColor(nameColor);
			g.drawString(name, x, y, 0);
			return nameWidth;
		}
	}

	private int drawColon(MilkGraphics g, int x, int y) {
		MilkFont font = g.getFont();
		if (type == Def.CHAT_TYPE_PRIVATE) {
			g.setColor(youToColor);
		} else {
			g.setColor(titleColor);
		}
		g.drawString(":", x, y, 0);
		return font.stringWidth(":");
	}

    public boolean isSendByMyself() {
		return sendByMyself;
	}

	public byte getType() {
		return type;
	}

	String[] getContent() {
		return contentLines;
	}

	private String getSenderName() {
		if (sendByMyself && type == Def.CHAT_TYPE_PRIVATE) {
			if (toName != null && toName.length() > 0) {
				return toName;
			} else
				return String.valueOf(toId);
		} else {
			if (fromName != null && fromName.length() > 0)
				return fromName;
			else {
				return String.valueOf(fromId);
			}
		}
	}

	public int getDrawHeight() {
		if (Def.CHAT_TYPE_WORLD_TOP == type) {
			if (paintBubble) {
				return (contentLines.length + 1) * lineH;
			} else {
				return WORLD_TOP_MESSAGE_LINE * lineH;
			}
		} else {
			return (contentLines.length + 1) * lineH;
		}
	}
	
	public int getTopMessageRectHeight(){
		return WORLD_TOP_MESSAGE_LINE * lineH;
	}
	
	public int getBubbleHeight() {
		int bubbleH;
		if (contentLines == null || contentLines.length == 0
				|| contentLines[0] == null)
			return 0;
		if (Def.CHAT_TYPE_WORLD_TOP == type) {
			bubbleH= contentLines.length * lineH;
		} else
			bubbleH= contentLines.length * lineH;
		
		return bubbleH+2*bubbleArcSize;
	}

	public int getBubbleWidth() {
		if (contentLines == null || contentLines.length == 0
				|| contentLines[0] == null)
			return 0;
		int contentW = Utils.getMaxLineWidth(lineWidth);
//		int titleW = this.getTitleDrawWidth(font) + 5;
//		if (Def.CHAT_TYPE_WORLD_TOP == type) {
//			if (getCreateTime() != 0) {
//				titleW += font.stringWidth(Def.timeLeft);
//				titleW += font.stringWidth("10");
//			}
//		}
//		if (titleW > contentW) {
//			contentW = titleW;
//		}
		return contentW+2*bubbleArcSize;
	}

	public static int getTopMessageHeight() {
		return WORLD_TOP_MESSAGE_LINE * lineH;
	}

	public int getPopMenuUserId() {
		if (isSendByMyself()) {
			if (type == Def.CHAT_TYPE_PRIVATE)
				return toId;
			else {
				return fromId;
			}
		} else {
			return fromId;
		}
	}

	public String getPopMenuUserName() {
		if (isSendByMyself()) {
			if (type == Def.CHAT_TYPE_PRIVATE)
				if (toName != null && toName.length() > 0) {
					return toName;
				} else
					return String.valueOf(toId);
			else {
				return null;
			}
		} else {
			return this.fromName;
		}
	}

	public void sendMessageAgain(CoreListener chatListener) {
		chatListener.sendMessage(type, toId, toName, content);
	}

}
