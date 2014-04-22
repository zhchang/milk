package milk.implement;

import milk.implement.mk.MMap;
import smartview3.elements.Sv3Element;

public interface IMEvent {

	public class MFingerEvent implements IMEvent {

		private int x;
		private int y;
		private int type;
		private int ratio;

		public MFingerEvent(int x, int y, int type) {
			this.x = x;
			this.y = y;
			this.type = type;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int getRatio() {
			return ratio;
		}

		public void setRatio(int ratio) {
			this.ratio = ratio;
		}

	}

	public class MKeyEvent implements IMEvent {

		private int code;
		private int type;

		public MKeyEvent(int code, int type) {
			this.code = code;
			this.type = type;
		}

		public int getCode() {
			return code;
		}

		// public void setCode(int code) {
		// this.code = code;
		// }

		public int getType() {
			return type;
		}

		// public void setType(int type) {
		// this.type = type;
		// }

	}

	public class MCommandEvent implements IMEvent {
		String target;
		MMap command;

		MCommandEvent(String target, MMap command) {
			this.target = target;
			this.command = command;
		}
	}

	public class MDataEvent implements IMEvent {

		String key;

		Object value;

		int sourceHash = -1;

		MDataEvent(String key, String value, int sourceHash) {
			this.key = key;
			this.value = value;
			this.sourceHash = sourceHash;
		}

		MDataEvent(String key, int value) {
			this.key = key;
			this.value = new Integer(value);
		}
	}

	public class MResourceEvent implements IMEvent {
		String src;
		int width;
		int height;

		int sourceHash = -1;

		MResourceEvent(String src, int width, int height, int sourceHash) {
			this.src = src;
			this.width = width;
			this.height = height;
			this.sourceHash = sourceHash;
		}

		String getGuid() {
			return Adaptor.genImageGuid(src, width, height);
		}
	}

	public class MRightKeyEvent implements IMEvent {

	}

	public class MSmsEvent implements IMEvent {
		boolean sucess = false;

		MSmsEvent(boolean ok) {
			sucess = ok;
		}
	}

	public class MWindowEvent implements IMEvent {
		Sv3Element element;
		Integer event;

		MWindowEvent(Sv3Element element, Integer event) {
			this.element = element;
			this.event = event;
		}
	}

}
