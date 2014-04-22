package milk.ui;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.midlet.MIDlet;

import milk.chat.core.HallAccess;
import milk.ui.j2mechat.J2meHallScreen;
import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.implement.MD5;
import milk.implement.MilkInputStream;
import milk.implement.mk.MMap;
import milk.net.MoPacket;
//import milk.ui.iap.IapPayment;
import milk.ui2.InputListener;
import milk.ui2.MilkApp;
import milk.ui2.MilkCanvas;
import milk.ui2.MilkDisplayable;
import milk.ui2.MilkTask;
import milk.ui2.RawRequest;
import milk.ui2.SmsListener;

public class MilkAppImpl extends MIDlet implements Runnable, CommandListener,
		MilkApp {

	public Timer timer = new Timer();

	private int alertType = 1;;

	public MilkCanvasImpl canvas = null;
	private MilkDisplayable currentDisplay = null;

	private Hashtable audioPlayers = new Hashtable();

	private boolean exitMIDlet = false;

	private TextBox textBox;
	private InputListener inputListener;
	private Alert alert;

	public void getInput(String prompt, String initialContent, int maxLength,
			int constraints, InputListener listener) {
		if (prompt == null) {
			prompt = "";
		}
		if (initialContent == null) {
			initialContent = "";
		}
		inputListener = listener;
		if (maxLength == 0) {
			maxLength = 65535;
		}
		textBox = new TextBox(prompt, initialContent, maxLength, constraints);
		Command ok = new Command("Ok", Command.OK, 0);
		Command back = new Command("Back", Command.CANCEL, 0);
		textBox.addCommand(ok);
		textBox.addCommand(back);
		textBox.setCommandListener(this);

		MilkDisplayableImpl md = new MilkDisplayableImpl(textBox);
		switchDisplay(md);

	}

	public void showAlert(String prompt, int type) {
		alertType = type;
		alert = new Alert("ALERT");
		alert.setString(prompt);
		alert.setTimeout(Alert.FOREVER);
		alert.setCommandListener(this);
		alert.addCommand(new Command("OK", Command.OK, 0));
		MilkDisplayableImpl md = new MilkDisplayableImpl(alert);
		switchDisplay(md);
	}

	public void showAlert(String prompt) {
		// Dialog.alert(prompt);
	}

	public MilkCanvas getMilkCanvas() {
		return this.canvas;
	}

	public boolean isTouchDevice() {
		return canvas.hasPointerEvents() || canvas.hasPointerMotionEvents();
	}

	public MilkDisplayable getCurrentDisplay() {
		return currentDisplay;
	}

	public void switchDisplay(MilkDisplayable md) {
		currentDisplay = md;
		this.getDisplay().setCurrent(((MilkDisplayableImpl) md).displayable);
	}

	public void run() {
		// to speed up window loading process
		try {
			Class.forName("milk.implement.Window");
			Class.forName("smartview3.elements.Sv3Element");
		} catch (Exception e) {
		}

		exitMIDlet = false;
		J2meHallScreen hallScreen;
		try {
			hallScreen = new J2meHallScreen(this);
			HallAccess.init(this, hallScreen);
		} catch (Exception e) {
			Adaptor.exception(e);
		}
		try {
			Adaptor.getInstance().init();
		} catch (Exception e) {

		}
		Adaptor.getInstance().updateThread = Thread.currentThread();
		// MidpPlayer.loadMidpPlayer("splash.mid");
		// MidpPlayer.playMidpSound("splash.mid", 10);
		while (!exitMIDlet) {
			long start = System.currentTimeMillis();

			try {
				canvas.processKeyRepeatedEvent();
				Communicator.getInstance().processInMessage();
				Adaptor.getInstance().update();
				Communicator.getInstance().processDisconnection();
				Util.updateAllMappings();

			} catch (Exception t) {
				Adaptor.exception(t);
			} catch (Throwable t) {
				long diff = System.currentTimeMillis() - start;
				System.out.println("delay " + diff);
				t.printStackTrace();
			}
			// Adaptor.getInstance().fillGameBuffer();

			int take = (int) (System.currentTimeMillis() - start);

			int sleepTime = 50 - take;
			if (sleepTime < 30) {
				sleepTime = 30;
			}
			doSleep(sleepTime);
		}

		Communicator.getInstance().stop();
		HallAccess.exit();
		hallScreen = null;
		this.destroyApp(true);
		this.notifyDestroyed();
	}

	private void doSleep(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception t) {
			Adaptor.exception(t);
		}

	}

	public void exit() {
		exitMIDlet = true;
		notifyDestroyed();
	}

	public MilkAppImpl() {
		Adaptor.milk = this;
		Adaptor.uiFactory = new MilkJavaUiFactory();
		Communicator.getInstance().setCommListener(Adaptor.getInstance());
		Util.init();
		Adaptor.getInstance().load();
		canvas = new MilkCanvasImpl();
		canvas.repaint();
		currentDisplay = new MilkDisplayableImpl(canvas);
		// System.out.println("app started.");

		switchDisplay(currentDisplay);

		width = canvas.getWidth();
		height = canvas.getHeight();

		new Thread(this).start();
		new Thread(new Runnable() {

			public void run() {
				while (!exitMIDlet) {
					if (canvas.isShown()) {
						long s1 = 0, s2 = 0;
						try {
							s1 = System.currentTimeMillis();
							canvas.repaint();
							s2 = System.currentTimeMillis();
							// canvas.serviceRepaints();
						} catch (Exception e) {
						}
						try {
							if (s2 - s1 > 50) {
								Thread.sleep(20);
							} else {
								Thread.sleep(50 - (int) (s2 - s1));
							}

						} catch (Exception e) {
						}
					}

				}
			}
		}).start();

	}

	private void clear() {
		canvas = null;
	}

	protected void destroyApp(boolean b) {
		Adaptor.getInstance().exit();
		clear();
	}

	protected void pauseApp() {
		if (canvas != null)
			canvas.hideNotify();
	}

	protected void startApp() {

		if (canvas != null)
			canvas.showNotify();
	}

	public void drawNow() {
		// if (canvas != null) {
		// canvas.redrawWithLock();
		// }
	}

	private int width, height;

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public byte[] readImmutable(String key) {

		byte[] bytes = Util.readFile(key);
		// System.out.println("--readImmutable-------key:"+key+"/ bytes"+bytes);
		if (bytes == null) {
			bytes = readPackedFile(key);
		}

		return bytes;
	}

	public void writeImmutable(String key, byte[] bytes) {
		// long start = System.currentTimeMillis();
		// System.out.println("--writeImmutable-------key"+key+"/ bytes"+bytes);
		try {
			Util.saveFile(key, bytes);
		} catch (Exception t) {
			Adaptor.exception(t);
		}
		// Adaptor.debug("resource [" + key + "] writen in ["
		// + (System.currentTimeMillis() - start) + "]ms");
	}

	public byte[] readMutable(String key) {
		return readImmutable(key);
	}

	public void writeMutable(String key, byte[] bytes) {
		writeImmutable(key, bytes);
	}

	public void removeMutable(String key) {
		removeImmutable(key);
	}

	public boolean isMutableReadable(String key) {
		return isImmutableReadable(key);
	}

	public void clearKeyStatus() {
		canvas.clearKeyStatus();
	}

	public int getCanvasWidth() {
		return Adaptor.getInstance().getConfigWidth();
	}

	public int getCanvasHeight() {
		return Adaptor.getInstance().getConfigHeight();
	}

	public String getPlatform() {
		return "J2ME";
	}

	public int getPlatformCode() {
		return 3;
	}

	public void playSoundByBytes(String mimeType, byte[] aud, byte loopCount) {
	}

	public void stopSound(int id) {
		try {
			Player player = (Player) audioPlayers.get(new Integer(id));
			if (player != null) {
				player.stop();
			}
		} catch (Exception t) {
		}
	}

	private static final String midiMimeType = "audio/midi";

	public int loadSound(byte[] aud) {
		try {
			Player player = Manager.createPlayer(new ByteArrayInputStream(aud),
					midiMimeType);
			player.realize();
			player.prefetch();
			int id = player.hashCode();
			audioPlayers.put(new Integer(id), player);
			return id;

		} catch (Exception t) {
			// System.out.println("--------j2me----loadSound Exception:");
			return -1;
		}
	}

	public void playSoundById(int id, byte loopCount) {
		try {
			Player player = (Player) audioPlayers.get(new Integer(id));
			if (player != null && player.getState() != Player.STARTED) {
				if (loopCount > 0 || loopCount == -1)
					player.setLoopCount(loopCount);
				player.start();
			}
		} catch (Exception t) {
			// System.out.println("--------j2me----playSoundById Exception:");
		}
	}

	public boolean isImmutableReadable(String key) {
		boolean ok = Util.fileExists(key);
		if (!ok) {
			try {
				InputStream is = getClass().getResourceAsStream("/" + key);
				ok = is != null;
			} catch (Exception t) {
				Adaptor.infor("midlet Exception" + t.getMessage());
			}
		}
		return ok;
	}

	public void unloadSoundById(int id) {
		try {
			Player player = (Player) audioPlayers.get(new Integer(id));
			audioPlayers.remove(new Integer(id));
			if (player != null) {
				player.stop();
				player.deallocate();
			}
		} catch (Exception t) {
		}
	}

	public void onGameEnded(String url) {
		try {
			this.platformRequest(url);
		} catch (Exception t) {
		}
	}

	public void removeImmutable(String key) {
		Util.deleteFile(key);
	}

	public String getLang() {
		return "en";
	}

	public MIDlet getMIDlet() {
		return this;
	}

	public Display getDisplay() {
		return Display.getDisplay(this);
	}

	public void commandAction(Command c, Displayable d) {
		String contents = null;
		boolean cancelled = false;
		// System.out.println("commandAction called");

		if (textBox == d) {
			if (c.getCommandType() == Command.OK) {
				contents = textBox.getString().trim();
				MilkDisplayable md = new MilkDisplayableImpl(canvas);
				switchDisplay(md);
			} else if (c.getCommandType() == Command.CANCEL) {
				cancelled = true;
				MilkDisplayable md = new MilkDisplayableImpl(canvas);
				switchDisplay(md);
			}
			if (inputListener != null) {
				if (contents == null) {
					contents = "";
				}
				inputListener.onInput(cancelled, contents);
			}
		} else if (alert == d) {
			if (c.getCommandType() == Command.OK) {
				if (alertType == 1) {
					startNetwork();
				} else if (alertType == 2) {
					exit();
				}
			}
			MilkDisplayableImpl md = new MilkDisplayableImpl(canvas);
			switchDisplay(md);
		}

	}

	public int getGameAction(int keyCode) {
		try {
			return canvas.getKeyAction(keyCode);
		} catch (Exception e) {
			return 0;
		}
	}

	public SocketConnection getConnection() throws IOException {

		String serverUrl = "socket://" + Adaptor.getInstance().monetUrl + ":"
				+ Adaptor.getInstance().monetPort;
		return (SocketConnection) Connector.open(serverUrl);
	}

	public HttpConnection getHttpConnection(String url) throws IOException {
		return (HttpConnection) Connector.open(url, Connector.READ_WRITE, true);
	}

	public void showInput(final EditorSetting setting) {

	}

	public void hideInput() {

	}

	public void setInputText(String text) {
	}

	public String getModel() {
		return System.getProperty("microedition.platform");
	}

	public void startNetwork() {
		Communicator.getInstance().handShake();
	}

	public void scheduleTask(MilkTask task, long delay) {
		timer.schedule(((MilkTaskImpl) task).task, delay);
	}

	public void cancelTask(MilkTask task) {
		task.cancel();

	}

	public void send(MoPacket packet) {
		Communicator.getInstance().send(packet);
	}

	public void sendSMS(String to, String content, SmsListener listener) {
		new MilkSms(to, content, listener).sendShortMessage();
	}

	public void sendRawRequest(RawRequest request) {
		MoWebUtil.sendRawRequest(request);
	}

	public byte[] gunzip(byte[] input) {
		return MilkGzip.gunzip(input);
	}

	public void destroyApp() {
		exit();
	}

	public boolean openBrowser(String url) {
		// TODO Auto-generated method stub
		return false;
	}

	public MMap getAutoRegParams() {
		return null;
	}

	public MMap getAutoRegParams2(String salt1, String salt2) {
		MMap thing = new MMap();
		String uid = getUniqueId();
		thing.set("uid", uid);
		String hash = new MD5().getHashString(salt1 + uid + salt2);
		thing.set("hash", hash);
		return thing;
	}

	private String getUniqueId() {
		byte[] thing = readImmutable("my-unique-id");
		if (thing != null) {
			return new String(thing);
		}
		long current = System.currentTimeMillis();
		String uid = String.valueOf(current
				+ (current - -Adaptor.getInstance().gameStartTime));
		this.writeImmutable("my-unique-id", uid.getBytes());
		return uid;
	}

	public Vector getFileList() {
		Vector thing = Util.getFileList();
		byte[] bytes = readPackedFile("filelist");
		if (bytes != null) {
			MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(
					bytes));
			try {
				while (true) {
					thing.addElement(Adaptor.readVarChar(dis));
				}
			} catch (Exception e) {
			}
		}
		return thing;
	}

	public byte[] readPackedFile(String key) {
		byte[] bytes = null;
		try {
			key = Adaptor.replaceAll(key, "-", "_").toLowerCase();
			InputStream is = getClass().getResourceAsStream("/" + key);
			if (is != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] temp = new byte[1000];
				int len = -1;
				while ((len = is.read(temp)) != -1) {
					bos.write(temp, 0, len);
				}
				bytes = bos.toByteArray();
				is.close();
			}
		} catch (Exception t) {
			Adaptor.infor("midlet Throwable" + t.getMessage());
		}
		return bytes;
	}

	public void log(int level, String msg) {
		// TODO Auto-generated method stub
		System.out.println(msg);

	}

	public String getChannel() {
		return "";
	}

}
