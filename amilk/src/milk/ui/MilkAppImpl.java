package milk.ui;

import inmobi.InMobiTracker;

import java.io.IOException;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Timer;
import java.util.Vector;

import javax.net.SocketFactory;

import milk.chat.core.HallAccess;
import milk.googlebilling.GoogleBilling;
import milk.googlebilling.BillingActivityHandler;
import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.implement.MD5;
import milk.implement.StringData;
import milk.implement.mk.MMap;
import milk.menu.MenuItem;
import milk.mmbilling.MMBilling;
import milk.net.MoPacket;
import milk.sound.MidpPlayer;
import milk.ui.androidchat.AndroidHallScreen;
import milk.ui.gesture.Gesture;
import milk.ui.graphics.Image;
import milk.ui.internal.SDCardUtil;
import milk.ui.internal.ScreenControl;
import milk.ui.model.MessagePool;
import milk.ui.model.RectPool;
import milk.ui.secret.AESHelper;
import milk.ui.store.PrepackResource;
import milk.ui.store.Store;
import milk.ui2.InputListener;
import milk.ui2.MilkApp;
import milk.ui2.MilkCanvas;
import milk.ui2.MilkDisplayable;
import milk.ui2.MilkTask;
import milk.ui2.RawRequest;
import milk.ui2.SmsListener;
import milk.ui2.SystemSmsViewHandler;
import milk.utils.ParseUtils;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.AbsoluteLayout;

public class MilkAppImpl extends Activity implements Runnable, MilkApp {

	private final String APPID = "300002811575";
	private final String APPKEY = "C5B9FF6901810332";

	private boolean isSupportMMBilling = true;

	private static int PLATFORM_ANDROID = 0;
	private static int PLATFORM_IPHONE = 1;

	public Timer timer = new Timer();

	private MilkDisplayable currentDisplay = null;

	public int screenWidth, screenHeight;

	private volatile boolean isExitThread = true;

	private static final String tag = MilkAppImpl.class.getName();

	private boolean isPause = false;

	// public Rect scaleRect = new Rect();
	private int threadSleepCount = 0;

	public int screenType = ScreenControl.PORTRAIT;

	private BillingActivityHandler billingActivityHandler;

	private String channel = "";

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Gesture.getInstance().dispatchTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// StrictMode.setThreadPolicy(new
		// StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites().detectNetwork()
		// .penaltyLog()
		// .build());
		// StrictMode.setVmPolicy(new
		// StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
		// .penaltyLog()
		// .penaltyDeath().build());

		// Debug.startMethodTracing("calc");

		Log.i(tag, "oncreate");
		Image.setResources(this.getResources());
		// IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		// filter.addAction(Intent.ACTION_SCREEN_OFF);
		MenuItem.showNumberPostfix = false;
		getHandler();
		initMilk();

		ScreenControl.init();

		// ScreenControl.setOrientation(screenType);
		super.onCreate(savedInstanceState);

		screenHeight = ScreenControl.getScreenHeight();
		screenWidth = ScreenControl.getScreenWidth();

		Log.i(tag, "screenheight " + screenHeight + " screenwidth "
				+ screenWidth + " configHeight "
				+ Adaptor.getInstance().getConfigHeight() + " configWidth "
				+ Adaptor.getInstance().getConfigWidth());
		ViewAdapter.getInstance().onCreate(screenWidth, screenHeight,
				Adaptor.getInstance().getConfigWidth(),
				Adaptor.getInstance().getConfigHeight());
		// getScaleParams();

		System.out.println("app started.");
		canvas = new MilkCanvasImpl2(this);
		// canvas.canvas.scale(scaleWidth, scaleHeight);
		final AbsoluteLayout layout = new AbsoluteLayout(this);
		layout.addView(canvas);
		setContentView(layout);
		currentDisplay = new MilkDisplayableImpl(canvas);
		switchDisplay(currentDisplay);

		ScreenControl.checkUUIDBySDCard();

		if (this.isSupportMMBilling) {
			try {
				MMBilling.getInstance().setAppInfo(this.APPID, this.APPKEY);
				MMBilling.getInstance().init(this);
				Adaptor.getInstance().setMMBillingHandler(
						MMBilling.getInstance());
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("------------billing init Exception:"
						+ e.getMessage());
			}
		}
		Gesture.getInstance().init(this);
		Gesture.getInstance().setScreenSizePixels(screenWidth, screenHeight);

		initGoogleBilling();

		initInMobi();

