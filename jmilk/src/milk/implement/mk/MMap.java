package milk.implement.mk;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.Scene;
import smartview3.elements.Sv3Element;

public class MMap {
	public Hashtable table = null;

	Vector keys = new Vector();

	public MMap() {

	}

	public int size() {
		if (table != null) {
			return table.size();
		} else {
			return 0;
		}
	}

	public int getInt(String key) {
		if (table != null) {
			int value = -1;
			try {
				value = ((Integer) table.get(key)).intValue();
			} catch (Exception t) {
			}
			return value;
		} else {
			return -1;
		}
	}

	private void logNotFoundError(String key) {
		Adaptor.infor("key :[" + key + "] not found");
	}

	public String getString(String key) {
		if (table != null) {
			return (String) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MArray getArray(String key) {
		if (table != null) {
			return (MArray) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MMap getMap(String key) {
		if (table != null) {
			return (MMap) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MRect getRect(String key) {
		if (table != null) {
			return (MRect) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MPlayer getPlayer(String key) {
		if (table != null) {
			return (MPlayer) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MText getText(String key) {
		if (table != null) {
			return (MText) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MGroup getGroup(String key) {
		if (table != null) {
			return (MGroup) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public MTiles getTiles(String key) {
		if (table != null) {
			return (MTiles) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public Sv3Element getElement(String key) {
		if (table != null) {
			return (Sv3Element) table.get(key);
		} else {
			logNotFoundError(key);
			return null;
		}
	}

	public int getType(String key) {
		if (table != null && table.containsKey(key)) {
			Object obj = table.get(key);
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
			} else if (obj instanceof Sv3Element) {
				return Scene.OELEMENT;
			}
		} else {
			logNotFoundError(key);
		}
		return -1;
	}

	public void set(String key, Object obj) {
		if (table == null) {
			table = new Hashtable();
		}
		if (!keys.contains(key)) {
			keys.addElement(key);
		}
		table.put(key, obj);
	}

	public void set(String key, int value) {
		set(key, new Integer(value));
	}

	public void remove(String key) {
		if (table != null) {
			table.remove(key);
		}
		if (keys.contains(key)) {
			keys.removeElement(key);
		}
	}

	public boolean hasKey(String key) {
		if (table != null) {
			return table.containsKey(key);
		} else {
			return false;
		}
	}

	public MArray getMapKeys() {

		MArray temp = new MArray();
		if (keys != null && keys.size() > 0) {
			int count = keys.size();
			for (int i = 0; i < count; i++) {
				temp.append(keys.elementAt(i));
			}
		}
		return temp;
	}

	public Vector getKeys() {

		return keys;
	}

	public void clean() {
		keys.removeAllElements();
		if (table != null) {
			table.clear();
		}
	}

	public String toString() {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("{");
			if (table != null) {
				Enumeration enu = table.keys();
				while (enu.hasMoreElements()) {
					String key = (String) enu.nextElement();
					buffer.append('\"');
					buffer.append(key);
					buffer.append("\":");
					Object obj = table.get(key);
					if (obj instanceof String) {
						buffer.append('\"');
						buffer.append(obj.toString());
						buffer.append('\"');
					} else {
						buffer.append(obj.toString());
					}
					if (enu.hasMoreElements()) {
						buffer.append(",");
					}
				}
			}
			buffer.append("}");
			return buffer.toString();
		} catch (Exception e) {
			return "{}";
		}
	}
}
