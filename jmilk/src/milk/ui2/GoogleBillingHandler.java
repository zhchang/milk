package milk.ui2;

public interface GoogleBillingHandler {
	
	String[][]getPurchaseProductList();

	boolean supportInappBilling();
	
	void googlePlayPurchase(String productId,GoogleBillingResultListener l);
	
}