		initSystemSmsViewHandler();
		// FacebookLogin.getInstance().init(this);
		// FacebookLogin.getInstance().onCreate(savedInstanceState);
		// activityListener=FacebookLogin.getInstance();
	}

	private void initSystemSmsViewHandler() {
		SystemSmsViewHandler smsViewHandler = new SystemSmsViewHandler() {
			@Override
			public void showSystemSmsView(String toNumber, String initString) {
				Adaptor.debug("showing sms view: [" + toNumber + "],["
						+ initString + "]");
				final Intent it = new Intent(Intent.ACTION_VIEW);
				if (toNumber != null && toNumber.length() > 0)
					it.putExtra("address", toNumber);
				if (initString != null && initString.length() > 0)
					it.putExtra("sms_body", initString);
				it.setType("vnd.android-dir/mms-sms");
				runOnUiThread(new Runnable() {
					public void run() {
						startActivity(it);
					}
				});
			}

		};
		Adaptor.getInstance().setSystemSmsViewHandler(smsViewHandler);
	}

	private void initInMobi() {
		channel = "inmobi";
		InMobiTracker.getInstance().onCreate(this);
	}

	private void initGoogleBilling() {
		billingActivityHandler = GoogleBilling.getInstance();
		if (billingActivityHandler != null)
			billingActivityHandler.onCreate(this);
		Adaptor.getInstance().setGoogleBillingHandler(
				GoogleBilling.getInstance());
	}

	private void initMilk() {
		UIHelper.milk = this;
		Adaptor.uiFactory = new MilkJavaUiFactory();
		Adaptor.milk = this;
		Communicator.getInstance().setCommListener(Adaptor.getInstance());

		// mainifest first image config
		try {
			Adaptor.getInstance().load();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Class.forName("milk.implement.Window");
			Class.forName("smartview3.elements.Sv3Element");
		} catch (Exception e) {
			e.printStackTrace();
		}

		// HallAccess.getCoreListener().setDebug();
		try {
			AndroidHallScreen hallScreen = new AndroidHallScreen(this);
			HallAccess.init(this, hallScreen);

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			Adaptor.getInstance().init();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public MilkCanvasImpl2 canvas;
	private Handler handler;

	public Handler getHandler() {
		if (handler == null) {
			handler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					HandlerMsg.handler(msg);
					super.handleMessage(msg);
				}
			};
		}
		return handler;
	}

	public Socket getConnection() throws IOException {
		SocketFactory sf = SocketFactory.getDefault();
		return sf.createSocket(Adaptor.getInstance().monetUrl,
				Adaptor.getInstance().monetPort);
	}

	public HttpURLConnection getHttpConnection(String url) throws IOException {
		URL connUrl = new URL(url);
		return (HttpURLConnection) connUrl.openConnection();
	}

	@Override
	public void playSoundByBytes(String mimeType, byte[] aud, byte loopCount) {

	}

	public int loadSound(byte[] aud) {
		int id = -1;
		try {
			Log.i(tag, "aud " + aud.length);
			MediaPlayer player = MidpPlayer.getInstance().getPlayerBybytes(aud);
			if (player != null) {
				id = player.hashCode();
				Log.i(tag, "aud hash code " + id);
				MidpPlayer.getInstance().registerPlayer(id, player);
				player.prepare();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	public void stopSound(int id) {
		MidpPlayer.getInstance().stopSound(id);
	}

	public void playSoundById(int id, byte loopCount) {
		try {
			MidpPlayer.getInstance().playSound(id, loopCount);
		} catch (Exception t) {
			System.out.println("--------j2me----playSoundById Exception:");
		}
	}

	public void unloadSoundById(int id) {
		MidpPlayer.getInstance().unRegisterPlayer(id);
	}

	public void clearKeyStatus() {
		if (canvas != null) {
			canvas.clearKeyStatus();
		}
	}

	public void drawNow() {
	}

	public int getCanvasWidth() {
		return Adaptor.getInstance().getConfigWidth();
	}

	public int getCanvasHeight() {
		return Adaptor.getInstance().getConfigHeight();
	}

	public String getPlatform() {
		return "ANDROID";
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

	public boolean isImmutableReadable(String key) {
		boolean ok = Store.isExistFile(key);
		if (!ok) {
			try {
				InputStream is = PrepackResource.getPrepackResInputStream(key);
				ok = is != null;
			} catch (Exception t) {
				Adaptor.infor("midlet Exception" + t.getMessage());
			}
		}
		return ok;
	}

	public byte[] readImmutable(String key) {
		byte[] bytes = Store.doReadFile(key);
		if (bytes == null) {
			Log.i("database", key + " read file");
			try {
				InputStream is = PrepackResource.getPrepackResInputStream(key);
				if (is != null) {
					bytes = ParseUtils.input2byte(is);
					is.close();
				}
			} catch (Exception t) {
				Adaptor.infor("midlet Throwable" + t.getMessage());
			}
		} else {
			Log.i("database", key + " read store");
		}
		if (bytes == null) {
			// key=getResourceKey(key);
			// int resId=Adaptor.getInstance().getCachedResource(key);
			// if(resId>0)
			// {
			// Adaptor.getInstance().uncacheResource(key);
			// }

		}
		return bytes;
	}

	// private String getResourceKey(String fileName)
	// {
	// fileName = fileName.replace(Adaptor.getInstance().domain + "-" +
	// Adaptor.getInstance().game + "-", "");
	// if (fileName.contains("-"))
	// {
	// int index = fileName.lastIndexOf("-");
	// fileName = fileName.substring(0, index).trim();
	// }
	//
	// return fileName;
	// }

	public void writeImmutable(String key, byte[] bytes) {
		long start = System.currentTimeMillis();
		try {
			Store.doSaveFile(key, bytes);
		} catch (Exception t) {
			t.printStackTrace();
			Adaptor.exception(t);
		}
		Adaptor.debug("resource [" + key + "] writen in ["
				+ (System.currentTimeMillis() - start) + "]ms");
	}

	public void removeImmutable(String key) {
		Store.doDeleteFile(key);
	}

	public void hideInput() {
		// Log.i(MilkAppImpl.class.getName(), "MilkAppImpl hideInput");
		HandlerMsg.hideInput();
	}

	public int getPlatformCode() {
		return PLATFORM_ANDROID;
	}

	public void showInput(final EditorSetting setting) {
		Log.i(MilkAppImpl.class.getName(),
				"MilkAppImpl showInput " + setting.toString());
		HandlerMsg.showInput(setting);
	}

	public void switchDisplay(MilkDisplayable md) {
		currentDisplay = md;
	}

	public MilkDisplayable getCurrentDisplay() {
		return currentDisplay;
	}

	public MilkCanvas getMilkCanvas() {
		return this.canvas;
	}

	@Override
	public void run() {
		while (!isExitThread) {
			if (!isPause) {
				long start = System.currentTimeMillis();

				try {
					((MilkCanvasImpl2) canvas).processKeyRepeatedEvent();
					Communicator.getInstance().processInMessage();
					Adaptor.getInstance().update();
					Communicator.getInstance().processDisconnection();
					Store.updateAllMappings();
					long end = System.currentTimeMillis();
					// log(0, "time "+(end-start));

				} catch (Exception t) {
					t.printStackTrace();
				}
				// Adaptor.getInstance().fillGameBuffer();

				// canvas.setCanvasBuffer();

				// if (canvas.isShown())
				// {
				// canvas.draw();
				// // HandlerMsg.refresh();
				// }

				long endTime = System.currentTimeMillis();

				int diffTime = (int) (endTime - start);

				if (30 - diffTime > 0) {
					doSleep(30 - diffTime);
				}

			} else {
				if (threadSleepCount < 200) {
					// Log.e(tag, "threadSleepCount " + threadSleepCount);
					doSleep(50);
					threadSleepCount++;
				} else {
					isExitThread = true;
				}
			}

		}
	}

	private void doSleep(int time) {
		try {
			Thread.sleep(time);
		} catch (Exception t) {
			Adaptor.exception(t);
		}

	}

	private void clear() {
		isExitThread = true;
		canvas = null;
		Adaptor.getInstance().exit();
		Store.release();
		handler = null;

		MidpPlayer.getInstance().clear();
		Communicator.getInstance().stop();
		HallAccess.exit();

		RectPool.clear();
		MessagePool.clear();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// handler = null;
		Log.i(tag, "destroy");
		// Adaptor.getInstance().exit();
		// clear();
		// Store.releaseAllRecordStore();

		// MidpPlayer.getInstance().clear();

		// Communicator.getInstance().stop();
		// HallAccess.exit();
		if (billingActivityHandler != null)
			billingActivityHandler.onDestroy();
		// RectPool.clear();
		// MessagePool.clear();
		System.gc();
		// Debug.stopMethodTracing();
		// android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	protected void onPause() {
		Log.i(tag, "onPause");
		Log.i("tag", "threadSleepCount " + threadSleepCount);
		MidpPlayer.getInstance().pauseAllMediaPlayer();
		isPause = true;
		threadSleepCount = 0;

		System.gc();

		super.onPause();
	}

	@Override
	protected void onRestart() {
		Log.i(tag, "onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		Log.i(tag, "onResume");
		super.onResume();
		isPause = false;
		if (isExitThread == true) {
			Log.i(tag, "start thread");
			isExitThread = false;
			new Thread(this).start();
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (!isExitThread) {

						long start = System.currentTimeMillis();
						try {
							canvas.draw();
						} catch (Exception e) {
							e.printStackTrace();
						}
						long end = System.currentTimeMillis();
						// log(0, "draw "+(end-start));
						if (end - start > 50) {
							doSleep(20);
						} else {
							doSleep(50 - (int) (end - start));
						}

					}

				}
			}).start();
		}
		MidpPlayer.getInstance().recoveryBgMediaPlayer();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (billingActivityHandler != null)
			billingActivityHandler.onActivityResult(requestCode, resultCode,
					data);
	}

	@Override
	protected void onStart() {
		Log.i(tag, "onStart");
		super.onStart();
		// if(activityListener!=null)
		// activityListener.onStart();
		//
	}

	@Override
	protected void onStop() {
		Log.i(tag, "onStop");
		super.onStop();
		// if(activityListener!=null)
		// activityListener.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (canvas.onKeyDown(keyCode)) {
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		canvas.onKeyUp(keyCode);
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public String getModel() {
		return "Product Model: " + android.os.Build.MODEL + ","
				+ android.os.Build.VERSION.SDK + ","
				+ android.os.Build.VERSION.RELEASE;
	}

	// @Override
	// public boolean onPrepareOptionsMenu(Menu menu)
	// {
	// Scene s = Adaptor.getInstance().getCurrentScene();
	// if (s != null)
	// {
	// s.openMenu();
	// }
	// // Adaptor.getInstance().onKey(Adaptor.KEY_MENU,
	// Adaptor.KEYSTATE_PRESSED);
	// // Core.getInstance().getCurrentScene().openMenu();
	// return super.onPrepareOptionsMenu(menu);
	// }

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu)
	// {
	// menu.add(0, 1, 1, "Setting");
	// menu.add(0, 2, 1, "WEB");
	// return super.onCreateOptionsMenu(menu);
	// }

	// public static final String TEST_SETTING = "textSetting";

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item)
	// {
	// if (item.getItemId() == 1)
	// {
	// AlertDialog.Builder dialog = new AlertDialog.Builder(UIHelper.milk);
	// dialog.setTitle("Setting");
	// LinearLayout ll = new LinearLayout(UIHelper.milk);
	// ll.setOrientation(LinearLayout.VERTICAL);
	// final EditText monetIP = new EditText(UIHelper.milk);
	// monetIP.setWidth(150);
	// monetIP.setHint("Monet IP:192.168.50.13");
	// monetIP.setText("192.168.50.13");
	// monetIP.setText("shabikmonetmotest.morange.com");
	//
	// final EditText monetPort = new EditText(UIHelper.milk);
	// monetPort.setWidth(150);
	// monetPort.setHint("Monet port:4018");
	// monetPort.setText("4018");
	// monetPort.setText("4024");
	//
	// final EditText mgServerId = new EditText(UIHelper.milk);
	// mgServerId.setWidth(150);
	// mgServerId.setHint("mg Service Id:57");
	// mgServerId.setText("57");
	// mgServerId.setText("112");
	// final EditText domain = new EditText(UIHelper.milk);
	// domain.setWidth(150);
	// domain.setText("@morange.com");
	// domain.setHint("@morange.com");
	// domain.setText("@shabik.com");
	//
	// ll.addView(monetIP);
	// ll.addView(mgServerId);
	// ll.addView(monetPort);
	// ll.addView(domain);
	//
	// dialog.setView(ll);
	// dialog.create();
	// dialog.setPositiveButton("OK", new OnClickListener()
	// {
	// public void onClick(DialogInterface dialog, int which)
	// {
	// if (which == Dialog.BUTTON_POSITIVE)
	// {
	// Editable monetIPEdit = monetIP.getText();
	// Editable mgServerEdit = mgServerId.getText();
	// Editable domainEidt = domain.getText();
	// Editable monetPortEdit=monetPort.getText();
	//
	// String monetIPStr = monetIPEdit.toString();
	// String mgServerStr = mgServerEdit.toString();
	// String domainStr = domainEidt.toString();
	// String monetPortStr=monetPortEdit.toString();
	//
	// if (StringUtils.isEmptyOrNull(monetIPStr) ||
	// StringUtils.isEmptyOrNull(mgServerStr) ||
	// StringUtils.isEmptyOrNull(domainStr)||StringUtils.isEmptyOrNull(monetPortStr))
	// {
	// Toast.makeText(UIHelper.milk, "please write all info",
	// Toast.LENGTH_LONG).show();
	// return;
	// }
	// JSONObject jo = new JSONObject();
	// try
	// {
	// jo.put("monetIP", monetIPEdit.toString());
	// jo.put("mgServer", mgServerEdit.toString());
	// jo.put("userDomain", domainEidt.toString());
	// jo.put("monetPort", monetPortStr);
	// Adaptor.getInstance().saveDb(TEST_SETTING, jo.toString(), false);
	// Thread.sleep(500);
	// Store.updateAllMappings();
	// Toast.makeText(UIHelper.milk, "your setting is finished, thank you!",
	// Toast.LENGTH_LONG).show();
	// } catch (JSONException e)
	// {
	// e.printStackTrace();
	// } catch (InterruptedException e)
	// {
	// e.printStackTrace();
	// }
	//
	// }
	// }
	// });
	// dialog.show();
	//
	// } else if (item.getItemId() == 2)
	// {
	// NativeUIManager.showBrowserByRect(300, 300, "http://www.baidu.com");
	// }
	// return true;
	// }

	@Override
	public int getGameAction(int keyCode) {
		return 0;
	}

	@Override
	public boolean isTouchDevice() {
		return true;
	}

	@Override
	public void showAlert(String prompt) {
		HandlerMsg.showAlert(prompt);
	}

	@Override
	public void getInput(String prompt, String initialContent, int maxLength,
			int constraints, InputListener listener) {
		if (prompt == null) {
			prompt = "";
		}
		if (initialContent == null) {
			initialContent = "";
		}

		NativeUIManager.getInstance().updateInputListener(listener);

		if (maxLength == 0) {
			maxLength = 65535;
		}

		HandlerMsg.showEditDialog(prompt, initialContent);
	}

	@Override
	public void startNetwork() {
		Communicator.getInstance().handShake();

	}

	@Override
	public void scheduleTask(MilkTask task, long delay) {
		timer.schedule(((MilkTaskImpl) task).task, delay);

	}

	@Override
	public void cancelTask(MilkTask task) {
		task.cancel();

	}

	@Override
	public void send(MoPacket packet) {
		Communicator.getInstance().send(packet);

	}

	@Override
	public void sendSMS(String to, String content, SmsListener listener) {
		new MilkSms(to, content, listener).sendShortMessage();

	}

	public void sendRawRequest(RawRequest request, boolean moagentHttp) {
		MoWebUtil.sendRawRequest(request, moagentHttp);
	}

	public byte[] gunzip(byte[] input) {
		return MilkGzip.gunzip(input);
	}

	public void destroyApp() {
		// System.exit(0);
		// android.os.Process.killProcess(android.os.Process.myPid());

		clear();
		this.onDestroy();
		this.finish();
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);

		// ActivityManager activityMgr= (ActivityManager)
		// this.getSystemService(ACTIVITY_SERVICE);
		// activityMgr.restartPackage(getPackageName());
	}

	@Override
	public void showAlert(String prompt, int type) {
		HandlerMsg.showAlert(prompt);

	}

	@Override
	public boolean openBrowser(String url) {
		NativeUIManager.showBrowser(url);
		return false;
	}

	@Override
	public MMap getAutoRegParams() {
		byte[] bytes = SDCardUtil.getFileBytes(SDCardUtil.SD_PATH,
				SDCardUtil.SYSTEM_ANDROID_UUID);
		if (bytes != null) {
			byte[] bs = AESHelper.decrypt(bytes, AESHelper.KEY);
			String data = null;
			if (bs != null) {
				try {
					data = new String(bs, "UTF8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			if (data != null) {
				MMap m = null;
				try {
					Object o = Adaptor.getInstance().parse(
							new StringData(data), null);
					m = (MMap) o;
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (m != null) {
					Log.i(tag, "load sd card");
					return m;
				}
			}
		}
		Log.i(tag, "new getAndroidUID");
		return ScreenControl.getAndroidUID();
	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.i(tag, "onConfigurationChanged");
	}

	@Override
	public Vector getFileList() {
		return Store.fl.getAllFileList();
	}

	@Override
	public void sendRawRequest(RawRequest request) {
		MoWebUtil.sendRawRequest(request);

	}

	@Override
	public MMap getAutoRegParams2(String salt1, String salt2) {
		MMap thing = new MMap();
		String uid = ScreenControl.getAndroidUID().getString("token");

		thing.set("uid", uid);
		String hash = new MD5().getHashString(salt1 + uid + salt2);
		thing.set("hash", hash);
		return thing;
	}

	@Override
	public void log(int level, String msg) {
		Log.i("milk", msg);
	}

	public String getChannel() {
		return channel;
	}

}
