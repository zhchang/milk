package milk.mmbilling;

import java.util.HashMap;

import android.app.Activity;
import android.util.Log;
import milk.ui2.MMBillingCallback;
import milk.ui2.MMBillingHandler;
import mm.purchasesdk.OnPurchaseListener;
import mm.purchasesdk.Purchase;
import mm.purchasesdk.PurchaseCode;

public class MMBilling implements OnPurchaseListener, MMBillingHandler {
	private String APPID = "300002811575";
	private String APPKEY = "C5B9FF6901810332";
	private String tag = "MMBilling";
//	private String payCodeTest = "30000281157501";
	private Activity context;
//	private boolean initBillingOk = true;

	private Purchase purchase;
	private String tradeId, currentPayCode;

	private static MMBilling billing = new MMBilling();

	private MMBilling(){
		purchase = Purchase.getInstance();
	}
	
	public static MMBilling getInstance() {
		if (billing == null)
			billing = new MMBilling();
		return billing;
	}
	
	public void setAppInfo(String appId, String appKey) {
		this.APPID = appId;
		this.APPKEY = appKey;
//		try {
//			purchase.setAppInfo(APPID, APPKEY); // 设置计费应用ID和Key (必须)
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
	}

	private MMBillingCallback callback;

	public void setBillingCallback(MMBillingCallback mmbc) {
		callback = mmbc;
	}
	
	public void init(Activity context) {
		this.context = context;
		try {
			purchase.init(context, this); // 初始化，传入监听器
			if (APPID != null && APPKEY != null)
				purchase.setAppInfo(APPID, APPKEY); // 设置计费应用ID和Key (必须)
			else {
				System.out.println("------purchase setAppInfo error ------");
			}
			purchase.setTimeout(10000, 10000);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}
	
//	public MMBillingHandler getMMBillingHandler(){
//		return getInstance();
//	}

//	public void purchaseTest() {
//		purchase(payCodeTest, callback);
//	}

	public void purchase(final String paycode, MMBillingCallback mmCallback) {
		callback = mmCallback;
//		if (!initBillingOk) {
//			callback.purchaseResult(false, paycode, "fail");
//			System.out.println("------------billing init fail:");
//		} else {
			purchase(paycode, this);
//		}
	}

	private void purchase(final String paycode, final OnPurchaseListener l) {
		currentPayCode = paycode;
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					tradeId = purchase.order(context, paycode, l);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		};
		context.runOnUiThread(runnable);
	}

	@Override
	public void onAfterApply() {
		// TODO Auto-generated method stub
		System.out.println("------------purchase onAfterApply------");
	}

	@Override
	public void onAfterDownload() {
		// TODO Auto-generated method stub
		System.out.println("------------purchase onAfterDownload------");
	}

	@Override
	public void onBeforeApply() {
		// TODO Auto-generated method stub
		System.out.println("------------purchase onBeforeApply------");
	}

	@Override
	public void onBeforeDownload() {
		// TODO Auto-generated method stub
		System.out.println("------------purchase onBeforeDownload------");
	}

	@Override
	public void onBillingFinish(int code, HashMap map) {
		
		if (code == PurchaseCode.ORDER_OK || code == PurchaseCode.AUTH_OK) {
			/**
			 * BILL_SUCCEED,表示订购成功 AUTH_SUCCEED，表示该商品已经订购。
			 */
			// result = "订购结果：订购成功。";
			
			if (map != null) {
				String paycodeTmp = (String) map.get(OnPurchaseListener.PAYCODE);
				if (paycodeTmp != null && paycodeTmp.trim().length() != 0) {
					this.currentPayCode =  paycodeTmp;
				}
				String tradeID = (String) map.get(OnPurchaseListener.TRADEID);
				if (tradeID != null && tradeID.trim().length() != 0) {
					this.tradeId =  tradeID;
				}
			}
			
			callback.purchaseResult(true, currentPayCode, this.tradeId);
			
			Log.i(tag, "onBillingFinish, purchase ok "+Purchase.getReason(code));
		} else {
			/**
			 * 表示订购失败。
			 */
			callback.purchaseResult(false, currentPayCode, this.tradeId);
			Log.i(tag, "onBillingFinish, purchase fail Reason:"+Purchase.getReason(code));
			// result = "订购结果：" + Purchase.getReason(code);

		}

	}

	@Override
	public void onInitFinish(int code) {
		
		if (code == PurchaseCode.INIT_OK) {
			Log.i(tag, "onInitFinish, status code = ok "+Purchase.getReason(code));
//			initBillingOk = true;
		} else {
			Log.i(tag, "onInitFinish, status code = fail "+Purchase.getReason(code));
//			initBillingOk = false;
		}
	}

	@Override
	public void onQueryFinish(int statusCode, HashMap map) {
		// TODO Auto-generated method stub
		System.out.println("------------purchase onQueryFinish------");
	}

}
