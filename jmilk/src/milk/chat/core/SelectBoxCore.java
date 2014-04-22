package milk.chat.core;

import java.util.Vector;

public class SelectBoxCore {

	private static final int DEFAULT_QUEUE_LENGTH = 10;
	private Vector userQueue;
	private UserInfo lastUserInfo;

	public SelectBoxCore() {
		userQueue = new Vector(DEFAULT_QUEUE_LENGTH);
	}

	public int getMaxLength() {
		return DEFAULT_QUEUE_LENGTH;
	}
	
	public UserInfo getTopUserInfo(){
		return lastUserInfo;
	}
	
	public Vector getUserInfoQueue() {
		return userQueue;
	}
	
	public void addItem(String name, int id) {
		for (int i = userQueue.size() - 1; i >= 0; i--) {
			UserInfo user = (UserInfo) userQueue.elementAt(i);
			if (user.id == id) {
				userQueue.removeElementAt(i);
				user.updateTime();
				user.name = name;
				userQueue.insertElementAt(user, 0);
				lastUserInfo=user;
				return;
			}
		}
		lastUserInfo=new UserInfo(name, id);
		userQueue.insertElementAt(lastUserInfo, 0);
		if (userQueue.size() > DEFAULT_QUEUE_LENGTH) {
			removeOldestUser();
		}
	}

	private void removeOldestUser() {
		UserInfo oldest = null;
		for (int i = userQueue.size() - 1; i >= 0; i--) {
			UserInfo user = (UserInfo) userQueue.elementAt(i);
			if (oldest == null) {
				oldest = user;
			} else if (oldest.lastSelectTime > user.lastSelectTime) {
				oldest = user;
			}
		}
		userQueue.removeElement(oldest);
	}

	public void initUserQueue(Vector messageQueue) {
		int id = 0;
		String name = null;
		int start = 0;
		if (messageQueue.size() > DEFAULT_QUEUE_LENGTH) {
			start = messageQueue.size() - DEFAULT_QUEUE_LENGTH;
		}
		if (messageQueue.size() > 0) {
			for (int i = start; i < messageQueue.size()
					&& userQueue.size() < DEFAULT_QUEUE_LENGTH; i++) {
				Message msg = (Message) messageQueue.elementAt(i);
				id = msg.getPopMenuUserId();
				name = msg.getPopMenuUserName();
				this.addItem(name, id);
			}
		}
	}

}
