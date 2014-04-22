package milk.implement;

public class SetScene extends Scene {

	// private static final int titleColor = 0xffffff;
	// private static final int itemColor = 0xffffff;
	// private static final int itemColorFocus = 0xff0000;
	// private static final int frameColorFocus = 0xff0000;
	//
	// private static final int STATE_SET_SOUND = 1;
	// private static final int STATE_SET_LANG = 2;
	// private static final int STATE_SET_IMAGE = 3;
	// private int state = STATE_SET_SOUND;
	//
	// private int soundFlag = 0;
	// private int langFlag = 0;
	// private int imageFlag = 0;
	//
	// private SetItem soundSet, langSet, imageSet;
	// private MilkFont setFont = UIHelper.getDefaultFont();
	//
	// private int ItemHeight = setFont.getHeight();
	// private int screenW, screenH;
	//
	// private Scene backScene;
	//
	// private String leftKey = "Ok", rightKey = "Back";
	//
	// private SetEventListener lisetner;
	//
	// public SetScene(int width, int height) {
	//
	// screenW = width;
	// screenH = height;
	//
	// initSetting();
	// }
	//
	// public void SetLisetner(SetEventListener l) {
	// lisetner = l;
	// }
	//
	// private void initSetting() {
	//
	// String soundItem[] = { "on", "off" };
	// soundSet = new SetItem("Sound", soundItem);
	// soundSet.isFocus = true;
	//
	// String langItem[] = { "english", "chinese" };
	// langSet = new SetItem("Language", langItem);
	//
	// String imageItem[] = { "high", "normal", };
	// imageSet = new SetItem("Image Quality", imageItem);
	//
	// if (isExistSettingRms()) {
	//
	// loadFromRms();
	//
	// soundSet.select = soundFlag;
	// langSet.select = langFlag;
	// imageSet.select = imageFlag;
	// }
	//
	// }
	//
	// private void saveSetting() {
	// soundFlag = soundSet.select;
	// langFlag = langSet.select;
	// imageFlag = imageSet.select;
	// this.saveToRms();
	// }
	//
	// private void notifyChanged() {
	// if (lisetner != null) {
	// if (langFlag != langSet.select) {
	// int newLang;
	// if (langSet.select == 1) {
	// newLang = SetEventListener.LANGUAGE_CN;
	// } else {
	// newLang = SetEventListener.LANGUAGE_EN;
	// }
	// lisetner.languageChanged(newLang);
	// }
	//
	// if (imageSet.select != imageFlag) {
	// int newImageQuality;
	// if (imageSet.select == 1) {
	// newImageQuality = SetEventListener.IMAGE_QUALITY_NORMAL;
	// } else {
	// newImageQuality = SetEventListener.IMAGE_QUALITY_HIGH;
	// }
	// lisetner.imageQualityChanged(newImageQuality);
	// }
	// }
	// }
	//
	// public void setBackScene(Scene backScene) {
	// this.backScene = backScene;
	// //
	// Adaptor.getInstance().infor("SetScene>>setBackScene()setBackScene=:"+backScene);
	// }
	//
	// private static final int displayY = 40;
	//
	// protected void draw(MilkGraphics g) {
	// g.setColor(0x000000);
	// g.fillRect(0, 0, screenW, screenH);
	// g.setFont(setFont);
	// soundSet.draw(g, screenW, displayY, ItemHeight);
	// langSet.draw(g, screenW, displayY + 4 * ItemHeight, ItemHeight);
	// imageSet.draw(g, screenW, displayY + 8 * ItemHeight, ItemHeight);
	// g.drawString(leftKey, 2, screenH - setFont.getHeight(), 0);
	// g.drawString(rightKey, screenW - 2 - setFont.stringWidth(rightKey),
	// screenH - setFont.getHeight(), 0);
	// }
	//
	// public boolean doRightSoftKey() {
	// notifyChanged();
	// saveSetting();
	// Core.instance.setScene(backScene);
	// return true;
	// }
	//
	// void doLeftSoftKey() {
	// notifyChanged();
	// saveSetting();
	// Core.instance.setScene(backScene);
	// }
	//
	// public void handleFingerEvent(MFingerEvent finger) {
	// if (finger.getType() == -1) {
	// int x = finger.getX();
	// int y = finger.getY();
	// if (pointInRect(x, y, 0, screenH - setFont.getHeight(),
	// setFont.stringWidth(leftKey) + 4, setFont.getHeight())) {
	// doLeftSoftKey();
	// } else if (pointInRect(x, y,
	// screenW - 2 - setFont.stringWidth(rightKey), screenH
	// - setFont.getHeight(),
	// setFont.stringWidth(rightKey) + 4, setFont.getHeight())) {
	// doRightSoftKey();
	// } else {
	// boolean bHit = soundSet.pointerPressed(x, y, setFont,
	// ItemHeight);
	// if (!bHit)
	// bHit = langSet.pointerPressed(x, y, setFont, ItemHeight);
	// if (!bHit)
	// bHit = imageSet.pointerPressed(x, y, setFont, ItemHeight);
	//
	// if (bHit)
	// ;
	//
	// }
	// }
	// }
	//
	// public void runCallbacks() {
	// MKeyEvent key = Adaptor.instance.consumeKey();
	// if (key != null) {
	// handleKeyEvent(key);
	// }
	// MFingerEvent finger = Adaptor.instance.consumeFinger();
	// if (finger != null) {
	// handleFingerEvent(finger);
	// }
	// }
	//
	// public void handleKeyEvent(MKeyEvent key) {
	// if (key.getType() == Adaptor.KEYSTATE_PRESSED) {
	// int keyCode = key.getCode();
	// switch (state) {
	// case STATE_SET_SOUND:
	// if (keyCode == Adaptor.KEY_DOWN) {
	// state = STATE_SET_LANG;
	// soundSet.isFocus = false;
	// langSet.isFocus = true;
	// } else {
	// int old = soundSet.select;
	// soundSet.keyPressed(keyCode);
	// if (old != soundSet.select) {// change
	// boolean soundOn = soundSet.select == 0 ? true : false;
	// if (lisetner != null)
	// lisetner.soundStateChanged(soundOn);
	// }
	// }
	// break;
	// case STATE_SET_LANG:
	// if (keyCode == Adaptor.KEY_DOWN) {
	// state = STATE_SET_IMAGE;
	// langSet.isFocus = false;
	// imageSet.isFocus = true;
	// } else if (keyCode == Adaptor.KEY_UP) {
	// state = STATE_SET_SOUND;
	// langSet.isFocus = false;
	// soundSet.isFocus = true;
	// } else
	// langSet.keyPressed(keyCode);
	// break;
	// case STATE_SET_IMAGE:
	// if (keyCode == Adaptor.KEY_UP) {
	// state = STATE_SET_LANG;
	// imageSet.isFocus = false;
	// langSet.isFocus = true;
	// } else
	// imageSet.keyPressed(keyCode);
	// break;
	// }
	//
	// }
	// }
	//
	// // --------------------------rms for
	// setting------------------------------
	//
	// private final String RmsName = "setting_rms";
	//
	// private void loadFromRms() {
	// String rmsName = RmsName;
	// try {
	// RecordStore rs = RecordStore.openRecordStore(rmsName, false);
	// byte temp[] = rs.getRecord(1);
	// ByteArrayInputStream bais = new ByteArrayInputStream(temp);
	// DataInputStream dis = new DataInputStream(bais);
	//
	// soundFlag = dis.readInt();
	// langFlag = dis.readInt();
	// imageFlag = dis.readInt();
	//
	// if (rs != null) {
	// rs.closeRecordStore();
	// }
	// dis.close();
	// bais.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// private void saveToRms() {
	// String rmsName = RmsName;
	// try {
	// RecordStore rs = RecordStore.openRecordStore(rmsName, true);
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// DataOutputStream dao = new DataOutputStream(baos);
	//
	// dao.writeInt(soundFlag);
	// dao.writeInt(langFlag);
	// dao.writeInt(imageFlag);
	//
	// byte in[] = baos.toByteArray();
	// if (in != null) {
	// if (rs.getNumRecords() > 0)
	// rs.setRecord(1, in, 0, in.length);
	// else {
	// rs.addRecord(in, 0, in.length);
	// }
	// }
	// if (rs != null) {
	// rs.closeRecordStore();
	// }
	// dao.close();
	// baos.close();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private boolean isExistSettingRms() {
	// String rmsName = RmsName;
	// try {
	// RecordStore rs = RecordStore.openRecordStore(rmsName, false);
	// if (rs != null) {
	// rs.closeRecordStore();
	// return true;
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return false;
	// }
	//
	// // -------------------------------end for
	// // rms-----------------------------------------------------
	//
	// private boolean pointInRect(int px, int py, int x, int y, int w, int h) {
	// return px >= x && px <= x + w && py >= y && py <= y + h;
	// }
	//
	// private class SetItem {
	//
	// private String title;
	// private String items[];
	// private int select = 0;
	// private int titleX, titleY;
	// private int itemX, itemY, itemIntervalX = 40;
	// private boolean isFocus = false;
	//
	// private SetItem(String title, String[] items) {
	// this.title = title;
	// this.items = items;
	// }
	//
	// private void keyPressed(int keyCode) {
	// if (keyCode == Adaptor.KEY_LEFT) {
	// if (select > 0) {
	// select--;
	// }
	// } else if (keyCode == Adaptor.KEY_RIGHT) {
	// if (select < items.length - 1) {
	// select++;
	// }
	// }
	// }
	//
	// private void draw(MilkGraphics g, int screenW, int y, int ItemH) {
	// MilkFont f = g.getFont();
	// String displayTitle = title + "  (" + items[select] + ")";
	// titleX = (screenW - f.stringWidth(displayTitle)) / 2;
	// g.setColor(titleColor);
	// titleY = y;
	// g.drawString(displayTitle, titleX, y, 0);
	// int itemTotlaWidth = getAllItemsWidth(f);
	// itemX = (screenW - itemTotlaWidth - (items.length - 1)
	// * itemIntervalX) / 2;
	// itemY = titleY + ItemH;
	// int offset = 0;
	// for (int i = 0; i < items.length; i++) {
	// String value = items[i];
	// if (i == select) {
	// g.setColor(itemColorFocus);
	// value = "[" + items[i] + "]";
	// } else {
	// g.setColor(itemColor);
	// }
	// g.drawString(value, itemX + offset - f.stringWidth("["), itemY,
	// 0);
	// offset += f.stringWidth(items[i]) + itemIntervalX;
	// }
	//
	// if (this.isFocus) {
	// g.setColor(frameColorFocus);
	// g.drawRect(0, titleY - 10, screenW - 2, ItemH * 2 + 20);
	// }
	// }
	//
	// private boolean pointerPressed(int x, int y, MilkFont f, int ItemH) {
	// int offset = 0;
	// for (int i = 0; i < items.length; i++) {
	// if (pointInRect(x, y, itemX + offset, itemY,
	// f.stringWidth(items[i]), ItemH)) {
	// select = i;
	// return true;
	// }
	// offset += f.stringWidth(items[i]) + itemIntervalX;
	// }
	// return false;
	// }
	//
	// private int getAllItemsWidth(MilkFont f) {
	// int total = 0;
	// for (int i = 0; i < items.length; i++) {
	// total += f.stringWidth(items[i]);
	// }
	// return total;
	// }
	//
	// }
}
