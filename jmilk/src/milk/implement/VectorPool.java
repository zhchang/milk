package milk.implement;

import java.util.Vector;

public class VectorPool {

	private static Vector pool = new Vector();

	public static Vector produce() {
		Vector instance = null;
		if (pool.size() == 0) {
			instance = new Vector();
		} else {
			instance = (Vector) pool.elementAt(0);
			pool.removeElementAt(0);
		}
		return instance;
	}

	public static void recycle(Vector vector) {
		if (vector != null) {
			vector.removeAllElements();
			pool.addElement(vector);
		}
	}

}
