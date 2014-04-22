package milk.ui.model;

import java.util.Vector;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;

public class SqLitePool
{
	private static String tag = SqLitePool.class.getName();

	private static Vector<ContentValues> ContentValuesPool = new Vector<ContentValues>();
	
	public static  ContentValues getContentValues() {

		int size = ContentValuesPool.size();
		Log.i(tag, "ContentValuesPool size " + size);
		if (size > 0) {
			ContentValues cv = ContentValuesPool.remove(size - 1);
			cv.clear();
			return cv;
		} else {
			return  new ContentValues();
		}
	}

	public static  void recovery(ContentValues b) {
		ContentValuesPool.add(b);
	}

}
