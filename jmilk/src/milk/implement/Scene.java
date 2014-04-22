package milk.implement;

import java.io.ByteArrayInputStream;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import milk.chat.core.HallAccess;
import milk.implement.IMEvent.MCommandEvent;
import milk.implement.IMEvent.MDataEvent;
import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MKeyEvent;
import milk.implement.IMEvent.MResourceEvent;
import milk.implement.IMEvent.MRightKeyEvent;
import milk.implement.IMEvent.MSmsEvent;
import milk.implement.mk.MArray;
import milk.implement.mk.MDraw;
import milk.implement.mk.MGroup;
import milk.implement.mk.MMap;
import milk.implement.mk.MPlayer;
import milk.implement.mk.MRect;
import milk.implement.mk.MState;
import milk.implement.mk.MText;
import milk.implement.mk.MTiles;
import milk.implement.mk.MoveState;
import milk.menu.WindowMenu;
import milk.ui2.InputListener;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;
import milk.ui2.MilkLocker;
import milk.ui2.MilkSprite;
import smartview3.elements.Sv3Element;

class MyRandom extends Random {
	public int nextInt(int n) {
		if (n <= 0)
			throw new IllegalArgumentException("n must be positive");

		if ((n & -n) == n) // i.e., n is a power of 2
			return (int) ((n * (long) next(31)) >> 31);

		int bits, val;
		do {
			bits = next(31);
			val = bits % n;
		} while (bits - val + (n - 1) < 0);
		return val;
	}
}

public class Scene implements InputListener {

	protected static final int FLAG_AUTOCENTER = 0x01;
	protected static final int FLAG_SHADING = 0x02;
	protected static final int FLAG_FOCUSFIRST = 0x04;
	protected static final int FLAG_NOBG = 0x08;
	protected static final int FLAG_NOCLOSE = 0x10;
	protected static final int FLAG_FITHEIGHT = 0x20;
	protected static final int FLAG_HASMENU = 0x40;
	protected static final int FLAG_EMBEDDED = 0x80;

	private int strokeColor;
	private int fillColor;

	protected MRect screenRect;

	protected WindowMenu windowMenu;

	public boolean isMenuOpen() {
		return windowMenu != null && windowMenu.isOpen();
	}

	public boolean shouldTriggerMenu(int x, int y) {
		return windowMenu != null && WindowMenu.clickOnMenubar(x, y);
	}

	public void triggerMenu() {
		if (windowMenu != null) {
			windowMenu.trigger();
		}
	}

	public void openMenu() {
		if (windowMenu != null) {
			windowMenu.show();
		}
	}

	public void closeMenu() {
		if (windowMenu != null) {
			windowMenu.hide();
		}
	}

	private String input;
	private MilkLocker waiter = Adaptor.uiFactory.createLocker();
	private Hashtable libs = new Hashtable();

	protected int flags = 0;

	public int getFlags() {
		return flags;
	}

	protected Scene parent = null;

	public Scene getParent() {
		return parent;
	}

	public void setParent(Scene parent) {
		this.parent = parent;
	}

	protected String sceneId = null;
	protected String resId = null;
	protected MRect clipRect = new MRect();
	private MMap params = null;

	private int[] paramVars = new int[22];
	private Object[] paramObjs = new Object[11];

	protected MilkImage darkLine = null;

	protected Vector windows = VectorPool.produce();

	public MDraw currentDraw = null;
	public boolean processWillDraw = true;

	// public Vector loadingResources = new Vector();

	public String getSceneId() {
		return sceneId;
	}

	// public MMap getParams() {
	// return params;
	// }

	public void resourceLoaded(String resId, int hashcode) {
		if (this.hashCode() == hashcode) {
			// loadingResources.removeElement(resId);
			// if (loadingResources.size() == 0) {
			// resume();
			// }
		}
	}

	protected void initSceneParams(String sceneId, String resId,
			final MRect rect, MMap params, int flags, Scene parent) {
		this.sceneId = sceneId;
		this.resId = resId;
		this.params = params;
		this.screenRect = rect;

		if (flags != -1) {
			this.flags = flags;
		}
		this.parent = parent;
	}

	protected void init(byte[] byteCode) {
		try {
			global = initByteCode(byteCode, null);
		} catch (Exception t) {
			Adaptor.exception(t);
			Adaptor.error("instruction[" + curStack.istPtr + "] error["
					+ t.getMessage() + "]");
		}
	}

	Scene getTopMostScene() {
		if (windows == null) {
			return this;
		}
		int size = windows.size();
		if (size > 0) {
			return ((Scene) windows.elementAt(size - 1)).getTopMostScene();
		} else {
			return this;
		}
	}

	protected Scene() {
		Adaptor.getInstance().enableShowChatTab(0);
	}

	Scene(String sceneId, String resourceId, MRect rect, MMap params,
			int flags, Scene parent) throws Exception {
		Adaptor.milk.hideInput();
		Adaptor.getInstance().enableShowChatTab(0);
		initSceneParams(sceneId, resourceId, rect, params, flags, parent);
		byte[] byteCode = Adaptor.milk.gunzip(Adaptor.getInstance()
				.getResource(resourceId));
		if (byteCode != null) {
			init(byteCode);
		}

	}

	protected void drawScreen(MilkGraphics g) {

		screen.draw(g, screenRect, 0, 0);
		drawLayers(g);

	}

	protected void drawShader(MilkGraphics g) {
		if ((flags & FLAG_SHADING) > 0) {
			doDrawShader(g);
		}
	}

	protected void doDrawShader(MilkGraphics g) {
		int height = Adaptor.milk.getCanvasHeight() / 10;
		MilkImage line = Adaptor.getAlphaColorLine(0x80000000, height);
		for (int i = 0; i < 11; i++) {
			g.drawImage(line, 0, 0 + i * height, MilkGraphics.TOP
					| MilkGraphics.LEFT);

		}
	}

	protected void drawLayers(MilkGraphics g) {

		int count = windows.size();
		for (int i = 0; i < count; i++) {
			Scene scene = (Scene) windows.elementAt(i);
			scene.draw(g);
		}
	}

	protected void draw(MilkGraphics g) {
		drawShader(g);
		g.setColor(bgColor);
		g.fillRect(0, 0, Adaptor.milk.getCanvasWidth(),
				Adaptor.milk.getCanvasHeight());
		try {
			drawScreen(g);
		} catch (Exception e) {
			// Adaptor.exception(e);
		}
		drawLoading(g);
	}

	protected void drawLoading(MilkGraphics g) {
		if (showLoading || Adaptor.getInstance().isBlockingRequesting()) {
			doDrawShader(g);
			Adaptor.getInstance().drawLoading(g, 0, 0,
					Adaptor.milk.getCanvasWidth(),
					Adaptor.milk.getCanvasHeight(), false);
		}
	}

	public void runCallbacks() {
		if (!isInited) {
			runInit();
		}

		{
			MKeyEvent key = Adaptor.getInstance().consumeKey();
			while (key != null) {
				boolean handled = false;
				int count = windows.size();
				for (int i = 0; i < count; i++) {
					Scene scene = (Scene) windows.elementAt(i);
					if (scene.isMenuOpen()) {
						scene.windowMenu.onKeyEvent(key);
						handled = true;
						break;
					}
				}
				if (isMenuOpen() && !handled) {
					windowMenu.onKeyEvent(key);
					handled = true;
				}
				if (!handled) {
					((Scene) getTopMostScene()).handleKeyEvent(key);
				}
				key = Adaptor.getInstance().consumeKey();
			}

		}
		{
			MFingerEvent finger = Adaptor.getInstance().consumeFinger();
			if (finger != null) {

				if (finger.getType() == Adaptor.POINTER_ZOOMIN
						|| finger.getType() == Adaptor.POINTER_ZOOMOUT) {
					handleZoomEvent(finger);
				} else {

					boolean handled = false;
					int count = windows.size();
					// WindowMenu.clickOnMenubar(finger.getX(), finger.getY());
					for (int i = 0; i < count; i++) {
						Scene scene = (Scene) windows.elementAt(i);
						if (scene.isMenuOpen()
								|| scene.shouldTriggerMenu(finger.getX(),
										finger.getY())) {
							handled = scene.windowMenu.onFingerEvent(finger);
							if (handled) {
								break;
							}
						}
					}
					if (!handled) {
						if (isMenuOpen()
								|| shouldTriggerMenu(finger.getX(),
										finger.getY())) {
							handled = windowMenu.onFingerEvent(finger);
						}
					}
					if (!handled) {
						handleFingerEvent(finger);
					}
				}
				// finger = Adaptor.getInstance().consumeFinger();
			}
		}

		{
			MDataEvent data = Adaptor.getInstance().consumeData();
			if (data != null) {

				handleDataEvent(data);
			}
		}

		{

			MCommandEvent command = Adaptor.getInstance().consumeCommand();
			if (command != null) {
				try {
					handleCommandEvent(command);
				} catch (Exception e) {
					Adaptor.exception(e);
				}
			}

		}

		{
			MResourceEvent resourceEvent = Adaptor.getInstance()
					.consumeLastLoadedResource();
			if (resourceEvent != null) {

				handleResourceEvent(resourceEvent);
			}
		}

		{
			MRightKeyEvent rightKeyEvent = Adaptor.getInstance()
					.consumeRightKey();
			if (rightKeyEvent != null) {
				boolean handled = false;
				handled = closeMenus();
				if (!handled) {
					handled = handleRightKey(rightKeyEvent);
				}
				if (!handled) {
					closeWindowOnRightKey();
				}

			}
		}

		{
			MRightKeyEvent leftKeyEvent = Adaptor.getInstance()
					.consumeLeftKey();
			if (leftKeyEvent != null) {
				boolean handled = false;
				int count = windows.size();
				for (int i = 0; i < count; i++) {
					Scene scene = (Scene) windows.elementAt(i);
					if ((scene.getFlags() & FLAG_HASMENU) != 0) {
						if (scene.isMenuOpen()) {
							scene.triggerMenu();
						} else {
							scene.openMenu();
						}
						handled = true;
						break;
					}
				}
				if (!handled) {
					if ((this.getFlags() & FLAG_HASMENU) != 0) {
						if (this.isMenuOpen()) {
							this.triggerMenu();
						} else {
							this.openMenu();
						}
						handled = true;
					}
				}
				if (!handled) {
					if (this.onLeftKey != null && leftKeyEvent != null) {
						prepareCallParams();
						try {
							Adaptor.infor("Global.onLeftKey");
							execute(onLeftKey);
						} catch (Exception t) {
							Adaptor.exception(t);
							Adaptor.error("error at bytecode line :"
									+ curStack.istPtr);
						}
					} else if (leftKeyEvent != null) {// add leftKeyEvent for
														// chat
						((Scene) getTopMostScene())
								.handleLeftKeyEvent(leftKeyEvent);
					}
				}

			}
		}
		{
			MSmsEvent smsEvent = Adaptor.getInstance().consumeSmsEvent();
			if (smsEvent != null) {
				handleSmsEvent(smsEvent);
			}
		}

		{
			onWakeup();
			int count = windows.size();
			for (int i = count - 1; i >= 0; i--) {
				Scene scene = (Scene) windows.elementAt(i);
				scene.onWakeup();
			}
		}
	}

	boolean closeMenus() {
		boolean handled = false;
		int count = windows.size();
		for (int i = 0; i < count; i++) {
			Scene scene = (Scene) windows.elementAt(i);
			handled = scene.closeMenus();
			if (handled) {
				break;
			}
		}
		if (!handled) {
			if (!handled) {
				if ((this.getFlags() & FLAG_HASMENU) != 0) {
					if (this.isMenuOpen()) {
						this.closeMenu();
						handled = true;
					}
				}
			}
		}
		return handled;
	}

	public boolean handleRightKey(MRightKeyEvent rightKeyEvent) {
		boolean handled = false;
		Scene top = getTopMostScene();

		if (top.onRightKey != null) {
			top.handleRightKeyEvent(rightKeyEvent);
			handled = true;
		}
		return handled;
	}

	boolean closeWindowOnRightKey() {
		boolean handled = true;
		Scene top = getTopMostScene();
		if (top.parent != null && (top.flags & FLAG_NOCLOSE) == 0) {
			top.parent.closeWindow(top.getSceneId());
		}
		return handled;
	}

	static class TimeOut {
		long timeout;
		static int timeOutSid = 0;
		int id = timeOutSid++;
		MilkCallback callback;

		public TimeOut(int delay, MilkCallback callback) {
			this.callback = callback;
			this.timeout = System.currentTimeMillis() + delay;
		}

		public Date getDate() {
			Date thing = new Date(timeout);
			return thing;
		}
	}

	private static class TryBlock {
		int catchStart;
		int type;
		int stack = 0;
	}

