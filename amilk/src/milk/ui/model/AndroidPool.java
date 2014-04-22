package milk.ui.model;

import java.util.Vector;
import android.os.Bundle;
import android.util.Log;

public class AndroidPool
{
	private static String tag = EventPool.class.getName();

	private static Vector<Bundle> bundlePool = new Vector<Bundle>();
	
	public static  Bundle getBundle() {

		int size = bundlePool.size();
		Log.i(tag, "bundlePool size " + size);
		if (size > 0) {
			Bundle b = bundlePool.remove(size - 1);
			b.clear();
			return b;
		} else {
			return  new Bundle();
		}
	}

	public static void recovery(Bundle b) {
		bundlePool.add(b);
	}
}
