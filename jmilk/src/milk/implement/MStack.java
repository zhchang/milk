package milk.implement;

import java.util.Vector;

class MStack {

	private static Vector pool = new Vector();

	int istPtr;
	int stackStart;
	String funcName;
	String libName;

	private MStack(String name) {
		funcName = name;
	}

	public static MStack produce(String name) {
		MStack instance = null;
		if (pool.size() == 0) {
			instance = new MStack(name);
		} else {
			instance = (MStack) pool.elementAt(0);
			instance.funcName = name;
			pool.removeElementAt(0);
		}
		return instance;
	}

	public static void recycle(MStack thing) {
		if (thing != null) {
			thing.istPtr = 0;
			thing.stackStart = 0;
			thing.libName = null;
			thing.funcName = null;
			pool.addElement(thing);
		}
	}

}
