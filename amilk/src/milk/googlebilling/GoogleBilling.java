package milk.googlebilling;


//import java.util.Calendar;

import java.util.List;

import milk.googlebilling.IabHelper;
import milk.ui.MilkCanvasImpl2;
import milk.ui2.GoogleBillingHandler;
import milk.ui2.GoogleBillingResultListener;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;

public class GoogleBilling implements BillingActivityHandler,GoogleBillingHandler{
	private final boolean isDebug=false;
	
	private static GoogleBilling bill;
	private GoogleBillingResultListener resultListener;
	private final int myRequestCode = 123455;
	private final String extraData = "mozat-";
	private String productId;
	
	private IabHelper iapHelper;
	private Activity activity;

	public static GoogleBilling getInstance(){
		if (bill == null)
			bill = new GoogleBilling();
		return bill;
	}
	
	public boolean supportInappBilling(){
		logDebug("query supportInappBilling:"+iapHelper.mSetupDone);
		return iapHelper.mSetupDone;
	}

	private GoogleBilling(){
	}
	
	private String[][]productInfo;
	
	public String[][]getPurchaseProductList(){
		return productInfo;
	}
	
	private void receiveProductList(List<SkuDetails> list) {
		logDebug("receiveProductList size: " + list.size());
		if (list.size() <= 0)
			return;
		productInfo = new String[list.size()][4];
		for (int i = 0; i < list.size(); i++) {
			SkuDetails item = list.get(i);
			productInfo[i][0] = item.mSku;
			productInfo[i][1] = item.mTitle;
			productInfo[i][2] = item.mPrice;
			productInfo[i][3] = item.mDescription;
		}
	}
	
	public void onCreate(Activity _activity) {
//		logDebug("------------onCreate-------------");
		activity = _activity;
		iapHelper = new IabHelper(activity, "", false);
		iapHelper.enableDebugLogging(isDebug);
		Runnable r = new Runnable() {
			public void run() {
				iapHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
					public void onIabSetupFinished(IabResult result) {
						logDebug("Setup finished.");
						logDebug(result.toString());
						if (!result.isSuccess()) {
							return;
						}
						logDebug("Querying inventory.");
						iapHelper.queryInventoryAsync(mGotInventoryListener);
					}
				});
			}
		};
		new Thread(r).start();
	}

	public void onDestroy() {
		iapHelper.dispose();
	}


	public void googlePlayPurchase(final String itemId, GoogleBillingResultListener l) {
		logDebug("Purchase id:" + itemId);
		this.resultListener = l;
		productId = itemId;
		final String data = extraData + itemId;
		Runnable r = new Runnable() {
			public void run() {
				try {
					iapHelper.launchPurchaseFlow(activity, productId, myRequestCode,
							onIabPurchaseFinishedListener, data);
				} catch (Exception e) {
					logDebug("Purchase Exception id:"+ itemId);
					e.printStackTrace();
					resultListener.onBillingResult(false, productId,"","");
				}
			}
		};
		new Thread(r).start();
	}


	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		logDebug("onActivityResult--requestCode:" + requestCode
				+ "/ resultCode" + resultCode);
		iapHelper.handleActivityResult(requestCode, resultCode, data);
	}

	private final String tag = "google-billing";

	private final void logDebug(String info) {
		Log.i(tag, info);
		if(isDebug){
		MilkCanvasImpl2.debug(info);
		}
	}

	private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			logDebug("Query inventory finished.");
			if (result.isFailure()) {
				logDebug("Failed to query inventory: " + result);
				return;
			}
			logDebug("Query inventory was successful.");
			List<Purchase> list = inventory.getAllPurchases();
			for (int i = 0; i < list.size(); i++) {
				Purchase p = list.get(i);
				logDebug("Purchase ItemType:" + p.getItemType() + "/ Sku:"
						+ p.getSku());
				if (p.getItemType().equals(IabHelper.ITEM_TYPE_INAPP))
					iapHelper.consumeAsync(p, mConsumeFinishedListener);
			}
			
			receiveProductList(inventory.getAllSkuDetails());
		}
	};

	private IabHelper.OnIabPurchaseFinishedListener  onIabPurchaseFinishedListener=new IabHelper.OnIabPurchaseFinishedListener(){
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			logDebug("Purchase finished: " + result + ", purchase: " + purchase);
	         if (result.isFailure()) {
	        	 logDebug("Error purchasing: " + result);
	        	 resultListener.onBillingResult(false, productId,"","");
	             return;
	         }
//	         if (!verifyDeveloperPayload(purchase)) {
//	             complain("Error purchasing. Authenticity verification failed.");
//	             setWaitScreen(false);
//	             return;
//	         }
	         logDebug("Purchase successful. sku="+purchase.getSku()+"ItemType"+purchase.getItemType());
	         if (purchase.getItemType().equals(IabHelper.ITEM_TYPE_INAPP)) {
	        	 logDebug("Purchase is "+IabHelper.ITEM_TYPE_INAPP+". Starting consumption.");
	             iapHelper.consumeAsync(purchase, mConsumeFinishedListener);
	             resultListener.onBillingResult(true, purchase.getSku(),purchase.getOriginalJson(),purchase.getSignature());
	         }
	         else if(purchase.getItemType().equals(IabHelper.ITEM_TYPE_SUBS)){
//	        	 logDebug("Purchase is "+purchase.getSku()+". Starting consumption.");
//	             mHelper.consumeAsync(purchase, mConsumeFinishedListener); 
	        	 resultListener.onBillingResult(true, purchase.getSku(),purchase.getOriginalJson(),purchase.getSignature());
	         }
		}

	};
	
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
        	logDebug("Consumption finished. Purchase: " + purchase + ", result: " + result);
            if (result.isSuccess()) {
            	logDebug("Consumption successful. ");
//                mTank = mTank == TANK_MAX ? TANK_MAX : mTank + 1;
//                saveData();
//            	logDebug("You filled 1/4 tank. Your tank is now " + String.valueOf(mTank) + "/4 full!");
//            	resultListener.onBillingResult(true, purchase.getSku());
            }
            else {
            	logDebug("Error while consuming: " + result);
//            	resultListener.onBillingResult(false, purchase.getSku());
//            	mHelper.consumeAsync(purchase, this);
            }
            logDebug("End consumption flow.");
        }
    };
    
}
