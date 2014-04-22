package milk.ui.model;

import java.util.Vector;

import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Message;
import android.util.Log;

import milk.implement.Adaptor;
import milk.net.InDataEventMessage;
import milk.net.OutGameMessage;
import milk.net.OutHeartMessage;

public class MessagePool {

	private static String tag = MessagePool.class.getName();

	private static Vector<OutHeartMessage> outHeartMessagePool = new Vector<OutHeartMessage>();

	private static Vector<OutGameMessage> OutGameMessagePool = new Vector<OutGameMessage>();

	private static Vector<InDataEventMessage> InDataEventMessagePool = new Vector<InDataEventMessage>();
	
	
	public static  InDataEventMessage getInDataEventMessage() {

		int size = InDataEventMessagePool.size();
		Log.i(tag, "in data event message size " + size);
		if (size > 0) {
			InDataEventMessage idem = InDataEventMessagePool.remove(size - 1);
			return idem;
		} else {
			return new InDataEventMessage();
		}
	}

	public static  void recovery(InDataEventMessage rf) {
		InDataEventMessagePool.add(rf);
	}

//	public static  OutGameMessage getOutGameMessage(int serviceId,
//			String msgId, byte[] payload) {
//
//		int size = OutGameMessagePool.size();
//		Log.i(tag, "out game message size " + size);
//		if (size > 0) {
//			OutGameMessage ogm = OutGameMessagePool.remove(size - 1);
//			ogm.setParams(serviceId, msgId, payload);
//			return ogm;
//		} else {
//			return new OutGameMessage(serviceId, msgId, payload);
//		}
//	}

	public static  void recovery(OutGameMessage rf) {
		OutGameMessagePool.add(rf);
	}

	public static  OutHeartMessage getOutHeartMessage() {
		int size = outHeartMessagePool.size();
		Log.i(tag, "out heart message size " + size);
		if (size > 0) {
			OutHeartMessage rf = outHeartMessagePool.remove(size - 1);
			return rf;
		} else {
			return new OutHeartMessage();
		}
	}

	public static  void recovery(OutHeartMessage rf) {
		outHeartMessagePool.add(rf);
	}

	public static void clear()
	{
		outHeartMessagePool.clear();
		outHeartMessagePool=null;
		
		OutGameMessagePool.clear();
		OutGameMessagePool=null;
		
		InDataEventMessagePool.clear();
		InDataEventMessagePool=null;
	}
}