	public static final int END = 0;// 0
	public static final int ADD = END + 1;
	public static final int SUB = ADD + 1;
	public static final int MUL = SUB + 1;
	public static final int DIV = MUL + 1;
	public static final int MOD = DIV + 1;// 5
	public static final int LSH = MOD + 1;
	public static final int RSH = LSH + 1;
	public static final int AND = RSH + 1;
	public static final int OR = AND + 1;
	public static final int XOR = OR + 1;// 10
	public static final int CMP = XOR + 1;
	public static final int NOT = CMP + 1;
	public static final int MOVINT = NOT + 1;
	public static final int MOVSTR = MOVINT + 1;
	public static final int CREATEVAR = MOVSTR + 1;// 15
	public static final int DELETEVAR = CREATEVAR + 1;
	public static final int COPYVAR = DELETEVAR + 1;
	public static final int JE = COPYVAR + 1;
	public static final int JNE = JE + 1;
	public static final int JL = JNE + 1;// 20
	public static final int JLE = JL + 1;
	public static final int JG = JLE + 1;
	public static final int JGE = JG + 1;
	public static final int JMP = JGE + 1;
	public static final int CALL = JMP + 1;// 25
	public static final int RET = CALL + 1;
	public static final int TOSTR = RET + 1;
	public static final int TOINT = TOSTR + 1;
	public static final int STRLEN = TOINT + 1;
	public static final int SUBSTR = STRLEN + 1;// 30
	public static final int STRCAT = SUBSTR + 1;
	public static final int STRREPLACE = STRCAT + 1;
	public static final int STRMATCH = STRREPLACE + 1;
	public static final int STRPOS = STRMATCH + 1;
	public static final int STRCASE = STRPOS + 1;// 35
	public static final int LBLTOINT = STRCASE + 1;
	public static final int URLENCODE = LBLTOINT + 1;
	public static final int MSIZE = URLENCODE + 1;
	public static final int MHAS = MSIZE + 1;
	public static final int MGET = MHAS + 1;// 40
	public static final int MSET = MGET + 1;
	public static final int MDEL = MSET + 1;
	public static final int MGETTYPE = MDEL + 1;
	public static final int MFROMSTR = MGETTYPE + 1;
	public static final int ASIZE = MFROMSTR + 1;// 45
	public static final int AGET = ASIZE + 1;
	public static final int ASET = AGET + 1;
	public static final int ADEL = ASET + 1;
	public static final int AAPPEND = ADEL + 1;
	public static final int AINSERT = AAPPEND + 1;// 50
	public static final int AGETTYPE = AINSERT + 1;
	public static final int AFROMSTR = AGETTYPE + 1;
	public static final int REGCALLBACK = AFROMSTR + 1;
	public static final int EMPTY = REGCALLBACK + 1;
	public static final int RAND = EMPTY + 1;// 50
	public static final int SETTIMEOUT = RAND + 1;
	public static final int CANCELTIMEOUT = SETTIMEOUT + 1;
	public static final int PLAYSOUND = CANCELTIMEOUT + 1;
	public static final int STOPSOUND = PLAYSOUND + 1;
	public static final int LOADSOUND = STOPSOUND + 1;// 55
	public static final int UNLOADSOUND = LOADSOUND + 1;
	public static final int GETENV = UNLOADSOUND + 1;
	public static final int PLAYSCENE = GETENV + 1;
	public static final int GETTIMESTAMP = PLAYSCENE + 1;
	public static final int DEBUG = GETTIMESTAMP + 1;// 60
	public static final int GETRECTDATA = DEBUG + 1;
	public static final int SETRECTDATA = GETRECTDATA + 1;
	public static final int GETPLAYERDATA = SETRECTDATA + 1;
	public static final int SETPLAYERDATA = GETPLAYERDATA + 1;
	public static final int ATTACHPLAYER = SETPLAYERDATA + 1;// 65
	public static final int DETACHPLAYER = ATTACHPLAYER + 1;
	public static final int DEFINESTATE = DETACHPLAYER + 1;
	public static final int SENDDATA = DEFINESTATE + 1;
	public static final int GETTEXTDATA = SENDDATA + 1;
	public static final int SETTEXTDATA = GETTEXTDATA + 1;// 70
	public static final int ATTACHTEXT = SETTEXTDATA + 1;
	public static final int DETACHTEXT = ATTACHTEXT + 1;
	public static final int LAYOUTTEXT = DETACHTEXT + 1;
	public static final int SAVEDB = LAYOUTTEXT + 1;
	public static final int LOADDB = SAVEDB + 1;// 75
	public static final int MOVE = LOADDB + 1;
	public static final int STOP = MOVE + 1;
	public static final int REGOBJECTCALLBACK = STOP + 1;
	public static final int NAVIGATE = REGOBJECTCALLBACK + 1;
	public static final int ISNULL = NAVIGATE + 1;// 80
	public static final int INITOBJ = ISNULL + 1;
	public static final int LOADRESOURCE = INITOBJ + 1;
	public static final int GETIMAGESIZE = LOADRESOURCE + 1;
	public static final int STACKSIZE = GETIMAGESIZE + 1;
	public static final int SETENV = STACKSIZE + 1;// 85
	public static final int GETGROUPDATA = SETENV + 1;
	public static final int SETGROUPDATA = GETGROUPDATA + 1;
	public static final int ADDCHILD = SETGROUPDATA + 1;
	public static final int REMOVECHILD = ADDCHILD + 1;
	public static final int RELATIVEPOS = REMOVECHILD + 1;// 90
	public static final int GETSCREEN = RELATIVEPOS + 1;
	public static final int GETPARENT = GETSCREEN + 1;
	public static final int GETCHILDREN = GETPARENT + 1;
	public static final int DRAWSHAPE = GETCHILDREN + 1;
	public static final int TRANSFORM = DRAWSHAPE + 1;// 95
	public static final int ARGB = TRANSFORM + 1;
	public static final int MASK = ARGB + 1;
	public static final int GETTILESDATA = MASK + 1;
	public static final int SETTILESDATA = GETTILESDATA + 1;
	public static final int TILESOP = SETTILESDATA + 1;// 100
	public static final int TRYSTART = TILESOP + 1;
	public static final int TRYFINISH = TRYSTART + 1;
	public static final int THROW = TRYFINISH + 1;
	public static final int GETELEMENT = THROW + 1;
	public static final int MOMLSET = GETELEMENT + 1;// 105
	public static final int MOMLGET = MOMLSET + 1;
	public static final int MOMLDO = MOMLGET + 1;
	public static final int WINDOWDO = MOMLDO + 1;
	public static final int CLEAN = WINDOWDO + 1;
	public static final int DODEBUG = CLEAN + 1;// 110
	public static final int QUICKINPUT = DODEBUG + 1;
	public static final int STRSPLIT = QUICKINPUT + 1;
	public static final int DOINPUT = STRSPLIT + 1;
	public static final int IMPORT = DOINPUT + 1;
	public static final int SENDHTTPDATA = IMPORT + 1;
	public static final int DOCHAT = SENDHTTPDATA + 1;
	public static final int RESOLVE = DOCHAT + 1;
	public static final int AHAS = RESOLVE + 1;
	public static final int QUERYENV = AHAS + 1;

	public static final int OINT = 0;
	public static final int OSTRING = 1;
	public static final int OARRAY = 2;
	public static final int OMAP = 3;
	public static final int ORECT = 4;
	public static final int OPLAYER = 5;
	public static final int OTEXT = 6;
	public static final int OGROUP = 7;
	public static final int OTILES = 8;
	public static final int OELEMENT = 9;

	private int[] vars = new int[0];
	private Object[] objs = new Object[0];

	int varSize = 0;

	public boolean isInited = false;

	ByteCode global;

	Vector stacks = new Vector();

	MStack curStack = MStack.produce("global");

	// Object[] inputParams = new Object[10];

	Hashtable timeOuts = new Hashtable();

	// Vector tempCallbacks = VectorPool.produce();

	Vector tryStack = VectorPool.produce();

	// private String lastExecutedFunc = null;

	protected ByteCode initByteCode(byte[] bytecode, String libName)
			throws Exception {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytecode);
		MilkInputStream dis = new MilkInputStream(bis);
		Adaptor.infor("init byte : " + System.currentTimeMillis());
		ByteCode byteCode = new ByteCode();
		byteCode.libName = libName;
		// read constant string number
		{
			int count = dis.readShort();
			byteCode.strConsts = new String[count];
			for (int i = 0; i < count; i++) {
				byteCode.strConsts[i] = Adaptor.readVarStr(dis);
			}
		}

