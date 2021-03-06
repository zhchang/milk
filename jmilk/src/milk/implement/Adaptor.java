package milk.implement;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import milk.chat.core.ChatListener;
import milk.chat.core.HallAccess;
import milk.implement.IMEvent.MCommandEvent;
import milk.implement.IMEvent.MDataEvent;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MKeyEvent;
import milk.implement.IMEvent.MResourceEvent;
import milk.implement.IMEvent.MRightKeyEvent;
import milk.implement.IMEvent.MSmsEvent;
import milk.implement.mk.MArray;
import milk.implement.mk.MMap;
import milk.implement.mk.MRect;
import milk.net.CommListener;
import milk.net.InChatMessage;
import milk.net.InChatResponseMessage;
import milk.net.InDataEventMessage;
import milk.net.InDynamicPayMessage;
import milk.net.InGameMessage;
//import milk.net.InIapItemMessage;
//import milk.net.InIapResultMessage;
import milk.net.InManifestMessage;
import milk.net.InMessage;
import milk.net.InMonetIdMessage;
import milk.net.InMultiResourceMessage;
import milk.net.InNewGameMessage;
import milk.net.InOneResourceMessage;
import milk.net.InRawRequestMessage;
import milk.net.InReportMessage;
import milk.net.OutGameMessage;
import milk.ui2.AshaMessageListener;
import milk.ui2.AshaPayHandler;
import milk.ui2.AshaResultListener;
import milk.ui2.GoogleBillingHandler;
import milk.ui2.GoogleBillingResultListener;
import milk.ui2.MMBillingCallback;
import milk.ui2.MMBillingHandler;
import milk.ui2.MilkApp;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkUiFactory;
import milk.ui2.MoWebListener;
import milk.ui2.RawRequest;
import milk.ui2.SmsListener;
import milk.ui2.SystemSmsViewHandler;

