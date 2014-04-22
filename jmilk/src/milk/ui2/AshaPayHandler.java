package milk.ui2;

public interface AshaPayHandler {
	
	void requestPayment(String id, AshaResultListener pl);
	
	String getTitle(String id);
	
	String getInfomation(String id);
	
}
