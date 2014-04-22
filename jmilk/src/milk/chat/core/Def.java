package milk.chat.core;

import milk.implement.Adaptor;

public class Def {

	public static final int CHAT_SERVICE_ID = 0;

	public static final byte CHAT_TYPE_PRIVATE = 1;
	public static final byte CHAT_TYPE_FAMILY = 2;
	public static final byte CHAT_TYPE_WORLD = 3;
	public static final byte CHAT_TYPE_WORLD_TOP = 4;
	public static final byte CHAT_TYPE_SYSTEM = 5;

	public static final byte CHAT_ACTION_SEND = 1;
	public static final byte CHAT_ACTION_RESPONSE = 1;
	public static final byte CHAT_ACTION_RECEIVE = 2;
//	public static final byte CHAT_ACTION_PAY = 3;
	
	public static final String chatInpuMsgTooLong = "Input too long. (more than {0} alphanumeric characters)";
			//"Input too long. (more than 75 alphanumeric characters)";
	public static String chatInputMsgIsNull = Adaptor.getInstance().getTranslation("Input is empty",null);
			//"Input is empty";
	public static String chatInputSendTooFast = Adaptor.getInstance().getTranslation("Type too fast. Please try again later",null);
			//"Type too fast. Please try again later";
	public static String chatInputErrorInfo = Adaptor.getInstance().getTranslation("Error:",null);
			//"Error: ";

	public static String chatRoomInputInfo =  Adaptor.getInstance().getTranslation("Key 5 to input", null);
			//"Key 5 to input";
	public static String privateChatSelectBoxInfo = Adaptor.getInstance().getTranslation("Key 9 to select chat target", null);
			//"Key 9 to select chat target";
	public static String privateChatSelectBoxInfoBB = Adaptor.getInstance().getTranslation("Key 9 to select chat target", null);
//	public static final String worldChatSelectBoxInfo = Adaptor.getInstance().getTranslation("Key 9 to select Send mode:",null);
			//"Key 9 to select Send mode:";
	public static String chatWithMyself =  Adaptor.getInstance().getTranslation("You cannot chat with yourself.", null);
			//"You cannot chat with yourself";

	public static String cmdNormalSend = Adaptor.getInstance().getTranslation("Send",null);
			//"Send";
	public static String cmdTopSend = Adaptor.getInstance().getTranslation("Send Top Message",null);
			//"Send Top Message";
	public static String cmdAddEmotion = Adaptor.getInstance().getTranslation("Insert Emoticons",null);
			//"Insert Emotikons";
	public static String cmdOk = Adaptor.getInstance().getTranslation("Ok",null);
			//"Ok";
	public static String cmdCancel = Adaptor.getInstance().getTranslation("Cancel",null);
			//"Cancel";
	public static String cmdReply = Adaptor.getInstance().getTranslation("Reply",null);
			//"Reply";
	public static String cmdHomePage =Adaptor.getInstance().getTranslation("Home page",null);
			//"Home page";
	public static String cmdPrivateChat =Adaptor.getInstance().getTranslation("Private chat",null);
			//"Private chat";

	public static String chatErrorNoTribe = Adaptor.getInstance().getTranslation("You are not a member of tribe, no tribe chat is avaialble.",null);
			//"You are not a member of tribe, no tribe chat is avaialble.";
	public static String chatErrorNoTarget = Adaptor.getInstance().getTranslation("Please choose a target user to start private chat.(see world channel)",null);
			//"Please choose a target user to start private chat.(see world channel)";

	public static String chatfreeface=Adaptor.getInstance().getTranslation("Free Emoticons:",null);
			//"Free face:";
	public static String chatface=Adaptor.getInstance().getTranslation("Emoticons",null);
			//"Face";
	public static String titleWorld=Adaptor.getInstance().getTranslation("World",null);
			//"World";
	public static String titlePrivate=Adaptor.getInstance().getTranslation("Private",null);
			//"Private";
	public static String titleSystem=Adaptor.getInstance().getTranslation("System",null);
			//"System";
	public static String titleTribe=Adaptor.getInstance().getTranslation("Tribe",null);
			//"Tribe";
	public static String chatchoosefriend=Adaptor.getInstance().getTranslation("Choose a friend",null);
	public static String chatfriendlistempty=Adaptor.getInstance().getTranslation("Friend list is empty",null);
	
	public static String chatSendTopMsg=Adaptor.getInstance().getTranslation("Send top message here",null);
	public static String chatInputTopMsgKey6=Adaptor.getInstance().getTranslation("Key 6 to input",null);
			//"Send top message here" ;
//	public static String chatTo="to ";
//	public static String chatSaid=" said";
	
