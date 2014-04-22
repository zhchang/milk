package milk.googlebilling;

import android.app.Activity;
import android.content.Intent;

public interface BillingActivityHandler {

	void onCreate(Activity _activity);
	void onDestroy();
	void onActivityResult(int requestCode, int resultCode, Intent data);
}
