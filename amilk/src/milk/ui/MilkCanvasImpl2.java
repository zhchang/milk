package milk.ui;

import java.util.ArrayList;
import milk.implement.Adaptor;
//import milk.ui.androidchat.AndroidInputBox;
import milk.ui.graphics.Graphics;
//import milk.ui.graphics.Image;
import milk.ui.internal.ScreenControl;
import milk.ui2.MilkCanvas;
import milk.ui2.MilkGraphics;
//import milk.ui2.MilkImage;
import android.annotation.SuppressLint;
import android.content.Context;
//import android.content.res.Configuration;
//import android.graphics.Bitmap;
import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Matrix;
//import android.graphics.Rect;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MilkCanvasImpl2 extends SurfaceView implements Callback,
		MilkCanvas {
	// private static String tag = MilkCanvasImpl2.class.getSimpleName();

	private boolean keyPressed = false;
	private boolean keyRepeating = false;
	private long pointerDraggedTime;
	private long keyRepeatedTime;
	private int repeatedKeyCode;
	private Graphics graphics;
	// private Rect bufferRect = new Rect(0, 0,
	// Adaptor.getInstance().getConfigWidth(),
	// Adaptor.getInstance().getConfigHeight());

	private MilkGraphics mg = new MilkGraphicsImpl();

	private SurfaceHolder sfh;
	public Canvas canvas;

	public MilkCanvasImpl2(Context context) {
		super(context);
		sfh = this.getHolder();
		sfh.addCallback(this);
		this.setKeepScreenOn(true);
		this.setFocusable(true);
	}

	private static ArrayList<String> debugList;
	private static boolean debugOn = false;
	private static int maxDebugLine = 20;

	public static void debug(String info) {
		if (!debugOn) {
			return;
		}
		if (debugList == null) {
			debugList = new ArrayList<String>();
		}
		debugList.add(info);
		if (debugList.size() >= maxDebugLine)
			debugList.remove(0);
	}

	public void clearKeyStatus() {
		keyPressed = false;
		keyRepeating = false;
	}

	public void setCallSuperEvent(boolean b) {
	}

	public int getCanvasHeight() {
		int gameScreenHeight = super.getHeight();
		if (Adaptor.getInstance().height < super.getHeight()) {
			gameScreenHeight = Adaptor.getInstance().getConfigHeight();
		}
		return gameScreenHeight;
	}

	private int canvasKey2AdatorKey(int keyCode) {
		// make mobile hardware key

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return Adaptor.KEY_RIGHT_SOFT;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
				|| keyCode == KeyEvent.KEYCODE_ENTER) {
			return Adaptor.KEY_FIRE;
		}

		if (keyCode == KeyEvent.KEYCODE_RIGHT_BRACKET) {
			return Adaptor.KEY_RIGHT_SOFT;
		}

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return Adaptor.KEY_MENU;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			return Adaptor.KEY_UP;
		}

		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			return Adaptor.KEY_DOWN;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			return Adaptor.KEY_LEFT;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			return Adaptor.KEY_RIGHT;
		}

		if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			int androidKeyCode0 = KeyEvent.KEYCODE_0;
			// int AdatorKeyCode0 = Adaptor.KEY_NUM0;
			int temp = androidKeyCode0 - androidKeyCode0;
			return keyCode + temp;
		}

		return keyCode;
	}

	public boolean onKeyUp(int keyCode) {
		if (keyPressed) {
			keyRepeating = false;
			keyPressed = false;
			if (Adaptor.getInstance().ignoreInputEvent())
				return true;
			keyCode = canvasKey2AdatorKey(keyCode);
			if (keyCode == Adaptor.KEY_LEFT_SOFT
					|| keyCode == Adaptor.KEY_RIGHT_SOFT) {
				return false;
			}
			Adaptor.getInstance().onKey(keyCode, Adaptor.KEYSTATE_RELEASED);
		}
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (Adaptor.getInstance().ignoreInputEvent()) {
			return true;
		}

		// float evX = ev.getX();
		// float evY = ev.getY();
		// // Log.i(tag, "x " + evX + " y " + evY);
		// evX -= UIHelper.milk.xOffset;
		// evY -= UIHelper.milk.yOffset;
		//
		// if (UIHelper.milk.isScale)
		// {
		// evX /= UIHelper.milk.widthPer;
		// evY /= UIHelper.milk.heightPer;
		// }
		//
		// int x = (int) evX;
		// int y = (int) evY;
		int x = (int) ViewAdapter.getInstance().toGameViewX(ev.getX());
		int y = (int) ViewAdapter.getInstance().toGameViewY(ev.getY());
		// Log.i(tag, "x " + x + " y " + y);

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			// Log.i(tag, "Action_down");
			pointerDraggedTime = System.currentTimeMillis();
			Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_PRESSED);
			break;
		case MotionEvent.ACTION_MOVE:
			// Log.i(tag, "ACTION_MOVE");
			// Log.i(tag, "pointerDraggedTime " + pointerDraggedTime);
			// Log.i(tag, "currenttime " + System.currentTimeMillis());
			if (System.currentTimeMillis() - pointerDraggedTime > 100) {
				Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_DRAGGED);
				pointerDraggedTime = System.currentTimeMillis();
			}
			break;
		case MotionEvent.ACTION_UP:
			// Log.i(tag, "ACTION_UP");
			Adaptor.getInstance().onFinger(x, y, Adaptor.POINTER_RELEASED);
			break;
		default:
			break;
		}
		return true;
	}

	public void processKeyRepeatedEvent() {
		if (Adaptor.getInstance().ignoreInputEvent())
			return;
		if (keyPressed && isShown()) {
			boolean shouldRepeat = false;
			if (!keyRepeating) {
				shouldRepeat = System.currentTimeMillis() - keyRepeatedTime >= 500;
				if (shouldRepeat) {
					keyRepeating = true;
				}
			} else {
				shouldRepeat = System.currentTimeMillis() - keyRepeatedTime >= 60;
			}
			if (shouldRepeat) {
				Adaptor.getInstance().onKey(repeatedKeyCode,
						Adaptor.KEYSTATE_PRESSED);
				Adaptor.getInstance().onKey(repeatedKeyCode, 0);
				keyRepeatedTime = System.currentTimeMillis();
			}
		}
	}

	// private static void scale(int[] rgbData, int newWidth, int newHeight, int
	// oldWidth, int oldHeight, int[] newRgbData)
	// {
	//
	// int x, y, dy;
	// int srcOffset;
	// int destOffset;
	//
	// // Calculate the pixel ratio ( << 10 )
	// final int pixelRatioWidth = (1024 * oldWidth) / newWidth;
	// final int pixelRatioHeight = (1024 * oldHeight) / newHeight;
	//
	// y = 0;
	// destOffset = 0;
	// while (y < newHeight)
	// {
	// dy = ((pixelRatioHeight * y) >> 10) * oldWidth;
	// srcOffset = 0;
	//
	// x = 0;
	// while (x < newWidth)
	// {
	// newRgbData[destOffset + x] = rgbData[dy + (srcOffset >> 10)];
	// srcOffset += pixelRatioWidth;
	// x++;
	// }
	//
	// destOffset += newWidth;
	// y++;
	// }
	// }

	// private Bitmap canvasBGBuffer;

	// public void setCanvasBuffer()
	// {
	// //canvas.
	// //
	// ScaleImage(mg,Adaptor.getInstance().buffer,0,0,UIHelper.milk.scaleWidth,UIHelper.milk.scaleHeight);
	// if (UIHelper.milk.isScale)
	// {
	// MilkImageImpl imageImgl=(MilkImageImpl)Adaptor.getInstance().buffer;
	// if (imageImgl != null && imageImgl.image != null &&
	// imageImgl.image.getImg() != null)
	// {
	// canvasBGBuffer =
	// Image.resizeImage(imageImgl.image.getImg(),UIHelper.milk.scaleWidth,
	// UIHelper.milk.scaleHeight);
	// }
	// }
	// }

	@SuppressLint("WrongCall")
	public void draw() {
		sfh = getHolder();
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {
				onDraw(canvas);
				// sfh.unlockCanvasAndPost(canvas);
			}
		} catch (Exception e) {
		} finally {
			try {
				sfh.unlockCanvasAndPost(canvas);
			} catch (Exception e) {
			}
		}
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		System.out.println("-------- onLayout--------");
		super.onLayout(changed, left, top, right, bottom);
		for (int i = 0; i < 6; i++)
			this.postDelayed(layoutInputTask, 200 + i * 100);
	}

	private Runnable layoutInputTask = new Runnable() {
		public void run() {
			HandlerMsg.layoutInputFrame();
		}
	};

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	public boolean onKeyDown(int keyCode) {
		if (Adaptor.getInstance().ignoreInputEvent())
			return true;
		keyCode = canvasKey2AdatorKey(keyCode);
		if (keyCode == Adaptor.KEY_LEFT_SOFT || keyCode == Adaptor.KEY_MENU) {
			Adaptor.getInstance().onLeftSoftKey();
		} else if (keyCode == Adaptor.KEY_RIGHT_SOFT) {
			Adaptor.getInstance().onRightSoftKey();
			return false;
		} else {
			keyPressed = true;
			keyRepeatedTime = System.currentTimeMillis();
			repeatedKeyCode = keyCode;
			Adaptor.getInstance().onKey(keyCode, Adaptor.KEYSTATE_PRESSED);
		}
		return true;
	}

	// private MilkImage buffer;

	public void onDraw(Canvas canvas) {
		if (graphics == null) {
			graphics = new Graphics(canvas);
		}

		((MilkGraphicsImpl) mg).setG(graphics);
		// graphics.setColor(0);
		// graphics.fillRect(0, 0, ScreenControl.getScreenWidth(),
		// ScreenControl.getScreenHeight());

		// mg.setClip(0, 0, ScreenControl.getScreenWidth(),
		// ScreenControl.getScreenHeight());
		ViewAdapter.getInstance().onPreDraw(canvas);

		Adaptor.getInstance().draw(mg);

		// UIHelper.milk.isScale=false;
		// if (UIHelper.milk.isScale)
		// {
		// canvas.save();
		// canvas.translate(UIHelper.milk.xOffset, UIHelper.milk.yOffset);
		// canvas.scale(UIHelper.milk.widthPer, UIHelper.milk.heightPer);
		// // long start = System.currentTimeMillis();
		// Adaptor.getInstance().draw(mg);
		// // long end = System.currentTimeMillis();
		// canvas.restore();
		//
		// } else
		// {
		// canvas.save();
		// canvas.translate(UIHelper.milk.xOffset, UIHelper.milk.yOffset);
		// Adaptor.getInstance().draw(mg);
		// canvas.restore();
		// }
		try {
			if (debugOn) {
				mg.setColor(0xff0000);
				for (int i = 0; i < debugList.size(); i++) {
					mg.drawString(debugList.get(i), 0, i
							* mg.getFont().getHeight(), 0);
				}
				maxDebugLine = ScreenControl.getScreenHeight()
						/ mg.getFont().getHeight();
			}
		} catch (Exception e) {
		}
		ViewAdapter.getInstance().onPostDraw(canvas);
		graphics.setColor(0);
		if (ViewAdapter.getInstance().yOffset > 0) {
			graphics.fillRect(0, 0, ScreenControl.getScreenWidth(),
					ViewAdapter.getInstance().yOffset);
			graphics.fillRect(0,
					ScreenControl.getScreenHeight()
							- ViewAdapter.getInstance().yOffset,
					ScreenControl.getScreenWidth(),
					ViewAdapter.getInstance().yOffset);
		}
		if (ViewAdapter.getInstance().xOffset > 0) {
			graphics.fillRect(0, 0, ViewAdapter.getInstance().xOffset,
					ScreenControl.getScreenHeight());
			graphics.fillRect(
					ScreenControl.getScreenWidth()
							- ViewAdapter.getInstance().xOffset, 0,
					ViewAdapter.getInstance().xOffset,
					ScreenControl.getScreenHeight());
		}
	}

}
