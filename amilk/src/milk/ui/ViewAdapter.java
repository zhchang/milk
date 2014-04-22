package milk.ui;

import android.graphics.Canvas;
import android.util.Log;

public class ViewAdapter {

	private int scaleWidth = -1;
	private int scaleHeight = -1;
	public int xOffset = -1;
	public int yOffset = -1;
	private float heightAspect;
	private float widthAspect;
	private static ViewAdapter instance;

	static ViewAdapter getInstance() {
		if (instance == null) {
			instance = new ViewAdapter();
		}
		return instance;
	}

	private ViewAdapter() {
	}

	void onCreate(int deviceViewWidth, int deviceViewHeight, int gameViewWidth,
			int gameViewHeight) {
		heightAspect = ((float) deviceViewHeight) / gameViewHeight;
		widthAspect = ((float) deviceViewWidth) / gameViewWidth;

		if (widthAspect < heightAspect) {
			heightAspect = widthAspect;
		} else {
			widthAspect = heightAspect;
		}

		scaleWidth = (int) (gameViewWidth * widthAspect);
		scaleHeight = (int) (gameViewHeight * heightAspect);

		xOffset = (deviceViewWidth - scaleWidth) / 2;
		yOffset = (deviceViewHeight - scaleHeight) / 2;
		Log.i("ViewAdapter", "widthPer " + widthAspect + " heightPer " + heightAspect
				+ " xOffset " + xOffset + " yOffset " + yOffset);
	}

	int toDeviceViewX(int gameX) {
		gameX *= widthAspect;
		gameX += xOffset;
		return gameX;
	}

	int toDeviceViewY(int gameY) {
		gameY *= heightAspect;
		gameY += yOffset;
		return gameY;
	}

	int toDeviceViewWidth(int gameWidth) {
		gameWidth *= widthAspect;
		return gameWidth;
	}

	int toDeviceViewHeight(int gameHeight) {
		gameHeight *= heightAspect;
		return gameHeight;
	}

	float toGameViewX(float deviceX) {
		deviceX -= xOffset;
		deviceX /= widthAspect;
		return deviceX;
	}

	float toGameViewY(float deviceY) {
		deviceY -= yOffset;
		deviceY /= heightAspect;
		return deviceY;
	}

	void onPreDraw(Canvas canvas) {
		canvas.save();
		canvas.translate(xOffset, yOffset);
		canvas.scale(widthAspect, heightAspect);
	}

	void onPostDraw(Canvas canvas) {
		canvas.restore();
	}

}
