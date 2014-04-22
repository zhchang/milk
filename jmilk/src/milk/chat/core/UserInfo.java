package milk.chat.core;

public class UserInfo {

	public String name;
	public int id;
	public long lastSelectTime;

	public UserInfo(String name, int id) {
		this.name = name;
		this.id = id;
		lastSelectTime = System.currentTimeMillis();
	}

	public void updateTime() {
		lastSelectTime = System.currentTimeMillis();
	}
	
}