public class Adaptor implements CommListener, MoWebListener, SmsListener,
		AshaResultListener {

	public int majorVersion = 1;
	public int minorVersion = 1;
	public int buildVersion = 13;
	private MArray version = null;

	public Thread updateThread;

	public String display = null;

	public int requiredMajorVersion = 0;
	public int requiredMinorVersion = 0;
	public int requiredBuildVersion = 0;
	private MArray requiredVersion = null;

	private MMap configMapToRecord = null;

	private Hashtable cachedResources = new Hashtable();

	private static boolean supportAshaBilling = false;

	private AshaPayHandler ashaHandler;

	private AshaMessageListener ashaMessageListener;

	private MMap realmInfos = null;
	boolean realmInfosFetched = false;

	public void payResultReceived(boolean success, String id) {
		int result = 0;
		if (success) {
			result = 1;
		}
		MMap command = new MMap();
		command.set("result", new Integer(result));
		this.sendCommand("main", command);
	}

	private void requestPayment(String id) {
		if (supportAshaBilling && ashaHandler != null) {
			ashaHandler.requestPayment(id, this);
		} else {
			MMap command = new MMap();
			command.set("result", new Integer(0));
			this.sendCommand("main", command);
		}
	}

	public void setIapMessageListener(AshaMessageListener l) {
		ashaMessageListener = l;
	}

	public void setIapPayHandler(AshaPayHandler l) {
		ashaHandler = l;
	}

	public MArray getVersion() {
		if (version == null) {
			version = new MArray();
			version.append(new Integer(majorVersion));
			version.append(new Integer(minorVersion));
			version.append(new Integer(buildVersion));
		}
		return version;
	}

	public MMap getAutoRegParams() {
		return milk.getAutoRegParams();
	}

	public MMap getAutoRegParams2(String salt1, String salt2) {
		return milk.getAutoRegParams2(salt1, salt2);
	}

	public MArray getRequiredVersion() {
		if (requiredVersion == null) {
			requiredVersion = new MArray();
			requiredVersion.append(new Integer(requiredMajorVersion));
			requiredVersion.append(new Integer(requiredMinorVersion));
			requiredVersion.append(new Integer(requiredBuildVersion));
		}
		return requiredVersion;
	}

	public void showOtherHomePage(int id) {
		MMap params = new MMap();
		params.set("action", "showOtherHome");
		params.set("id", new Integer(id));
		Core.getInstance().aboutToSwitchScene("action-handler", params,
				(byte) 0);
	}

	public String userDomain;
	public String username;
	public String password;
	public int mgServerServiceId;
	public int reportServiceId = 0x421;
	public String monetUrl;
	public int monetPort;
	public int browserServiceId = 10000;
	public int chatServiceId;
	public int gameServiceId = -1;
	public int newGameServiceId = -1;
	public String domain;
	public String game;
	public String moagentWap = "http://moagentHttpSTC.morange.com:8080/index.jsp";
	public String billing = "";
	public int channelId = 0;

	public int width;
	public int height;

	public static final int LOG_INFOR = 0;
	public static final int LOG_DEBUG = 1;
	public static final int LOG_ERROR = -1;

	public static final int KEYSTATE_PRESSED = -1;
	public static final int KEYSTATE_RELEASED = 1;

	public static final int POINTER_PRESSED = -1;
	public static final int POINTER_RELEASED = 1;
	public static final int POINTER_DRAGGED = 0;
	public static final int POINTER_ZOOMIN = 2;
	public static final int POINTER_ZOOMOUT = 3;

	private static final int APP_STATE_BEGIN = 1;
	private static final int APP_STATE_HOME = 2;
	// private static final int APP_STATE_AUTH = 3;
	// private static final int APP_STATE_NICKNAME = 4;
	// private static final int APP_STATE_AUTOREG_TOKEN = 5;
	// private static final int APP_STATE_AUTOREG_SMS = 6;
	// private static final int APP_STATE_AUTOREG_POLLING = 7;
	// private static final int APP_STATE_MANUALREG = 8;
	// private static final int APP_STATE_MANUALREG_GETOTP = 9;
	// private static final int APP_STATE_MANUALREG_VRFOTP = 10;
	private static final int APP_STATE_AUTOLOGIN = 15;
	// private static final int APP_STATE_MANUALLOGIN = 16;

	public static final int APP_STATE_INIT_WAIT = 17;
	public static final int APP_STATE_LOAD_RES = 18;
	public static final int APP_STATE_GAME_PLAY = 19;

	// all key, logic value for dev
	public static final int KEY_LEFT_SOFT = -6;
	public static final int KEY_RIGHT_SOFT = -7;
	public static final int KEY_MENU = -11111;
	public static final int KEY_UP = -1;
	public static final int KEY_DOWN = -2;
	public static final int KEY_LEFT = -3;
	public static final int KEY_RIGHT = -4;
	public static final int KEY_FIRE = -5;
	public static final int KEY_NUM0 = 48;// Canvas.KEY_NUM0;// 48
	public static final int KEY_NUM1 = 49;// Canvas.KEY_NUM1;// 49
	public static final int KEY_NUM2 = 50;// Canvas.KEY_NUM2;// 50
	public static final int KEY_NUM3 = 51;// Canvas.KEY_NUM3;// 51
	public static final int KEY_NUM4 = 52;// Canvas.KEY_NUM4;// 52
	public static final int KEY_NUM5 = 53;// Canvas.KEY_NUM5;// 53
	public static final int KEY_NUM6 = 54;// Canvas.KEY_NUM6;// 54
	public static final int KEY_NUM7 = 55;// Canvas.KEY_NUM7;// 55
	public static final int KEY_NUM8 = 56;// Canvas.KEY_NUM8;// 56
	public static final int KEY_NUM9 = 57;// Canvas.KEY_NUM9;// 57
	public static final int KEY_POUND = 35;// Canvas.KEY_POUND;// 35
	public static final int KEY_STAR = 42;// Canvas.KEY_STAR;// 42

	private static String fileKeyPrefix = null;

	private boolean hasLogined = false;
	private boolean manifestReceived = false;

	public String language = "ar";

	public int reportFlags = 0;
	public long loginDuration = 0;
	private static final int IsSendReport = 1;
	private static final int LoginReport = 2;
	private static final int ResourceFileReport = 3;
	private static final int UpdateResourceFile = 4;
	private static final int GameRequestReport = 5;
	private static final int ExceptionReport = 6;

	private Vector dataReports = new Vector();
	private Hashtable sendingData = new Hashtable();
	private long lastDataReportTime = 0;
	public long manifestRequestDuration = 0;

	private long lastBlockingRequest = -1;
	private Hashtable blockRequests = new Hashtable();
	private int blockRequestCount = 0;

	private Hashtable newGameMsgs = new Hashtable();

	public boolean isBlockingRequesting() {
		boolean yes = blockRequests.size() > 0;
		if (!yes) {
			yes = (System.currentTimeMillis() - lastBlockingRequest) < 500;
			if (!yes) {
				lastBlockingRequest = -1;
			}
		}
		return yes;
	}

	public static String genImageGuid(String url, int w, int h) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(url).append("[").append(w).append(",").append(h)
				.append("]");
		return buffer.toString();
	}

	public String connErr;

	private Vector keyEvents = VectorPool.produce();
	public Vector fingerEvents = VectorPool.produce();
	private Vector networkEvents = VectorPool.produce();
	private Vector commandEvents = VectorPool.produce();
	private Vector rightKeyEvents = VectorPool.produce();

	private Vector leftKeyEvents = VectorPool.produce();
	private Vector smsEvents = VectorPool.produce();

	private ChatListener chatListener;
	// private IapMessageListener iapMessageListener;

	private Scene loadingScene = null;

	private boolean eatNextUp = false;
	private Hashtable memory = new Hashtable();

	private Hashtable database = null;
	private Vector loadedResources = VectorPool.produce();

	private Hashtable loadingResources = new Hashtable();

	// private Timer drawer = new Timer();

	private static Adaptor instance = new Adaptor();
	public static MilkApp app;

	private int monetId = 0;
	// byte msgBoxChoice = -1;
	private Hashtable sounds = new Hashtable();
	// private long lastDrawTime = 0;
	// String startScene = null;
	private int drawIndex = 0;
	private long lastLoadingTime = 0;

	private String errorMsg;

	// final int scrollColor1 = 0x0;
	// final int scrollColor2 = 0xffffff;
	public long gameStartTime;

	private static Hashtable alphaLines = new Hashtable();

	// private MMap resourceRegister;

	private String userAgent = null;

	public int getCachedResource(String resId, int version) {
		resId = Adaptor.replaceAll(resId, "-", "_").toLowerCase();
		if (cachedResources.containsKey(resId)) {
			return ((Integer) (cachedResources.get(resId))).intValue();
		}
		return -1;
	}

	public boolean isResourceCached(String resId) {
		resId = Adaptor.replaceAll(resId, "-", "_").toLowerCase();
		return cachedResources.containsKey(resId);
	}

	public void uncacheResource(String resId, int version) {
		resId = Adaptor.replaceAll(resId, "-", "_").toLowerCase();
		String fileKey = genFileKey(resId + "_" + version);
		deleteMutable(fileKey);
	}

	public void cacheResource(String resId, int version, byte[] bytes) {
		resId = Adaptor.replaceAll(resId, "-", "_").toLowerCase();
		cachedResources.put(resId, new Integer(version));
		String fileKey = genFileKey(resId + "-" + version);
		writeMutable(fileKey, bytes);
	}

	public static void console(String thing) {
		milk.log(1, thing);
	}

	public static void debug(String log) {
		log(log, LOG_DEBUG);
	}

	public static void error(String log) {
		log(log, LOG_ERROR);
	}

	public static void exception(Exception t) {
		// System.err.println(t.getMessage());
		t.printStackTrace();
	}

	public static MilkImage getAlphaColorLine(int color, int height) {
		Long key = new Long((long) height << 4 + (long) color);
		MilkImage line = null;
		if (alphaLines.containsKey(key)) {
			line = (MilkImage) alphaLines.get(key);
		} else {
			int dim = Math.max(milk.getCanvasWidth(), milk.getCanvasHeight());

			int[] rgb = new int[dim * height];

			for (int i = 0; i < dim * height; i++) {
				rgb[i] = color;
			}
			line = uiFactory.createRGBImage(rgb, dim, height, true);
			alphaLines.put(key, line);
		}
		return line;
	}

	public static Adaptor getInstance() {

		return instance;
	}

	MMap queryEnv(MMap query) {
		MMap map = new MMap();
		try {
			int action = query.getInt("action");

			switch (action) {
			case 0: {
				int supportedAshaBilling = 0;
				if (supportAshaBilling)
					supportedAshaBilling = 1;
				// is asha-billing supported
				map.set("result", new Integer(supportedAshaBilling));
				break;
			}
			case 1: {
				// get asha-billing info for a product (id)
				String pid = query.getString("pid");
				if (this.ashaHandler != null) {
					map.set("title", ashaHandler.getTitle(pid));
					map.set("info", ashaHandler.getInfomation(pid));
				}
				break;
			}
			case 2: {
				// get channel-id
				map.set("channel-id", new Integer(channelId));
				break;
			}
			case 3: {
				// get realms-info
				map.set("realms-info", realmInfos);
				break;
			}
			case 4: {
				map.set("billing", billing);
				break;
			}
			case 5: {
				map.set("supportGoogleVerify", "true");
				break;
			}
			case 6: {
				map.set("supportSystemSmsView", "true");
				break;
			}
			case 7: {
				map.set("client-channel", milk.getChannel());
				break;
			}

			}

		} catch (Exception e) {

		}
		return map;
	}

	public static void infor(String log) {
		log(log, LOG_INFOR);
	}

	public static void log(String log, int level) {
		if (level == LOG_DEBUG) {
			console("log[" + level + "] : " + log);
		}
	}

	public static String readIntStr(MilkInputStream dis) throws IOException {
		int count = dis.readInt();
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

	public static String readShortStr(MilkInputStream dis) throws IOException {
		int count = dis.readShort();
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

	public static String readVarChar(MilkInputStream dis) throws IOException {
		int count = dis.readByte();
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

	public static int readVarInt(MilkInputStream dis) throws IOException {
		int _rs = 0;
		int _b;
		do {
			_b = dis.readByte();
			_rs = (_rs << 7) + (_b & 0x7F);
		} while (0 < (_b & 0x80));
		// } while (0 > _b);
		if (_rs < 0)
			_rs++;
		return _rs;
	}

	public static void readVarInt(byte[] data, int[] store, byte[] indexes)
			throws IOException {
		int _rs = 0;
		int _b;
		do {
			_b = data[indexes[0]];
			indexes[0] = (byte) (indexes[0] + 1);
			_rs = (_rs << 7) + (_b & 0x7F);
		} while (0 < (_b & 0x80));
		// } while (0 > _b);
		if (_rs < 0)
			_rs++;
		store[indexes[1]] = _rs;
		indexes[1] = (byte) (indexes[1] + 1);
	}

	public static String readVarStr(MilkInputStream dis) throws IOException {
		int count = readVarInt(dis);
		byte[] bytes = new byte[count];
		dis.read(bytes);
		return new String(bytes, "UTF-8");
	}

	public static void writeIntStr(MilkOutputStream dos, String str)
			throws IOException {
		if (str != null && str.length() > 0) {
			byte[] bytes = str.getBytes("UTF-8");
			dos.writeInt(bytes.length);
			dos.write(bytes);
		} else {
			dos.writeInt(0);
		}
	}

	public static void writeShortStr(MilkOutputStream dos, String str)
			throws IOException {
		if (str != null && str.length() > 0) {
			byte[] bytes = str.getBytes("UTF-8");
			dos.writeShort(bytes.length);
			dos.write(bytes);
		} else {
			dos.writeShort(0);
		}
	}

	public static void writeVarChar(MilkOutputStream dos, String str)
			throws IOException {
		if (str != null && str.length() > 0) {
			byte[] bytes = str.getBytes("UTF-8");
			dos.writeByte(bytes.length);
			dos.write(bytes);
		} else {
			dos.writeByte(0);
		}
	}

	private boolean pauseApp = false;

	private long lastMultiResourceMessageTime;

	private int state = -1;

	MRect screen;
	MilkImage buffer;

	public Adaptor() {
	}

	void bufferReady() {
		milk.drawNow();

	}

	private void clear() {
		monetId = 0;

		if (sounds != null) {
			sounds.clear();
		}
		// startScene = null;
		eatNextUp = false;

		clearEvents();

	}

	void clearEvents() {

		clearInputEvents();

		if (loadedResources != null) {
			// synchronized (loadedResources) {
			loadedResources.removeAllElements();
			// }
		}
		if (networkEvents != null) {
			// synchronized (networkEvents) {
			networkEvents.removeAllElements();
			// }
		}

		if (commandEvents != null) {
			// synchronized (commandEvents) {
			commandEvents.removeAllElements();
			// }
		}
	}

	void clearInputEvents() {
		if (rightKeyEvents != null) {
			synchronized (rightKeyEvents) {
				rightKeyEvents.removeAllElements();
			}
		}
		if (keyEvents != null) {
			synchronized (keyEvents) {
				keyEvents.removeAllElements();
			}
		}
		if (fingerEvents != null) {
			synchronized (fingerEvents) {
				fingerEvents.removeAllElements();
			}
		}
	}

	MCommandEvent consumeCommand() {
		MCommandEvent event = null;
		if (commandEvents.size() == 0) {
			return null;
		}
		synchronized (commandEvents) {
			if (commandEvents.size() > 0) {
				event = (MCommandEvent) commandEvents.firstElement();
				commandEvents.removeElementAt(0);
			}
		}
		return event;
	}

	MDataEvent consumeData() {
		MDataEvent event = null;
		if (networkEvents.size() == 0) {
			return event;
		}
		synchronized (networkEvents) {
			if (networkEvents.size() > 0) {
				event = (MDataEvent) networkEvents.firstElement();
				networkEvents.removeElementAt(0);
			}
		}
		return event;
	}

	MFingerEvent consumeFinger() {
		MFingerEvent event = null;
		if (fingerEvents.size() == 0) {
			return null;
		}
		synchronized (fingerEvents) {
			if (fingerEvents.size() > 0) {
				event = (MFingerEvent) fingerEvents.firstElement();
				fingerEvents.removeElementAt(0);
			}
		}
		return event;
	}

	MKeyEvent consumeKey() {
		MKeyEvent event = null;
		if (keyEvents.size() == 0) {
			return null;
		}
		synchronized (keyEvents) {
			if (keyEvents.size() > 0) {
				event = (MKeyEvent) keyEvents.firstElement();
				keyEvents.removeElementAt(0);
			}
		}
		return event;
	}

	MResourceEvent consumeLastLoadedResource() {
		MResourceEvent temp = null;
		if (loadedResources.size() == 0) {
			return null;
		}
		synchronized (loadedResources) {
			if (loadedResources.size() > 0) {
				temp = (MResourceEvent) loadedResources.firstElement();
				loadedResources.removeElementAt(0);
			}
		}
		return temp;
	}

	MRightKeyEvent consumeLeftKey() {
		MRightKeyEvent event = null;
		if (leftKeyEvents.size() == 0) {
			return null;
		}
		synchronized (leftKeyEvents) {
			if (leftKeyEvents.size() > 0) {
				event = (MRightKeyEvent) leftKeyEvents.firstElement();
				leftKeyEvents.removeElementAt(0);
			}
		}
		return event;
	}

	MRightKeyEvent consumeRightKey() {
		MRightKeyEvent event = null;
		if (rightKeyEvents.size() == 0) {
			return null;
		}
		synchronized (rightKeyEvents) {
			if (rightKeyEvents.size() > 0) {
				event = (MRightKeyEvent) rightKeyEvents.firstElement();
				rightKeyEvents.removeElementAt(0);
			}
		}
		return event;
	}

	public Vector contentsToLines(String content, int width, MilkFont font,
			int maxLines) {
		Vector lines = VectorPool.produce();
		new StringBreaker().contentToLines(content, lines, font, width, width,
				maxLines, 0, content.length());
		return lines;
	}

	void doPlaySound(int id, int repeat) {

		milk.playSoundById(id, (byte) (repeat != 0 ? -1 : 1));
	}

	void doUnloadSound(int id) {
		milk.unloadSoundById(id);
	}

	public boolean allowChatEvents() {
		boolean result = false;
		// Scene focus = Core.getInstance().getCurrentScene();
		// if (!(focus instanceof Hall)) {
		// String chatNotify = loadDb("chat-notify", true);
		// if (chatNotify != null && chatNotify.equals("1")) {
		// result = true;
		// }
		// }
		result = true;
		// System.out.println("-----------------------------allowChatEvents :"+result);
		return result;
	}

	// public String deviceInfo;

	public void draw(MilkGraphics g) {
		if (state == APP_STATE_GAME_PLAY || state == APP_STATE_HOME) {
			Core.getInstance().draw(g);
			if (state == APP_STATE_GAME_PLAY && allowChatEvents()) {
				if (this.showTopChatTab)
					HallAccess.drawTopNotificationBar(g);
				if (this.showBottomChatTab)
					HallAccess.drawBottomNotificationBar(g);
			}
		} else {
			drawLoadingScene(g);
		}
		// if(deviceInfo!=null){
		// g.setColor(0xff0000);
		// g.drawString(deviceInfo,
		// (milk.getCanvasWidth()-g.getFont().stringWidth(deviceInfo))/2,
		// milk.getCanvasHeight()/2,0);
		// }

		// if (display != null && display.length() > 0) {
		// g.setColor(0);
		// g.fillRect(0, milk.getCanvasHeight() - 40, milk.getCanvasWidth(),
		// 40);
		// g.setFont(this.uiFactory.getDefaultFont());
		// g.setColor(0xffffff);
		// g.drawString(display, 0, milk.getCanvasHeight() - 40,
		// MilkGraphics.LEFT | MilkGraphics.TOP);
		// }

		// int netState = Communicator.getInstance().state;
		// switch (netState) {
		// case Communicator.STATE_CONNECTING: {
		// g.setFont(alertFont);
		// g.setColor(0x888888);
		// g.fillRect(0, this.getHeight() - 30, this.getWidth(), 30);
		// g.setColor(0xffffff);
		// g.drawString("CONNECTING", 1, (30 - alertFont.getHeight()) / 2
		// + getHeight() - 30, MilkGraphics.TOP | MilkGraphics.LEFT);
		// break;
		// }
		// case Communicator.STATE_ERROR: {
		// g.setFont(alertFont);
		// g.setColor(0x888888);
		// g.fillRect(0, this.getHeight() - 30, this.getWidth(), 30);
		// g.setColor(0xffffff);
		// g.drawString("CONNERROR " + connErr, 1,
		// (30 - alertFont.getHeight()) / 2 + getHeight() - 30,
		// MilkGraphics.TOP | MilkGraphics.LEFT);
		// break;
		// }
		// }

	}

	public void drawLoadingScene(MilkGraphics g) {
		if (loadingScene == null) {
			drawLoading(g, 0, 0, milk.getCanvasWidth(), milk.getCanvasHeight(),
					true);
		} else {
			loadingScene.draw(g);
		}
	}

	private MilkImage spinner0, spinner1;

	public void drawLoading(MilkGraphics g, int xOffset, int yOffset,
			int width, int height, boolean fill) {

		try {
			if (fill) {
				g.setColor(0);
				g.fillRect(xOffset, yOffset, width, height);
			}

			if (spinner0 == null || spinner1 == null) {
				spinner0 = (MilkImage) getImageResource("spinner0");
				spinner1 = (MilkImage) getImageResource("spinner1");
			}
			if (spinner0 != null && spinner1 != null) {

				int x = xOffset + (width - spinner0.getWidth()) / 2;
				int y = yOffset + (height - spinner0.getHeight()) / 2;

				long now = System.currentTimeMillis();
				if (now - 100 >= lastLoadingTime) {
					lastLoadingTime = now;
					if (drawIndex == 0) {
						drawIndex = 1;

					} else {
						drawIndex = 0;

					}
				}
				if (drawIndex == 0) {
					g.drawImage(spinner0, x, y, MilkGraphics.TOP
							| MilkGraphics.LEFT);
				} else {
					g.drawImage(spinner1, x, y, MilkGraphics.TOP
							| MilkGraphics.LEFT);
				}
			}

		} catch (Exception t) {
			// t.printStackTrace();
		}
	}

	public void exit() {
		setNextState(APP_STATE_INIT_WAIT);
		GameManager.getInstance().exit();
		Core.exit();
		clear();
		instance = null;
	}

	public void forceClearImages() {
		GameManager.getInstance().forceReleaseImages();
	}

	public static String genFileKey(String name) {
		if (fileKeyPrefix == null) {
			fileKeyPrefix = Adaptor.instance.domain + "_"
					+ Adaptor.instance.game + "_";
		}
		return fileKeyPrefix + name;
	}

	public Object getImageResource(String resourceId) {
		return GameManager.getInstance().getImageResource(resourceId);
	}

	public int getLoadingProgress() {
		return GameManager.getInstance().getResourceLoadStatus();
	}

	public int getMonetId() {
		return monetId;
	}

	/**
	 * For Engine to fetch game resource: img/aud/bytecode
	 * 
	 * @param id
	 * @param gameId
	 * @return
	 */
	byte[] getResource(String resourceId) {
		return GameManager.getInstance().loadResourceBytes(resourceId);
	}

	public String getTranslation(String org, MArray replaces) {
		return GameManager.getInstance().getTranslation(org, replaces);
	}

	String getUa() {
		if (userAgent == null) {
			MMap thing = new MMap();
			thing.set("width", new Integer(milk.getCanvasWidth()));
			thing.set("height", new Integer(milk.getCanvasHeight()));
			thing.set("platform", milk.getPlatform());
			thing.set("density", "4");
			thing.set("lang", language);
			thing.set("model", milk.getModel());
			thing.set("client", getVersion().toString());
			thing.set("channel", new Integer(channelId));
			thing.set("billing", billing);
			userAgent = thing.toString();
		}
		return userAgent;
	}

	public int getConfigWidth() {
		return width;
	}

	public int getConfigHeight() {
		return height;
	}

	public void grabImageResource(String resourceId) {
		GameManager.getInstance().grabImageResource(resourceId);
	}

	public boolean ignoreInputEvent() {
		return state == APP_STATE_GAME_PLAY
				&& Core.getInstance().ignoreInputEvent();
	}

	String loadDb(String key, boolean temp) {
		String value = null;
		if (key == null) {
			return value;
		}
		if (temp) {
			value = (String) memory.get(key);
		} else {
			value = (String) database.get(key);
			System.out
					.println("--loadDb-------key:" + key + "/ value:" + value);
		}
		if (value == null) {
			value = "";
		}
		return value;
	}

	public MilkImage loadImageResource(String resourceId) {
		return GameManager.getInstance().loadImageResource(resourceId);
	}

	public void loadExternalImage(String id, int width, int height, int source) {
		if (id != null) {
			MResourceEvent event = new MResourceEvent(id, width, height, source);
			String fileKey = Adaptor.genFileKey(event.getGuid() + "_0");
			if (Adaptor.getInstance().mutalbeExist(fileKey)) {
				Adaptor.getInstance().notifyResourceLoaded(event);
			} else {
				String hash = new MD5().getHashString(id + width + height);
				String msgId = hash;
				for (int i = 1;; i++) {
					if (loadingResources.containsKey(msgId)) {
						msgId = hash + i;
					} else {
						loadingResources.put(msgId, event);
						break;
					}
				}
				requestResource(id, width, height, msgId);
			}
		}
	}

	public void loadResource(String id) {

		if (Thread.currentThread() != updateThread) {
			int brk = 1;
			int a = brk;
		}

		String msgId = String.valueOf(blockRequestCount++);
		Object waiter = new Object();
		requestResource(id, 0, 0, msgId);
		blockRequests.put(msgId, waiter);
		System.out.println("sync loading " + id);
		synchronized (waiter) {
			try {
				waiter.wait();
			} catch (Exception e) {
			}
		}

	}

	public Scene loadScene(String windowId, String resourceId,
			MRect screenRect, MMap params) {
		return loadScene(windowId, resourceId, screenRect, params, -1, null);
	}

	public Scene loadScene(String windowId, String resourceId,
			final MRect screenRect, MMap params, int flags, Scene parent) {
		Scene scene = GameManager.getInstance().loadScene(windowId, resourceId,
				screenRect, params, flags, parent);
		return scene;
	}

	int loadSound(String resourceId) {
		return GameManager.getInstance().loadSound(resourceId);
	}

	int loadSoundByBytes(byte[] bytes) {
		return milk.loadSound(bytes);
	}

	boolean mutalbeExist(String key) {
		return milk.isMutableReadable(key);
	}

	void deleteMutable(String key) {
		milk.removeMutable(key);
	}

	void notifyResourceLoaded(MResourceEvent event) {

		// synchronized (loadedResources) {
		loadedResources.addElement(event);
		// }
		// Core.getInstance().wakeUp();
	}

	public void onFingerToZoom(int x, int y, int type, int ratio) {
		MFingerEvent event = new MFingerEvent(x, y, type);
		event.setRatio(ratio);
		synchronized (fingerEvents) {
			if (fingerEvents.size() > 0) {
				MFingerEvent thing = (MFingerEvent) fingerEvents.lastElement();
				if (thing.getType() == POINTER_DRAGGED
						&& type == POINTER_DRAGGED) {
					fingerEvents.setElementAt(event, fingerEvents.size() - 1);
				}
			} else {
				fingerEvents.addElement(event);
			}
		}
	}

	public void onFinger(int x, int y, int type) {
		if (allowChatEvents() && Adaptor.APP_STATE_GAME_PLAY == state
				&& type == Adaptor.POINTER_PRESSED) {
			if (this.showTopChatTab) {
				boolean handle = HallAccess.pointerPressedTopTab(x, y);
				if (handle) {
					return;
				}
			}
			if (this.showBottomChatTab) {
				boolean handle = HallAccess.pointerPressedBottomTab(x, y);
				if (handle) {
					return;
				}
			}
		}
		MFingerEvent event = new MFingerEvent(x, y, type);
		synchronized (fingerEvents) {
			if (fingerEvents.size() > 0) {
				MFingerEvent thing = (MFingerEvent) fingerEvents.lastElement();
				if (thing.getType() == POINTER_DRAGGED
						&& type == POINTER_DRAGGED) {
					// fingerEvents.removeAllElements();
					// fingerEvents.addElement(event);
					fingerEvents.setElementAt(event, fingerEvents.size() - 1);
				} else {
					fingerEvents.addElement(event);
				}
			} else {
				fingerEvents.addElement(event);
			}
		}
		// Core.getInstance().wakeUp();
	}

	public void onIdle(boolean isIdle) {
		// Todo
	}

	public void onKey(int keyCode, int type) {
		// System.out
		// .println("keyevent: code[" + keyCode + "] type[" + type + "]");
		if (eatNextUp && type == 1) {
			eatNextUp = false;
			return;
		}
		if (allowChatEvents() && Adaptor.APP_STATE_GAME_PLAY == state
				&& type == Adaptor.KEYSTATE_PRESSED
				&& (this.showBottomChatTab || this.showTopChatTab)) {
			boolean handled = HallAccess.keyPressed(keyCode);
			if (handled)
				return;
		}

		MKeyEvent event = new MKeyEvent(keyCode, type);

		synchronized (keyEvents) {
			if (keyEvents.size() > 0) {
				MKeyEvent thing = (MKeyEvent) keyEvents.lastElement();
				if (thing.getType() != 0 || type != 0) {
					keyEvents.addElement(event);
				}
			} else {
				keyEvents.addElement(event);
			}
		}

	}

	public void onLeftSoftKey() {
		synchronized (leftKeyEvents) {
			leftKeyEvents.addElement(new MRightKeyEvent());
		}

	}

	public void onRightSoftKey() {

		synchronized (rightKeyEvents) {
			rightKeyEvents.addElement(new MRightKeyEvent());
		}

	}

	public void pauseApp() {
		if (!pauseApp) {
			// stop sound
			pauseApp = true;
			Scene focus = Core.getInstance().getCurrentScene();
			if (focus != null)
				focus.hideNotify();
		}
	}

	int playSound(String resourceId, int repeat) {
		return GameManager.getInstance().playSound(resourceId, repeat);
	}

	/**
	 * prepare game running environment including fetching all resource from
	 * server.
	 */
	void prepare() {
		try {
			// infor("sending mserver login bytes");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(0);
			dos.writeByte(0);// not gzipped
			String ua = getUa();
			infor("ua [" + ua + "]");
			writeIntStr(dos, ua);
			sendMServerPacket("login", bos.toByteArray());
		} catch (Exception t) {
			Adaptor.exception(t);
			error("prepare exception");
		}
		GameManager.getInstance().loadGame();
	}

	public void processRequestResources() {
		if (state == APP_STATE_LOAD_RES) {
			GameManager.getInstance().processRequestResources();
			lastMultiResourceMessageTime = System.currentTimeMillis();
		}
	}

	public byte[] readMutable(String key) {
		return milk.readMutable(key);
	}

	private void processSendingData(String msgId, String key) {
		String sendDataKey = msgId + "|" + key;
		if (sendingData.containsKey(sendDataKey)) {
			MArray dataReport = new MArray();
			dataReport.append(key);
			dataReport.append(new Integer(
					(int) (System.currentTimeMillis() - ((Long) sendingData
							.get(sendDataKey)).longValue())));
			dataReports.addElement(dataReport);
			sendingData.remove(sendDataKey);
		}
	}

	public void receiveMServerPacket(InMessage message) {
		if (message instanceof InMonetIdMessage) {
			InMonetIdMessage monet = (InMonetIdMessage) message;

			monetId = monet.monetId;
			requiredMajorVersion = monet.requiredMajorVersion;
			requiredMinorVersion = monet.requiredMinorVersion;
			requiredBuildVersion = monet.requiredBuildVersion;
			infor("---- monetId:" + monetId);

		} else if (message instanceof InManifestMessage) {
			InManifestMessage manifest = (InManifestMessage) message;

			String domain = manifest.domain;
			String game = manifest.game;
			byte[] data = manifest.manifestData;

			GameManager.getInstance().onManifestReceived(domain, game, data);
			manifestReceived = true;
		} else if (message instanceof InDataEventMessage) {
			InDataEventMessage dataEvent = (InDataEventMessage) message;

			String key = dataEvent.key;
			String value = dataEvent.value;
			int sourceHash = -1;
			try {
				sourceHash = Integer.parseInt(message.msgId);
			} catch (Exception e) {
			}
			debug("<<<<<sendData reply [" + key + "] [" + value.length()
					+ "]chars>>>>>");
			networkEvents.addElement(new MDataEvent(key, value, sourceHash));
			processSendingData(message.msgId, key);
			// String sendDataKey = message.msgId + "|" + key;
			// if (sendingData.containsKey(sendDataKey)) {
			// MArray dataReport = new MArray();
			// dataReport.append(key);
			// dataReport.append(new Integer(
			// (int) (System.currentTimeMillis() - ((Long) sendingData
			// .get(sendDataKey)).longValue())));
			// dataReports.addElement(dataReport);
			// sendingData.remove(sendDataKey);
			// }
			// forceRedrawNow();

		} else if (message instanceof InOneResourceMessage) {
			InOneResourceMessage oneResMessage = (InOneResourceMessage) message;

			String msgId = oneResMessage.msgId;
			String domain = oneResMessage.domain;
			String game = oneResMessage.game;
			String resourceId = oneResMessage.resourceId;
			int version = oneResMessage.version;
			byte[] resourceData = oneResMessage.resourceData;

			MResourceEvent re = (MResourceEvent) loadingResources.get(msgId);
			if (re != null) {
				resourceId = genImageGuid(resourceId, re.width, re.height);
				infor("external image: " + resourceId);
				GameManager.getInstance().writeExternalResource(resourceId,
						resourceData);
				notifyResourceLoaded((MResourceEvent) loadingResources
						.get(msgId));
				loadingResources.remove(msgId);
			} else {
				try {
					int hashcode = Integer.parseInt(msgId);

					GameManager.getInstance().onResouceReceived(domain, game,
							resourceId, version, resourceData, hashcode);

					Object waiter = this.blockRequests.get(msgId);

					if (waiter != null) {
						synchronized (waiter) {
							waiter.notifyAll();
						}
						waiter = null;
						blockRequests.remove(msgId);
						lastBlockingRequest = System.currentTimeMillis();
					}
					//

				} catch (Exception e) {
					exception(e);
				}
			}

		} else if (message instanceof InMultiResourceMessage) {
			InMultiResourceMessage manyResMessage = (InMultiResourceMessage) message;

			lastMultiResourceMessageTime = System.currentTimeMillis();

			String domain = manyResMessage.domain;
			String game = manyResMessage.game;
			int count = manyResMessage.resCount;
			String resourceId[] = manyResMessage.resourceId;
			int version[] = manyResMessage.version;

			byte[][] resourceData = manyResMessage.resourceData;

			// long start = System.currentTimeMillis();
			GameManager.getInstance().onResoucesReceived(domain, game, count,
					resourceId, version, resourceData);
			// Adaptor.debug("msg [" + resourceId[0] + "] process in ["
			// + (System.currentTimeMillis() - start) + "]ms");
			// } else if (message instanceof InIapItemMessage
			// || message instanceof InIapResultMessage) {
			// System.out.println("---- receive IapMessage:" + message);
			// if (iapMessageListener != null) {
			// iapMessageListener.receiveIapMessage(message);
			// }
			// } else {
			infor("--------------Adaptor receive unknow Message----------------");
		} else {
			if (ashaMessageListener != null)
				ashaMessageListener.receiveIapMessage(message);
		}
	}

	public void releaseImageResource(String resourceId) {
		GameManager.getInstance().releaseImageResource(resourceId);
	}

	void requestResource(String resourceId, int width, int height, String msgId) {
		try {
			infor("processing loadResource[" + resourceId + "]");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(2);
			dos.writeByte(0);// not gzipped
			writeIntStr(dos, getUa());
			writeVarChar(dos, domain);
			writeVarChar(dos, game);
			writeVarChar(dos, resourceId);
			MMap thing = new MMap();
			thing.set("width", new Integer(width));
			thing.set("height", new Integer(height));
			String constraints = thing.toString();
			writeIntStr(dos, constraints);
			sendMServerPacket(msgId, bos.toByteArray());
		} catch (Exception t) {
			Adaptor.exception(t);
			error("request resource exception");
		}
	}

	void requestResources(Vector resources) {
		try {
			infor("processing [" + resources.size() + "] loadResources");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(4);
			dos.writeByte(0);// not gzipped
			writeIntStr(dos, getUa());
			writeVarChar(dos, domain);
			writeVarChar(dos, game);
			int count = resources.size();
			dos.writeInt(count);
			for (int i = 0; i < count; i++) {
				writeVarChar(dos, (String) resources.elementAt(i));
			}
			sendMServerPacket("loadResources", bos.toByteArray());
			lastMultiResourceMessageTime = System.currentTimeMillis();
		} catch (Exception t) {
			Adaptor.exception(t);
			error("request resources exception");
		}
	}

	public void resumeApp() {
		if (pauseApp) {
			// resume sound
			pauseApp = false;
			Scene focus = Core.getInstance().getCurrentScene();
			if (focus != null)
				focus.showNotify();
		}
	}

	void saveDb(String key, String value, boolean temp) {
		if (key == null) {
			return;
		}
		if (temp) {
			memory.put(key, value);
		} else {
			try {
				database.put(key, value);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				Enumeration keys = database.keys();
				while (keys.hasMoreElements()) {
					String thing = (String) keys.nextElement();
					writeVarChar(dos, thing);
					writeIntStr(dos, (String) database.get(thing));
				}
				milk.writeMutable(genFileKey("database"), bos.toByteArray());
				// System.out.println("----------------------saveDb-----database:"+bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
			}
		}
	}

	public void sendCommand(String target, MMap command) {
		if (target != null && target.equalsIgnoreCase("system")) {
			doCommand(command);
		} else {
			MCommandEvent event = new MCommandEvent(target, command);
			// synchronized (commandEvents) {
			commandEvents.addElement(event);
			// }
			// Core.getInstance().wakeUp();
		}
	}

	void sendHttpData(String url, String key, String value, int sourceHash,
			String from, int moagentWap) {

		RawRequest request = new RawRequest();
		request.url = url;
		if (request.url != null) {
			String append = "client_platform=" + milk.getPlatform()
					+ "&client_version=" + this.majorVersion + "."
					+ this.minorVersion + "." + this.buildVersion
					+ "&client_channel=" + milk.getChannel()
					+ "&client_billing=" + billing;
			if (request.url.indexOf("?") != -1) {
				request.url += "&" + append;
			} else {
				request.url += "?" + append;
			}
		}
		request.key = key;
		request.value = value;
		request.sourceHash = sourceHash;
		request.from = from;
		request.listener = this;
		request.moagentWap = moagentWap != 0;

		milk.sendRawRequest(request);

	}

	void sendData(String key, String value, String serverKey, int sourceHash,
			String from) {
		try {
			infor("processing sendData");
			String msgId = String.valueOf(sourceHash);
			if (newGameServiceId != -1) {
				// send using new game service protocol
				if (key != null && value != null) {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					MilkOutputStream mos = new MilkOutputStream(bos);
					mos.writeByte(0);
					writeIntStr(mos, getUa());
					writeIntStr(mos, value);
					OutGameMessage message = new OutGameMessage(
							newGameServiceId, key, bos.toByteArray());
					newGameMsgs.put(key, new Integer(sourceHash));
					Adaptor.milk.send(message);
					sendingData.put(msgId + "|" + key,
							new Long(System.currentTimeMillis()));
				}

			} else if (gameServiceId != -1
					&& (serverKey == null || serverKey.length() == 0)) {
				// send data to monet game service
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(0);
				writeIntStr(dos, getUa());
				writeIntStr(dos, key);
				writeIntStr(dos, value);
				writeIntStr(dos, from);
				OutGameMessage message = new OutGameMessage(
						Adaptor.instance.gameServiceId, msgId,
						bos.toByteArray());
				Adaptor.milk.send(message);
				sendingData.put(msgId + "|" + key,
						new Long(System.currentTimeMillis()));
			} else {
				// send data to mgserver
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(3);
				dos.writeByte(0);// not gzipped
				writeIntStr(dos, getUa());
				writeVarChar(dos, domain);
				writeVarChar(dos, game);
				writeVarChar(dos, key);
				writeIntStr(dos, value);
				// dos.writeByte(0);
				writeVarChar(dos, serverKey);
				writeVarChar(dos, from);
				sendMServerPacket(msgId, bos.toByteArray());
			}

			// TODO: check flags before add sendingData
			sendingData.put(msgId + "|" + key,
					new Long(System.currentTimeMillis()));
			debug("<<<<<SendingData : [" + key + "]/[" + value + "]>>>>>");
		} catch (Exception t) {
			Adaptor.exception(t);
			error("senddata exception");
		}
	}

	public void queryRealm() {
		sendData("Realm.Query", "", null,
				(int) (System.currentTimeMillis() / 1000), "system");
	}

	public void queryIsSendReport() {

		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(IsSendReport);
			writeIntStr(dos, getUa());
			sendReportPacket("query-report", bos.toByteArray());
		} catch (Exception t) {
			Adaptor.exception(t);
			error("queryReport exception");
		}

	}

	public void sendLoginReport(long time) {
		if ((reportFlags & 0x01) > 0) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(LoginReport);
				dos.writeLong(time);
				sendReportPacket("login-report", bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
				error("loginReport exception");
			}
		}
	}

	public void sendResourceFileReport(int size, long loadTime, int count) {
		if ((reportFlags & 0x02) > 0) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(ResourceFileReport);
				dos.writeInt(size);
				dos.writeLong(loadTime);
				writeVarChar(dos, "");
				writeVarChar(dos, "");
				dos.writeInt(count);
				sendReportPacket("resource-file-report", bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
				error("resourceFileReport exception");
			}
		}
	}

	public void sendUpdateResourceFileReport(int count, int totalSize,
			long loadTime) {
		if ((reportFlags & 0x04) > 0) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(UpdateResourceFile);
				dos.writeInt(count);
				dos.writeInt(totalSize);
				dos.writeLong(loadTime);
				writeVarChar(dos, "");
				writeVarChar(dos, "");
				sendReportPacket("update-resource-file", bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
				error("resourceFileReport exception");
			}
		}
	}

	public void sendGameRequestReport(Vector dataReports) {
		if ((reportFlags & 0x08) > 0 && dataReports != null
				&& dataReports.size() > 0) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(GameRequestReport);
				int count = dataReports.size();
				dos.writeInt(count);
				for (int i = 0; i < count; i++) {
					MArray dataReport = (MArray) dataReports.elementAt(i);
					writeIntStr(dos, dataReport.getString(0));
					dos.writeLong(dataReport.getInt(1));
				}
				sendReportPacket("game-request-report", bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
				error("gameRequestReport exception");
			}
		}
	}

	public void sendExceptionReport(String exception) {
		if (true) {
			try {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream dos = new MilkOutputStream(bos);
				dos.writeByte(ExceptionReport);
				writeIntStr(dos, exception);
				sendReportPacket("game-request-report", bos.toByteArray());
			} catch (Exception t) {
				Adaptor.exception(t);
				error("gameRequestReport exception");
			}
		}
	}

	void sendMServerPacket(String msgId, byte[] payload) {
		OutGameMessage message = new OutGameMessage(
				Adaptor.instance.mgServerServiceId, msgId, payload);
		Adaptor.milk.send(message);
	}

	void sendReportPacket(String msgId, byte[] payload) {
		OutGameMessage message = new OutGameMessage(
				Adaptor.instance.reportServiceId, msgId, payload);
		Adaptor.milk.send(message);
	}

	public void getIAPProductList() {
		try {
			debug("-------processing getIAPProductList----------");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(5);
			dos.writeByte(0);// not gzipped
			writeIntStr(dos, getUa());
			writeVarChar(dos, domain);
			writeVarChar(dos, game);
			sendMServerPacket("get-iap-list", bos.toByteArray());
		} catch (Exception t) {
			Adaptor.exception(t);
			error("getIAPProductList exception");
		}
	}

	public void sendIAPTicket(String productId, String ticket) {
		try {
			debug("-------processing sendIAPTicket----------");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			MilkOutputStream dos = new MilkOutputStream(bos);
			dos.writeByte(6);
			dos.writeByte(0);// not gzipped
			writeIntStr(dos, getUa());
			writeVarChar(dos, domain);
			writeVarChar(dos, game);
			writeIntStr(dos, productId);
			writeIntStr(dos, ticket);
			sendMServerPacket("verify-iap-ticket", bos.toByteArray());
		} catch (Exception t) {
			Adaptor.exception(t);
			error("getIAPProductList exception");
		}
	}

	// public void showSetScene() {
	// SetScene set = new SetScene(UIHelper.milk.getCanvasWidth(),
	// UIHelper.milk.getCanvasWidth());
	// set.setBackScene(Core.getInstance().getCurrentScene());
	// Core.getInstance().setScene(set);
	// }

	private void startGame(MMap params) {
		String startScene = GameManager.getInstance().getStartSceneId();

		Core.getInstance().aboutToSwitchScene(startScene, null, (byte) 0);

	}

	void stopSound(int soundId) {
		milk.stopSound(soundId);
	}

	void unloadSound(String resourceId) {
		GameManager.getInstance().unloadSound(resourceId);
	}

	public static String replaceAll(String input, String pattern, String replace) {
		if (input != null) {
			int index = input.indexOf(pattern);
			while (index != -1) {
				input = input.substring(0, index) + replace
						+ input.substring(index + pattern.length());
				index = input.indexOf(pattern);
			}
		}
		return input;
	}

	public static void skipWhite(StringData input) {
		if (input.data.length() > input.index) {
			char thing = input.data.charAt(input.index);
			while (thing == '\t' || thing == '\r' || thing == '\n'
					|| thing == ' ' || thing == 0xFEFF) {
				thing = input.data.charAt(++input.index);
				if (input.data.length() <= input.index) {
					break;
				}
			}
		}
	}

	public static Object parse(StringData input, Object parent)
			throws Exception {

		if (input == null || input.data == null || input.data.length() == 0) {
			return null;
		}

		Object obj = null;
		skipWhite(input);
		char start = input.getChar(false);

		if (start == '[') {
			MArray array = new MArray();
			obj = array;
			char next = 0;
			while (input.hasMore()) {
				if (parse(input, obj) != null) {
					skipWhite(input);

					next = input.getChar(false);
					if (next == ',') {
					} else if (next == ']') {
						break;
					} else {
						throw new Exception("unexpected char1 " + (char) next);
					}
				} else {
					break;
				}
			}
		} else if (start == ']') {
			return obj;

		} else if (start == '{') {
			MMap map = new MMap();
			obj = map;
			char next = 0;
			while (input.hasMore()) {
				String key = (String) parse(input, null);
				if (key == null) {
					break;
				}
				skipWhite(input);
				if (input.getChar(false) == ':') {
					Object data = parse(input, null);
					map.set(key, data);
					skipWhite(input);
					next = input.getChar(false);
					if (next == ',') {
					} else if (next == '}') {
						break;
					} else {
						throw new Exception("unexpected char2 " + (char) next);
					}
				} else {
					throw new Exception("expect : after key: " + key);
				}
			}
		} else if (start == '}') {
			return obj;
		} else if (start == '"') {
			StringBuffer buffer = new StringBuffer();

			char next = input.getChar(false);
			boolean slash = false;
			while (input.hasMore() && (next != '"' || slash)) {
				buffer.append(next);
				slash = (next == '\\');
				next = input.getChar(false);
			}
			if (next != '"' || slash) {
				throw new Exception("unexpected string ending");
			}
			String value = buffer.toString();
			value = replaceAll(value, "\\\"", "\"");
			obj = value;

		} else if ((start >= '0' && start <= '9') || start == '-') {
			StringBuffer buffer = new StringBuffer();
			buffer.append(start);
			while (input.hasMore()) {
				start = input.getChar(true);
				if (start >= '0' && start <= '9') {
					buffer.append(start);
					input.skip();
				} else {
					break;
				}
			}
			obj = new Integer(Integer.parseInt(buffer.toString()));
		}
		if (parent != null && parent instanceof MArray) {
			((MArray) parent).append(obj);
		}
		return obj;
	}

	private MMap getAuthCache() {
		MMap result = null;
		String thing = loadDb("auth-data", false);
		if (thing != null) {
			try {
				result = (MMap) parse(new StringData(thing), null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	private void updateAuthCache() {
		String username = this.username;
		String password = this.password;
		if (username != null && username.length() > 0) {
			MMap auth = new MMap();
			auth.set("username", username);
			if (password != null && password.length() > 0) {
				auth.set("password", password);
			}
			saveDb("auth-data", auth.toString(), false);
		}
	}

	public void updateLoadingScene(String resourceId) {
		milk.clearKeyStatus();
		if (loadingScene != null) {
			loadingScene.onStop();
			loadingScene = null;
			this.clearEvents();
		}
		if (resourceId != null) {
			try {
				MMap thing = new MMap();
				if (errorMsg != null) {
					thing.set("error", errorMsg);
					errorMsg = null;
				}
				loadingScene = loadScene("main", resourceId, screen, thing,
						Scene.FLAG_FOCUSFIRST | Scene.FLAG_HASMENU, null);
				// System.out.println("loaded loading resource : " +
				// resourceId);
				loadingScene.runInit();
				// System.out.println("run init finished");
			} catch (Exception e) {
				// System.out
				// .println("---------updateLoadingScene Exception resourceId:"
				// + resourceId);
			}
		}
	}

	private void setNextState(int nextState) {
		if (state != nextState) {
			state = nextState;
			switch (nextState) {
			case APP_STATE_BEGIN: {
				MMap auth = getAuthCache();
				try {
					username = auth.getString("username");
					password = auth.getString("password");
				} catch (Exception e) {
					e.printStackTrace();
				}
				System.out.println("----------------------setNextState auth:"
						+ auth);
				if (username != null && password != null) {
					doLogin(username, password);
					setNextState(APP_STATE_AUTOLOGIN);
				} else {
					setNextState(APP_STATE_HOME);
				}
				break;
			}
			case APP_STATE_HOME: {
				Core.getInstance().aboutToSwitchScene("boot-home", null,
						(byte) 0);
				break;
			}
			case APP_STATE_LOAD_RES: {
				lastMultiResourceMessageTime = System.currentTimeMillis();
				updateLoadingScene("boot-loading");
				break;
			}
			case APP_STATE_GAME_PLAY: {
				startGame(null);
				updateLoadingScene(null);
				break;
			}
			default: {
				// updateLoadingScene(null);
				break;
			}

			}
		}
	}

	/*
	 * private void doOtp() { httpState = APP_STATE_MANUALREG_VRFOTP; MMap map =
	 * new MMap(); map.set("verifCode", userOtp); map.set("phone", userNumber);
	 * map.set("sessionID", sessionId); map.set("domain", "shabik.com"); //
	 * MoWebUtil // .sendRawRequest( //
	 * "http://mchatapi.mozat.com/index.php?r=MTRegister/sendVerifCode", //
	 * map.toString(), this, false); }
	 */

	/*
	 * private void doGetOtp() { httpState = APP_STATE_MANUALREG_GETOTP; MMap
	 * map = new MMap(); map.set("phone", userNumber); map.set("product", "oa");
	 * map.set("domain", "shabik.com"); // MoWebUtil // .sendRawRequest( //
	 * "http://mchatapi.mozat.com/index.php?r=MTRegister/sendPhoneNum", //
	 * map.toString(), this, false); }
	 */

	private void doLogin(String username, String password) {
		debug("--doLogin---username:" + username + "/password:" + password);
		this.username = username;
		this.password = password;
		milk.startNetwork();

	}

	/*
	 * private void getAutoRegToken() { // MoWebUtil // .sendRawRequest( //
	 * "http://moweb-stc.morange.com/new_auth_id.ashx", "", // this, true); //
	 * MoWebUtil.sendRawRequest( // "http://www.google.com", "", this); }
	 */

	/*
	 * private void doRegister() { regTryCount = 0;
	 * setNextState(APP_STATE_AUTOREG_TOKEN); getAutoRegToken(); }
	 */

	private void doCommand(MMap command) {
		debug("doCommand: " + command.toString());
		try {
			if (command != null && command.hasKey("action")) {
				String action = command.getString("action");
				if (action.equals("doLogin")) {
					String username = command.getString("username");
					String password = command.getString("password");
					String passwordMd5 = command.getString("password-md5");
					if (username != null && username.length() > 0) {
						if (password != null && password.length() > 0) {
							password = new MD5().getHashString(password);
							doLogin(username, password);
						} else if (passwordMd5 != null
								&& passwordMd5.length() > 0) {
							doLogin(username, passwordMd5);
						}
					}

					setNextState(APP_STATE_AUTOLOGIN);

				} else if (action.equals("showHome")) {
					setNextState(APP_STATE_HOME);
				} else if (action.equals("changeLanguage")) {
					language = command.getString("lang");
					if (language == null || language.length() == 0) {
						language = "ar";
					}
					saveDb("lang", language, false);
					this.userAgent = null;
				} else if (action.equals("quit")) {
					milk.destroyApp();
				} else if (action.equals("openBrowser")) {
					String url = command.getString("url");
					milk.openBrowser(url);
				} else if (action.equals("ashaBilling")) {
					String pid = command.getString("pid");
					this.requestPayment(pid);
					// TODO: do asha billing,
					// return result:
					// Adaptor.getInstance().sendCommand("main",map);
				} else if (action.equals("mmBilling")) {
					String paycode = command.getString("paycode");
					this.purchaseForMM(paycode);
				} else if (action.equals("googleplay_inapp")) {
					String productId = command.getString("productId");
					this.purchaseForGooglePlay(productId);
				} else if (action.equals("googleplay_productlist")) {
					getProductListForGooglePlay();
				} else if (action.equals("googleplay_support_inapp")) {
					queryGooglePlayInappSupport();
				} else if (action.equals("showSystemSmsView")) {
					String toNumber = command.getString("toPhoneNumber");
					String initString = command.getString("initString");
					showSystemSmsView(toNumber, initString);
					debug("show system sms view");
				} else if (action.equals("changeService")) {
					try {
						mergeConfigMap(command);
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			error("error executing system command " + command);
		}
	}

	public boolean mergeConfigMap(MMap command) throws Exception {
		boolean serviceChanged = false;
		byte[] data = milk.readImmutable("config");
		if (data != null) {

			String configText = new String(data, "UTF-8");
			MMap configMap = (MMap) parse(new StringData(configText), null);

			if (command.hasKey("oapro")) {
				int temp = command.getInt("oapro");
				if (temp != gameServiceId) {
					gameServiceId = temp;
					configMap.set("game-service-id", gameServiceId);
					serviceChanged = true;
				}
			}

			if (command.hasKey("feedback")) {
				int temp = command.getInt("feedback");
				if (temp != reportServiceId) {
					reportServiceId = temp;
					configMap.set("feedback-service-id", reportServiceId);
					serviceChanged = true;
				}

			}
			if (command.hasKey("mgserver")) {
				int temp = command.getInt("mgserver");
				if (temp != mgServerServiceId) {
					mgServerServiceId = temp;
					configMap.set("mgserver-service-id", mgServerServiceId);
					serviceChanged = true;
				}
			}
			if (command.hasKey("chat")) {
				int temp = command.getInt("chat");
				if (temp != chatServiceId) {
					chatServiceId = temp;
					configMap.set("chat-service-id", chatServiceId);
					serviceChanged = true;
				}
			}
			if (command.hasKey("mu")) {
				String temp = command.getString("mu");
				if (temp != null && !temp.equals(monetUrl)) {
					monetUrl = temp;
					configMap.set("monet-url", monetUrl);
					serviceChanged = true;
				}
			}
			if (command.hasKey("mp")) {
				int temp = command.getInt("mp");
				if (temp != monetPort) {
					monetPort = temp;
					configMap.set("monet-port", monetPort);
					serviceChanged = true;
				}
			}
			if (serviceChanged) {
				debug("change service :" + configMap.toString());
				configMapToRecord = configMap;
				this.doLogin(username, password);
			}
		}
		return serviceChanged;
	}

	public void update() {
		if (pauseApp) {
			// doSleep(100);
			return;
		}
		long now = System.currentTimeMillis();
		if (now - this.lastDataReportTime > 30000) {
			lastDataReportTime = now;
			this.sendGameRequestReport(dataReports);
			dataReports.removeAllElements();
		}

		if (state == -1) {
			setNextState(APP_STATE_BEGIN);
		}
		updateLoading();
		switch (state) {
		case APP_STATE_HOME: {
			Core.getInstance().update();
			break;
		}
		case APP_STATE_INIT_WAIT: {

			int nextState = GameManager.getInstance()
					.getAppStateByResInitStep();
			setNextState(nextState);
			break;
		}
		case APP_STATE_LOAD_RES: {

			int nextState = GameManager.getInstance()
					.getAppStateByResInitStep();

			if (nextState != APP_STATE_GAME_PLAY
					&& System.currentTimeMillis()
							- lastMultiResourceMessageTime > 20 * 1000) {

				processRequestResources();
				infor("wait for InMultiResourceMessage time out.request resources again.");
			}
			setNextState(nextState);
			break;
		}
		case APP_STATE_GAME_PLAY:
			Core.getInstance().update();
			GameManager.getInstance().imageResourcesRecycle();
			break;
		}
		// clearInputEvents();
	}

	public void updateLoading() {
		if (loadingScene != null && loadingScene.isInited) {
			try {
				loadingScene.runCallbacks();
			} catch (Exception t) {
				exception(t);
			}
		}
	}

	void writeMutable(String key, byte[] bytes) {
		milk.writeMutable(key, bytes);
	}

	public void loginSuccess() {
		if (configMapToRecord != null) {
			try {
				milk.writeMutable("config", configMapToRecord.toString()
						.getBytes("UTF-8"));
			} catch (Exception e) {
			}
			configMapToRecord = null;
		}
		queryIsSendReport();
		debug("LOGIN SUCCESS!");
		if (!hasLogined || !this.realmInfosFetched) {
			queryRealm();
			updateAuthCache();
			hasLogined = true;
		}
		if (!manifestReceived && this.realmInfosFetched) {
			prepare();
			setNextState(APP_STATE_INIT_WAIT);
		}

		if (state == APP_STATE_LOAD_RES) {
			processRequestResources();
		}

		if (supportAshaBilling) {
			Adaptor.getInstance().getIAPProductList();
			// Adaptor.getInstance().sendIAPTicket("123456", "ticket");
		}

	}

	public void loginFailure(boolean clearCache) {
		configMapToRecord = null;
		if (clearCache) {
			saveDb("auth-data", "", false);
			setNextState(APP_STATE_HOME);
		}
		try {
			MMap command = new MMap();
			command.set("login", "1");
			sendCommand(Core.getInstance().getCurrentScene().getSceneId(),
					command);
		} catch (Exception e) {
		}
	}

	public void process(InMessage packet) {
		if (packet instanceof InChatMessage
				|| packet instanceof InChatResponseMessage
				|| packet instanceof InDynamicPayMessage) {
			chatListener.receiveMessage(packet);
		} else if (packet instanceof InReportMessage) {
			reportFlags = ((InReportMessage) packet).isSendReport;
			System.out.println("reportFlags: " + reportFlags);
			sendLoginReport(loginDuration);
		} else if (packet instanceof InGameMessage) {
			InGameMessage gameMsg = (InGameMessage) packet;

			String key = gameMsg.key;
			String value = gameMsg.value;
			int sourceHash = -1;
			try {
				sourceHash = Integer.parseInt(gameMsg.msgId);
			} catch (Exception e) {
			}

			if ("Realm.Query".equals(key)) {
				realmInfosFetched = true;
				try {

					this.realmInfos = (MMap) parse(new StringData(value), null);
					if (realmInfos == null) {
						Adaptor.debug("realmInfo response is null");
					} else {
						Adaptor.debug(realmInfos.toString());
						String last = realmInfos.getString("last");
						if (last != null) {
							MArray others = realmInfos.getArray("other");
							int osize = others.size();
							for (int i = 0; i < osize; i++) {
								MMap realmInfo = others.getMap(i);
								String name = realmInfo.getString("name");
								if (name.equals(last)) {
									if (!mergeConfigMap(realmInfo)) {
										if (!this.manifestReceived) {
											prepare();
											setNextState(APP_STATE_INIT_WAIT);
										}
									}

									break;
								}
							}
						}
						else{
							if (!this.manifestReceived) {
								prepare();
								setNextState(APP_STATE_INIT_WAIT);
							}
						}
					}
				} catch (Exception e) {
					if (!this.manifestReceived) {
						prepare();
						setNextState(APP_STATE_INIT_WAIT);
					}
				}
			}
			debug("<<<<<sendData reply [" + key + "] [" + value.length()
					+ "]chars>>>>>");
			networkEvents.addElement(new MDataEvent(key, value, sourceHash));
			processSendingData(gameMsg.msgId, key);
			// String sendDataKey = gameMsg.msgId + "|" + key;
			// if (sendingData.containsKey(sendDataKey)) {
			// MArray dataReport = new MArray();
			// dataReport.append(key);
			// dataReport.append(new Integer(
			// (int) (System.currentTimeMillis() - ((Long) sendingData
			// .get(sendDataKey)).longValue())));
			// dataReports.addElement(dataReport);
			// sendingData.remove(sendDataKey);
			// }
		} else if (packet instanceof InNewGameMessage) {
			InNewGameMessage msg = (InNewGameMessage) packet;
			String key = msg.msgId;
			String value = msg.value;
			Integer thing = (Integer) newGameMsgs.get(key);

			if (thing != null) {
				newGameMsgs.remove(key);
				int sourceHash = thing.intValue();

				debug("<<<<<sendData reply [" + key + "] [" + value.length()
						+ "]chars>>>>>");
				networkEvents
						.addElement(new MDataEvent(key, value, sourceHash));
				processSendingData(msg.msgId, key);
				// String sendDataKey = msg.msgId + "|" + key;
				// if (sendingData.containsKey(sendDataKey)) {
				// MArray dataReport = new MArray();
				// dataReport.append(key);
				// dataReport.append(new Integer((int) (System
				// .currentTimeMillis() - ((Long) sendingData
				// .get(sendDataKey)).longValue())));
				// dataReports.addElement(dataReport);
				// sendingData.remove(sendDataKey);
				// }
			}

		} else if (packet instanceof InMessage) {
			receiveMServerPacket(packet);
		} else {
			Adaptor.infor("MilkMidlet receive unknow message." + packet);
		}

	}

	public static void setSupportIAPPayment(boolean supportPayment) {
		supportAshaBilling = supportPayment;
	}

	public void load() {
		try {
			byte[] data = milk.readImmutable("config");
			if (data != null) {
				String configText = new String(data, "UTF-8");
				MMap configMap = (MMap) parse(new StringData(configText), null);
				// this.browserServiceId =
				// configMap.getInt("browser-service-id");
				// debug("browser service id " + browserServiceId);

				if (configMap.hasKey("game-service-id")) {
					gameServiceId = configMap.getInt("game-service-id");
					debug("game service id " + gameServiceId);
				}

				if (configMap.hasKey("channel-id")) {
					channelId = configMap.getInt("channel-id");
					debug("channel id " + channelId);
				}

				if (configMap.hasKey("new-game-service-id")) {
					newGameServiceId = configMap.getInt("new-game-service-id");
					debug("new game service id " + gameServiceId);
				}
				if (configMap.hasKey("moagent-wap-url")) {
					moagentWap = configMap.getString("moagent-wap-url");
					debug("moagentWap url " + moagentWap);
				}
				if (configMap.hasKey("billing")) {
					billing = configMap.getString("billing");
					debug("billing " + billing);
				}

				if (configMap.hasKey("feedback-service-id")) {
					int temp = configMap.getInt("feedback-service-id");
					if (temp != -1) {
						this.reportServiceId = temp;
					}

					debug("GameFeedback serviceId " + reportServiceId);
				}

				this.mgServerServiceId = configMap
						.getInt("mgserver-service-id");
				debug("mgserver service id " + mgServerServiceId);
				this.monetUrl = configMap.getString("monet-url");
				debug("monet url " + monetUrl);
				this.monetPort = configMap.getInt("monet-port");
				debug("monet port " + monetPort);
				this.domain = configMap.getString("domain");
				debug("domain " + domain);
				this.game = configMap.getString("game");
				debug("game " + game);
				this.userDomain = configMap.getString("user-domain");
				debug("userDomain " + userDomain);
				this.chatServiceId = configMap.getInt("chat-service-id");
				debug("chat service id : " + chatServiceId);
				this.width = configMap.getInt("width");
				this.height = configMap.getInt("height");
				debug("dimension : [" + width + "," + height + "]");

			}

		} catch (Exception e) {
		}

		try {
			String begin = domain + "_" + game + "_";
			Vector files = milk.getFileList();
			if (files != null) {
				int count = files.size();
				for (int i = 0; i < count; i++) {
					String fileName = (String) files.elementAt(i);
					// if (fileName.indexOf("chat") != -1) {
					// int brk = 1;
					// int a = brk;
					// }

					try {
						int index1 = fileName.lastIndexOf('_');
						if (index1 != -1) {
							int index2 = fileName.indexOf(begin);
							if (index2 == 0) {
								String name = fileName.substring(
										begin.length(), index1);
								if (name.equals("boot_home")) {
									int brk = 1;
									int a = brk;
								}
								String versionStr = fileName
										.substring(index1 + 1);
								int version = Integer.parseInt(versionStr);
								Integer last = (Integer) cachedResources
										.get(name);
								if (last == null || last.intValue() < version) {

									cachedResources.put(name, new Integer(
											version));
								}

							}
						}
					} catch (Exception e) {

					}
				}
			}

		} catch (Exception e) {

		}

		screen = new MRect(0, 0, milk.getCanvasWidth(), milk.getCanvasHeight());

		try {
			byte[] data = milk.readImmutable("manifest");
			if (data != null) {
				GameManager.getInstance().parseManifest(data, false);
				GameManager.getInstance().loadL10n();
			}
		} catch (Exception e) {

		}
		database = new Hashtable();
		try {
			byte[] bytes = milk.readMutable(genFileKey("database"));

			if (bytes != null) {
				MilkInputStream dis = new MilkInputStream(
						new ByteArrayInputStream(bytes));
				while (dis.available() > 0) {
					String key = readVarChar(dis);
					String value = readIntStr(dis);
					database.put(key, value);
					System.out.println("-------------------database-----key:"
							+ key + "/ value:" + value);
				}
			}

		} catch (Exception t) {
			t.printStackTrace();
		}
		// language = loadDb("language", false);
		language = loadDb("lang", false);
		if (language == null || language.length() == 0) {
			language = "ar";
		}
		this.userAgent = null;

	}

	public void init() {
		this.chatListener = HallAccess.getChatListener();
		if (supportAshaBilling) {
			this.getIAPProductList();
		}
		try {
			this.grabImageResource("spinner0");
			this.grabImageResource("spinner1");
		} catch (Exception e) {
		}
	}

	public static MilkApp milk;

	public static MilkUiFactory uiFactory;

	public void onComplete(byte[] bytes, RawRequest request) {
		if (bytes == null) {
			networkEvents.addElement(new MDataEvent(request.key, null,
					request.sourceHash));
		} else {
			if (!request.moagentWap) {
				try {
					networkEvents.addElement(new MDataEvent(request.key,
							new String(bytes, "UTF-8"), request.sourceHash));
					request = null;
				} catch (Exception e) {

				}
			} else {
				try {
					InMessage msg = InMessage.parse(new MilkInputStream(
							new ByteArrayInputStream(bytes)));
					if (msg instanceof InRawRequestMessage) {
						InRawRequestMessage rawRequestMsg = (InRawRequestMessage) msg;
						networkEvents.addElement(new MDataEvent(request.key,
								rawRequestMsg.response, request.sourceHash));
					}
				} catch (Exception e) {
					networkEvents.addElement(new MDataEvent(request.key, null,
							request.sourceHash));
				}
			}

		}
	}

	public void sendSMSResult(boolean successToSend) {

		synchronized (smsEvents) {
			smsEvents.addElement(new MSmsEvent(successToSend));
		}

	}

	public MSmsEvent consumeSmsEvent() {
		MSmsEvent event = null;
		synchronized (smsEvents) {
			if (smsEvents.size() > 0) {
				event = (MSmsEvent) smsEvents.elementAt(0);
				smsEvents.removeElementAt(0);
			}
		}
		return event;
	}

	void initChatTabRect(int x, int y, int w, int flag) {

		if (flag == 1) {// normal
			HallAccess.setNormalNotifyTab(x, y, w);
		} else if (flag == 0) {// top
			HallAccess.setTopNotifyTab(x, y, w);
		}
	}

	private boolean showBottomChatTab = false;
	private boolean showTopChatTab = false;

	void enableShowChatTab(int flag) {
		if (flag == 1) {// top show
			showTopChatTab = true;
		} else if (flag == 2) {// bottom show
			showBottomChatTab = true;
		} else {// 0 hide all
			showBottomChatTab = false;
			showTopChatTab = false;
		}
	}

	// public void setIapMessageListener(IapMessageListener listener) {
	// this.iapMessageListener = listener;
	// }

	public static Vector split(String original, String separator, int count) {
		Vector result = new Vector();
		int index = 0;
		int begin = 0;
		int added = 0;
		index = original.indexOf(separator, begin);
		if (index == -1) {
			result.addElement(original);
			return result;
		}
		while (added < count) {
			if (index == -1 || added == count - 1) {
				if (begin < original.length()) {
					added++;
					result.addElement(original.substring(begin));
				}
				break;
			}
			if (begin < index) {
				added++;
				result.addElement(original.substring(begin, index));
			}
			begin = index + separator.length();
			index = original.indexOf(separator, begin);

		}
		return result;

	}

	private MMBillingHandler mmBillingHandler;
	private MMBillingCallback billingCallback;

	public void setMMBillingHandler(MMBillingHandler mmbh) {
		mmBillingHandler = mmbh;
	}

	private void purchaseForMM(String paycode) {
		System.out.println("------------purchaseForMM paycode" + paycode);
		if (mmBillingHandler != null) {

			if (billingCallback == null) {
				billingCallback = new MMBillingCallback() {
					public void purchaseResult(boolean success, String paycode,
							String id) {
						sendMMBillingResultCommand(success, paycode, id);
					}
				};
			}

			mmBillingHandler.purchase(paycode, billingCallback);
		} else {
			System.out.println("------------mmBillingHandler == null");
		}
	}

	private void sendMMBillingResultCommand(boolean success, String paycode,
			String returnId) {
		int result = 0;
		if (success) {
			result = 1;
		}
		System.out.println("------------sendMMBillingResultCommand paycode:"
				+ paycode + "/ returnId" + returnId + " success" + success);
		MMap command = new MMap();
		command.set("result", new Integer(result));
		command.set("paycode", paycode);
		command.set("returnId", returnId);
		this.sendCommand("main", command);
	}

	private GoogleBillingResultListener googleBillingResultListener;

	private GoogleBillingHandler googleBillingHandler;

	public void setGoogleBillingHandler(GoogleBillingHandler billhandler) {
		googleBillingHandler = billhandler;
	}

	private void getProductListForGooglePlay() {
		System.out
				.println("------------getProductListForGooglePlay googleBillingHandler:"
						+ googleBillingHandler);
		if (googleBillingHandler != null) {
			String info[][] = googleBillingHandler.getPurchaseProductList();
			MMap command = new MMap();
			command.set("count", new Integer(info.length));
			for (int i = 0; i < info.length; i++) {
				command.set("productId" + i, info[i][0]);
				command.set("title" + i, info[i][1]);
				command.set("price" + i, info[i][2]);
				command.set("description" + i, info[i][3]);
			}
			this.sendCommand("main", command);
		}
	}

	private void queryGooglePlayInappSupport() {
		boolean inappSupport = false;
		if (googleBillingHandler != null) {
			inappSupport = googleBillingHandler.supportInappBilling();
		}
		int result = 0;
		if (inappSupport) {
			result = 1;
		}
		MMap command = new MMap();
		command.set("supportResult", new Integer(result));
		this.sendCommand("main", command);
	}

	private SystemSmsViewHandler systemSmsViewHandler;

	public void setSystemSmsViewHandler(SystemSmsViewHandler smsViewHandler) {
		systemSmsViewHandler = smsViewHandler;
	}

	private void showSystemSmsView(String toNumber, String initString) {
		if (systemSmsViewHandler != null) {
			systemSmsViewHandler.showSystemSmsView(toNumber, initString);
		}
	}

	private void purchaseForGooglePlay(String productId) {
		System.out.println("------------purchaseForGooglePlay productId:"
				+ productId + "/ googleBillingHandler:" + googleBillingHandler);
		if (googleBillingHandler != null) {
			if (googleBillingResultListener == null) {
				googleBillingResultListener = new GoogleBillingResultListener() {
					public void onBillingResult(boolean success,
							String productId, String signedData,
							String signature) {
						sendGoogleBillingResultCommand(success, productId,
								signedData, signature);
					}
				};
			}
			googleBillingHandler.googlePlayPurchase(productId,
					googleBillingResultListener);
		}
	}

	private void sendGoogleBillingResultCommand(boolean success,
			String productId, String signedData, String signature) {
		int result = 0;
		if (success)
			result = 1;
		System.out
				.println("------------sendGoogleBillingResultCommand productId:"
						+ productId + "/ success" + success);
		MMap command = new MMap();
		command.set("result", new Integer(result));
		command.set("productId", productId);
		if (success) {
			command.set("signedData", signedData);
			command.set("signature", signature);
		}
		this.sendCommand("main", command);
	}

}
