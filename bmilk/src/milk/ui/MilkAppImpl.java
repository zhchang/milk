package milk.ui;

import java.io.ByteArrayInputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Timer;
import java.util.Vector;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;

import milk.ui.bbchat.BBHallScreen;
import milk.chat.core.HallAccess;
import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.implement.MD5;
import milk.implement.MilkInputStream;
import milk.implement.mk.MMap;
import milk.net.MoPacket;
import milk.ui2.InputListener;
import milk.ui2.MilkApp;
import milk.ui2.MilkCanvas;
import milk.ui2.MilkDisplayable;
import milk.ui2.MilkTask;
import milk.ui2.RawRequest;
import milk.ui2.SmsListener;
import net.rim.blackberry.api.browser.Browser;
import net.rim.device.api.io.transport.ConnectionDescriptor;
import net.rim.device.api.io.transport.ConnectionFactory;
import net.rim.device.api.io.transport.TransportInfo;
import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;

public class MilkAppImpl extends UiApplication implements Runnable,
		CommandListener, MilkApp {

	public Timer timer = new Timer();

	private MilkCanvasImpl canvas;
	private MilkDisplayable currentDisplay = null;

	private Hashtable audioPlayers = new Hashtable();

	private boolean exitMIDlet;

	private TextBox textBox;
	private InputListener inputListener;

	public void activate() {
		super.activate();
		Adaptor.getInstance().sendExceptionReport("foreground");
	}

	public void deactivate() {
		super.deactivate();
		Adaptor.getInstance().sendExceptionReport("background");
	}

	public void getInput(final String prompt, final String initialContent,
			final int maxLength, final int constraints,
			final InputListener listener) {
		this.invokeLater(new Runnable() {

			public void run() {
				// try {
				Dialog query = new Dialog(Dialog.D_OK, prompt, 0, null,
						Screen.FIELD_HCENTER);
				EditField edit = new EditField();
				query.add(edit);
				edit.setText(initialContent);
				int max = maxLength;
				if (max <= 0) {
					max = 65536;
				}
				edit.setMaxSize(max);
				edit.setEditable(true);
				edit.setFocus();
				query.doModal();
				listener.onInput(false, edit.getText());
				// } catch (Exception e) {
				// e.printStackTrace();
				// listener.onInput(false, null);
				// }
			}
		});

	}

	public void showAlert(String prompt, int type) {
		int user = Dialog.ask(Dialog.D_OK_CANCEL, prompt);
		if (user == Dialog.CANCEL) {
			destroyApp();
		} else if (user == Dialog.OK) {
		}
	}

	public void showAlert(final String prompt) {
		invokeLater(new Runnable() {
			public void run() {
				Dialog.alert(prompt);
			}
		});
	}

	public MilkCanvas getMilkCanvas() {
		return this.canvas;
	}

	public MilkDisplayable getCurrentDisplay() {
		return currentDisplay;
	}

	public void switchDisplay(final MilkDisplayable md) {
		// Object lock = Application.getEventLock();
		// synchronized (lock) {
		// try {
		// popScreen(currentDisplay.screen);
		// } catch (Exception e) {
		// System.out
		// .println("===========================error in switching display=========================");
		// }
		// currentDisplay = md;
		// pushScreen(currentDisplay.screen);
		// }
		invokeLater(new Runnable() {
			public void run() {
				try {
					popScreen(((MilkDisplayableImpl) currentDisplay).screen);
				} catch (Exception e) {
					System.out
							.println("===========================error in switching display=========================");
				}
				currentDisplay = md;
				pushScreen(((MilkDisplayableImpl) currentDisplay).screen);

			}
		});

	}

	public void run() {

		Adaptor.getInstance().sendExceptionReport("game starts");
		// to speed up window loading process
		try {
			Class.forName("milk.implement.Window");
			Class.forName("smartview3.elements.Sv3Element");
		} catch (Exception e) {
		}

		Communicator.getInstance().setCommListener(Adaptor.getInstance());
		Adaptor.getInstance().load();
		canvas = new MilkCanvasImpl();
		canvas.init();
		currentDisplay = new MilkDisplayableImpl(canvas);
		// System.out.println("app started.");

		switchDisplay(currentDisplay);

		exitMIDlet = false;

		BBHallScreen hallScreen;
		try {
			hallScreen = new BBHallScreen(this);
			HallAccess.init(this, hallScreen);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Adaptor.getInstance().init();
		} catch (Exception e) {

		}
		while (!exitMIDlet) {
			long start = System.currentTimeMillis();

			try {
				canvas.processKeyRepeatedEvent();
				Communicator.getInstance().processInMessage();
				Adaptor.getInstance().update();
				Communicator.getInstance().processDisconnection();
				Util.updateAllMappings();
				// Adaptor.getInstance().fillGameBuffer();
			} catch (Exception t) {
				// t.printStackTrace();
			}
			int take = (int) (System.currentTimeMillis() - start);
			int sleepTime = 80 - take;
			if (sleepTime < 30) {
				sleepTime = 30;
			}
			doSleep(sleepTime);

		}

		Communicator.getInstance().stop();
		try {
			HallAccess.exit();
			hallScreen = null;
		} catch (Exception e) {

		}
		this.destroyApp(true);
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
	}

	public MilkAppImpl() {
		Adaptor.milk = this;
		Adaptor.uiFactory = new MilkJavaUiFactory();
		new Thread(this).start();
		new Thread(new Runnable() {

			public void run() {
				while (!exitMIDlet) {
					drawNow();
					try {
						Thread.sleep(50);
					} catch (Exception e) {
					}
				}
			}
		}).start();
	}

	private void clear() {
		currentDisplay = null;
	}

	protected void destroyApp(boolean b) {
		Adaptor.getInstance().exit();
		clear();
		this.invokeLater(new Runnable() {

			public void run() {
				try {
					if (canvas != null) {
						canvas.close();
					}
					System.gc();
				} catch (Exception e) {
					System.gc();
				} finally {
					System.exit(0);
				}
			}

		});

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
		if (canvas != null) {
			canvas.repaint();
		}
	}

	public int getCanvasWidth() {
		return Adaptor.getInstance().getConfigWidth();
	}

	public int getCanvasHeight() {
		return Adaptor.getInstance().getConfigHeight();
	}

	public byte[] readImmutable(String key) {

		byte[] bytes = Util.readFile(key);
		if (bytes == null) {
			bytes = readPackedFile(key);
		}
		return bytes;
	}

	public void writeImmutable(String key, byte[] bytes) {
		// long start = System.currentTimeMillis();
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

	private static final String amrMimeType = "audio/amr";

	public int loadSound(final byte[] aud) {
		try {
			Player player = Manager.createPlayer(new ByteArrayInputStream(aud,
					0, aud.length), amrMimeType);
			player.realize();
			player.prefetch();
			int id = player.hashCode();
			audioPlayers.put(new Integer(id), player);
			return id;

		} catch (Exception t) {
			System.out.println("--------BB----loadSound Exception:");
			return -1;
		}
	}

	public void playSoundById(final int id, final byte loopCount) {
		try {
			Player player = (Player) audioPlayers.get(new Integer(id));
			if (player != null && player.getState() != Player.STARTED) {
				if (loopCount > 0 || loopCount == -1)
					player.setLoopCount(loopCount);
				player.start();
			}
		} catch (Exception t) {
			System.out.println("--------BB----playSoundById Exception:" + id);
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

	public void removeImmutable(String key) {
		Util.deleteFile(key);
	}

	public void commandAction(Command c, Displayable d) {
		String contents = null;
		boolean cancelled = false;
		System.out.println("commandAction called");

		if (textBox == d) {
			if (c.getCommandType() == Command.OK) {
				contents = textBox.getString().trim();
				MilkDisplayableImpl md = new MilkDisplayableImpl(canvas);
				switchDisplay(md);
			} else if (c.getCommandType() == Command.CANCEL) {
				cancelled = true;
				MilkDisplayableImpl md = new MilkDisplayableImpl(canvas);
				switchDisplay(md);
			}
			if (inputListener != null) {
				if (contents == null) {
					contents = "";
				}
				inputListener.onInput(cancelled, contents);
			}
		}

	}

	public static void main(String[] args) {

		System.gc();
		MilkAppImpl theApp = new MilkAppImpl();
		theApp.enterEventDispatcher();
		System.gc();
	}

	public int getGameAction(int keyCode) {
		return 0;
	}

	public void clearKeyStatus() {
		canvas.clearKeyStatus();
	}

	public SocketConnection getConnection() throws IOException {
		String serverUrl = "socket://" + Adaptor.getInstance().monetUrl + ":"
				+ Adaptor.getInstance().monetPort;
		// return (SocketConnection) Connector.open(serverUrl);

		// return (SocketConnection) openConnection(serverUrl);

		ConnectionFactory connFactory = new ConnectionFactory();
		connFactory.setConnectionTimeout(5000);
		connFactory.setTimeoutSupported(true);
		connFactory.setPreferredTransportTypes(preferredTransportTypes);
		ConnectionDescriptor cd = connFactory.getConnection(serverUrl);

		SocketConnection sc = (SocketConnection) cd.getConnection();

		Adaptor.getInstance().display = String.valueOf(cd
				.getTransportDescriptor().getTransportType());
		return sc;
	}

	private static int[] preferredTransportTypes = {
			TransportInfo.TRANSPORT_TCP_WIFI,
			TransportInfo.TRANSPORT_TCP_CELLULAR, TransportInfo.TRANSPORT_WAP2,
			TransportInfo.TRANSPORT_WAP, TransportInfo.TRANSPORT_BIS_B,
			TransportInfo.TRANSPORT_MDS, };//

	private static Connection openConnection(String url) throws IOException {
		try {
			// DEFAULT
			String connUrl = url + ";deviceside=true";
			String sbUID = getSBUID(true);
			if (sbUID != null && sbUID.length() > 0) {
				connUrl += ";ConnectionUID=" + sbUID;
			}
			Adaptor.getInstance().display = "default";
			return (Connection) Connector.open(connUrl);
		} catch (Exception e) {
		}
		try {
			// CUSTOM
			Adaptor.getInstance().display = "custom";
			return (Connection) Connector.open(url + ";deviceside=true");
		} catch (Exception e) {
		}
		try {
			// BIS
			String connUrl = url
					+ ";deviceside=false;ConnectionType=mds-public";
			String sbUID = getSBUID(true);
			if (sbUID != null && sbUID.length() > 0) {
				connUrl += ";ConnectionUID=" + sbUID;
			}
			Adaptor.getInstance().display = "bis";
			return (Connection) Connector.open(connUrl);
		} catch (Exception e) {
		}
		try {
			// BIS_NO_APN
			return (Connection) Connector.open(url
					+ ";deviceside=false;ConnectionType=mds-public");
		} catch (Exception e) {
		}
		try {
			// BIS_WIFI
			Adaptor.getInstance().display = "wifi";
			return (Connection) Connector.open(url
					+ ";deviceside=false;ConnectionType=wifi");
		} catch (Exception e) {
		}
		try {
			// BIS_APN
			Adaptor.getInstance().display = "bis_apn";
			return (HttpConnection) Connector.open(url
					+ ";deviceside=false;apn=jawalnet.com.sa");
		} catch (Exception e) {
		}
		try {
			Adaptor.getInstance().display = "factory";
			return openConnectionFromFactory(url);
		} catch (Exception e) {
		}
		throw new IOException("all http connection methods failed.");
	}

	protected static String getSBUID(boolean forBIS) {
		ServiceBook sb = ServiceBook.getSB();

		ServiceRecord[] records = sb.getRecords();// sb.findRecordsByCid("WPTCP");

		String uid = null;
		for (int i = 0; i < records.length; i++) {
			ServiceRecord r = records[i];
			// Search through all service records to find the
			// valid non-Wi-Fi and non-MMS
			// WAP 2.0 Gateway Service Record.
			if (r.isValid() && !r.isDisabled()) {
				String _uid = r.getUid().toLowerCase();
				String _cid = r.getCid().toLowerCase();
				String _name = r.getName().toLowerCase();
				// MORANGE.log("BB.GSBU: (" + _uid + ") (" + _cid + ") (" +
				// _name + ")");
				if (forBIS && _cid.indexOf("ippp") >= 0
						&& _name.indexOf("bibs") >= 0) {
					return r.getUid();
				}
				// We can't use WiFi, MMS, or WAP.
				if (!forBIS
						&& (_cid.indexOf("wptcp") >= 0
								&& _uid.indexOf("wifi") < 0 && _uid
								.indexOf("mms") < 0)) {
					// BIBS has higher priority than WAP.
					if (_uid.indexOf("bibs") >= 0)
						return r.getUid(); // Must get the original one. _uid is
											// lowercase.
					// Only note down WAP and return it at last. This is in
					// order to use BIBS whenever possible.
					// And only use WAP as the last resort.
					// Because not all WAP works for Internet connection.
					else if (_uid.indexOf("wap") >= 0)
						uid = r.getUid(); // Must get the original one. _uid is
											// lowercase.
				}
			}
		}
		return uid;
	}

	private static HttpConnection openConnectionFromFactory(String url)
			throws IOException {

		HttpConnection con = null;
		// if(DeviceInfo.isSimulator()){
		// con = (HttpConnection) Connector.open(url + ";deviceside=true");
		// }else{
		// con = (HttpConnection) Connector.open(url +
		// ";deviceside=true;interface=wifi");
		// // con = (HttpConnection) Connector.open(url + ";deviceside=true",
		// Connector.READ_WRITE);
		// }

		// Create ConnectionFactory
		ConnectionFactory factory = new ConnectionFactory();
		factory.setConnectionTimeout(5000);
		// factory.setTransportTypeOptions(TransportInfo.TRANSPORT_WAP2, new
		// TcpCellularOptions());
		factory.setPreferredTransportTypes(preferredTransportTypes);
		factory.setTimeoutSupported(true);
		// use the factory to get a connection
		ConnectionDescriptor conDescriptor = factory.getConnection(url);// �����κβ���
		if (conDescriptor != null) {
			// connection succeeded
			int transportUsed = conDescriptor.getTransportDescriptor()
					.getTransportType();
			// using the connection
			con = (HttpConnection) conDescriptor.getConnection();
		}
		con.setRequestProperty("Accept", "*/*");
		return con;
	}

	public HttpConnection getHttpConnection(String url) throws IOException {

		// ConnectionFactory connFactory = new ConnectionFactory();
		// ConnectionDescriptor cd = connFactory.getConnection(url);
		// return (HttpConnection) cd.getConnection();
		return openConnectionFromFactory(url);

	}

	public void showInput(final EditorSetting setting) {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.showEdit(setting);
				}
			}
		});

	}

	public void hideInput() {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.hideInput();
				}
			}
		});
	}

	public void clearInputText() {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.clearInputText();
				}
			}
		});
	}

	public void setInputText(final String text) {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.setInitInputText(text);
				}
			}
		});
	}

	public void setEditFocus() {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.setEditFocus();
				}
			}
		});
	}

	public void insertInputText(final String text) {
		this.invokeLater(new Runnable() {
			public void run() {
				if (canvas != null) {
					canvas.insertInputText(text);
				}
			}
		});
	}

	public String getPlatform() {
		return "BB";
	}

	public int getPlatformCode() {
		return 4;
	}

	public String getModel() {
		return DeviceInfo.getDeviceName();
	}

	public boolean isTouchDevice() {
		return VirtualKeyboard.isSupported();
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
		try {
			Browser.getDefaultSession().displayPage(url);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public MMap getAutoRegParams() {
		String time = String.valueOf(System.currentTimeMillis());
		String token = SecurityUtil.getUUid();
		String aes = new MD5().getHashString(SecurityUtil.encryptAES(time
				+ token + time));
		MMap map = new MMap();
		map.set("time", time);
		map.set("token", token);
		map.set("aes", aes);
		System.out.println(map.getString("time"));
		System.out.println(map.getString("token"));
		System.out.println(map.getString("aes"));
		String thing = map.toString();
		System.out.println(thing);
		System.out.println(map.toString());
		return map;
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

	public MMap getAutoRegParams2(String salt1, String salt2) {
		MMap thing = new MMap();
		String uid = SecurityUtil.getUUid();

		thing.set("uid", uid);
		String hash = new MD5().getHashString(salt1 + uid + salt2);
		thing.set("hash", hash);
		return thing;
	}

	public void log(int level, String msg) {
		System.out.println(msg);
	}

	public String getChannel() {
		return "";
	}

}