	public static String popTitleWantTo=Adaptor.getInstance().getTranslation("You want to",null);
			//"You want to";
//	public static final String popTitleToPlayer="To player ";
	public static String msgTitleTop=Adaptor.getInstance().getTranslation("[Top]",null);
			//"[Top]";
	public static String addFaceFail=Adaptor.getInstance().getTranslation("Can not add face,",null);
			//"Can not add face,";
	
	public static String inputPrivate=Adaptor.getInstance().getTranslation("Private Chat",null);
			//"Private Chat";
	public static String inputTribe=Adaptor.getInstance().getTranslation("Tribe Chat",null);
			//"Tribe Chat";
	public static String inputWorld=Adaptor.getInstance().getTranslation("World Message",null);
			//"World Message";
	public static String inputTop=Adaptor.getInstance().getTranslation("Top Message",null);
			//"Top Message";
	
	public static String sendFailTooFast=Adaptor.getInstance().getTranslation("You send message too frequently",null);
			//"You send message too frequently";
	public static String sendFailTooLong=Adaptor.getInstance().getTranslation("Message is too long.",null);
			//"Message is too long.";
	public static final String sendFailTopValid="Last top message is still valid.{0}s Left";
//			"Last top message is still valid.";
	public static String sendFailWrongId=Adaptor.getInstance().getTranslation("Send fail,Wrong user id.",null);
			//"Send fail,Wrong user id.";
	public static String sendFailForbidenWord=Adaptor.getInstance().getTranslation("Send fail,Message contains forbiden word.",null);
			//"Send fail,Message contains forbiden word.";
	public static String sendFail=Adaptor.getInstance().getTranslation("Send fail",null);
			//"Send fail";
	
	public static String chatDynamic1 = Adaptor.getInstance().getTranslation("Buy 1 (1 sapphire)", null);
	public static String chatDynamic2 = Adaptor.getInstance().getTranslation("Buy 10 (10 sapphire)", null);
	public static String chatDynamic3 = Adaptor.getInstance().getTranslation("Buy 1 (8 sapphire)", null);
	public static String chatDynamic4 = Adaptor.getInstance().getTranslation("Buy 10 (80 sapphire)", null);
	public static String chatDynamic5 = Adaptor.getInstance().getTranslation("buy successfully", null);
	public static String chatDynamic6 = Adaptor.getInstance().getTranslation("You don't have any world speakers. You want to?", null);
	public static String chatDynamic7 = Adaptor.getInstance().getTranslation("You don't have any top speakers. You want to?", null);
	
	public static final String timeLeft="{0}s Left";
	public static String titleChat= Adaptor.getInstance().getTranslation("Chat",null);
	
	
	public static String roomNameList[] = { titleWorld, titlePrivate, titleTribe, titleSystem };
	public static String msgTitlePrivate="["+titlePrivate+"]";
	public static String msgTitleWorld="["+titleWorld+"]";
	public static String msgTitleSystem="["+titleSystem+"]";
	public static String msgTitleTribe="["+titleTribe+"]";
	
	public static String noSystemMessage=Adaptor.getInstance().getTranslation("There is no system message.",null);
	public static String noTribeMessage=Adaptor.getInstance().getTranslation("There is no tribe message.",null);
	
	public static String chatselectfriend=Adaptor.getInstance().getTranslation("Choase a friend",null);
	
