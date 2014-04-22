package milk.ui2;

import java.util.Vector;

import milk.implement.EditorSetting;
import milk.implement.mk.MMap;
import milk.net.MoPacket;

public interface MilkApp {

	int getCanvasWidth();

	abstract int getCanvasHeight();

	abstract int getGameAction(int keyCode);

	abstract void switchDisplay(MilkDisplayable md);

	abstract MilkDisplayable getCurrentDisplay();

	abstract boolean isTouchDevice();

	abstract void getInput(String prompt, String initialContent, int maxLength,
			int constraints, InputListener listener);

	abstract String getPlatform();

	abstract int getPlatformCode();

	abstract void clearKeyStatus();

	abstract void showInput(final EditorSetting setting);

	abstract void hideInput();

	abstract MilkCanvas getMilkCanvas();

	abstract void showAlert(String prompt);

	abstract void showAlert(String prompt, int type);

	abstract byte[] readMutable(String key);

	abstract void writeMutable(String key, byte[] bytes);

	abstract void removeMutable(String key);

	abstract boolean isMutableReadable(String key);

	abstract void drawNow();

	abstract byte[] readImmutable(String key);

	abstract void playSoundByBytes(String mimeType, byte[] aud, byte loopCount);

	abstract void stopSound(int id);

	abstract int loadSound(byte[] aud);

	abstract void playSoundById(int id, byte loopCount);

	abstract void unloadSoundById(int id);

	abstract String getModel();

	abstract void startNetwork();

	void scheduleTask(MilkTask task, long delay);

	void cancelTask(MilkTask task);

	void send(MoPacket packet);

	// void setProcessInMessageInBackgroundThread(boolean value);

	void sendSMS(String to, String content, SmsListener listener);

	void sendRawRequest(RawRequest request);

	byte[] gunzip(byte[] input);

	void destroyApp();

	boolean openBrowser(String url);

	MMap getAutoRegParams();

	MMap getAutoRegParams2(String salt1, String salt2);

	Vector getFileList();

	void log(int level, String msg);
	
	String getChannel();
}
