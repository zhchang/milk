package milk.implement;

import java.util.Random;

import milk.implement.mk.MRect;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkImage;

public class AnimationScreen {

	private static final byte TRANSITION_FLAT_LEFT = 1;
	private static final byte TRANSITION_FLAT_RIGHT = 2;
	private static final byte TRANSITION_FLAT_UP = 3;
	private static final byte TRANSITION_FLAT_DOWN = 4;
	private static final byte TRANSITION_FLAT_FLIP_X = 5;
	private static final byte TRANSITION_OPEN_VERCITAL = 6;
	private static final byte TRANSITION_OPEN_HORIZON = 7;
	private static final byte TRANSITION_FLAT_FLIP_Y = 8;
	private static final byte TRANSITION_FLAT_FLIP_XY = 9;
	private static final byte TRANSITION_OPEN_WINDOW = 10;
	private static final byte TRANSITION_FLAT_GRADIENT = 11;

	private static final int borderSize = 4;
	private static final int minFlatSize = 4;

	private int flipNum = 10;
	private int flipGradSize;
	private static final int FLIP_STEP = 6;

	private MilkImage lastSceneScreen;
	private MilkGraphics lastGraphics;
	private int animationType = 0;
	private MRect screenRect = new MRect(), bufferRect = new MRect(),
			clipSaveRect = new MRect();
	// private Scene nextScene;
	private boolean finishAnimation;
	private boolean aboutToFinish = false;

	private int argbDada[];

	public AnimationScreen() {

	}

	public void finish() {

		lastSceneScreen = null;
		lastGraphics = null;
		argbDada = null;

	}

	public void reset(int type, MRect rect) {
		finishAnimation = false;
		aboutToFinish = false;

		bufferRect.set(rect.x, rect.y, rect.width, rect.height);
		screenRect.set(rect.x, rect.y, rect.width, rect.height);

		clipSaveRect.set(0, 0, 0, 0);
		animationType = type;
		if (animationType < TRANSITION_FLAT_LEFT
				|| type > TRANSITION_FLAT_GRADIENT) {
			animationType = rand(TRANSITION_FLAT_FLIP_X, TRANSITION_OPEN_WINDOW);
		}

		if (TRANSITION_FLAT_FLIP_X == animationType) {
			flipNum = screenRect.width / 20;
			flipGradSize = screenRect.width / flipNum;
		} else if (TRANSITION_FLAT_FLIP_Y == animationType) {
			flipNum = screenRect.height / 20;
			flipGradSize = screenRect.height / flipNum;
		} else if (TRANSITION_FLAT_FLIP_XY == animationType) {
			flipNum = screenRect.width / 20;
			flipGradSize = screenRect.width / flipNum;
		} else if (TRANSITION_OPEN_VERCITAL == animationType) {
			bufferRect.height = 0;
		} else if (TRANSITION_OPEN_HORIZON == animationType) {
			bufferRect.width = 0;
		} else if (TRANSITION_OPEN_WINDOW == animationType) {
			bufferRect.width = screenRect.width;
		}
	}


	public MilkGraphics getGraphics() {
		if (lastSceneScreen == null) {
			lastSceneScreen = Adaptor.uiFactory.createImage(screenRect.width,
					screenRect.height);
			lastGraphics = lastSceneScreen.getGraphics();
		}
		return lastGraphics;
	}

