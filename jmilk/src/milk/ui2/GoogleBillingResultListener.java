package milk.ui2;

public interface GoogleBillingResultListener {

	void onBillingResult(boolean success,String productId,String signedData,String signature);
	
}