	static void initL10nString() {
		noSystemMessage=Adaptor.getInstance().getTranslation("There is no system message.",null);
		noTribeMessage=Adaptor.getInstance().getTranslation("There is no tribe message.",null);
		
		chatInputMsgIsNull = Adaptor.getInstance().getTranslation("Input is empty",
				null);
		// "Input is empty";
		chatInputSendTooFast = Adaptor.getInstance().getTranslation(
				"Type too fast. Please try again later", null);
		// "Type too fast. Please try again later";
		chatInputErrorInfo = Adaptor.getInstance().getTranslation("Error:", null);
		// "Error: ";

		chatRoomInputInfo = Adaptor.getInstance().getTranslation("Key 5 to input",
				null);
		// "Key 5 to input";
		privateChatSelectBoxInfo = Adaptor.getInstance().getTranslation(
				"Key 9 to select chat target", null);
		// "Key 9 to select chat target";
		privateChatSelectBoxInfoBB = Adaptor.getInstance().getTranslation(
				"Key 9 to select chat target", null);
		// public static final String worldChatSelectBoxInfo =
		// Adaptor.getInstance().getTranslation("Key 9 to select Send mode:",null);
		// "Key 9 to select Send mode:";
		chatWithMyself = Adaptor.getInstance().getTranslation(
				"You cannot chat with yourself.", null);
		// "You cannot chat with yourself";

		cmdNormalSend = Adaptor.getInstance().getTranslation("Send", null);
		// "Send";
		cmdTopSend = Adaptor.getInstance().getTranslation("Send Top Message", null);
		// "Send Top Message";
		cmdAddEmotion = Adaptor.getInstance().getTranslation("Insert Emoticons",
				null);
		// "Insert Emotikons";
		cmdOk = Adaptor.getInstance().getTranslation("Ok", null);
		// "Ok";
		cmdCancel = Adaptor.getInstance().getTranslation("Cancel", null);
		// "Cancel";
		cmdReply = Adaptor.getInstance().getTranslation("Reply", null);
		// "Reply";
		cmdHomePage = Adaptor.getInstance().getTranslation("Home page", null);
		// "Home page";
		cmdPrivateChat = Adaptor.getInstance().getTranslation("Private chat", null);
		// "Private chat";

		chatErrorNoTribe = Adaptor.getInstance().getTranslation(
				"You are not a member of tribe, no tribe chat is avaialble.",
				null);
		// "You are not a member of tribe, no tribe chat is avaialble.";
		chatErrorNoTarget = Adaptor.getInstance()
				.getTranslation(
						"Please choose a target user to start private chat.(see world channel)",
						null);
		// "Please choose a target user to start private chat.(see world channel)";

		chatfreeface = Adaptor.getInstance().getTranslation("Free Emoticons:", null);
		// "Free face:";
		chatface = Adaptor.getInstance().getTranslation("Emoticons", null);
		// "Face";
		titleWorld = Adaptor.getInstance().getTranslation("World", null);
		// "World";
		titlePrivate = Adaptor.getInstance().getTranslation("Private", null);
		// "Private";
		titleSystem = Adaptor.getInstance().getTranslation("System", null);
		// "System";
		titleTribe = Adaptor.getInstance().getTranslation("Tribe", null);
		// "Tribe";

		chatSendTopMsg = Adaptor.getInstance().getTranslation(
				"Send top message here", null);
		// "Send top message here" ;
//		chatTo = "to ";
//		chatSaid = " said";

		popTitleWantTo = Adaptor.getInstance().getTranslation("You want to", null);
		// "You want to";
		// public static final String popTitleToPlayer="To player ";
		msgTitleTop = Adaptor.getInstance().getTranslation("[Top]", null);
		// "[Top]";
		addFaceFail = Adaptor.getInstance()
				.getTranslation("Can not add face,", null);
		// "Can not add face,";

		inputPrivate = Adaptor.getInstance().getTranslation("Private Chat", null);
		// "Private Chat";
		inputTribe = Adaptor.getInstance().getTranslation("Tribe Chat", null);
		// "Tribe Chat";
		inputWorld = Adaptor.getInstance().getTranslation("World Message", null);
		// "World Message";
		inputTop = Adaptor.getInstance().getTranslation("Top Message", null);
		// "Top Message";

		sendFailTooFast = Adaptor.getInstance().getTranslation(
				"You send message too frequently", null);
		// "You send message too frequently";
		sendFailTooLong = Adaptor.getInstance().getTranslation(
				"Message is too long.", null);
		// "Message is too long.";
//		sendFailTopValid = Adaptor.getInstance().getTranslation(
//				"Last top message is still valid.", null);
		// "Last top message is still valid.";
		sendFailWrongId = Adaptor.getInstance().getTranslation(
				"Send fail,Wrong user id.", null);
		// "Send fail,Wrong user id.";
		sendFailForbidenWord = Adaptor.getInstance().getTranslation(
				"Send fail,Message contains forbiden word.", null);
		// "Send fail,Message contains forbiden word.";
		sendFail = Adaptor.getInstance().getTranslation("Send fail", null);
		// "Send fail";
		titleChat = Adaptor.getInstance().getTranslation("Chat", null);

		roomNameList = new String[] { titleWorld, titlePrivate, titleTribe,
				titleSystem };
		msgTitlePrivate = "[" + titlePrivate + "]";
		msgTitleWorld = "[" + titleWorld + "]";
		msgTitleSystem = "[" + titleSystem + "]";
		msgTitleTribe = "[" + titleTribe + "]";
		chatInputTopMsgKey6=Adaptor.getInstance().getTranslation("Key 6 to input",null);
		chatchoosefriend=Adaptor.getInstance().getTranslation("Choose a friend",null);
		chatfriendlistempty=Adaptor.getInstance().getTranslation("Friend list is empty",null);
	}
	
	public static final byte[] roomTypeList={
		Def.CHAT_TYPE_WORLD,
		Def.CHAT_TYPE_PRIVATE,
		Def.CHAT_TYPE_FAMILY,
		Def.CHAT_TYPE_SYSTEM
	};
	
	public static final String toWho="to {0}";
	public static final String whoSaid="{0} said";
	
}