		// read instructions
		{
			int count = dis.readInt();
			byteCode.instructions = new Object[count];

			for (int i = 0; i < count; i++) {
				byteCode.instructions[i] = MInstruction.getInstruction(dis);
			}
		}
		// read debug infor
		{
			int count = dis.readInt();
			for (int i = 0; i < count; i++) {
				String func = Adaptor.readVarChar(dis);
				String key = String.valueOf(dis.readShort());
				// Adaptor.debug("fucntion: " + func + " " + key);
				byteCode.functions.put(key, func);
			}
		}
		if (byteCode.instructions != null) {
			Adaptor.infor("instructions [" + byteCode.instructions.length
					+ "] parsed " + System.currentTimeMillis());
		}
		return byteCode;
	}

	void pushStack(String name) {
		stacks.addElement(curStack);
		curStack = MStack.produce(name);
	}

	void popStack() {
		// lastExecutedFunc = curStack.funcName;
		MStack.recycle(curStack);
		curStack = (MStack) stacks.lastElement();
		stacks.removeElementAt(stacks.size() - 1);
	}

	MilkCallback onKeyDown = null;
	MilkCallback onKeyPress = null;
	MilkCallback onKeyUp = null;
	MilkCallback onFingerDown = null;
	MilkCallback onFingerMove = null;
	MilkCallback onFingerUp = null;
	MilkCallback onFingerZoomOut = null;
	MilkCallback onFrame = null;
	MilkCallback onFingerZoomIn = null;
	MilkCallback onResourceLoaded = null;
	MilkCallback onData = null;
	MilkCallback onCommand = null;
	MilkCallback onFocus = null;
	MilkCallback onLostFocus = null;
	MilkCallback onLeftKey = null;
	MilkCallback onRightKey = null;
	MilkCallback onSms = null;

	int bgColor = 0;
	boolean showLoading = false;

	MGroup screen = new MGroup();
	MDraw fingerPlayer = null;
	MPlayer focusPlayer = null;
	private Vector focusablePlayers = VectorPool.produce();

	public void activateRightKey() {
		onRightKey = null;
	}

	public Vector getFocusablePlayers() {
		return focusablePlayers;
	}

	protected void clearMem() {
		global = null;
		closeAllWindows();
		screen.clearImageReferences();
		screen.clearChildren();
		libs.clear();
		parent = null;
		if (windowMenu != null) {
			windowMenu.clearMem();
		}
	}

	public void onStop() {
		isInited = false;
		clearMem();
		Adaptor.getInstance().forceClearImages();
	}

	public void showNotify() {
	}

	public void hideNotify() {

	}

	public void prepareCallParams() {
		curStack.stackStart = varSize;
		clearCallParams();
	}

	public void clearCallParams() {
		for (int i = 0; i < 11; i++) {
			paramObjs[i] = null;
		}
		paramVars[21] = 0;
	}

	protected void addCallParam(int value) {
		int cur = paramVars[21];
		paramObjs[cur] = null;
		paramVars[cur * 2] = 0;
		paramVars[cur * 2 + 1] = value;
		paramVars[21] = cur + 1;
	}

	public void addCallParam(Object value) {
		int cur = paramVars[21];
		paramObjs[cur] = value;
		paramVars[cur * 2] = 1;
		paramVars[21] = cur + 1;
	}

	void onWakeup() {
		{
			// timer callbacks

			if (timeOuts.size() > 0) {
				Enumeration things = timeOuts.keys();
				Vector temp = VectorPool.produce();
				while (things.hasMoreElements()) {
					Integer key = (Integer) things.nextElement();
					TimeOut timeOut = (TimeOut) timeOuts.get(key);
					long current = System.currentTimeMillis();
					if (timeOut.timeout <= current) {
						temp.addElement(key);
						try {
							prepareCallParams();
							execute(timeOut.callback);
						} catch (Exception e) {
							Adaptor.exception(e);
							Adaptor.error("error at bytecode line :"
									+ curStack.istPtr);
						}
					}
				}
				int tempSize = temp.size();
				if (tempSize > 0) {
					for (int i = 0; i < tempSize; i++) {
						Integer key = (Integer) temp.elementAt(i);
						timeOuts.remove(key);
					}
				}
				VectorPool.recycle(temp);

			}

		}
		// {
		//
		// long start = System.currentTimeMillis();
		// int size = tempCallbacks.size();
		// for (int i = 0; i < size; i += 2) {
		// Vector input = (Vector) tempCallbacks.elementAt(i);
		// MilkCallback callback = (MilkCallback) tempCallbacks
		// .elementAt(i + 1);
		// try {
		// this.prepareCallback(input);
		// this.execute(callback);
		// } catch (Exception t) {
		// Adaptor.exception(t);
		// }
		// }
		// tempCallbacks.removeAllElements();
		// Core.logTime(start, "tempcallback", 50);
		//
		// }
		doMore();
	}

	protected void doMore() {
		if (onFrame != null) {
			prepareCallParams();
			addCallParam((int) (System.currentTimeMillis() - Adaptor
					.getInstance().gameStartTime));
			try {
				execute(onFrame);
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
		if (isInited) {
			screen.processAnimation(0, 0);
			// screen.storeDrawStrokes(this, screenRect, 0, 0);
		}
	}

	boolean handleRightKeyEvent(MRightKeyEvent rightKey) {
		boolean result = false;
		if (this.onRightKey != null && rightKey != null) {
			prepareCallParams();
			try {
				Adaptor.infor("Global.onRightKey");
				execute(onRightKey);
				result = getIntVar(0) != 0;
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
		return result;
	}

	public void handleLeftKeyEvent(MRightKeyEvent rightKey) {

		if (this.onLeftKey != null && rightKey != null) {

			prepareCallParams();
			try {
				Adaptor.infor("Global.onLeftKey");
				execute(onLeftKey);
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
	}

	void handleResourceEvent(MResourceEvent resourceEvent) {

		int count = windows.size();
		for (int i = count - 1; i >= 0; i--) {
			((Scene) windows.elementAt(i)).handleResourceEvent(resourceEvent);
		}

		if (onResourceLoaded != null && resourceEvent != null
				&& resourceEvent.src != null
				&& resourceEvent.sourceHash == hashCode()) {
			prepareCallParams();
			addCallParam(resourceEvent.getGuid());
			try {
				Adaptor.infor("Global.onResourceLoaded");
				execute(onResourceLoaded);
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
	}

	void handleDataEvent(MDataEvent dataEvent) {

		boolean handled = false;
		int count = windows.size();
		for (int i = count - 1; i >= 0; i--) {
			Scene scene = ((Scene) windows.elementAt(i));
			if (scene.hashCode() == dataEvent.sourceHash
					|| dataEvent.sourceHash == -1) {

				scene.handleDataEvent(dataEvent);
				if (dataEvent.sourceHash != -1) {
					handled = true;
					break;
				}
			}

		}
		if (!handled
				&& this.onData != null
				&& dataEvent != null
				&& (dataEvent.sourceHash == this.hashCode() || dataEvent.sourceHash == -1)) {
			prepareCallParams();
			addCallParam(dataEvent.key);
			addCallParam(dataEvent.value);
			try {
				Adaptor.infor("Global.onData");
				long start = System.currentTimeMillis();
				execute(onData);
				System.out.println("onData delay : " + dataEvent.key + " "
						+ (System.currentTimeMillis() - start));

			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
	}

	boolean handleSmsEvent(MSmsEvent event) {
		boolean handled = false;

		int count = windows.size();
		for (int i = count - 1; i >= 0; i--) {
			Scene window = ((Scene) windows.elementAt(i));
			handled = window.handleSmsEvent(event);
			if (handled) {
				break;
			}
		}

		if (this.onSms != null && !handled) {
			handled = true;
			prepareCallParams();
			if (event.sucess) {
				addCallParam(1);
			} else {
				addCallParam(0);
			}
			try {
				Adaptor.infor("Global.onSms");
				execute(onSms);
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
		return handled;
	}

	void handleCommandEvent(MCommandEvent command) {

		int count = windows.size();
		for (int i = count - 1; i >= 0; i--) {
			Scene window = ((Scene) windows.elementAt(i));
			if (window.getSceneId() == command.target) {
				window.handleCommandEvent(command);
			}
		}

		if (this.onCommand != null && command != null
				&& command.target.equals(getSceneId())) {
			prepareCallParams();
			addCallParam(command.command);

			try {
				Adaptor.infor("Global.onCommand");
				execute(onCommand);
			} catch (Exception t) {
				Adaptor.exception(t);
				Adaptor.error("error at bytecode line :" + curStack.istPtr);
			}
		}
	}

	public void handleZoomEvent(MFingerEvent event) {

		screen.calcFingerRecursive(event);

		switch (event.getType()) {
		case Adaptor.POINTER_ZOOMIN: {
			if (onFingerZoomIn != null) {
				prepareCallParams();
				addCallParam(event.getX());
				addCallParam(event.getY());
				addCallParam(event.getRatio());
				try {
					Adaptor.infor("Player.onFingerZoomIn");
					execute(onFingerZoomIn);
				} catch (Exception e) {
					Adaptor.exception(e);
					Adaptor.error("error at bytecode line :" + curStack.istPtr);
				}
			}
			break;
		}
		case Adaptor.POINTER_ZOOMOUT: {
			if (onFingerZoomOut != null) {
				prepareCallParams();
				addCallParam(event.getX());
				addCallParam(event.getY());
				addCallParam(event.getRatio());
				try {
					Adaptor.infor("Player.onFingerZoomOut");
					execute(onFingerZoomOut);
				} catch (Exception e) {
					Adaptor.exception(e);
					Adaptor.error("error at bytecode line :" + curStack.istPtr);
				}
			}
			break;
		}

		}

	}

	public void handleFingerEvent(MFingerEvent finger) {

		if (windows.size() > 0) {
			((Scene) getTopMostScene()).handleFingerEvent(finger);
			return;
		}

		if (finger.getType() == -1) {
			fingerPlayer = screen.matchFinger(finger.getX(), finger.getY(), 0,
					0);
		}

		int result = 0;
		int globalX = finger.getX();
		int globalY = finger.getY();

		if (fingerPlayer != null) {
			// fingerPlayer.calcFingerUsingViewPort(finger);
			switch (finger.getType()) {
			case -1: {
				if (fingerPlayer.getOnFingerDown() != null) {
					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					addCallParam(fingerPlayer);
					try {
						Adaptor.infor("Player.onFingerDown");
						result = execute(fingerPlayer.getOnFingerDown());
					} catch (Exception e) {
						Adaptor.exception(e);
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			case 0: {
				if (fingerPlayer.getOnFingerMove() != null) {
					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					addCallParam(fingerPlayer);
					try {
						Adaptor.infor("Player.onFingerMove");
						result = execute(fingerPlayer.getOnFingerMove());
					} catch (Exception e) {
						Adaptor.exception(e);
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			case 1: {
				if (fingerPlayer.getOnFingerUp() != null) {
					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					addCallParam(fingerPlayer);
					try {
						Adaptor.infor("Player.onFingerUp");
						result = execute(fingerPlayer.getOnFingerUp());
					} catch (Exception e) {
						Adaptor.exception(e);
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			}
		}

		if (result == 0) {
			finger.setX(globalX);
			finger.setY(globalY);

			switch (finger.getType()) {
			case -1: {
				if (onFingerDown != null) {

					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					try {
						Adaptor.infor("Global.onFingerDown");
						execute(onFingerDown);
					} catch (Exception e) {
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			case 0: {
				if (onFingerMove != null) {
					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					try {
						Adaptor.infor("Global.onFingerMove");
						execute(onFingerMove);
					} catch (Exception e) {
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			case 1: {
				if (onFingerUp != null) {
					prepareCallParams();
					addCallParam(finger.getX());
					addCallParam(finger.getY());
					try {
						Adaptor.infor("Global.onFingerUp");
						execute(onFingerUp);
					} catch (Exception e) {
						Adaptor.error("error at bytecode line :"
								+ curStack.istPtr);
					}
				}
				break;
			}
			}
		}

		if (finger.getType() == 1) {
			fingerPlayer = null;
		}

	}

	public void handleKeyEvent(MKeyEvent key) {

		if (isMenuOpen()) {
			windowMenu.onKeyEvent(key);
			return;
		}

		Vector params = VectorPool.produce();
		switch (key.getType()) {
		case Adaptor.KEYSTATE_PRESSED: {
			if (onKeyDown != null) {
				prepareCallParams();
				addCallParam(key.getCode());
				try {
					execute(onKeyDown);
				} catch (Exception e) {
					Adaptor.exception(e);
					Adaptor.error("error at bytecode line :" + curStack.istPtr);
				}
			}
			break;
		}
		// case 0: {
		// if (onKeyPress > -1) {
		// params.addElement(new Integer(key.getCode()));
		// prepareCallback(params);
		// try {
		// execute(onKeyPress);
		// } catch (Exception e) {
		// Adaptor.exception(e);
		// Adaptor.error("error at bytecode line :" + curStack.istPtr);
		// }
		// }
		// break;
		// }
		case Adaptor.KEYSTATE_RELEASED: {
			if (onKeyUp != null) {
				prepareCallParams();
				addCallParam(key.getCode());
				try {
					execute(onKeyUp);
				} catch (Exception e) {
					Adaptor.exception(e);
					Adaptor.error("error at bytecode line :" + curStack.istPtr);
				}
			}
			break;
		}
		}
		VectorPool.recycle(params);
	}

	public void runInit() {
		if (global != null) {
			try {
				execute(0);
			} catch (Exception t) {
				Adaptor.exception(t);
			}
		}
		isInited = true;
	}

	public int execute(MilkCallback callback) throws Exception {
		curStack.libName = callback.lib;
		return execute(callback.isp);

	}

	private ByteCode getByteCode(String libName) {
		return (libName == null ? global : (ByteCode) libs.get(libName));
	}

	public int execute(int index) throws Exception {
		ByteCode byteCode = getByteCode(curStack.libName);
		if (byteCode == null) {
			return 0;
		}
		if (byteCode.instructions == null) {
			return 0;
		}
		boolean reloadByteCode = false;
		if (index >= 0 && index < byteCode.instructions.length) {

			curStack.istPtr = index;
			byte[] ins = null;

			while (byteCode.instructions != null
					&& (ins == null || ins[0] != END)) {
				if (reloadByteCode) {
					reloadByteCode = false;
					byteCode = getByteCode(curStack.libName);
				}
				if (byteCode == null) {
					return 0;
				}
				ins = (byte[]) byteCode.instructions[curStack.istPtr];
				curStack.istPtr++;

				int[] oprands = MInstruction.getOprands(ins);
				int oprandsLength = oprands[oprands.length - 1];

				try {
					if (subExecute(ins)) {
						continue;
					}
					switch (ins[0]) {
					case END: {
						break;
					}
					case ADD:
					case SUB:
					case MUL:
					case DIV:
					case MOD:
					case LSH:
					case RSH:
					case AND:
					case OR:
					case XOR: {
						try {
							doArith(ins[0], oprands);
						} catch (Exception e) {
							Adaptor.debug("ARITH OP ERROR IN OPCODE : "
									+ ins[0]);
							throw e;
						}
						break;
					}
					case CMP: {
						if (oprands[3] >= 2 && oprands[4] != 0) {
							throw new Exception(
									"> >= < <= operations cannot apply to object type.");
						}
						int result = 0;
						switch (oprands[3]) {
						case 0: {
							switch (oprands[4]) {
							case 0: {
								result = getIntVar(oprands[1]) == getIntVar(oprands[2]) ? 1
										: 0;
								break;
							}
							case 1: {
								Object obj1 = getObjVar(oprands[1]);
								if (obj1 == null) {
									result = 0;
								} else {
									result = obj1.equals(getObjVar(oprands[2])) ? 1
											: 0;
								}
								break;
							}
							default: {
								Object obj1 = getObjVar(oprands[1]);
								if (obj1 == null) {
									result = 0;
								} else {
									result = obj1 == getObjVar(oprands[2]) ? 1
											: 0;
								}
								break;
							}
							}

							break;
						}
						case 1: {
							switch (oprands[4]) {
							case 0: {
								result = getIntVar(oprands[1]) == getIntVar(oprands[2]) ? 0
										: 1;
								break;
							}
							case 1: {
								Object obj1 = getObjVar(oprands[1]);
								if (obj1 == null) {
									result = 1;
								} else {
									result = obj1.equals(getObjVar(oprands[2])) ? 0
											: 1;
								}
								break;
							}
							default: {
								Object obj1 = getObjVar(oprands[1]);
								if (obj1 == null) {
									result = 1;
								} else {
									result = obj1 == getObjVar(oprands[2]) ? 0
											: 1;
								}
								break;
							}
							}
							break;
						}
						case 2: {
							result = getIntVar(oprands[1]) > getIntVar(oprands[2]) ? 1
									: 0;
							break;
						}
						case 3: {
							result = getIntVar(oprands[1]) >= getIntVar(oprands[2]) ? 1
									: 0;
							break;
						}
						case 4: {
							result = getIntVar(oprands[1]) < getIntVar(oprands[2]) ? 1
									: 0;
							break;
						}
						case 5: {
							result = getIntVar(oprands[1]) <= getIntVar(oprands[2]) ? 1
									: 0;
							break;
						}

						}
						setVar(oprands[0], result);
						break;
					}
					case NOT: {
						setVar(oprands[0], ~getIntVar(oprands[1]));
						break;
					}
					case MOVINT: {
						setVar(oprands[0], oprands[1]);
						break;
					}
					case MOVSTR: {
						setVar(oprands[0], byteCode.strConsts[oprands[1]]);
						break;
					}
					case CREATEVAR: {
						break;
					}
					case DELETEVAR: {
						break;
					}
					case COPYVAR: {

						int targetMode = oprands[1];
						int sourceMode = oprands[3];
						if (targetMode == 0) {
							if (oprands[0] == 7) {
								int brk = 1;
								int a = brk;
							}
							if (sourceMode == 0) {
								if (getGlobalVarType(oprands[2]) == 0) {
									setGlobalVar(oprands[0],
											getGlobalIntVar(oprands[2]));
								} else {
									setGlobalVar(oprands[0],
											getGlobalObjVar(oprands[2]));
								}
							} else {
								if (getVarType(oprands[2]) == 0) {
									setGlobalVar(oprands[0],
											getIntVar(oprands[2]));
								} else {
									setGlobalVar(oprands[0],
											getObjVar(oprands[2]));
								}
							}
						} else {
							if (sourceMode == 0) {
								if (getGlobalVarType(oprands[2]) == 0) {
									setVar(oprands[0],
											getGlobalIntVar(oprands[2]));
								} else {
									setVar(oprands[0],
											getGlobalObjVar(oprands[2]));
								}
							} else {
								if (getVarType(oprands[2]) == 0) {
									setVar(oprands[0], getIntVar(oprands[2]));
								} else {
									setVar(oprands[0], getObjVar(oprands[2]));
								}
							}
						}
						break;
					}
					case JE:
					case JNE:
					case JL:
					case JLE:
					case JGE:
					case JG:
					case JMP: {
						boolean condition = getJumpCondition(ins[0], oprands);
						if (condition) {
							curStack.istPtr = oprands[0];
						}
						break;
					}
					case CALL: {
						clearCallParams();
						int count = oprandsLength;

						for (int i = 2; i < count; i++) {

							if (getVarType(oprands[i]) == 0) {
								addCallParam(getIntVar(oprands[i]));
							} else {
								addCallParam(getObjVar(oprands[i]));
							}
						}

						int nextPtr = getIntVar(oprands[0]);
						String libName = getLibName(oprands[1], byteCode);
						ByteCode temp = getByteCode(libName);
						if (temp == null) {
							throw new Exception("lib " + libName
									+ " not loaded!");
						}

						String funcName = (String) temp.functions.get(String
								.valueOf(nextPtr));

						if (libName != null) {

							funcName = libName + ":" + funcName;
						}
						pushStack(funcName);
						curStack.istPtr = nextPtr;
						curStack.stackStart = varSize;
						curStack.libName = libName;

						reloadByteCode = true;

						break;
					}
					case RET: {
						reloadByteCode = true;
						boolean isInt = getVarType(0) == 0;
						Object rObj = null;
						int rInt = -1;
						try {
							if (isInt) {
								rInt = getIntVar(0);
							} else {
								rObj = getObjVar(0);
							}
						} catch (Exception t) {
							Adaptor.debug("error in executing RET : " + t);
						}
						if (stacks.size() > 0) {

							setVarSize(curStack.stackStart);
							popStack();
							if (isInt) {
								setVar(0, rInt);
							} else {
								setVar(0, rObj);
							}
						} else {
							setVarSize(curStack.stackStart);
							curStack.stackStart = 0;
							if (isInt) {
								setVar(0, rInt);
							} else {
								setVar(0, rObj);
							}

						}
						break;
					}
					case TOSTR: {
						int type = oprands[2];
						switch (type) {
						case OINT: {
							setVar(oprands[0],
									String.valueOf(getIntVar(oprands[1])));
							break;
						}
						case OARRAY: {
							MArray temp = getArrayVar(oprands[1]);
							if (temp != null) {
								setVar(oprands[0], temp.toString());
							} else {
								setVar(oprands[0], "null");
							}
							break;
						}
						case OMAP: {
							MMap temp = getMapVar(oprands[1]);
							if (temp != null) {
								setVar(oprands[0], temp.toString());
							} else {
								setVar(oprands[0], "null");
							}
							break;
						}
						}
						break;
					}
					case TOINT: {
						int r = 0;
						try {
							r = Integer.parseInt(getStringVar(oprands[1]));
						} catch (Exception t) {
						}
						setVar(oprands[0], r);
						break;
					}
					case STRLEN: {
						setVar(oprands[0], getStringVar(oprands[1]).length());
						break;
					}
					case SUBSTR: {
						setVar(oprands[0],
								getStringVar(oprands[1]).substring(
										getIntVar(oprands[2]),
										getIntVar(oprands[3])));
						break;
					}
					case STRCAT: {
						setVar(oprands[0], getStringVar(oprands[1])
								+ getStringVar(oprands[2]));
						break;
					}
					case STRREPLACE: {
						String org = getStringVar(oprands[1]);
						String pattern = getStringVar(oprands[2]);
						String replace = getStringVar(oprands[3]);
						String result = org;
						int pi = 0;
						while ((pi = result.indexOf(pattern, pi)) != -1) {
							result = (pi > 0 ? result.substring(0, pi) : "")
									+ replace
									+ (pi + pattern.length() < result.length() ? result
											.substring(pi + pattern.length())
											: "");
							pi += replace.length();
						}
						setVar(oprands[0], result);
						break;
					}
					case STRMATCH: {

						break;
					}
					case STRPOS: {
						String org = getStringVar(oprands[1]);
						String pattern = getStringVar(oprands[2]);
						int startIndex = getIntVar(oprands[3]);
						boolean ignoreCase = (getIntVar(oprands[4]) != 0);
						if (ignoreCase) {
							org = org.toLowerCase();
							pattern = pattern.toLowerCase();
						}
						setVar(oprands[0], org.indexOf(pattern, startIndex));
						break;
					}
					case STRCASE: {
						int mode = oprands[3];
						switch (mode) {
						case 1: {
							setVar(oprands[0], getStringVar(oprands[1])
									.toUpperCase());
							break;
						}
						case 2: {
							setVar(oprands[0], getStringVar(oprands[1])
									.toLowerCase());
							break;
						}
						}
						break;
					}
					case LBLTOINT: {
						setVar(oprands[0], oprands[1]);
						break;
					}
					case URLENCODE: {
						break;
					}
					case MSIZE: {
						setVar(oprands[0], getMapVar(oprands[1]).size());
						break;
					}
					case MHAS: {
						setVar(oprands[0],
								getMapVar(oprands[1]).hasKey(
										getStringVar(oprands[2])) ? 1 : 0);
						break;
					}
					case MGET: {
						MMap map = getMapVar(oprands[1]);
						String key = null;
						if (oprands[3] != -1) {
							key = getStringVar(oprands[2]);
						}

						// System.out
						// .println("getting key from map [" + key + "]");
						switch (oprands[3]) {
						case OINT: {
							setVar(oprands[0], map.getInt(key));
							break;
						}
						case OSTRING: {
							setVar(oprands[0], map.getString(key));
							break;
						}
						case OARRAY: {
							setVar(oprands[0], map.getArray(key));
							break;
						}
						case OMAP: {
							setVar(oprands[0], map.getMap(key));
							break;
						}
						case ORECT: {
							setVar(oprands[0], map.getRect(key));
							break;
						}
						case OPLAYER: {
							setVar(oprands[0], map.getPlayer(key));
							break;
						}
						case OTEXT: {
							setVar(oprands[0], map.getText(key));
							break;
						}
						case OGROUP: {
							setVar(oprands[0], map.getGroup(key));
							break;
						}
						case OTILES: {
							setVar(oprands[0], map.getTiles(key));
							break;
						}
						case OELEMENT: {
							setVar(oprands[0], map.getElement(key));
							break;
						}
						case -1: {
							setVar(oprands[0], map.getMapKeys());
							break;
						}
						}
						break;
					}
					case MSET: {
						MMap map = getMapVar(oprands[0]);
						String key = getStringVar(oprands[1]);
						switch (oprands[3]) {
						case OINT: {
							map.set(key, new Integer(getIntVar(oprands[2])));
							break;
						}
						case OSTRING: {
							map.set(key, getStringVar(oprands[2]));
							break;
						}
						case OARRAY: {
							map.set(key, getArrayVar(oprands[2]));
							break;
						}
						case OMAP: {
							map.set(key, getMapVar(oprands[2]));
							break;
						}
						case ORECT: {
							map.set(key, getRectVar(oprands[2]));
							break;
						}
						case OPLAYER: {
							map.set(key, getPlayerVar(oprands[2]));
							break;
						}
						case OTEXT: {
							map.set(key, getTextVar(oprands[2]));
							break;
						}
						case OGROUP: {
							map.set(key, getGroupVar(oprands[2]));
							break;
						}
						case OTILES: {
							map.set(key, getTilesVar(oprands[2]));
							break;
						}
						}
						break;
					}
					case MDEL: {
						getMapVar(oprands[0]).remove(getStringVar(oprands[1]));
						break;
					}
					case MGETTYPE: {
						setVar(oprands[0],
								getMapVar(oprands[1]).getType(
										getStringVar(oprands[2])));
						break;
					}
					case MFROMSTR: {

						MMap map = (MMap) Adaptor.parse(new StringData(
								getStringVar(oprands[1])), null);
						setVar(oprands[0], map);
						break;
					}
					case ASIZE: {
						setVar(oprands[0], getArrayVar(oprands[1]).size());
						break;
					}
					case AHAS: {
						int varType = getVarType(oprands[2]);
						if (varType != 0) {
							setVar(oprands[0], getArrayVar(oprands[1])
									.hasValue(getObjVar(oprands[2])) ? 1 : 0);
						} else {
							setVar(oprands[0], getArrayVar(oprands[1])
									.hasValue(getIntVar(oprands[2])) ? 1 : 0);
						}
						break;
					}
					case AGET: {

						MArray array = getArrayVar(oprands[1]);
						int arrayIndex = getIntVar(oprands[2]);
						switch (oprands[3]) {
						case OINT: {
							setVar(oprands[0], array.getInt(arrayIndex));
							break;
						}
						case OSTRING: {
							setVar(oprands[0], array.getString(arrayIndex));
							break;
						}
						case OARRAY: {
							setVar(oprands[0], array.getArray(arrayIndex));
							break;
						}
						case OMAP: {
							setVar(oprands[0], array.getMap(arrayIndex));
							break;
						}
						case ORECT: {
							setVar(oprands[0], array.getRect(arrayIndex));
							break;
						}
						case OPLAYER: {
							setVar(oprands[0], array.getPlayer(arrayIndex));
							break;
						}
						case OTEXT: {
							setVar(oprands[0], array.getText(arrayIndex));
							break;
						}
						case OGROUP: {
							setVar(oprands[0], array.getGroup(arrayIndex));
							break;
						}
						case OTILES: {
							setVar(oprands[0], array.getTiles(arrayIndex));
							break;
						}
						case OELEMENT: {
							setVar(oprands[0], array.getElement(arrayIndex));
							break;
						}
						}
						break;
					}
					case ASET: {

						MArray array = getArrayVar(oprands[0]);
						int arrayIndex = getIntVar(oprands[1]);
						if (oprands[3] == 0) {
							array.set(arrayIndex, new Integer(
									getIntVar(oprands[2])));
						} else {
							array.set(arrayIndex, getObjVar(oprands[2]));
						}
						break;
					}
					case ADEL: {
						getArrayVar(oprands[0]).remove(getIntVar(oprands[1]));
						break;
					}
					case AAPPEND: {
						MArray array = getArrayVar(oprands[0]);
						switch (oprands[2]) {
						case OINT: {
							array.append(new Integer(getIntVar(oprands[1])));
							break;
						}
						case OSTRING: {
							array.append(getStringVar(oprands[1]));
							break;
						}
						case OARRAY: {
							array.append(getArrayVar(oprands[1]));
							break;
						}
						case OMAP: {
							array.append(getMapVar(oprands[1]));
							break;
						}
						case ORECT: {
							array.append(getRectVar(oprands[1]));
							break;
						}
						case OPLAYER: {
							array.append(getPlayerVar(oprands[1]));
							break;
						}
						case OTEXT: {
							array.append(getTextVar(oprands[1]));
							break;
						}
						case OGROUP: {
							array.append(getGroupVar(oprands[1]));
							break;
						}
						}
						break;
					}
					case AINSERT: {
						MArray array = getArrayVar(oprands[0]);
						int arrayIndex = getIntVar(oprands[1]);
						switch (oprands[3]) {
						case OINT: {
							array.insert(arrayIndex, new Integer(
									getIntVar(oprands[2])));
							break;
						}
						case OSTRING: {
							array.insert(arrayIndex, getStringVar(oprands[2]));
							break;
						}
						case OARRAY: {
							array.insert(arrayIndex, getArrayVar(oprands[2]));
							break;
						}
						case OMAP: {
							array.insert(arrayIndex, getMapVar(oprands[2]));
							break;
						}
						case ORECT: {
							array.insert(arrayIndex, getRectVar(oprands[2]));
							break;
						}
						case OPLAYER: {
							array.insert(arrayIndex, getPlayerVar(oprands[2]));
							break;
						}
						case OTEXT: {
							array.insert(arrayIndex, getTextVar(oprands[2]));
							break;
						}
						case OGROUP: {
							array.insert(arrayIndex, getGroupVar(oprands[2]));
							break;
						}
						}
						break;
					}
					case AGETTYPE: {
						setVar(oprands[0],
								getArrayVar(oprands[1]).getType(
										getIntVar(oprands[2])));
						break;
					}
					case AFROMSTR: {

						MArray array = (MArray) Adaptor.parse(new StringData(
								getStringVar(oprands[1])), null);

						setVar(oprands[0], array);
						break;
					}
					case REGCALLBACK: {
						String libName = getLibName(oprands[2], byteCode);
						switch (oprands[0]) {
						case 1: {
							onKeyDown = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 2: {
							onKeyPress = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 3: {
							onKeyUp = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 4: {
							onFingerDown = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 5: {
							onFingerMove = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 6: {
							onFingerUp = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 7: {
							onResourceLoaded = genCallback(
									getIntVar(oprands[1]), libName);
							break;
						}
						case 8: {
							onData = genCallback(getIntVar(oprands[1]), libName);
							break;
						}
						case 9: {
							onFocus = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 10: {
							onLostFocus = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 11: {
							onCommand = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 12: {
							onLeftKey = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 13: {
							onRightKey = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 14: {
							onSms = genCallback(getIntVar(oprands[1]), libName);
							break;
						}
						case 15: {
							onFingerZoomIn = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						case 16: {
							onFingerZoomOut = genCallback(
									getIntVar(oprands[1]), libName);
							break;
						}
						case 17: {
							onFrame = genCallback(getIntVar(oprands[1]),
									libName);
							break;
						}
						}
						break;
					}
					case EMPTY: {
						break;
					}
					case RAND: {
						MyRandom number = new MyRandom();
						int start = getIntVar(oprands[1]);
						int end = getIntVar(oprands[2]);
						int thing = number.nextInt(end - start) + start;
						setVar(oprands[0], thing);
						break;
					}
					case SETTIMEOUT: {
						int callbackIsp = getIntVar(oprands[1]);
						String lib = getLibName(oprands[2], byteCode);
						MilkCallback callback = new MilkCallback(callbackIsp,
								lib);
						int delay = getIntVar(oprands[3]);
						TimeOut to = new TimeOut(delay, callback);
						setVar(oprands[0], to.id);
						timeOuts.put(new Integer(to.id), to);
						break;
					}
					case CANCELTIMEOUT: {
						int id = getIntVar(oprands[0]);
						Integer key = new Integer(id);
						TimeOut to = (TimeOut) timeOuts.get(key);
						if (to != null) {
							timeOuts.remove(key);
						}

						break;
					}
					case PLAYSOUND: {
						setVar(oprands[0],
								Adaptor.getInstance().playSound(
										getStringVar(oprands[1]),
										getIntVar(oprands[2])));
						break;
					}
					case STOPSOUND: {
						Adaptor.getInstance().stopSound(getIntVar(oprands[0]));
						break;
					}
					case LOADSOUND: {
						Adaptor.getInstance().loadSound(
								getStringVar(oprands[0]));
						break;
					}
					case UNLOADSOUND: {
						Adaptor.getInstance().unloadSound(
								getStringVar(oprands[0]));
						break;
					}
					case GETENV: {
						switch (oprands[1]) {
						case 0: {
							setVar(oprands[0], Adaptor.milk.getCanvasWidth());
							break;
						}
						case 1: {
							setVar(oprands[0], Adaptor.milk.getCanvasHeight());
							break;
						}
						case 2: {
							setVar(oprands[0], Adaptor.milk.getPlatformCode());
							break;
						}
						case 3: {
							int isTouch = Adaptor.milk.isTouchDevice() ? 1 : 0;
							setVar(oprands[0], isTouch);
							break;
						}
						case 4: {
							setVar(oprands[0], Adaptor.getInstance()
									.getMonetId());
							break;
						}
						case 5: {
							setVar(oprands[0], this.params);
							break;
						}
						case 6: {
							if (oprandsLength == 3) {
								setVar(oprands[0],
										Adaptor.getInstance().getTranslation(
												getStringVar(oprands[2]), null));
							} else if (oprandsLength == 4) {
								setVar(oprands[0],
										Adaptor.getInstance().getTranslation(
												getStringVar(oprands[2]),
												getArrayVar(oprands[3])));
							}
							break;
						}
						case 7: {
							setVar(oprands[0], Adaptor.getInstance()
									.getLoadingProgress());
							break;
						}
						case 8: {// username
							setVar(oprands[0], Adaptor.getInstance().username);
							break;
						}
						case 9: {// password
							setVar(oprands[0], Adaptor.getInstance().password);
							break;
						}
						case 10: {// get language
							setVar(oprands[0], Adaptor.getInstance().language);
							break;
						}
						case 11: { // get version
							setVar(oprands[0], Adaptor.getInstance()
									.getVersion());
							break;
						}
						case 12: {// get requiredVersion
							setVar(oprands[0], Adaptor.getInstance()
									.getRequiredVersion());
							break;
						}
						case 13: {// get auto reg params
							setVar(oprands[0], Adaptor.getInstance()
									.getAutoRegParams());
							break;
						}
						case 14: {
							// get auto reg params 2
							String salt1 = getStringVar(oprands[2]);
							String salt2 = getStringVar(oprands[3]);
							setVar(oprands[0], Adaptor.getInstance()
									.getAutoRegParams2(salt1, salt2));
							break;
						}
						}
						break;
					}
					case PLAYSCENE: {
						// Core.getInstance().curSceneId =
						// getStringVar(oprands[0]);
						// Core.getInstance().sceneSwitch = true;
						int mode = oprands[0];
						switch (mode) {
						case 0: {
							String resId = getStringVar(oprands[1]);
							MMap params = getMapVar(oprands[3]);
							byte transType = (byte) getIntVar(oprands[2]);
							Core.getInstance().aboutToSwitchScene(resId,
									params, transType);
							break;
						}
						case 1: {
							break;
						}

						}

						break;
					}
					case GETTIMESTAMP: {
						switch (oprands[1]) {
						case 0: {
							setVar(oprands[0],
									(int) (System.currentTimeMillis() / 1000));
							break;
						}
						case 1: {
							setVar(oprands[0],
									(int) (System.currentTimeMillis() - Adaptor
											.getInstance().gameStartTime));
							break;
						}
						}
						break;
					}
					case DEBUG: {
						Adaptor.debug(getStringVar(oprands[0]));
						break;
					}
					case GETRECTDATA: {
						switch (oprands[2]) {
						case 0: {
							setVar(oprands[0], getRectVar(oprands[1]).getX());
							break;
						}
						case 1: {
							setVar(oprands[0], getRectVar(oprands[1]).getY());
							break;
						}
						case 2: {
							setVar(oprands[0], getRectVar(oprands[1])
									.getWidth());
							break;
						}
						case 3: {
							setVar(oprands[0], getRectVar(oprands[1])
									.getHeight());
							break;
						}
						case 6: {
							// contains point
							int x = getIntVar(oprands[3]);
							int y = getIntVar(oprands[4]);
							MRect rect = getRectVar(oprands[1]);
							if (rect.contains(x, y)) {
								setVar(oprands[0], 1);
							} else {
								setVar(oprands[0], 0);
							}
							break;
						}
						case 7: {
							// contains rect
							MRect rect = getRectVar(oprands[1]);
							boolean result = false;
							if (oprandsLength == 4) {
								MRect thing = getRectVar(oprands[3]);
								result = rect.contains(thing.x, thing.y,
										thing.width, thing.height);
							} else if (oprandsLength == 7) {
								int x = getIntVar(oprands[3]);
								int y = getIntVar(oprands[4]);
								int w = getIntVar(oprands[5]);
								int h = getIntVar(oprands[6]);
								result = rect.contains(x, y, w, h);
							}
							if (result) {
								setVar(oprands[0], 1);
							} else {
								setVar(oprands[0], 0);
							}
							break;
						}
						case 8: {
							// intersacts
							MRect rect = getRectVar(oprands[1]);
							boolean result = false;
							if (oprandsLength == 4) {
								MRect thing = getRectVar(oprands[3]);
								result = rect.intersacts(thing.x, thing.y,
										thing.width, thing.height);
							} else if (oprandsLength == 7) {
								int x = getIntVar(oprands[3]);
								int y = getIntVar(oprands[4]);
								int w = getIntVar(oprands[5]);
								int h = getIntVar(oprands[6]);
								result = rect.intersacts(x, y, w, h);
							}
							if (result) {
								setVar(oprands[0], 1);
							} else {
								setVar(oprands[0], 0);
							}
							break;
						}
						}
						break;
					}
					case SETRECTDATA: {
						MRect rect = getRectVar(oprands[1]);
						switch (oprands[2]) {
						case 0: {
							rect.setX(getIntVar(oprands[0]));
							break;
						}
						case 1: {
							rect.setY(getIntVar(oprands[0]));
							break;
						}
						case 2: {
							rect.setWidth(getIntVar(oprands[0]));
							break;
						}
						case 3: {
							rect.setHeight(getIntVar(oprands[0]));
							break;
						}
						case 4: {
							// MOVEPOS
							int x = getIntVar(oprands[0]);
							int y = getIntVar(oprands[3]);
							rect.move(x, y);
							break;
						}
						case 5: {
							// resizeBounds
							int wr = getIntVar(oprands[0]);
							int hr = getIntVar(oprands[3]);
							rect.resize(wr, hr);
							break;
						}
						}
						break;
					}
					case GETPLAYERDATA: {

						switch (oprands[2]) {
						case 0: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getzIndex());
							break;
						}
						case 1: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.isVisible() ? 1 : 0);
							break;
						}
						case 2: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getState());
							break;
						}
						case 3: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.isFocusable() ? 1 : 0);
							break;
						}

						case 6: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getPosition());
							break;
						}

						case 8: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getData());
							break;
						}
						case 10: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getBgColor());
							break;
						}
						case 16: {
							setVar(oprands[0], getPlayerVar(oprands[1]).getX());
							break;
						}
						case 17: {
							setVar(oprands[0], getPlayerVar(oprands[1]).getY());
							break;
						}
						case 18: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getWidth());
							break;
						}
						case 19: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getHeight());
							break;
						}
						case 20: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getClip());
							break;
						}
						case 21: {
							setVar(oprands[0], getPlayerVar(oprands[1])
									.getBorderColor());
							break;
						}

						}
						break;
					}
					case SETPLAYERDATA: {
						MPlayer player = getPlayerVar(oprands[1]);

						switch (oprands[2]) {
						case 0: {
							player.setzIndex(getIntVar(oprands[0]));
							break;
						}
						case 1: {
							player.setVisible(getIntVar(oprands[0]) == 1);
							break;
						}
						case 2: {
							player.setState(getIntVar(oprands[0]));
							break;
						}
						case 3: {
							player.setFocusable(getIntVar(oprands[0]) == 1);
							break;
						}

						case 6: {
							player.setPosition(getRectVar(oprands[0]));
							break;
						}

						case 8: {
							player.setData(getStringVar(oprands[0]));
							break;
						}
						case 9: {
							getArrayVar(oprands[0]);
							// TODO: support set states for player
							break;
						}
						case 10: {
							player.setBgColor(getIntVar(oprands[0]));
							break;
						}
						case 16: {
							player.setX(getIntVar(oprands[0]));
							break;
						}
						case 17: {
							player.setY(getIntVar(oprands[0]));
							break;
						}
						case 18: {
							player.setWidth(getIntVar(oprands[0]));
							break;
						}
						case 19: {
							player.setHeight(getIntVar(oprands[0]));
							break;
						}
						case 20: {
							if (oprands[0] >= 0) {
								player.setClip(getRectVar(oprands[0]));
							} else {
								player.cancelClip();
							}
							break;
						}
						case 21: {
							player.setBorderColor(getIntVar(oprands[0]));
							break;
						}
						}
						break;
					}
					case ATTACHPLAYER:
					case DETACHPLAYER: {
						break;
					}
					case DEFINESTATE: {
						MPlayer player = getPlayerVar(oprands[0]);
						int stateId = getIntVar(oprands[1]);
						MState state = new MState();
						state.id = stateId;
						Object var = getObjVar(oprands[2]);
						if (var instanceof MArray) {
							MArray array = (MArray) var;
							Vector frames = VectorPool.produce();
							int count = array.size();
							state.frameDelay = array.getInt(0);
							state.finishCallback = genCallback(array.getInt(1),
									null);
							if (array.getType(count - 1) == OARRAY) {
								state.spriteId = array.getString(2);
								MArray temp = array.getArray(count - 1);
								int count2 = temp.size();
								for (int i = 0; i < count2; i++) {
									frames.addElement(new Integer(temp
											.getInt(i)));
								}
								state.type = MState.stateTypeSprite;
							} else if (array.getType(count - 1) == OSTRING) {
								for (int i = 2; i < count; i++) {
									frames.addElement(array.getString(i));
								}
								state.type = MState.stateTypeDynamic;
							}

							state.setFrames(frames);
						} else if (var instanceof String) {
							String frame = (String) var;
							state.type = MState.stateTypeStatic;
							state.staticFrame = frame;
						}
						player.defineState(stateId, state);
						break;
					}
					case SENDDATA: {

						String key = getStringVar(oprands[0]);
						Object value = getObjVar(oprands[1]);
						String serverKey = null;
						if (oprandsLength == 3) {
							serverKey = getStringVar(oprands[2]);
						}
						if (value instanceof String) {
							Adaptor.getInstance().sendData(key, (String) value,
									serverKey, this.hashCode(), resId);
						} else if (value instanceof MMap) {
							Adaptor.getInstance()
									.sendCommand(key, (MMap) value);
						}

						break;
					}
					case GETTEXTDATA: {
						MText text = getTextVar(oprands[1]);
						int type = oprands[2];
						switch (type) {
						case 0: {
							setVar(oprands[0], text.getX());
							break;
						}
						case 1: {
							setVar(oprands[0], text.getY());
							break;
						}
						case 2: {
							setVar(oprands[0], text.getUserWidth());
							break;
						}
						case 3: {
							setVar(oprands[0], text.getUserHeight());
							break;
						}
						case 4: {
							setVar(oprands[0], text.getzIndex());
							break;
						}
						case 5: {
							setVar(oprands[0], text.isVisible() ? 1 : 0);
							break;
						}
						case 6: {
							setVar(oprands[0], text.getFontSize());
							break;
						}
						case 7: {
							setVar(oprands[0], text.getFontModifier());
							break;
						}
						case 8: {
							setVar(oprands[0], text.getBgColor());
							break;
						}
						case 9: {
							setVar(oprands[0], text.getBorderColor());
							break;
						}
						case 10: {
							setVar(oprands[0], text.getTextColor());
							break;
						}
						case 11: {
							setVar(oprands[0], text.getMaxLines());
							break;
						}
						case 12: {
							setVar(oprands[0], text.getAlign());
							break;
						}
						case 13: {
							setVar(oprands[0], text.getvAlign());
							break;
						}
						case 14: {
							setVar(oprands[0], text.isBgTransparent() ? 1 : 0);
							break;
						}
						case 15: {
							setVar(oprands[0], text.getText());
							break;
						}
						case 21: {
							setVar(oprands[0], text.getLayoutWidth());
							break;
						}
						case 22: {
							setVar(oprands[0], text.getLayoutHeight());
							break;
						}
						}
						break;
					}
					case SETTEXTDATA: {
						MText text = getTextVar(oprands[1]);
						int type = oprands[2];
						switch (type) {
						case 0: {
							text.setX(getIntVar(oprands[0]));
							break;
						}
						case 1: {
							text.setY(getIntVar(oprands[0]));
							break;
						}
						case 2: {
							text.setUserWidth(getIntVar(oprands[0]));
							break;
						}
						case 3: {
							text.setUserHeight(getIntVar(oprands[0]));
							break;
						}
						case 4: {
							text.setzIndex(getIntVar(oprands[0]));
							break;
						}
						case 5: {
							text.setVisible(getIntVar(oprands[0]) != 0);
							break;
						}
						case 6: {
							text.setFontSize(getIntVar(oprands[0]));
							break;
						}
						case 7: {
							text.setFontModifier(getIntVar(oprands[0]));
							break;
						}
						case 8: {
							text.setBgColor(getIntVar(oprands[0]));
							break;
						}
						case 9: {
							text.setBorderColor(getIntVar(oprands[0]));
							break;
						}
						case 10: {
							text.setTextColor(getIntVar(oprands[0]));
							break;
						}
						case 11: {
							text.setMaxLines(getIntVar(oprands[0]));
							break;
						}
						case 12: {
							text.setAlign(getIntVar(oprands[0]));
							break;
						}
						case 13: {
							text.setvAlign(getIntVar(oprands[0]));
							break;
						}
						case 14: {
							text.setBgTransparent(getIntVar(oprands[0]) != 0);
							break;
						}
						case 15: {
							text.setText(getStringVar(oprands[0]));
							break;
						}
						}
						break;
					}
					case ATTACHTEXT:
					case DETACHTEXT: {
						break;
					}
					case LAYOUTTEXT: {
						getTextVar(oprands[0]).layout();
						break;
					}
					case SAVEDB: {

						Adaptor.getInstance().saveDb(getStringVar(oprands[0]),
								getStringVar(oprands[1]), oprands[2] == 1);
						break;
					}
					case LOADDB: {
						setVar(oprands[0],
								Adaptor.getInstance().loadDb(
										getStringVar(oprands[1]),
										oprands[2] == 1));
						break;
					}
					case MOVE: {
						MDraw draw = (MDraw) this.getObjVar(oprands[0]);
						int type = oprands[1];
						int time = getIntVar(oprands[2]);
						int moveX = getIntVar(oprands[3]);
						int moveY = getIntVar(oprands[4]);
						MoveState moveState = new MoveState();
						moveState.startX = draw.getX();
						moveState.startY = draw.getY();
						moveState.destX = moveX;
						moveState.destY = moveY;
						moveState.time = time;
						moveState.startTime = System.currentTimeMillis();
						moveState.mode = type;
						if (type == 0 || type == 4) {
							int callback = getIntVar(oprands[5]);
							String libName = getLibName(oprands[6], byteCode);
							moveState.callback = genCallback(callback, libName);
							draw.setMoveState(moveState);
						} else if (type == 1) {
							draw.setMoveState(moveState);
						}
						break;
					}
					case STOP: {
						MDraw draw = (MDraw) this.getObjVar(oprands[0]);
						draw.setMoveState(null);
						break;
					}
					case REGOBJECTCALLBACK: {
						MDraw draw = (MDraw) this.getObjVar(oprands[0]);
						String libName = getLibName(oprands[3], byteCode);
						switch (oprands[1]) {
						case 3: {
							draw.setOnFingerDown(genCallback(
									getIntVar(oprands[2]), libName));
							break;
						}
						case 4: {
							draw.setOnFingerMove(genCallback(
									getIntVar(oprands[2]), libName));
							break;
						}
						case 5: {
							draw.setOnFingerUp(genCallback(
									getIntVar(oprands[2]), libName));
							break;
						}
						}
						break;
					}
					case NAVIGATE: {
						// if (oprands[0] == 0) {
						// MPlayer player = getPlayerVar(oprands[1]);
						// if (player != null && player.isFocusable()) {
						// if (focusPlayer != null && onLostFocus != null) {
						//
						// Vector input = VectorPool.produce();
						// input.addElement(focusPlayer);
						// tempCallbacks.addElement(input);
						// tempCallbacks.addElement(onLostFocus);
						// // Core.getInstance().wakeUp();
						//
						// }
						// focusPlayer = player;
						// if (onFocus != null) {
						//
						// Vector input = VectorPool.produce();
						// input.addElement(player);
						// tempCallbacks.addElement(input);
						// tempCallbacks.addElement(onFocus);
						// // Core.getInstance().wakeUp();
						// }
						//
						// }
						// } else if (oprands[0] == 1) {
						// MPlayer found = null;
						// boolean foundNew = false;
						// int size = focusablePlayers.size();
						// if (size > 0) {
						// int focusX = 0;
						// int focusY = 0;
						// if (focusPlayer != null) {
						// focusX = focusPlayer.getCenterX();
						// focusY = focusPlayer.getCenterY();
						// }
						// switch (getIntVar(oprands[1])) {
						// case -1: {
						// // up
						// for (int i = 0; i < size; i++) {
						// MPlayer thing = (MPlayer) focusablePlayers
						// .elementAt(i);
						// int diff = thing.getCenterY() - focusY;
						// if (diff < 0 && thing.onScreen) {
						// if (found == null) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterY() > found
						// .getCenterY()) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterY() == found
						// .getCenterY()) {
						// if (Math.abs(thing.getCenterX()
						// - focusX) < Math
						// .abs(found.getCenterX()
						// - focusX)) {
						// found = thing;
						// foundNew = true;
						// }
						// }
						// }
						// }
						// break;
						// }
						// case -2: {
						// // down
						// for (int i = 0; i < size; i++) {
						// MPlayer thing = (MPlayer) focusablePlayers
						// .elementAt(i);
						// int diff = thing.getCenterY() - focusY;
						// if (diff > 0 && thing.onScreen) {
						// if (found == null) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterY() < found
						// .getCenterY()) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterY() == found
						// .getCenterY()) {
						// if (Math.abs(thing.getCenterX()
						// - focusX) < Math
						// .abs(found.getCenterX()
						// - focusX)) {
						// found = thing;
						// foundNew = true;
						// }
						// }
						// }
						// }
						// break;
						// }
						// case -3: {
						// // left
						// for (int i = 0; i < size; i++) {
						// MPlayer thing = (MPlayer) focusablePlayers
						// .elementAt(i);
						// int diff = thing.getCenterX() - focusX;
						// if (diff < 0 && thing.onScreen) {
						// if (found == null) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterX() > found
						// .getCenterX()) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterX() == found
						// .getCenterX()) {
						// if (Math.abs(thing.getCenterY()
						// - focusY) < Math
						// .abs(found.getCenterY()
						// - focusY)) {
						// found = thing;
						// foundNew = true;
						// }
						// }
						// }
						// }
						// break;
						// }
						// case -4: {
						// // right
						// for (int i = 0; i < size; i++) {
						// MPlayer thing = (MPlayer) focusablePlayers
						// .elementAt(i);
						// int diff = thing.getCenterX() - focusX;
						// if (diff > 0 && thing.onScreen) {
						// if (found == null) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterX() < found
						// .getCenterX()) {
						// found = thing;
						// foundNew = true;
						// } else if (thing.getCenterX() == found
						// .getCenterX()) {
						// if (Math.abs(thing.getCenterY()
						// - focusY) < Math
						// .abs(found.getCenterY()
						// - focusY)) {
						// found = thing;
						// foundNew = true;
						// }
						// }
						// }
						// }
						// break;
						// }
						// }
						// if (foundNew) {
						// if (focusPlayer != null
						// && onLostFocus != null) {
						// Vector input = VectorPool.produce();
						// input.addElement(focusPlayer);
						// tempCallbacks.addElement(input);
						// tempCallbacks.addElement(onLostFocus);
						// // Core.getInstance().wakeUp();
						// }
						// focusPlayer = found;
						// if (onFocus != null) {
						//
						// Vector input = VectorPool.produce();
						// input.addElement(found);
						// tempCallbacks.addElement(input);
						// tempCallbacks.addElement(onFocus);
						// // Core.getInstance().wakeUp();
						// }
						//
						// }
						// }
						// } else if (oprands[0] == 2) {
						// setVar(oprands[1], focusPlayer);
						// }
						break;
					}

					case ISNULL: {
						if (oprandsLength <= 2 || oprands[2] == 0) {
							if (getVarType(oprands[1]) == 0) {
								setVar(oprands[0], 1);
							} else {
								setVar(oprands[0],
										getObjVar(oprands[1]) == null ? 1 : 0);
							}
						} else {
							if (getVarType(oprands[1]) == 0) {
								setVar(oprands[0], 0);
							} else {
								setVar(oprands[0],
										getObjVar(oprands[1]) == null ? 0 : 1);
							}
						}
						break;
					}
					case INITOBJ: {
						switch (oprands[0]) {
						case 0: {
							setVar(oprands[1], new MRect(getIntVar(oprands[2]),
									getIntVar(oprands[3]),
									getIntVar(oprands[4]),
									getIntVar(oprands[5])));
							break;
						}
						case 1: {
							setVar(oprands[1],
									new MRect(getRectVar(oprands[2])));
							break;
						}
						case 2: {
							setVar(oprands[1], Adaptor.uiFactory.createMPlayer(
									getIntVar(oprands[2]),
									getIntVar(oprands[3]),
									getIntVar(oprands[4]),
									getIntVar(oprands[5])));
							break;
						}
						case 3: {
							setVar(oprands[1],
									Adaptor.uiFactory
											.createMPlayer(getPlayerVar(oprands[2])));
							break;
						}
						case 4: {
							setVar(oprands[1],
									Adaptor.uiFactory
											.createMPlayer(getRectVar(oprands[2])));
							break;
						}
						case 5: {
							setVar(oprands[1], new MText(
									getStringVar(oprands[2]),
									getIntVar(oprands[3]),
									getIntVar(oprands[4])));
							break;
						}
						case 6: {
							setVar(oprands[1],
									new MText(getTextVar(oprands[2])));
							break;
						}
						case 7: {
							setVar(oprands[1], new MGroup(
									getGroupVar(oprands[2])));
							break;
						}
						case 8: {
							setVar(oprands[1], new MGroup(
									getIntVar(oprands[2]),
									getIntVar(oprands[3])));
							break;
						}
						case 9: {
							if (oprands[3] == -1) {
								setVar(oprands[1],
										Adaptor.uiFactory
												.createMTiles(getStringVar(oprands[2])));
							} else {
								setVar(oprands[1],
										Adaptor.uiFactory.createMTiles(
												getStringVar(oprands[2]),
												getIntVar(oprands[3]),
												getIntVar(oprands[4]),
												getIntVar(oprands[5]),
												getIntVar(oprands[6])));
							}
							break;
						}
						case 10: {
							setVar(oprands[1], new MArray());
							break;
						}
						case 11: {
							setVar(oprands[1], new MMap());
							break;
						}
						case 12: {
							setVar(oprands[1], new MArray(oprands[2]));
							break;
						}
						}
						break;
					}
					case LOADRESOURCE: {
						String resourceId = getStringVar(oprands[0]);
						int width = getIntVar(oprands[1]);
						int height = getIntVar(oprands[2]);
						Adaptor.getInstance().loadExternalImage(resourceId,
								width, height, this.hashCode());
						break;
					}
					case GETIMAGESIZE: {
						String resourceId = getStringVar(oprands[2]);

						Adaptor.getInstance().grabImageResource(resourceId);
						Object image = Adaptor.getInstance().getImageResource(
								resourceId);
						switch (oprands[1]) {
						case 0: {
							if (image != null) {
								if (image instanceof MilkImage) {
									setVar(oprands[0],
											((MilkImage) image).getWidth());
								} else if (image instanceof MilkSprite) {
									setVar(oprands[0],
											((MilkSprite) image).getWidth());
								}
							} else {
								if (resourceId.equals("ship_1")) {
									int brk = 1;
									int a = brk;
								}
								setVar(oprands[0], 0);
							}
							break;

						}
						case 1: {
							if (image != null) {
								if (image instanceof MilkImage) {
									setVar(oprands[0],
											((MilkImage) image).getHeight());
								} else if (image instanceof MilkSprite) {
									setVar(oprands[0],
											((MilkSprite) image).getHeight());
								}
							} else {
								setVar(oprands[0], 0);
							}
							break;
						}
						}
						Adaptor.getInstance().releaseImageResource(resourceId);
						break;
					}
					case STACKSIZE: {
						setVarSize(varSize + oprands[0]);

						int count = paramVars[21];
						for (int i = 0; i < count; i++) {
							if (paramVars[i * 2] == 0) {
								setVar(i + 1, paramVars[i * 2 + 1]);
							} else if (paramVars[i * 2] == 1) {
								setVar(i + 1, paramObjs[i]);
								paramObjs[i] = null;
							}
						}

						break;
					}
					case SETENV: {
						switch (oprands[1]) {
						case 0: {
							bgColor = getIntVar(oprands[0]);
							break;
						}
						case 1: {
							showLoading = (getIntVar(oprands[0]) != 0);
							break;
						}
						}
						break;
					}
					case GETGROUPDATA: {
						MGroup group = getGroupVar(oprands[1]);
						switch (oprands[2]) {
						case 0: {
							setVar(oprands[0], group.getzIndex());
							break;
						}
						case 1: {
							setVar(oprands[0], group.isVisible() ? 1 : 0);
							break;
						}
						case 2: {
							setVar(oprands[0], group.getX());
							break;
						}
						case 3: {
							setVar(oprands[0], group.getY());
							break;
						}
						case 4: {
							setVar(oprands[0], group.getData());
							break;
						}
						case 5: {
							setVar(oprands[0], group.getViewPort());
							break;
						}
						}
						break;
					}
					case SETGROUPDATA: {
						MGroup group = getGroupVar(oprands[1]);
						switch (oprands[2]) {
						case 0: {
							group.setzIndex(getIntVar(oprands[0]));
							break;
						}
						case 1: {
							group.setVisible(getIntVar(oprands[0]) != 0);
							break;
						}
						case 2: {
							group.setX(getIntVar(oprands[0]));
							break;
						}
						case 3: {
							group.setY(getIntVar(oprands[0]));
							break;
						}
						case 4: {
							group.setData(getStringVar(oprands[0]));
							break;
						}
						case 5: {
							// set view port
							group.setViewPort(getRectVar(oprands[0]));
						}
						}
						break;
					}
					case ADDCHILD: {
						MGroup group = getGroupVar(oprands[0]);

						Object var = getObjVar(oprands[1]);
						if (var instanceof MDraw) {
							group.addChild((MDraw) var);
						} else {
							throw new Exception("invalid children");
						}
						break;
					}
					case REMOVECHILD: {
						MGroup group = getGroupVar(oprands[0]);
						Object var = getObjVar(oprands[1]);
						if (var instanceof MDraw) {
							group.removeChild((MDraw) var);
						} else {
							throw new Exception("invalid children");
						}
						break;
					}
					case RELATIVEPOS: {
						MDraw draw = (MDraw) getObjVar(oprands[1]);
						int inputx = getIntVar(oprands[2]);
						int inputy = getIntVar(oprands[3]);
						switch (oprands[4]) {
						case 0: {
							MArray result = new MArray();
							result.append(new Integer(draw.resolveX(inputx,
									false)));
							result.append(new Integer(draw.resolveY(inputy,
									false)));
							setVar(oprands[0], result);
							break;
						}
						case 1: {
							MArray result = new MArray();
							result.append(new Integer(draw.resolveX(inputx,
									true)));
							result.append(new Integer(draw.resolveY(inputy,
									true)));
							setVar(oprands[0], result);
							break;
						}

						}
						break;
					}
					case GETSCREEN: {
						setVar(oprands[0], screen);
						break;
					}
					case GETPARENT: {
						setVar(oprands[0],
								((MDraw) getObjVar(oprands[1])).getParent());
						break;
					}
					case GETCHILDREN: {
						setVar(oprands[0],
								((MGroup) getObjVar(oprands[1])).getChildren());
						break;
					}
					case DRAWSHAPE: {
						if (currentDraw == null) {
							break;
						}
						Vector stroke = VectorPool.produce();
						stroke.addElement(new Integer(oprands[0]));
						switch (oprands[0]) {
						case 0: {
							// Core.getInstance().drawDef.strokeColor =
							// getIntVar(oprands[1]);

							strokeColor = getIntVar(oprands[1]);
							stroke.addElement(new Integer(strokeColor));

							break;
						}
						case 1: {
							// Core.getInstance().drawDef.fillColor =
							// getIntVar(oprands[1]);
							fillColor = getIntVar(oprands[1]);
							stroke.addElement(new Integer(fillColor));

							break;
						}
						case 2: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							// Core.getInstance().drawDef.gradientStart =
							// getIntVar(oprands[1]);
							// Core.getInstance().drawDef.gradientEnd =
							// getIntVar(oprands[2]);
							// Core.getInstance().drawDef.gradientType =
							// getIntVar(oprands[3]);
							break;
						}
						case 3: {
							MRect rect = getRectVar(oprands[1]);
							stroke.addElement(rect);
							// if (rect != null) {
							// Core.getInstance().drawDef.useStrokeColor();
							// int x = Core.getInstance().drawDef.adjustX(rect
							// .getX());
							// int y = Core.getInstance().drawDef.adjustY(rect
							// .getY());
							// Core.getInstance().drawDef.g.drawRect(x, y,
							// rect.getWidth(), rect.getHeight());
							// }
							break;
						}
						case 4: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							// Core.getInstance().drawDef.useStrokeColor();
							// int x = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// Core.getInstance().drawDef.g.drawRect(x, y,
							// getIntVar(oprands[3]),
							// getIntVar(oprands[4]));
							break;
						}
						case 5: {

							MRect rect = getRectVar(oprands[1]);
							stroke.addElement(rect);

							// if (rect != null) {
							// Core.getInstance().drawDef.useFillColor();
							// int x = Core.getInstance().drawDef.adjustX(rect
							// .getX());
							// int y = Core.getInstance().drawDef.adjustY(rect
							// .getY());
							// Core.getInstance().drawDef.g.fillRect(x, y,
							// rect.getWidth(), rect.getHeight());
							// }
							break;
						}
						case 6: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							// Core.getInstance().drawDef.useFillColor();
							// int x = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// int width = getIntVar(oprands[3]);
							// int height = getIntVar(oprands[4]);

							// Core.getInstance().drawDef.g.fillRect(x, y,
							// width,
							// height);
							break;
						}
						case 7: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							// Core.getInstance().drawDef.useStrokeColor();
							// int x1 = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y1 = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// int x2 = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[3]));
							// int y2 = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[4]));
							// Core.getInstance().drawDef.g.drawLine(x1, y1, x2,
							// y2);
							break;
						}
						case 8: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							stroke.addElement(new Integer(getIntVar(oprands[5])));
							// Core.getInstance().drawDef.useStrokeColor();
							// int x = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// Core.getInstance().drawDef.g.drawArc(x, y,
							// getIntVar(oprands[3]),
							// getIntVar(oprands[3]),
							// getIntVar(oprands[4]),
							// getIntVar(oprands[5]));
							break;
						}
						case 9: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							stroke.addElement(new Integer(getIntVar(oprands[5])));
							// Core.getInstance().drawDef.useFillColor();
							// int x = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// Core.getInstance().drawDef.g.fillArc(x, y,
							// getIntVar(oprands[3]),
							// getIntVar(oprands[3]),
							// getIntVar(oprands[4]),
							// getIntVar(oprands[5]));
							break;
						}
						case 10: {

							MRect rect = getRectVar(oprands[1]);
							stroke.addElement(rect);
							// Core.getInstance().drawDef.useStrokeColor();
							// if (rect != null) {
							// int x = Core.getInstance().drawDef.adjustX(rect
							// .getX());
							// int y = Core.getInstance().drawDef.adjustY(rect
							// .getY());
							// Core.getInstance().drawDef.g.drawArc(x, y,
							// rect.getWidth(), rect.getHeight(), 0,
							// 360);
							// }
							break;
						}
						case 11: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							// Core.getInstance().drawDef.useStrokeColor();
							// int rx = getIntVar(oprands[1]);
							// int ry = getIntVar(oprands[2]);
							// int rw = getIntVar(oprands[3]);
							// int rh = getIntVar(oprands[4]);
							//
							// int x = Core.getInstance().drawDef.adjustX(rx);
							// int y = Core.getInstance().drawDef.adjustY(ry);
							// Core.getInstance().drawDef.g.drawArc(x, y, rw,
							// rh,
							// 0, 360);
							break;
						}
						case 12: {

							MRect rect = getRectVar(oprands[1]);
							stroke.addElement(rect);
							// Core.getInstance().drawDef.useFillColor();
							// if (rect != null) {
							// int x = Core.getInstance().drawDef.adjustX(rect
							// .getX());
							// int y = Core.getInstance().drawDef.adjustY(rect
							// .getY());
							// Core.getInstance().drawDef.g.fillArc(x, y,
							// rect.getWidth(), rect.getHeight(), 0,
							// 360);
							// }
							break;
						}
						case 13: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							// Core.getInstance().drawDef.useFillColor();
							// int rx = getIntVar(oprands[1]);
							// int ry = getIntVar(oprands[2]);
							// int rw = getIntVar(oprands[3]);
							// int rh = getIntVar(oprands[4]);
							//
							// int x = Core.getInstance().drawDef.adjustX(rx);
							// int y = Core.getInstance().drawDef.adjustY(ry);
							// Core.getInstance().drawDef.g.fillArc(x, y, rw,
							// rh,
							// 0, 360);
							break;
						}
						case 14: {
							String image = getStringVar(oprands[1]);
							Adaptor.getInstance().grabImageResource(image);
							Adaptor.getInstance().releaseImageResource(image);
							MRect rect = getRectVar(oprands[2]);
							stroke.addElement(image);
							stroke.addElement(new Integer(rect.x));
							stroke.addElement(new Integer(rect.y));
							// Adaptor.getInstance().grabImageResource(image);
							// Object toDraw = Adaptor.getInstance()
							// .getImageResource(image);
							// if (toDraw != null) {
							//
							// int x = Core.getInstance().drawDef.adjustX(rect
							// .getX());
							// int y = Core.getInstance().drawDef.adjustY(rect
							// .getY());
							// if (toDraw instanceof Image) {
							// Core.getInstance().drawDef.g.drawImage(
							// (Image) toDraw, x, y, Graphics.TOP
							// | Graphics.LEFT);
							// } else {
							// Sprite sprite = (Sprite) toDraw;
							// sprite.setPosition(x, y);
							// sprite.paint(Core.getInstance().drawDef.g);
							// }
							// }
							// Adaptor.getInstance().releaseImageResource(image);
							break;
						}
						case 15:
						case 16: {
							String image = getStringVar(oprands[1]);
							Adaptor.getInstance().grabImageResource(image);
							Adaptor.getInstance().releaseImageResource(image);
							int int2 = getIntVar(oprands[2]);
							int int3 = getIntVar(oprands[3]);
							stroke.addElement(image);
							stroke.addElement(new Integer(int2));
							stroke.addElement(new Integer(int3));
							// Adaptor.getInstance().grabImageResource(image);
							// Object toDraw = Adaptor.getInstance()
							// .getImageResource(image);
							// if (toDraw != null) {
							// int x = Core.getInstance().drawDef
							// .adjustX(int2);
							// int y = Core.getInstance().drawDef
							// .adjustY(int3);
							// if (toDraw instanceof Image) {
							// Core.getInstance().drawDef.g.drawImage(
							// (Image) toDraw, x, y, Graphics.TOP
							// | Graphics.LEFT);
							// } else {
							// Sprite sprite = (Sprite) toDraw;
							// sprite.setPosition(x, y);
							// sprite.paint(Core.getInstance().drawDef.g);
							// }
							// }
							// Adaptor.getInstance().releaseImageResource(image);
							break;
						}
						case 17:
						case 18: {
							stroke.addElement(new Integer(getIntVar(oprands[1])));
							stroke.addElement(new Integer(getIntVar(oprands[2])));
							stroke.addElement(new Integer(getIntVar(oprands[3])));
							stroke.addElement(new Integer(getIntVar(oprands[4])));
							stroke.addElement(new Integer(getIntVar(oprands[5])));
							// int x = Core.getInstance().drawDef
							// .adjustX(getIntVar(oprands[1]));
							// int y = Core.getInstance().drawDef
							// .adjustY(getIntVar(oprands[2]));
							// int w = getIntVar(oprands[3]);
							// int h = getIntVar(oprands[4]);
							// int r = getIntVar(oprands[5]);
							// Graphics g = Core.getInstance().drawDef.g;
							// if (oprands[0] == 17) {
							// Core.getInstance().drawDef.useStrokeColor();
							// g.drawLine(x + r, y, x + w - r, y);
							// g.drawLine(x, y + r, x + r, y);
							// g.drawLine(x, y + r, x, y + h - r);
							// g.drawLine(x + w - r, y, x + w, y + r);
							// g.drawLine(x + w, y + h - r, x + w - r, y + h);
							// g.drawLine(x + w, y + r, x + w, y + h - r);
							// g.drawLine(x + r, y + h, x, y + h - r);
							// g.drawLine(x + r, y + h, x + w - r, y + h);
							// } else if (oprands[0] == 18) {
							// int color = Core.getInstance().drawDef.fillColor;
							// Image[] images = new Image[r + 1];
							// for (int i = images.length - 1, k = 0; i >= 0;
							// i--, k++) {
							// images[k] = Adaptor.getAlphaColorLine(
							// color, w - 2 * i, 1);
							// }
							// for (int i = 0; i < r; i++) {
							// g.drawImage(images[i], x + (r - i), y + i,
							// Graphics.TOP | Graphics.LEFT);
							// }
							// for (int i = r; i < h - r; i++) {
							// g.drawImage(images[r], x, y + i,
							// Graphics.TOP | Graphics.LEFT);
							// }
							// for (int i = h - r, k = r - 1; i < h; i++, k--) {
							// g.drawImage(images[k], x + (r - k), y + i,
							// Graphics.TOP | Graphics.LEFT);
							// }
							// }

							break;
						}

						}

						break;
					}
					case ARGB: {
						int color = (getIntVar(oprands[1]) << 24)
								+ (getIntVar(oprands[2]) << 16)
								+ (getIntVar(oprands[3]) << 8)
								+ getIntVar(oprands[4]);
						setVar(oprands[0], color);
						break;
					}
					case TRANSFORM: {
						MPlayer player = getPlayerVar(oprands[0]);
						int param0 = getIntVar(oprands[1]);
						int param1 = getIntVar(oprands[2]);
						int param2 = getIntVar(oprands[3]);
						int param3 = getIntVar(oprands[4]);
						int param4 = getIntVar(oprands[5]);
						player.transform(param0, param1, param2, param3, param4);

						break;
					}
					case GETTILESDATA: {
						switch (oprands[2]) {
						case 0: {
							setVar(oprands[0], getTilesVar(oprands[1])
									.getzIndex());
							break;
						}
						case 1: {
							setVar(oprands[0], getTilesVar(oprands[1])
									.isVisible() ? 1 : 0);
							break;
						}
						case 2: {
							setVar(oprands[0], getTilesVar(oprands[1]).getX());
							break;
						}
						case 3: {
							setVar(oprands[0], getTilesVar(oprands[1]).getY());
							break;
						}
						case 4: {
							setVar(oprands[0], getTilesVar(oprands[1])
									.getWidth());
							break;
						}
						case 5: {
							setVar(oprands[0], getTilesVar(oprands[1])
									.getHeight());
							break;
						}
						case 7: {
							setVar(oprands[0],
									getTilesVar(oprands[1]).matchFingerToCell(
											getIntVar(oprands[3]),
											getIntVar(oprands[4])));
							break;
						}
						case 8: {
							setVar(oprands[0],
									getTilesVar(oprands[1]).matchCellToCoord(
											getIntVar(oprands[3]),
											getIntVar(oprands[4])));
							break;
						}
						}
						break;
					}
					case SETTILESDATA: {
						switch (oprands[2]) {
						case 0: {
							getTilesVar(oprands[1]).setzIndex(
									getIntVar(oprands[0]));
							break;
						}
						case 1: {
							getTilesVar(oprands[1]).setVisible(
									getIntVar(oprands[0]) == 1);
							break;
						}
						case 2: {
							getTilesVar(oprands[1]).setX(getIntVar(oprands[0]));
							break;
						}
						case 3: {
							getTilesVar(oprands[1]).setY(getIntVar(oprands[0]));
							break;
						}
						case 6: {
							getTilesVar(oprands[1]).setCells(
									getArrayVar(oprands[0]));
							break;
						}
						case 7: {
							getTilesVar(oprands[1]).setTileMode(
									getIntVar(oprands[0]));
							break;
						}
						}
						break;
					}
					case TILESOP: {
						MTiles tiles = getTilesVar(oprands[1]);
						switch (oprands[0]) {
						case 0: {
							setVar(oprands[2],
									tiles.createAnimatedTile(getIntVar(oprands[3])));
							break;
						}
						case 1: {
							setVar(oprands[2],
									tiles.getAnimatedTile(getIntVar(oprands[3])));
							break;
						}
						case 2: {
							tiles.setAnimatedTile(getIntVar(oprands[2]),
									getIntVar(oprands[3]));
							break;
						}
						case 3: {
							tiles.setCell(getIntVar(oprands[2]),
									getIntVar(oprands[3]),
									getIntVar(oprands[4]));
							break;
						}
						case 4: {
							setVar(oprands[2], tiles.getCell(
									getIntVar(oprands[3]),
									getIntVar(oprands[4])));
							break;
						}
						case 5: {
							tiles.startAnimation(getArrayVar(oprands[2]));
							break;
						}
						case 6: {
							tiles.stopAnimation();
							break;
						}
						}
						break;
					}
					case TRYSTART: {
						TryBlock tb = new TryBlock();
						tb.catchStart = oprands[0];
						tb.stack = curStack.hashCode();
						tb.type = getIntVar(oprands[1]);
						tryStack.addElement(tb);
						break;
					}
					case TRYFINISH: {
						tryStack.removeElementAt(tryStack.size() - 1);
						curStack.istPtr = oprands[0];
						break;
					}
					case THROW: {
						throw new CodeException(getIntVar(oprands[0]));
					}
					case WINDOWDO: {
						int mode = oprands[0];
						switch (mode) {
						case 0: {// open window
							int flags = getIntVar(oprands[5]);
							String windowId = getStringVar(oprands[1]);
							String resourceId = getStringVar(oprands[2]);
							MRect rect = getRectVar(oprands[3]);
							MMap params = getMapVar(oprands[4]);
							openWindow(windowId, resourceId, rect, params,
									flags);
							break;
						}
						case 1: {// close window
							String windowId = getStringVar(oprands[1]);
							closeWindow(windowId);
							break;
						}
						case 2: {// close all windows
							closeAllWindows();
							break;
						}
						case 3: {// open url, for supporting moml2 integration
									// mode.
									// String url = getStringVar(oprands[1]);
							// Adaptor.getInstance().openUrl(url);
							break;
						}
						case 4: {// goto chat page, show world chat
							HallAccess.intoWorldRoom();
							break;
						}
						case 5: {// goto private chat page, show private chat
									// with id.
							int id = getIntVar(oprands[1]);
							String name = getStringVar(oprands[2]);
							HallAccess.intoPrivateRoom(id, name);
							break;
						}
						case 6: {// send sms;
							String to = getStringVar(oprands[1]);
							String content = getStringVar(oprands[2]);

							Adaptor.milk.sendSMS(to, content,
									Adaptor.getInstance());
							break;
						}
						case 7: {// set chat params
							String name = getStringVar(oprands[1]);
							int familyId = getIntVar(oprands[2]);
							String familyName = getStringVar(oprands[3]);
							HallAccess.setMyChatUserInfo(name, familyId,
									familyName);
							break;
						}
						case 8: {// load resources
							// MArray resIds = getArrayVar(oprands[1]);
							// int count = resIds.size();
							// loadingResources.removeAllElements();
							// for (int i = 0; i < count; i++) {
							// String resId = resIds.getString(i);
							// if (!Adaptor.getInstance().isResourceCached(
							// resId)) {
							// loadingResources.addElement(resId);
							// Adaptor.getInstance().loadResource(resId,
							// this.hashCode());
							// }
							// }
							// if (loadingResources.size() > 0) {
							// halt();
							// }
						}

						}
						break;
					}
					case CLEAN: {
						Object obj = getObjVar(oprands[0]);
						if (obj instanceof MMap) {
							MMap map = (MMap) obj;
							map.clean();
						} else if (obj instanceof MArray) {
							MArray array = (MArray) obj;
							array.clean();
						}
						break;
					}
					case DODEBUG: {
						int debug = 0;
						int a = debug;
						break;
					}
					case QUICKINPUT: {
						String title = getStringVar(oprands[1]);
						String initText = getStringVar(oprands[2]);
						int maxLength = getIntVar(oprands[3]);
						int constraints = getIntVar(oprands[4]);
						try {
							Adaptor.milk.getInput(title, initText, maxLength,
									constraints, this);
						} catch (Exception e1) {
							System.out
									.println("---------QUICKINPUT Exception input");
						}
						halt();
						setVar(oprands[0], this.input);
						break;
					}
					case STRSPLIT: {
						try {
							String input = getStringVar(oprands[1]);
							String pattern = getStringVar(oprands[2]);
							int count = getIntVar(oprands[3]);
							if (count == 0) {
								count = 1000;
							}
							Vector values = Adaptor
									.split(input, pattern, count);
							MArray array = new MArray();
							for (int i = 0; i < values.size(); i++) {
								array.append(values.elementAt(i));
							}
							setVar(oprands[0], array);

						} catch (Exception e) {
							Adaptor.exception(e);
						}
						break;
					}
					case DOINPUT: {
						int mode = oprands[1];
						if (mode == 0) {
							if (this instanceof Window) {
								Window window = (Window) this;
								if (oprands[0] == 0) {
									// show input
									window.doInput(getElementVar(oprands[2]),
											getElementVar(oprands[3]),
											getIntVar(oprands[4]),
											getIntVar(oprands[5]),
											getIntVar(oprands[6]));
								} else {
									// hide input
									window.hideInput();
								}
							}
						}
						break;
					}
					case IMPORT: {
						String libName = byteCode.strConsts[oprands[0]];
						byte[] bytes = Adaptor.milk.gunzip(Adaptor
								.getInstance().getResource(libName));
						ByteCode thing = this.initByteCode(bytes, libName);
						libs.put(libName, thing);
						break;
					}
					case SENDHTTPDATA: {
						int count = oprandsLength;
						String url = getStringVar(oprands[0]);
						String key = getStringVar(oprands[1]);
						String value = getStringVar(oprands[2]);
						int moagentWap = 1;
						if (count == 4) {
							moagentWap = getIntVar(oprands[3]);
						}
						Adaptor.getInstance().sendHttpData(url, key, value,
								this.hashCode(), resId, moagentWap);
						break;
					}
					case DOCHAT: {
						int mode = oprands[0];
						switch (mode) {
						case 0: {
							Adaptor.getInstance().initChatTabRect(
									getIntVar(oprands[1]),
									getIntVar(oprands[2]),
									getIntVar(oprands[3]),
									getIntVar(oprands[4]));
							break;
						}
						case 1: {
							Adaptor.getInstance().enableShowChatTab(
									getIntVar(oprands[1]));
							break;
						}
						}
						break;
					}
					case RESOLVE: {
						int mode = oprands[3];
						MDraw draw = (MDraw) getObjVar(oprands[1]);
						switch (mode) {
						case 1: {// getGlobalX
							setVar(oprands[0],
									draw.resolveX(getIntVar(oprands[2]), true));
							break;
						}
						case 2: {// getGlobalY
							setVar(oprands[0],
									draw.resolveY(getIntVar(oprands[2]), true));
							break;
						}
						case 3: {// getLocalX
							setVar(oprands[0],
									draw.resolveX(getIntVar(oprands[2]), false));
							break;
						}
						case 4: {// getLocalY
							setVar(oprands[0],
									draw.resolveY(getIntVar(oprands[2]), true));
							break;
						}
						}
						break;
					}
					case QUERYENV: {
						MMap query = getMapVar(oprands[1]);
						MMap env = Adaptor.getInstance().queryEnv(query);
						setVar(oprands[0], env);
						break;
					}
					}
				} catch (Exception t) {
					boolean handled = false;
					while (tryStack.size() > 0) {
						int size = tryStack.size();
						TryBlock tb = (TryBlock) tryStack.elementAt(size - 1);
						tryStack.removeElementAt(size - 1);
						if (tb.type == 0
								|| (t instanceof CodeException && tb.type == ((CodeException) t).error)) {
							handled = true;
							if (curStack.hashCode() == tb.stack) {
								curStack.istPtr = tb.catchStart;
							} else {
								setVarSize(curStack.stackStart);
								while (stacks.size() > 0) {
									popStack();
									if (curStack.hashCode() != tb.stack) {
										setVarSize(curStack.stackStart);
									} else {
										curStack.istPtr = tb.catchStart;
										break;
									}
								}
							}
							break;
						}
					}
					if (!handled) {
						int count = stacks.size();
						StringBuffer buffer = new StringBuffer();
						for (int i = 0; i < count; i++) {
							MStack stack = (MStack) stacks.elementAt(i);
							if (stack.funcName == null) {
								break;
							}
							buffer.append(stack.funcName);
							buffer.append(" => ");
						}
						buffer.append(curStack.funcName);
						String thing = buffer.toString();
						if (thing != null && thing.length() > 0) {
							Adaptor.debug("CALLSTACK: [" + thing + "]");
						}
						Adaptor.debug("===error in instruction("
								+ curStack.istPtr + ") opcode[" + ins[0]
								+ "] LIB: " + byteCode.libName);
						Adaptor.exception(t);
						stacks.removeAllElements();
						throw new Exception(t.getMessage());
					} else {
						// Adaptor.getInstance().log(
						// t.toString() + " : " + t.getMessage(), 0);
					}
				}

			}
		}
		boolean isInt = getVarType(0) == 0;
		int rInt = 0;
		try {
			if (isInt) {
				rInt = getIntVar(0);
			}
		} catch (Exception e) {
		}
		tryStack.removeAllElements();
		return rInt;
	}

	private void halt() {
		waiter.lock();
	}

	private void resume() {
		waiter.unlock();
	}

	protected boolean subExecute(byte[] ins) {
		return false;
	}

	boolean getJumpCondition(int opCode, int[] oprands) {
		switch (opCode) {
		case JE: {
			return getIntVar(oprands[1]) == getIntVar(oprands[2]);
		}
		case JNE: {
			return getIntVar(oprands[1]) != getIntVar(oprands[2]);
		}
		case JL: {
			return getIntVar(oprands[1]) < getIntVar(oprands[2]);
		}
		case JLE: {
			return getIntVar(oprands[1]) <= getIntVar(oprands[2]);
		}
		case JG: {
			return getIntVar(oprands[1]) > getIntVar(oprands[2]);
		}
		case JGE: {
			return getIntVar(oprands[1]) >= getIntVar(oprands[2]);
		}
		case JMP: {
			return true;
		}
		}
		return false;
	}

	void doArith(int opCode, int[] oprands) {
		int op0 = oprands[0];
		int op1 = oprands[1];
		int op2 = oprands[2];
		switch (opCode) {
		case ADD: {
			setVar(op0, getIntVar(op1) + getIntVar(op2));
			break;
		}
		case SUB: {
			setVar(op0, getIntVar(op1) - getIntVar(op2));
			break;
		}
		case MUL: {
			setVar(op0, getIntVar(op1) * getIntVar(op2));
			break;
		}
		case DIV: {
			setVar(op0, getIntVar(op1) / getIntVar(op2));
			break;
		}
		case MOD: {
			setVar(op0, getIntVar(op1) % getIntVar(op2));
			break;
		}
		case LSH: {
			setVar(op0, getIntVar(op1) << getIntVar(op2));
			break;
		}
		case RSH: {
			setVar(op0, getIntVar(op1) >> getIntVar(op2));
			break;
		}
		case AND: {
			setVar(op0, getIntVar(op1) & getIntVar(op2));
			break;
		}
		case OR: {
			setVar(op0, getIntVar(op1) | getIntVar(op2));
			break;
		}
		case XOR: {
			setVar(op0, getIntVar(op1) ^ getIntVar(op2));
			break;
		}
		}
	}

	void setVar(int index, Object value) {
		if (value instanceof Integer) {
			throw new RuntimeException("Invalid Integer set integer var");
		}

		int ti = (index + curStack.stackStart) * 2;
		if (vars[ti] == 0) {
			vars[ti] = 1;
		}
		objs[index + curStack.stackStart] = value;
	}

	void setVar(int index, int value) {

		int ti = (index + curStack.stackStart) * 2;
		if (vars[ti] == 1) {
			vars[ti] = 0;
		}
		vars[ti + 1] = value;
		objs[index + curStack.stackStart] = null;
	}

	void setGlobalVar(int index, Object value) {
		if (value instanceof Integer) {
			throw new RuntimeException("Invalid Integer set integer var");
		}
		int ti = (index) * 2;
		if (vars[ti] == 0) {
			vars[ti] = 1;
		}
		objs[index] = value;
	}

	void setGlobalVar(int index, int value) {

		int ti = (index) * 2;
		if (vars[ti] == 1) {
			vars[ti] = 0;
		}
		vars[ti + 1] = value;
		objs[index] = null;
	}

	int getIntVar(int index) {
		int ti = (index + curStack.stackStart) * 2;
		return vars[ti + 1];
	}

	String getStringVar(int index) {
		return ((String) getObjVar(index));
	}

	MArray getArrayVar(int index) {
		return ((MArray) getObjVar(index));
	}

	MMap getMapVar(int index) {
		return ((MMap) getObjVar(index));
	}

	MRect getRectVar(int index) {
		return ((MRect) getObjVar(index));
	}

	MPlayer getPlayerVar(int index) {
		return ((MPlayer) getObjVar(index));
	}

	MText getTextVar(int index) {
		return ((MText) getObjVar(index));
	}

	MGroup getGroupVar(int index) {
		return ((MGroup) getObjVar(index));
	}

	MTiles getTilesVar(int index) {
		return ((MTiles) getObjVar(index));
	}

	Sv3Element getElementVar(int index) {
		return (Sv3Element) getObjVar(index);
	}

	int getVarType(int index) {
		return vars[2 * (index + curStack.stackStart)];
	}

	Object getObjVar(int index) {
		return objs[index + curStack.stackStart];
	}

	int getGlobalVarType(int index) {
		return vars[2 * index];
	}

	int getGlobalIntVar(int index) {
		int ti = index * 2;
		return vars[ti + 1];
	}

	Object getGlobalObjVar(int index) {
		return objs[index];
	}

	void setVarSize(int value) {
		if (value * 2 > vars.length) {
			int[] temp = new int[value * 2];
			System.arraycopy(vars, 0, temp, 0, vars.length);
			vars = temp;
			Object[] temp2 = new Object[value];
			System.arraycopy(objs, 0, temp2, 0, objs.length);
			objs = temp2;
		} else if (value < varSize) {
			for (int i = value; i < varSize; i++) {
				vars[i * 2] = 0;
				objs[i] = null;
			}
		}
		varSize = value;

	}

	void openWindow(String windowId, String resourceId, MRect position,
			MMap params, int flags) throws Exception {
		Adaptor.milk.clearKeyStatus();
		boolean found = false;
		int count = windows.size();
		for (int i = 0; i < count; i++) {
			Window window = (Window) windows.elementAt(i);
			if (window.getSceneId().equals(windowId)) {
				found = true;
				windows.removeElementAt(i);
				windows.addElement(window);

				break;
			}
		}
		if (!found) {
			Scene window = Adaptor.getInstance().loadScene(windowId,
					resourceId, position, params, flags, this);
			// System.out.println("-----------------open new window rect width:"
			// + position.width);
			if (window != null) {
				window.runInit();
				windows.addElement(window);
			} else {
				Adaptor.debug("failed to load resourceId : " + resourceId);
			}
			if (window instanceof Window) {
				Window win = (Window) window;
				win.layoutWindow();
			}
		}
		Adaptor.getInstance().bufferReady();
	}

	void closeWindow(String windowId) {
		int count = windows.size();
		for (int i = 0; i < count; i++) {
			Scene window = (Scene) windows.elementAt(i);
			if (window.getSceneId().equals(windowId)) {
				windows.removeElementAt(i);
				window.onStop();
				window = null;
				break;
			}
		}
		Adaptor.getInstance().bufferReady();
	}

	void closeAllWindows() {
		int count = windows.size();
		for (int i = 0; i < count; i++) {
			Scene window = (Scene) windows.elementAt(i);
			window.onStop();
		}
		windows.removeAllElements();
		windows = null;
		Adaptor.getInstance().bufferReady();
	}

	public void onInput(boolean cancelled, String input) {
		System.out.println("--------onInput--cancelled" + cancelled + "/input"
				+ input);
		if (cancelled) {
			this.input = null;
		} else {
			this.input = input;
		}
		resume();
	}

	protected MilkCallback genCallback(int isp, String libName) {
		if (isp == -1 || isp == 0) {
			return null;
		} else {
			return new MilkCallback(isp, libName);
		}
	}

	protected String getLibName(int index, ByteCode byteCode) {
		if (index == -1) {
			return null;
		} else {
			return byteCode.strConsts[index];
		}
	}
}
