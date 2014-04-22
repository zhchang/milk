package milk.implement;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import milk.implement.mk.MArray;
import milk.implement.mk.MMap;
import milk.implement.mk.MRect;
import milk.ui2.MilkImage;
import milk.ui2.MilkSprite;

public class GameManager implements BlockingRequester {

	public static final byte RESOURCE_TYPE_SOURCE = 0;
	public static final byte RESOURCE_TYPE_IMAGE = 1;
	public static final byte RESOURCE_TYPE_AUDIO = 2;
	public static final byte RESOURCE_TYPE_9PATCH = 3;
	public static final byte RESOURCE_TYPE_L10N = 4;
	public static final byte RESOURCE_TYPE_SPRITE = 5;
	public static final byte RESOURCE_TYPE_PAGE = 6;

	private static final int REQUEST_COUNT = 500;

	private int requestCount;
	private int totalRequestedSize;
	private long resourceRequestTime;
	private boolean manifestReceived = false;;

	private static GameManager instance;

	private boolean manifestParsed = false;

	public static synchronized GameManager getInstance() {
		if (instance == null) {
			instance = new GameManager();
		}
		return instance;
	}

	private Hashtable imageResources = new Hashtable();

	private Hashtable sounds = new Hashtable();
	private Hashtable i18n = new Hashtable();

	private boolean allResInitFinish = false;

	private long lastRecycleTime;

	private Hashtable resources = new Hashtable();

	private Vector pending = new Vector();

	private Vector currentRequestBatch = new Vector();

	private String startScene = null;
	private boolean forceStartScene = startScene != null;

	private GameManager() {
	}

	void exit() {
		manifestParsed = false;
		allResInitFinish = false;
		imageResources.clear();
	}

