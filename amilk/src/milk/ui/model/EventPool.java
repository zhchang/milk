package milk.ui.model;

import java.util.Vector;

import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MResourceEvent;
import android.util.Log;

public class EventPool {

	private static String tag = EventPool.class.getName();

	private static Vector<MResourceEvent> MResourceEventPool = new Vector<MResourceEvent>();

//	public static  MResourceEvent getMResourceEvent(String id, int width, int height, int source) {
//
//		int size = MResourceEventPool.size();
//		Log.i(tag, "in data event MResourceEventPool size " + size);
//		if (size > 0) {
//			MResourceEvent mre = MResourceEventPool.remove(size - 1);
//			mre.setParams(id, width, height, source);
//			return mre;
//		} else {
//			return  new MResourceEvent(id, width, height, source);
//		}
//	}

	public static  void recovery(MResourceEvent rf) {
		MResourceEventPool.add(rf);
	}

	private static Vector<MFingerEvent> MFingerEventPool = new Vector<MFingerEvent>();

	public static  MFingerEvent getMFingerEvent(int x, int y,
			int type) {

		int size = MFingerEventPool.size();
		Log.i(tag, "in data event MFingerEventPool size " + size);
		if (size > 0) {
			MFingerEvent me = MFingerEventPool.remove(size - 1);
			me.setX(x);
			me.setY(y);
			me.setType(type);
			return me;
		} else {
			return new MFingerEvent(x, y, type);
		}
	}

	public static  void recovery(MFingerEvent rf) {
		MFingerEventPool.add(rf);
	}

}
