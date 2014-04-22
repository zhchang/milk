package milk.implement;


import milk.implement.mk.MMap;
import milk.implement.mk.MRect;
import milk.ui2.MilkGraphics;

public class Core {

	private static final int GAME_STATE_NORMAL = 0;
	private static final int GAME_STATE_SWITCH_SCENE = 1;
	private static final int GAME_STATE_SWITCH_ANIMATION = 2;
	private int state = GAME_STATE_NORMAL;

	private String nextSceneResId;
	private MMap nextSceneParams;

	private static Core instance = new Core();

	public static synchronized Core getInstance() {
		if (instance == null) {
			instance = new Core();
		}
		return instance;
	}

	private Core() {
		if (screenRect == null)
			screenRect = new MRect(0, 0, Adaptor.milk.getCanvasWidth(),
					Adaptor.milk.getCanvasHeight());
	}

	static void exit() {
		if (instance != null) {
			instance.stopGame();
			instance = null;
		}
	}

	public void aboutToSwitchScene(String resourceId, MMap params,
			byte transitionType) {
		nextSceneResId = resourceId;
		nextSceneParams = params;
		this.transitionType = transitionType;
		this.state = Core.GAME_STATE_SWITCH_SCENE;
	}

	private byte transitionType = 0;

	private Scene currentScene = null;
	private Scene nextScene = null;
	private boolean nextSceneLoaded = false;
	private AnimationScreen animationScreen;

	public Scene getCurrentScene() {
		synchronized (instance) {
			return currentScene;
		}
	}

	public Scene getTopMostScene() {
		return currentScene.getTopMostScene();
	}

	public DrawShapeDef drawShapeDef = new DrawShapeDef();

	private MRect screenRect;

	static void logTime(long start, String object, long min) {
		long diff = System.currentTimeMillis() - start;
		if (diff > min) {
			Adaptor.console(object + " consumed [" + diff + "] ms");
		}
	}

	private void setScene(Scene scene, boolean needInit) {
		Adaptor.milk.clearKeyStatus();
		if (currentScene != null) {
			currentScene.hideNotify();
		}
		if (scene != null) {
			currentScene = scene;
//			currentScene.showNotify();
			// if (needInit)
			// currentScene.runInit();
		} else {
			Adaptor.debug("failed to load scene");
		}
	}

	public void replaceScene(Scene scene) {
		if (currentScene != null) {
			currentScene.hideNotify();
		}
		if (scene != null) {
			currentScene = scene;
			currentScene.showNotify();
		} else {
			Adaptor.debug("failed to load scene");
		}
	}

	private boolean switchSceneForChat = false;

	public void switchSceneForChat(Scene scene) {
		nextScene = scene;
		switchSceneForChat = true;
		this.transitionType = -100;
		state = Core.GAME_STATE_SWITCH_SCENE;
	}

	public void update() {
		switch (this.state) {
		case Core.GAME_STATE_NORMAL:

			if (currentScene != null) {
				currentScene.runCallbacks();
			}
			break;
		case Core.GAME_STATE_SWITCH_SCENE:
			if (animationScreen == null) {
				animationScreen = new AnimationScreen();
			}
			animationScreen.reset(transitionType, screenRect);
			if (currentScene != null) {
				// MilkGraphics g = animationScreen.getGraphics();
				// currentScene.draw(g);
				if (!switchSceneForChat) {
					currentScene.onStop();
					currentScene = null;
				}
			}
			nextSceneLoaded = false;

			// MilkTask task = Adaptor.uiFactory.createMilkTask(this);
			if (!switchSceneForChat) {
				nextScene = null;
				nextScene = Adaptor.getInstance().loadScene("main",
						nextSceneResId, screenRect, nextSceneParams);
			}

			nextSceneLoaded = true;
			if (switchSceneForChat) {
				setScene(nextScene, false);
			} else {
				long s1 = System.currentTimeMillis();
				setScene(nextScene, true);
				long s2 = System.currentTimeMillis();
				System.out.println("setScene delay : " + (s2 - s1));
			}
			switchSceneForChat = false;

			state = Core.GAME_STATE_SWITCH_ANIMATION;
			break;
		case Core.GAME_STATE_SWITCH_ANIMATION:
			if (animationScreen != null) {
				boolean finish = false;
				try {
					finish = animationScreen.update();
				} catch (Exception t) {
					Adaptor.exception(t);
				}

				if (finish) {
					animationScreen.finish();
					state = Core.GAME_STATE_NORMAL;
					nextScene = null;
					if(currentScene!=null)
					currentScene.showNotify();
				}
			}
			break;
		}
	}

	boolean ignoreInputEvent() {
		return state != GAME_STATE_NORMAL || currentScene == null;
	}

	void stopGame() {
		this.state = Core.GAME_STATE_SWITCH_SCENE;
	}

	/**
	 * Draw players and texts
	 */
	protected void draw(MilkGraphics g) {
		switch (this.state) {
		case Core.GAME_STATE_NORMAL:
			if (currentScene != null) {
				currentScene.draw(g);
			} else {
				Adaptor.getInstance().drawLoadingScene(g);
			}
			break;
		case Core.GAME_STATE_SWITCH_SCENE:

		case Core.GAME_STATE_SWITCH_ANIMATION:
			if (animationScreen != null) {
				try {
					animationScreen.draw(g);
				} catch (Exception t) {
					Adaptor.exception(t);
				}
			} else {
				Adaptor.getInstance().drawLoadingScene(g);
			}
			break;
		}
	}

}