	public void forceReleaseImages() {
		Enumeration keys = imageResources.keys();
		Vector temp = VectorPool.produce();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			ImageResource ir = (ImageResource) imageResources.get(key);
			if (ir.refCount == 0) {
				temp.addElement(key);
				ir.clearMem();
				// Adaptor.debug("force clear image[" + key + "]");
			}
		}
		int size = temp.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				imageResources.remove((String) temp.elementAt(i));
			}
			// Runtime.getRuntime().gc();
		}
		VectorPool.recycle(temp);
	}

	byte getAppStateByResInitStep() {
		// byte result = Adaptor.APP_STATE_BEGIN;
		byte result = Adaptor.APP_STATE_LOAD_RES;
		if (allResInitFinish) {
			result = Adaptor.APP_STATE_GAME_PLAY;
		}

		return result;
	}

	Object getImageResource(String resourceId) {
		if (resourceId != null) {
			ImageResource ir = (ImageResource) imageResources.get(resourceId);
			if (ir != null) {
				ir.lastUsedTime = System.currentTimeMillis();
				return ir.getImage();
			}
		}
		return null;
	}

	int getResourceLoadStatus() {
		if (!manifestReceived) {
			return 0;
		}
		int total = resources.size();
		int pendingCount = pending.size();
		if (total == 0) {
			return 0;
		} else {
			return (total - pendingCount) * 100 / total;
		}

	}

	public String getStartSceneId() {
		return startScene;
	}

	String getTranslation(String org, MArray replaces) {

		String result = null;
		try {
			result = (String) ((Hashtable) i18n
					.get(Adaptor.getInstance().language)).get(org);
			if (result != null && result.length() > 0) {
				if (replaces != null) {
					int size = replaces.size();
					for (int i = 0; i < size; i++) {
						String replace = replaces.getString(i);
						try {
							result = Adaptor.replaceAll(result, "{" + i + "}",
									replace);
						} catch (Exception e) {
						}
					}
				}
			} else {
				result = null;
			}
		} catch (Exception t) {
		}
		if (result == null) {
			result = org;
			if (replaces != null) {
				int size = replaces.size();
				for (int i = 0; i < size; i++) {
					String replace = replaces.getString(i);
					try {
						result = Adaptor.replaceAll(result, "{" + i + "}",
								replace);
					} catch (Exception e) {
					}
				}
			}
		}
		return result;
	}

	void grabImageResource(String resourceId) {
		if (resourceId != null) {
			ImageResource ir = (ImageResource) imageResources.get(resourceId);
			if (ir == null) {
				Resource resource = null;

				resource = (Resource) resources.get(resourceId);
				int loadW = 0;
				int loadH = 0;
				if (resource != null) {
					loadW = resource.loadW;
					loadH = resource.loadH;
				}

				byte[] bytes = loadResourceBytes(resourceId);
				ir = new ImageResource();
				if (bytes != null) {
					MilkImage image = Adaptor.uiFactory.createImage(bytes,
							resource);
					boolean isImage = false;
					boolean isSprite = false;
					int split = 1;
					if (resource != null) {

						if (resource.type == Resource.TYPE_IMAGE) {
							isImage = true;
						} else if (resource.type == Resource.TYPE_PANORAMA) {
							isSprite = true;
							split = resource.split;

						}
					} else {
						isImage = true;
					}
					if (isSprite) {
						if (image != null) {

							try {
								MilkSprite sprite = Adaptor.uiFactory
										.createMilkSprite(image,
												image.getWidth() / split,
												image.getHeight(), loadW, loadH);
								ir.setImage(sprite);
							} catch (Exception t) {
								Adaptor.exception(t);
							}

						}
					} else if (isImage) {
						ir.setImage(image);
					}
				} else {
					if (resourceId.equals("ship_1")) {
						int brk = 1;
						int a = brk;
					}
				}

				if (ir.image != null) {
					imageResources.put(resourceId, ir);
				}
			}
			ir.lastUsedTime = System.currentTimeMillis();
			ir.setRefCount(ir.getRefCount() + 1);
		}
	}

	public void imageResourcesRecycle() {
		if (System.currentTimeMillis() - lastRecycleTime > 3000
				&& allResInitFinish) {
			Enumeration keys = imageResources.keys();
			long currentTime = System.currentTimeMillis();
			Vector temp = VectorPool.produce();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				ImageResource ir = (ImageResource) imageResources.get(key);
				if (ir.refCount == 0 && ir.lastUsedTime + 10000 < currentTime) {
					ir.clearMem();
					temp.addElement(key);
					// Adaptor.debug("about to clear image[" + key + "]");
				}
			}
			int size = temp.size();
			if (size > 0) {
				for (int i = 0; i < size; i++) {
					imageResources.remove((String) temp.elementAt(i));
				}
				// Runtime.getRuntime().gc();
			}
			VectorPool.recycle(temp);
			lastRecycleTime = System.currentTimeMillis();
		}

	}

	void loadL10n() {
		i18n.clear();
		Enumeration keys = resources.keys();

		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			Resource resource = (Resource) resources.get(key);
			if (resource.type == Resource.TYPE_L10N2) {
				// l10n resource
				// System.out.println("-------------loadL10n()-L10N key:" +
				// key);
				try {
					byte[] bytes = instance.loadResourceBytes(key);
					if (bytes != null) {
						MilkInputStream dis = new MilkInputStream(
								new ByteArrayInputStream(bytes));
						String lang = Adaptor.readIntStr(dis);
						Hashtable l10n = (Hashtable) i18n.get(lang);
						if (l10n == null) {
							l10n = new Hashtable();
						}
						while (dis.available() > 0) {
							l10n.put(Adaptor.readIntStr(dis),
									Adaptor.readIntStr(dis));
						}
						if (lang != null && l10n.size() > 0) {
							i18n.put(lang, l10n);
						}
					}

				} catch (Exception t) {
					// t.printStackTrace();
					// Adaptor.getInstance().exception(t);
					Adaptor.debug("error loading l10n : " + resource.resourceId);
					// System.out.println("--------------Exception l10n size:"
					// + l10n.size());
				}
			}

		}
		String thing = this
				.getTranslation(
						"You are not lucky enough this time and get nothing! Next time you will get lucky equipment, never give up!",
						null);
		Adaptor.debug(thing);
		// System.out
		// .println("--------------finish load l10n size:" + l10n.size());
	}

	void initTranslationRes() {
		// Adaptor.getInstance().screenInfo("starting game");
		allResInitFinish = true;
		loadL10n();
		lastRecycleTime = System.currentTimeMillis();

		resourceRequestTime = System.currentTimeMillis() - resourceRequestTime;

		Adaptor.getInstance().sendUpdateResourceFileReport(requestCount,
				totalRequestedSize, resourceRequestTime);

	}

	void loadGame() {
		// request manifest here.
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(1);
			dos.writeByte(0);// not gzipped
			Adaptor.writeIntStr(dos, Adaptor.getInstance().getUa());
			Adaptor.writeVarChar(dos, Adaptor.getInstance().domain);
			Adaptor.writeVarChar(dos, Adaptor.getInstance().game);
			Adaptor.writeIntStr(dos, "{}");
			Adaptor.getInstance().sendMServerPacket("getmanifest",
					bos.toByteArray());
			Adaptor.getInstance().manifestRequestDuration = System
					.currentTimeMillis();

		} catch (Exception t) {
			Adaptor.exception(t);
		}
	}

	MilkImage loadImageResource(String resourceId) {
		if (resourceId != null) {
			ImageResource ir = (ImageResource) imageResources.get(resourceId);
			if (ir == null) {
				Resource resource = (Resource) resources.get(resourceId);
				int loadW = 0;
				int loadH = 0;
				if (resource != null) {
					loadW = resource.loadW;
					loadH = resource.loadH;
				}
				byte[] bytes = loadResourceBytes(resourceId);
				MilkImage image = null;
				if (bytes != null) {
					image = Adaptor.uiFactory.createImage(bytes, resource);

				}
				return image;
			} else {
				return (MilkImage) ir.getImage();
			}
		}
		return null;
	}

	byte[] loadResourceBytes(String resourceId) {

		byte[] thing = null;

		Resource resource = null;

		resource = (Resource) resources.get(resourceId);

		if (resource != null) {
			String fileKey = Adaptor.genFileKey(resourceId + "_"
					+ resource.version);
			thing = Adaptor.getInstance().readMutable(fileKey);
			if (thing == null) {
				Adaptor.getInstance().loadResource(resourceId);
				thing = Adaptor.getInstance().readMutable(fileKey);
				if (thing == null) {
					int brk = 1;
					int a = brk;
				}
			}
		} else {
			String fileKey = Adaptor.genFileKey(resourceId + "_0");
			if (Adaptor.getInstance().mutalbeExist(fileKey)) {
				thing = Adaptor.getInstance().readMutable(fileKey);
			}
		}

		return thing;

	}

	Scene loadScene(String windowId, String resourceId, MRect screenRect,
			MMap params, int flags, Scene parent) {

		Scene result = null;
		Resource resource = (Resource) resources.get(resourceId);
		boolean isScene = false;
		boolean isWindow = false;
		if (resource != null) {

			if (resource.type == RESOURCE_TYPE_SOURCE) {
				isScene = true;

			} else if (resource.type == RESOURCE_TYPE_PAGE) {
				isWindow = true;
			}
		} else {
			isScene = true;

		}
		if (isWindow) {
			try {
				result = new Window(windowId, resourceId, screenRect, params,
						flags, parent);
			} catch (Exception e) {
			}
		} else if (isScene) {
			try {
				result = new Scene(windowId, resourceId, screenRect, params,
						flags, parent);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	int loadSound(String resourceId) {
		int id = -1;
		if (sounds.containsKey(resourceId)) {
			return ((Integer) sounds.get(resourceId)).intValue();
		}
		// System.out.println("------------------------loadSound resourceId:"+resourceId);
		byte[] bytes = this.loadResourceBytes(resourceId);

		if (bytes != null) {
			id = Adaptor.getInstance().loadSoundByBytes(bytes);
			if (id != -1) {
				sounds.put(resourceId, new Integer(id));
			}
		}

		return id;
	}

	void onManifestReceived(final String domain, final String game,
			final byte[] bytes) {

		byte[] existingBytes = Adaptor.getInstance().readMutable(
				Adaptor.genFileKey("manifest"));
		if (!new MD5().getHash(existingBytes).equals(new MD5().getHash(bytes))) {
			Adaptor.getInstance().writeMutable(Adaptor.genFileKey("manifest"),
					bytes);
		}
		Adaptor.getInstance().manifestRequestDuration = System
				.currentTimeMillis()
				- Adaptor.getInstance().manifestRequestDuration;
		resourceRequestTime = System.currentTimeMillis();
		manifestReceived = true;

		parseManifest(bytes, true);
	}

	void onResouceReceived(String domain, String game, String resourceId,
			int version, byte[] bytes, int hashcode) {

		Adaptor.infor("resource received <" + resourceId + ">[" + version + "]");
		Adaptor.getInstance().cacheResource(resourceId, version, bytes);

		// Scene current = Core.getInstance().getCurrentScene();
		// Vector windows = current.windows;
		// int count = windows.size();
		// for (int i = 0; i < count; i++) {
		// ((Scene) windows.elementAt(i)).resourceLoaded(resourceId, hashcode);
		// }
		// current.resourceLoaded(resourceId, hashcode);
	}

	void onResoucesReceived(String domain, String game, int count,
			String resourceId[], int version[], byte bytes[][]) {
		if (pending.size() == 0) {
			return;
		}

		for (int i = 0; i < count; i++) {
			Adaptor.infor("resource received <" + resourceId[i] + ">["
					+ version[i] + "]");
			// long start = System.currentTimeMillis();
			Adaptor.getInstance().cacheResource(resourceId[i], version[i],
					bytes[i]);
			totalRequestedSize += bytes[i].length;
			// Adaptor.debug("msg [" + resourceId[i] + "] cached in ["
			// + (System.currentTimeMillis() - start) + "]ms");
			if (!allResInitFinish) {
				pending.removeElement(resourceId[i]);
			}
		}
		if (!allResInitFinish) {
			// Adaptor.getInstance().updateLoading();
			Adaptor.getInstance().bufferReady();
			if (pending.size() == 0) {
				instance.initTranslationRes();
			}
		}
	}

	void requestNextBatch() {

		if (manifestParsed) {
			if (pending.size() > 0) {
				Adaptor.debug("Requesting [" + pending.size() + "]Resources");
				int limit = Math.min(pending.size(), REQUEST_COUNT);
				currentRequestBatch.removeAllElements();
				for (int i = 0; i < limit; i++) {
					currentRequestBatch.addElement(pending.elementAt(i));
				}
				Adaptor.getInstance().requestResources(currentRequestBatch);
			} else {
				if (!this.allResInitFinish) {
					instance.initTranslationRes();
				}
			}
		}
	}

	void parseManifest(byte[] data, boolean request) {
		MilkInputStream dis = new MilkInputStream(
				new ByteArrayInputStream(data));
		resources.clear();
		pending.removeAllElements();
		try {
			Adaptor.readVarChar(dis);// domain
			Adaptor.readVarChar(dis);// game
			dis.readByte();
			Adaptor.readVarChar(dis);
			Adaptor.readShortStr(dis);
			// int flag = dis.readInt();
			dis.readInt();
			int count = dis.readInt();
			for (int i = 0; i < count; i++) {
				int tagCount = dis.readByte();
				Resource resource = new Resource();
				for (int k = 0; k < tagCount; k++) {
					int tag = dis.readByte();
					int len = dis.readShort();
					byte[] bytes = new byte[len];
					dis.read(bytes);
					MilkInputStream read = new MilkInputStream(
							new ByteArrayInputStream(bytes));
					switch (tag) {
					case 0: {
						resource.resourceId = new String(bytes, "UTF-8");
						break;
					}
					case 1: {
						resource.version = read.readInt();
						break;
					}
					case 2: {
						resource.type = read.readByte();
						break;
					}
					case 4: {
						int main = read.readByte();
						if (main == 1 && !forceStartScene) {
							startScene = resource.resourceId;
						}
						break;
					}
					case 5: {
						byte split = read.readByte();
						resource.split = split;
						break;
					}
					case 6: {
						byte must = read.readByte();
						resource.must = must;
						break;
					}
					case 7: {
						resource.loadW = read.readShort();

						break;
					}
					case 8: {
						resource.loadH = read.readShort();
						break;
					}
					case 9: {
						resource.alpha = read.readByte();
						break;
					}
					}
				}

				if (resource.type == Resource.TYPE_SOURCE && startScene == null
						&& resource.resourceId != null) {
					startScene = resource.resourceId;
				}
				resources.put(resource.resourceId, resource);
			}

			if (request) {
				manifestParsed = true;
				Enumeration keys = resources.keys();
				while (keys.hasMoreElements()) {
					String resourceId = (String) keys.nextElement();

					Resource resource = (Resource) resources.get(resourceId);
					int cached = Adaptor.getInstance().getCachedResource(
							resourceId, resource.version);
					if (cached != -1) {
						if (cached != resource.version) {
							Adaptor.getInstance().uncacheResource(resourceId,
									cached);
							pending.addElement(resourceId);
						}
					} else if (resource.must == 1) {
						pending.addElement(resourceId);
					} else if (resource.must == 0) {
						// pending.addElement(resourceId);
						Adaptor.infor("optional resource " + resourceId);
					}
				}
				// Adaptor.getInstance().clearResources(resources);
				this.requestCount = pending.size();
				Adaptor.getInstance().sendResourceFileReport(data.length,
						Adaptor.getInstance().manifestRequestDuration,
						requestCount);
				requestNextBatch();

			}
		} catch (Exception t) {
			Adaptor.exception(t);
		}

	}

	int playSound(String resourceId, int repeat) {
		int soundId = -1;
		soundId = loadSound(resourceId);
		if (soundId != -1) {
			Adaptor.getInstance().doPlaySound(soundId, repeat);
			sounds.put(resourceId, new Integer(soundId));
		}

		return soundId;
	}

	void processRequestResources() {
		requestNextBatch();
	}

	void releaseImageResource(String resourceId) {
		if (resourceId != null) {
			ImageResource ir = (ImageResource) imageResources.get(resourceId);
			if (ir != null) {
				ir.setRefCount(ir.getRefCount() - 1);
			}
		}
	}

	void saveResourceBytes(String resourceId, int version, byte[] bytes) {
		String fileKey = Adaptor.genFileKey(resourceId + "_" + version);
		Adaptor.getInstance().writeMutable(fileKey, bytes);
	}

	void stopSound(int soundId) {
		Adaptor.getInstance().stopSound(soundId);
	}

	void unloadSound(String resourceId) {
		Integer id = (Integer) sounds.get(resourceId);
		sounds.remove(resourceId);
		if (id != null && id.intValue() != -1) {
			Adaptor.getInstance().doUnloadSound(id.intValue());
		}
	}

	void writeExternalResource(String resourceId, byte[] bytes) {
		if (resourceId != null) {
			String fileKey = Adaptor.genFileKey(resourceId + "_0");
			Adaptor.getInstance().writeMutable(fileKey, bytes);
		}
	}

	private int myHashCode = hashCode();

	public int getHashCode() {
		return myHashCode;
	}

}
