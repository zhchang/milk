package milk.chat.core;

import milk.implement.Adaptor;

public class MenuItem {
	
    public String showName;
    public String actionName;
    public String payAction;
    public String payParameters;
    
//    public byte chatType;
//    public int toId;
    
    public MenuItem(String showName, String actionName) {
//		this.showName = showName;
//		this.actionName = actionName;
//		payAction=null;
//		payParameters=null;
		setItem(showName,actionName);
	}
    
	void setItem(String showName, String actionName) {
		this.showName = showName;
		this.actionName = actionName;
		payAction = null;
		payParameters = null;
	}

	public MenuItem(String payOption) {
		String temp = Utils.getOptoionsItem(payOption, "content");
		this.showName = Adaptor.getInstance().getTranslation(temp, null);
		this.actionName = PopMenuListener.actionPayForTool;
		payAction = Utils.getOptoionActionItem(payOption, "action");
		this.payParameters = Utils.getOptoionsItem(payOption, "parameters");
	}
	
}
