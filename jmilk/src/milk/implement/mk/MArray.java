package milk.implement.mk;

import java.util.Vector;

import milk.implement.Scene;
import smartview3.elements.Sv3Element;

public class MArray {

	Vector array = null;

	private void init() {
		if (array == null) {
			array = new Vector();
		}
	}

	private boolean validate(int index) {
		return (array != null && array.size() > index && index >= 0);
	}

	public void clean() {
		if (array != null) {
			array.removeAllElements();
		}
	}

	public int getInt(int index) {

		if (array != null) {
			int value = 0;
			try {
				value = ((Integer) (array.elementAt(index))).intValue();
			} catch (Exception t) {
			}
			return value;
		} else {
			return 0;
		}
	}

	public void set(int index, Object value) {
		init();
		if (value instanceof Integer) {
			int brk = 1;
			int a = brk;
		}
		array.setElementAt(value, index);

	}

	public String getString(int index) {
		if (array != null) {
			return (String) array.elementAt(index);
		} else {
			return null;
		}
	}

	public MArray getArray(int index) {
		if (array != null) {
			return (MArray) (array.elementAt(index));
		} else {
			return null;
		}
	}

	public MMap getMap(int index) {
		if (array != null) {
			return (MMap) (array.elementAt(index));
		} else {
			return null;
		}
	}

	public MRect getRect(int index) {
		if (array != null) {
			return (MRect) array.elementAt(index);
		} else {
			return null;
		}
	}

	public MPlayer getPlayer(int index) {

		if (array != null) {
			return (MPlayer) array.elementAt(index);
		} else {
			return null;
		}

	}

	public MText getText(int index) {
		if (array != null) {
			return (MText) array.elementAt(index);
		} else {
			return null;
		}
	}

	public MGroup getGroup(int index) {
		if (array != null) {
			return (MGroup) array.elementAt(index);
		} else {
			return null;
		}
	}

	public MTiles getTiles(int index) {
		if (array != null) {
			return (MTiles) array.elementAt(index);
		} else {
			return null;
		}
	}

	public Sv3Element getElement(int index) {
		if (array != null) {
			return (Sv3Element) array.elementAt(index);
		} else {
			return null;
		}
	}

	public MDraw getDraw(int index) {
		if (array != null) {
			return (MDraw) array.elementAt(index);
		} else {
			return null;
		}
	}

	public void append(Object value) {
		init();
		array.addElement(value);

	}

	public void remove(int index) {
		if (validate(index)) {
			array.removeElementAt(index);
		}
	}

	public void remove(Object obj) {
		if (array != null) {
			array.removeElement(obj);
		}
	}

	public void insert(int index, Object obj) {
		init();
		if (validate(index)) {
			array.insertElementAt(obj, index);
		} else if (array != null && index == array.size()) {
			array.addElement(obj);
		}
	}

	public int size() {
		if (array != null) {
			return array.size();
		} else {
			return 0;
		}
	}

	public boolean hasValue(Object value) {
		if (array != null) {
			return array.contains(value);
		}
		return false;
	}

	public boolean hasValue(int value) {
		if (array != null) {
			int size = array.size();
			for (int i = 0; i < size; i++) {
				Object thing = array.elementAt(i);
				if (thing instanceof Integer
						&& ((Integer) thing).intValue() == value) {
					return true;
				}
			}
		}
		return false;
	}

	public MArray() {

	}

	public MArray(int size) {
		if (size > 0) {
			init();
			array.setSize(size);
		}
	}

	public MArray(boolean init) {
		if (init) {
			init();
		}
	}

	public MArray(MArray obj) {
		if (obj.array != null) {
			init();
			int length = obj.array.size();
			for (int i = 0; i < length; i++) {
				array.addElement(obj.array.elementAt(i));
			}
		}
	}

	public int getType(int index) {
		if (array != null) {
			Object obj = array.elementAt(index);
			if (obj instanceof Integer) {
				return Scene.OINT;
			} else if (obj instanceof String) {
				return Scene.OSTRING;
			} else if (obj instanceof MArray) {
				return Scene.OARRAY;
			} else if (obj instanceof MMap) {
				return Scene.OMAP;
			} else if (obj instanceof MRect) {
				return Scene.ORECT;
			} else if (obj instanceof MPlayer) {
				return Scene.OPLAYER;
			} else if (obj instanceof MText) {
				return Scene.OTEXT;
			} else if (obj instanceof MGroup) {
				return Scene.OGROUP;
			} else if (obj instanceof MTiles) {
				return Scene.OTILES;
			} else if (obj instanceof Sv3Element) {
				return Scene.OELEMENT;
			}
		}
		return -1;
	}

	public String toString() {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("[");
			if (array != null) {
				int count = array.size();
				for (int i = 0; i < count; i++) {
					Object obj = array.elementAt(i);
					if (obj instanceof String) {
						buffer.append('\"');
						buffer.append(obj.toString());
						buffer.append('\"');
					} else {
						buffer.append(obj.toString());
					}
					if (i < count - 1) {
						buffer.append(",");
					}
				}
			}
			buffer.append("]");
			return buffer.toString();
		} catch (Exception e) {
			return "[]";
		}
	}

	public MArray clone() {
		MArray temp = new MArray();
		if (array != null) {
			int count = array.size();
			for (int i = 0; i < count; i++) {
				temp.append(array.elementAt(i));
			}
		}
		return temp;
	}

}