	public boolean update() {
		// long timePassed = System.currentTimeMillis() - startTime;
		switch (animationType) {
		case TRANSITION_FLAT_LEFT:

			if (bufferRect.x > -screenRect.width * 2 / 3) {
				bufferRect.x -= screenRect.width / 6;
			} else if (bufferRect.x <= -screenRect.width + minFlatSize) {
				bufferRect.x = screenRect.width;
				aboutToFinish = true;
			} else {
				bufferRect.x = (bufferRect.x - screenRect.width) / 2;
			}
			break;

		case TRANSITION_FLAT_RIGHT:
			if (bufferRect.x < screenRect.width / 3) {
				bufferRect.x += screenRect.width / 6;
			} else if (bufferRect.x >= screenRect.width - minFlatSize) {
				bufferRect.x = screenRect.width;
				aboutToFinish = true;
			} else {
				bufferRect.x = (bufferRect.x + screenRect.width) / 2;
			}
			break;
		case TRANSITION_FLAT_UP:
			if (bufferRect.y > -screenRect.height / 3) {
				bufferRect.y -= screenRect.height / 6;
			} else if (bufferRect.y <= -screenRect.height + minFlatSize) {
				bufferRect.y = -screenRect.height;
				finishAnimation = true;
			} else {
				bufferRect.y = (bufferRect.y - screenRect.height) / 2;
			}
			break;
		case TRANSITION_FLAT_DOWN:
			if (bufferRect.y < screenRect.height / 3) {
				bufferRect.y += screenRect.height / 6;
			} else if (bufferRect.y >= screenRect.height - minFlatSize) {
				bufferRect.y = screenRect.height;
				aboutToFinish = true;
			} else {
				bufferRect.y = (bufferRect.y + screenRect.height) / 2;
			}
			break;
		case TRANSITION_OPEN_VERCITAL:
			if (bufferRect.height < screenRect.height / 3) {
				bufferRect.height += screenRect.height / 6;
			} else if (bufferRect.height > screenRect.height - minFlatSize) {
				bufferRect.height = screenRect.height;
				aboutToFinish = true;
			} else {
				bufferRect.height = (screenRect.height + bufferRect.height) / 2;
			}
			break;
		case TRANSITION_OPEN_HORIZON:
			if (bufferRect.width < screenRect.width / 3) {
				bufferRect.width += screenRect.width / 6;
			} else if (bufferRect.width > screenRect.width - minFlatSize) {
				bufferRect.width = screenRect.width;
				aboutToFinish = true;
			} else {
				bufferRect.width = (screenRect.width + bufferRect.width) / 2;
			}
			break;
		case TRANSITION_FLAT_FLIP_X:
			if (flipGradSize < FLIP_STEP) {
				flipGradSize = 0;
				aboutToFinish = true;
			} else {
				flipGradSize = flipGradSize - FLIP_STEP;
			}
			break;
		case TRANSITION_FLAT_FLIP_Y:
			if (flipGradSize < FLIP_STEP) {
				flipGradSize = 0;
				aboutToFinish = true;
			} else {
				flipGradSize = flipGradSize - FLIP_STEP;
			}
			break;
		case TRANSITION_FLAT_FLIP_XY:
			if (flipGradSize < FLIP_STEP) {
				flipGradSize = 0;
				aboutToFinish = true;
			} else {
				flipGradSize = flipGradSize - FLIP_STEP;
			}
			break;
		case TRANSITION_OPEN_WINDOW:
			if (bufferRect.width > screenRect.width * 2 / 3) {
				bufferRect.width -= screenRect.width / 6;
			} else if (bufferRect.width > 4) {
				bufferRect.width = bufferRect.width / 2;
			} else {
				bufferRect.width = 0;
				aboutToFinish = true;
			}
			break;
		case TRANSITION_FLAT_GRADIENT:
			aboutToFinish = updateRGBdata();
			break;
		}

		if (finishAnimation) {
			return true;
		} else if (aboutToFinish) {
			finishAnimation = true;
		}
		return false;
	}

	private void drawPrepareScene(MilkGraphics g) {
		Adaptor.getInstance().drawLoading(g, screenRect.x, screenRect.y,
				screenRect.width, screenRect.height, true);
	}

