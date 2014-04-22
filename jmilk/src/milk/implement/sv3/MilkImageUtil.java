package milk.implement.sv3;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.ui2.MilkImage;
import mobile.MobileImageUtil;
import smartview3.utils.IImageRequester;

public class MilkImageUtil extends MobileImageUtil {

	private MilkImageUtil() {
	}

	Hashtable receivers = new Hashtable();

	private static MilkImageUtil instance;

	public synchronized static MilkImageUtil getInstance() {
		if (instance == null) {
			instance = new MilkImageUtil();
		}
		return instance;
	}

	public Object loadLocalImage(String name, int width, int heigth) {
		// MilkImage image = null;
		// byte[] bytes = Adaptor.getInstance().readMutable(name);
		// if (bytes != null) {
		// try {
		// image = Adaptor.uiFactory.createImage(bytes, 0, bytes.length);
		// } catch (Exception t) {
		// }
		// }
		// return image;
		return null;
	}

	public void loadImageAsync(String src, IImageRequester receiver, int width,
			int height) {
		// String localResName = this.resolveAsLocalResource(src);
		// if (localResName != null) {
		// receiver.didReceiveImage(
		// loadLocalImage(localResName, width, height), src);
		// return;
		// }
		Adaptor adaptor = Adaptor.getInstance();
		String key = Adaptor.genImageGuid(src, width, height);
		adaptor.grabImageResource(src);
		Object thing = adaptor.getImageResource(src);
		if (thing == null) {
			adaptor.grabImageResource(key);
			thing = adaptor.getImageResource(key);
		}
		if (thing != null) {
			adaptor.releaseImageResource(src);
			adaptor.releaseImageResource(key);
			if (thing instanceof MilkImage) {
				receiver.didReceiveImage(thing, src);
			} else {
				receiver.didReceiveImage(null, src);
			}
		} else {
			if (registerReceiver(src, width, height, receiver)) {
				Adaptor.getInstance().loadExternalImage(src, width, height,
						hashCode());
			}
		}
	}

	public void imageArrive(String src, int width, int height, int sourceHash) {
		if (hashCode() != sourceHash) {
			return;
		}
		Adaptor adaptor = Adaptor.getInstance();
		String key = Adaptor.genImageGuid(src, width, height);
		Vector list = (Vector) receivers.get(key);
		if (list != null) {
			synchronized (list) {
				int count = list.size();
				for (int i = count - 1; i >= 0; i--) {
					IImageRequester receiver = (IImageRequester) list
							.elementAt(i);
					adaptor.grabImageResource(key);
					Object thing = adaptor.getImageResource(key);
					if (thing != null) {
						receiver.didReceiveImage(thing, key);
						adaptor.releaseImageResource(key);
						list.removeElementAt(i);
					}
				}
				if (list.size() == 0) {
					receivers.remove(key);
				}
			}
		}
	}

	boolean registerReceiver(String src, int width, int height,
			IImageRequester receiver) {
		String key = Adaptor.genImageGuid(src, width, height);
		Vector list = (Vector) receivers.get(key);
		if (list == null) {
			list = new Vector();
			receivers.put(key, list);
		}
		synchronized (list) {
			list.addElement(receiver);
			return list.size() == 1;
		}
	}

	public void removeReceiver(IImageRequester receiver) {
		Enumeration re = receivers.elements();
		while (re.hasMoreElements()) {
			Vector list = (Vector) re.nextElement();
			synchronized (list) {
				list.removeElement(receiver);
			}
		}
	}
}
