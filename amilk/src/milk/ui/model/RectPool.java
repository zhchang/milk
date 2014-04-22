package milk.ui.model;

import java.util.Vector;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

public class RectPool {
	private static Vector<Rect> rectPool = new Vector<Rect>();

	public static Rect getRect(int x, int y, int width, int height) {
		Rect r = getRect();
		r.left = x;
		r.top = y;
		r.right = x + width;
		r.bottom = y + height;
		return r;
	}

	public static  Rect getRect() {
		if (rectPool == null) {
			rectPool = new Vector<Rect>();
		}

		int size = rectPool.size();
		if (size > 0) {
			Rect r = rectPool.remove(size - 1);
			return r;
		} else {
			return new Rect();
		}
	}

	public static  void recovery(Rect r) {
		rectPool.add(r);
	}

	private static Vector<RectF> rectFPool = new Vector<RectF>();

	public static  RectF getRectf(int x,int y,int width,int height) {
		if (rectFPool == null) {
			rectFPool = new Vector<RectF>();
		}

		int size = rectFPool.size();
		if (size > 0) {
			RectF rf = rectFPool.remove(size - 1);
			rf.setEmpty();
			rf.set(x, y, width, height);
			return rf;
		} else {
			return new RectF(x, y, width, height);
		}
	}

	
	public static  RectF getRectf(Rect r) {
		if (rectFPool == null) {
			rectFPool = new Vector<RectF>();
		}

		int size = rectFPool.size();
		if (size > 0) {
			RectF rf = rectFPool.remove(size - 1);
			rf.setEmpty();
			rf.set(r);
			return rf;
		} else {
			return new RectF(r);
		}
	}
	
	public static  void recovery(RectF rf) {
		rectFPool.add(rf);
	}

	public static void clear()
	{
		rectPool.clear();
		rectPool=null;
		
		rectFPool.clear();
		rectFPool=null;
	}


}