	public void draw(MilkGraphics g) {

		g.setColor(0x000000);
		g.fillRect(screenRect.x, screenRect.y, screenRect.width,
				screenRect.height);
		saveClip(g);
		if (lastSceneScreen != null && !aboutToFinish && !finishAnimation) {
			switch (animationType) {
			case TRANSITION_FLAT_LEFT:
				g.translate(bufferRect.x + screenRect.width, bufferRect.y);
				drawPrepareScene(g);
				g.translate(-g.getTranslateX(), -g.getTranslateY());
				g.drawImage(lastSceneScreen, bufferRect.x - borderSize,
						bufferRect.y, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case TRANSITION_FLAT_RIGHT:
				g.translate(bufferRect.x - screenRect.width, bufferRect.y);
				drawPrepareScene(g);
				g.translate(-g.getTranslateX(), -g.getTranslateY());
				g.drawImage(lastSceneScreen, bufferRect.x + borderSize,
						bufferRect.y, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case TRANSITION_FLAT_UP:
				g.translate(bufferRect.x, bufferRect.y + screenRect.height);
				drawPrepareScene(g);
				g.translate(-g.getTranslateX(), -g.getTranslateY());
				g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y
						- borderSize, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case TRANSITION_FLAT_DOWN:
				g.translate(bufferRect.x, bufferRect.y - screenRect.height);
				drawPrepareScene(g);
				g.translate(-g.getTranslateX(), -g.getTranslateY());
				g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y
						+ borderSize, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case TRANSITION_OPEN_VERCITAL:
				drawPrepareScene(g);
				int windowY = (screenRect.height - bufferRect.height) / 2;

				g.setClip(screenRect.x, screenRect.y, screenRect.width, windowY);
				g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y
						- borderSize, MilkGraphics.TOP | MilkGraphics.LEFT);

				g.setClip(screenRect.x, screenRect.height - windowY,
						screenRect.width, windowY);
				g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y
						+ borderSize, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case AnimationScreen.TRANSITION_OPEN_HORIZON:
				drawPrepareScene(g);
				int windowX = (screenRect.width - bufferRect.width) / 2;
				g.setClip(screenRect.x, screenRect.y, windowX,
						screenRect.height);
				g.drawImage(lastSceneScreen, bufferRect.x - borderSize,
						bufferRect.y, MilkGraphics.TOP | MilkGraphics.LEFT);
				g.setClip(screenRect.width - windowX, screenRect.y, windowX,
						screenRect.height);
				g.drawImage(lastSceneScreen, bufferRect.x + borderSize,
						bufferRect.y, MilkGraphics.TOP | MilkGraphics.LEFT);
				break;
			case TRANSITION_FLAT_FLIP_X: {
				drawPrepareScene(g);
				int maxGradSize = screenRect.width / flipNum;
				for (int i = 0; i < flipNum + 1; i++) {
					g.setClip(screenRect.x + maxGradSize * i, screenRect.y,
							flipGradSize, screenRect.height);
					g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y,
							MilkGraphics.TOP | MilkGraphics.LEFT);
				}
				break;
			}
			case TRANSITION_FLAT_FLIP_Y: {
				drawPrepareScene(g);
				int maxGradSize = screenRect.height / flipNum;

				for (int i = 0; i < flipNum + 1; i++) {
					g.setClip(screenRect.x, screenRect.y + maxGradSize * i,
							screenRect.width, flipGradSize);
					g.drawImage(lastSceneScreen, bufferRect.x, bufferRect.y,
							MilkGraphics.TOP | MilkGraphics.LEFT);
				}
				break;
			}
			case TRANSITION_FLAT_FLIP_XY: {
				drawPrepareScene(g);
				int maxGradSize = screenRect.width / flipNum;
				for (int col = 0; col < flipNum + 1; col++) {
					for (int row = 0; row * maxGradSize < screenRect.height; row++) {
						g.setClip(screenRect.x + maxGradSize * col,
								screenRect.y + maxGradSize * row, flipGradSize,
								flipGradSize);
						g.drawImage(lastSceneScreen, bufferRect.x,
								bufferRect.y, MilkGraphics.TOP
										| MilkGraphics.LEFT);
					}
				}
			}
				break;
			case TRANSITION_OPEN_WINDOW:
				drawPrepareScene(g);
				int rectX = (screenRect.width - bufferRect.width) / 2;
				int rectH = screenRect.height * bufferRect.width
						/ screenRect.width;
				int rectY = (screenRect.height - rectH) / 2;
				g.setClip(rectX, rectY, bufferRect.width, rectH);
				g.drawImage(lastSceneScreen, screenRect.x, screenRect.y,
						MilkGraphics.TOP | MilkGraphics.LEFT);

				break;
			case TRANSITION_FLAT_GRADIENT:
				drawPrepareScene(g);
				if (argbDada == null) {
					initRgbDada();
				}
				if (argbDada != null)
					g.drawRGB(argbDada, 0, screenRect.width, screenRect.x,
							screenRect.y, screenRect.width, screenRect.height,
							true);
				break;
			}

			g.translate(-g.getTranslateX(), -g.getTranslateY());
		} else {
			drawPrepareScene(g);
		}
		restoreClip(g);
	}

	private void initRgbDada() {
		int imgWidth = lastSceneScreen.getWidth();
		int imgHeight = lastSceneScreen.getHeight();
		this.argbDada = new int[imgWidth * imgHeight];
		lastSceneScreen
				.getRGB(argbDada, 0, imgWidth, 0, 0, imgWidth, imgHeight);
		lastSceneScreen = null;
	}

	private boolean updateRGBdata() {
		if (argbDada == null)
			return false;
		boolean finish = false;
		for (int i = 0; i < argbDada.length; i++) {
			int alpha = (argbDada[i] & 0xff000000) >>> 24;
			// if (i == 0)
			// System.out.print(alpha + ",");
			if (alpha > 122)
				alpha = alpha - 30;
			else {
				alpha = alpha / 2;
			}
			if (alpha < 0)
				alpha = 0;
			argbDada[i] = ((alpha) << 24) | (argbDada[i] & 0x00ffffff);
			if (alpha < 8)
				finish = true;
		}
		return finish;
	}

	private void saveClip(MilkGraphics g) {
		clipSaveRect.x = g.getClipX();
		clipSaveRect.y = g.getClipY();
		clipSaveRect.width = g.getClipWidth();
		clipSaveRect.height = g.getClipHeight();
	}

	private void restoreClip(MilkGraphics g) {
		g.setClip(clipSaveRect.x, clipSaveRect.y, clipSaveRect.width,
				clipSaveRect.height);
	}

	private static Random rand = new Random();

	private static int rand(int min, int max) {
		return Math.abs(rand.nextInt() % (max - min + 1)) + min;
	}

}
