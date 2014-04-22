package inmobi;


import com.inmobi.adtracker.androidsdk.IMAdTracker;
import com.inmobi.commons.IMCommonUtil;

import android.app.Activity;
import android.util.Log;

public class InMobiTracker {
	private final boolean isDebug=true;
	private final String defaultAppId = "e59531101bb84dd6ab70264d0d853b2c";
	private Activity activity;
	private static InMobiTracker mobileTracker;

	public static InMobiTracker getInstance() {
		if (mobileTracker == null) {
			mobileTracker = new InMobiTracker();
		}
		return mobileTracker;
	}

	private String userSetAppId;

	public void setAppId(String appId) {
		userSetAppId = appId;
	}

	public void onCreate(Activity a) {
		String realAppId = defaultAppId;
		if (userSetAppId != null) {
			realAppId = userSetAppId;
		}
		activity = a;
		log("-InMobiTracker-Version---:" + IMCommonUtil.getReleaseVersion());
		IMCommonUtil.setLogLevel(IMCommonUtil.LOG_LEVEL.DEBUG);
		IMAdTracker.getInstance().init(activity.getApplicationContext(),
				realAppId);
		// Reporting download goal
		IMAdTracker.getInstance().reportAppDownloadGoal();
		log("-InMobiTracker-reportAppDownloadGoal()-appID:" + realAppId);
	}
	
	public void reportCustomGoal(String flag){
		log("---reportCustomGoal---flag:"+flag);
		IMAdTracker.getInstance().reportCustomGoal(flag);
	}

	private final String tag = "InMobiTracker";

	private final void log(String info) {
		if(isDebug)
		Log.i(tag, info);
	}
	
}
