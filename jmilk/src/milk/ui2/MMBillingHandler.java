package milk.ui2;

public interface MMBillingHandler {
	
    void setAppInfo(String appId,String appKey);
    
	void purchase(String paycode, MMBillingCallback mmbc) ;
	
}
