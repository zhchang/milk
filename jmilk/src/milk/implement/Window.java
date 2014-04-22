package milk.implement;

import java.io.ByteArrayInputStream;

import java.util.Hashtable;
import java.util.Vector;

import milk.implement.IMEvent.MFingerEvent;
import milk.implement.IMEvent.MKeyEvent;
import milk.implement.IMEvent.MResourceEvent;
import milk.implement.IMEvent.MWindowEvent;
import milk.implement.mk.MArray;
import milk.implement.mk.MMap;
import milk.implement.mk.MRect;
import milk.implement.sv3.MilkDiv9;
import milk.implement.sv3.MilkFactory;
import milk.implement.sv3.MilkImageUtil;
import milk.implement.sv3.MilkPage;
import milk.implement.sv3.MilkRenderContext;
import milk.menu.WindowMenu;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import milk.ui2.MilkTask;
import mobon.MobonReader;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Factory;
import smartview3.elements.Sv3Input;
import smartview3.elements.Sv3Page;
import smartview3.elements.Sv3Text;
import smartview3.layout.LayoutContext;
import smartview3.layout.Rect;
import smartview3.layout.TextBreaker;
import smartview3.layout.TextSegment;
import smartview3.utils.IEventHandler;

public class Window extends Scene implements IEventHandler, TaskRunner {

	private MilkPage page;
	private boolean inited = false;

	private MRect titleRect = new MRect(0, 0, 0, 0);
	private MRect contentRect = null;
	private Vector titlePieces = new Vector();
	private TextSegment titleSegment;
	private static MilkFont windowFont = Adaptor.uiFactory.getFont(
			MilkFont.STYLE_PLAIN, MilkFont.SIZE_MEDIUM);

	private MilkTask layoutTask;

	private long layoutTimer = 0;

	private static final int event_trigger = 1;
	private static final int event_will_focus = 2;
	private static final int event_will_unfocus = 3;

	private MilkRenderContext renderContext;
	private Rect bounding = new Rect();
	private Rect viewPort = new Rect();
	private Rect lastClip = new Rect();

	private Vector windowEvents = new Vector();

	private Hashtable callbacks = new Hashtable();

	static Sv3Factory factory = new MilkFactory();

	Sv3Element lastHitElement = null;

	public Sv3Page getPage() {
		return page;
	}

	private void adjustContentRect() {
		if (page != null && page.getTitle() != null
				&& page.getTitle().length() > 0) {
			int height = windowFont.getHeight();
			titleRect = new MRect(screenRect.x, screenRect.y, screenRect.width,
					height);
			contentRect = new MRect(screenRect.x, screenRect.y + height,
					screenRect.width, screenRect.height - height);
		} else {
			contentRect = screenRect;
		}

	}

	public void onStop() {
		clearMem();
		if (page != null) {
			page.clearImageReference();
			page.clearMem();
			page = null;
		}
		if (windowMenu != null) {
			windowMenu.clearMem();
			windowMenu.clearImageReferences();
			windowMenu = null;
		}
		Adaptor.getInstance().forceClearImages();

	}

	public void doLayout() {
		// System.out.println("----------window doLayout----------");
		layoutTimer = 0;
		adjustContentRect();
		if (titleRect.height > 0) {
			try {
				titlePieces.removeAllElements();
				new TextBreaker().breakText(page.getTitle(), titlePieces,
						windowFont, titleRect.width * 3 / 4,
						(short) (titleRect.width * 3 / 4), 1, null);
				titleSegment = (TextSegment) titlePieces.elementAt(0);
				titlePieces.removeAllElements();
			} catch (Exception t) {
				// System.out.println("----------window doLayout Exception----------");
			}
		}
		layoutContent();
	}

	private void layoutContent() {
		// System.out.println("----------window layoutContent----------contentRectWidth:"+contentRect.width);
		try {
			Sv3Div root = page.getRoot();
			LayoutContext lc = new LayoutContext(contentRect.width, 0x7ffff);
			root.layout(lc);
		} catch (Exception t) {
			Adaptor.exception(t);
			// System.out.println("----------window layoutContent Exception----------");
		}
		adjustScreenUsingFlags();
	}

	public void runInit() {
		if ((flags & FLAG_HASMENU) != 0) {
			windowMenu = new WindowMenu(this.getSceneId());
		}
		try {
			page.setNoBg((flags & FLAG_NOBG) > 0);
		} catch (Exception t) {
		}
		super.runInit();

		doLayout();
		inited = true;

	}

	Window(String windowId, String resourceId, final MRect rect, MMap params,
			int flags, Scene parent) throws Exception {
		Adaptor.getInstance().enableShowChatTab(0);
		this.flags = FLAG_HASMENU;
		initSceneParams(windowId, resourceId, rect, params, flags, parent);
		byte[] mobon = Adaptor.milk.gunzip(Adaptor.getInstance().getResource(
				resourceId));
		page = (MilkPage) new MobonReader(new ByteArrayInputStream(mobon),
				factory).readPage();
		final byte[] byteCode = page.getByteCode();
		page.setEventHandler(Window.this);
		adjustContentRect();
		if (byteCode != null && byteCode.length > 0) {
			try {
				init(byteCode);
			} catch (Exception t) {
			}
		}

	}

	private void adjustScreenUsingFlags() {
		if ((flags & FLAG_AUTOCENTER) > 0 && parent != null) {
			Sv3Div root = page.getRoot();
			screenRect.height = root.getRect().height + titleRect.height;
			if (screenRect.height > parent.screenRect.height * 3 / 4) {
				screenRect.height = parent.screenRect.height * 3 / 4;
			}

			screenRect.x = parent.screenRect.x
					+ (parent.screenRect.width - screenRect.width) / 2;

			screenRect.y = parent.screenRect.y
					+ (parent.screenRect.height - screenRect.height) / 2;
			adjustContentRect();

		}
		if ((flags & FLAG_FITHEIGHT) > 0) {
			Sv3Div root = page.getRoot();
			screenRect.height = root.getRect().height + titleRect.height;
			adjustContentRect();
		}
		if ((flags & FLAG_FOCUSFIRST) > 0) {
			try {
				page.focusFirst();
			} catch (Exception t) {
			}
		}
		Adaptor.getInstance().bufferReady();
	}

	protected void draw(MilkGraphics g) {
		drawShader(g);
		try {
			drawScreen(g);
		} catch (Exception t) {
		}
		drawLoading(g);
	}

	protected void drawScreen(MilkGraphics g) {
		if (page != null) {
			if (titleRect.height > 0) {
				g.setColor(0x333333);
				g.fillRect(titleRect.x, titleRect.y, titleRect.width,
						titleRect.height);
				g.setFont(windowFont);
				// System.out.println("-----------titleRect.y:"+titleRect.y);
				if (titleSegment != null) {
					g.setColor(0xffffff);
					if (titleSegment.location == 0
							&& titleSegment.length == page.getTitle().length())
						g.drawString(page.getTitle(), titleRect.x
								+ (titleRect.width - titleSegment.width) / 2,
								titleRect.y, MilkGraphics.TOP
										| MilkGraphics.LEFT);
					else
						g.drawSubstring(
								page.getTitle(),
								titleSegment.location,
								titleSegment.length,
								titleRect.x
										+ (titleRect.width - titleSegment.width)
										/ 2, titleRect.y, MilkGraphics.TOP
										| MilkGraphics.LEFT);
				}
			}
		}
		if (inited && page != null) {
			lastClip.copy(g.getClipX(), g.getClipY(), g.getClipWidth(),
					g.getClipHeight());
			g.setClip(contentRect.x, contentRect.y, contentRect.width,
					contentRect.height);
			try {

				int w = contentRect.width;
				int h = contentRect.height;
				viewPort.copy(contentRect.x, contentRect.y, w, h);
				bounding.copy(contentRect.x, contentRect.y
						- getPage().getScrollY(), w, h);
				if (renderContext == null) {
					renderContext = new MilkRenderContext(g, bounding, viewPort);
				} else {
					renderContext.reset(g, bounding, viewPort);
				}
				getPage().render(renderContext);
				if (page.getRoot().getRect().height > contentRect.height) {
					Rect r = getPage().getScrollRect(w, h);
					if (r != null) {
						r.toRectWithOffset(viewPort, r);
						g.setColor(0xffffff);
						g.drawRect(r.x, r.y, r.width, r.height);
						g.setColor(0x61482c);
						g.fillRect(r.x + 1, r.y + 1, r.width - 1, r.height - 1);
					}
				}

			} finally {
				g.setClip(lastClip.x, lastClip.y, lastClip.width,
						lastClip.height);
			}

		} else {
			Adaptor.getInstance().drawLoading(g, contentRect.x, contentRect.y,
					contentRect.width, contentRect.height, true);
		}
		drawLayers(g);

		if (windowMenu != null)
			windowMenu.draw(g);
	}

	protected void doInput(Sv3Element hoster, Sv3Element span, int maxSize,
			int maxLines, int constraints) {
		if (hoster != null && span != null && span instanceof Sv3Text) {
			EditorSetting setting = new EditorSetting();
			Rect abs = hoster.getAbsoluteRect();
			if (hoster instanceof MilkDiv9) {
				MilkDiv9 div9 = (MilkDiv9) hoster;
				setting.bgColor = div9.getFillColor();
				short[] paddings = div9.getPadding();
				setting.x = abs.x + paddings[0];
				setting.y = abs.y + paddings[1] - page.getScrollY();
				setting.width = abs.width - (paddings[0] + paddings[2]);
				setting.height = abs.height - (paddings[1] + paddings[3]);
			} else {
				setting.x = abs.x;
				setting.y = abs.y - page.getScrollY();
				setting.width = abs.width;
				setting.height = abs.height;
			}
			setting.x += contentRect.x;
			setting.y += contentRect.y;
			setting.receiver = (Sv3Text) span;
			setting.constraints = constraints;
			setting.maxlength = maxSize;
			setting.maxLines = maxLines;
			Adaptor.milk.showInput(setting);
		}
	}

	protected void hideInput() {
		Adaptor.milk.hideInput();
	}

	protected boolean subExecute(byte[] ins) {
		boolean result = true;
		int[] oprands = MInstruction.getOprands(ins);
		switch (ins[0]) {
		case GETELEMENT: {
			if (oprands[1] < 0) {
				//which means we are returning root element here
				setVar(oprands[0], page.getRoot());
			} else {
				String id = getStringVar(oprands[1]);
				Sv3Element ele = page.getElementById(id);
				if (ele == null) {
					Adaptor.debug("element[" + id + "] is null");
				}
				setVar(oprands[0], ele);
			}

			break;
		}
		case ADDCHILD: {
			Sv3Div parent = (Sv3Div) getElementVar(oprands[0]);
			parent.addChild(getElementVar(oprands[1]));
			if (parent.getPage() != null) {
				parent.getPage().didChangeAttrib(parent, "child", true, true);
			}
			break;
		}
		case REMOVECHILD: {
			Sv3Div parent = (Sv3Div) getElementVar(oprands[0]);
			parent.removeChild(getElementVar(oprands[1]));
			if (parent.getPage() != null) {
				parent.getPage().didChangeAttrib(parent, "child", true, true);
			}
			break;
		}
		case MOMLSET: {
			int mode = oprands[0];
			Sv3Element element = getElementVar(oprands[1]);
			if (element != null) {
				String key = getStringVar(oprands[2]);
				if (mode == 0) {
					// set int attribute
					element.setIntAttrib(key, getIntVar(oprands[3]));
				} else if (mode == 1) {
					// set string attribute
					element.setStrAttrib(key, getStringVar(oprands[3]));
				}
			}
			break;
		}
		case MOMLGET: {
			int mode = oprands[1];
			Sv3Element element = getElementVar(oprands[2]);
			if (element != null) {
				String key = getStringVar(oprands[3]);
				Object obj = element.getAttrib(key);
				if (mode == 0) {
					setVar(oprands[0], ((Integer) obj).intValue());
				} else if (mode == 1) {
					setVar(oprands[0], obj);
				}
			}
			break;
		}
		case MOMLDO: {
			int mode = oprands[0];
			switch (mode) {
			case 0: {// create moml element
				String tag = getStringVar(oprands[2]).toLowerCase();
				String id = getStringVar(oprands[3]);
				Sv3Element elem = null;
				if (tag.equals("div")) {
					elem = factory.createDiv();
				} else if (tag.equals("button")) {
					elem = factory.createButton();
				} else if (tag.equals("input")) {
					elem = factory.createInput();
				} else if (tag.equals("select")) {
					elem = factory.createSelect();
				} else if (tag.equals("span")) {
					elem = factory.createText();
				} else if (tag.equals("img")) {
					elem = factory.createImage();
				} else if (tag.equals("checkbox")) {
					elem = factory.createCheckbox();
				}
				if (elem != null) {
					elem.setIdRestricted(id);
					setVar(oprands[1], elem);
				}

				break;
			}
			case 1: {// delete moml element
				String id = getStringVar(oprands[1]);
				Sv3Element element = page.getElementById(id);
				if (element != null) {
					element.removeFromParent();
					page.deregisterElement(element);
					if (page != null) {
						page.didChangeAttrib(element, "child", true, true);
					}
				}
				break;
			}
			case 2: {// copy moml element
				Sv3Element sourceElement = getElementVar(oprands[2]);
				String destId = getStringVar(oprands[3]);
				setVar(oprands[1], sourceElement.clone(destId));
				break;
			}
			case 3: {// register event callback
				Sv3Element element = getElementVar(oprands[1]);
				int event = oprands[2];
				int function = getIntVar(oprands[3]);
				regCallback(element, new Integer(event), new Integer(function));
				break;
			}
			case 4: {
				int insertMode = oprands[1];
				Sv3Div parent = (Sv3Div) getElementVar(oprands[2]);
				Sv3Element toInsert = getElementVar(oprands[3]);
				String id = getStringVar(oprands[4]);
				int count = parent.getChildren().size();
				for (int i = 0; i < count; i++) {
					Sv3Element child = (Sv3Element) parent.getChildren()
							.elementAt(i);
					if (id.equals(child.getId())) {
						if (insertMode == 0) {// insert before
							parent.insertChild(toInsert, i);
						} else if (insertMode == 1) {// insert after
							parent.insertChild(toInsert, i + 1);
						}
						if (parent.getPage() != null) {
							parent.getPage().didChangeAttrib(parent, "child",
									true, true);
						}
						break;
					}
				}

				break;
			}
			case 5: {
				MArray array = getArrayVar(oprands[1]);
				if (array != null && windowMenu != null) {
					windowMenu.setMenu(array);
				}
				break;
			}
			}
			break;
		}
		default: {
			result = false;
			break;
		}
		}
		return result;

	}

	boolean runCallback(Sv3Element element, Integer event) {
		boolean result = false;
		Hashtable elementCallback = (Hashtable) callbacks.get(element);
		if (elementCallback != null && elementCallback.containsKey(event)) {
			MilkCallback callback = ((MilkCallback) elementCallback.get(event));
			if (callback != null) {
				prepareCallParams();
				addCallParam(element);
				try {
					result = execute(callback) == 0 ? false : true;
				} catch (Exception t) {
				}

			}
		}

		return result;

	}

	void regCallback(Sv3Element element, Integer event, Integer callback) {
		Hashtable elementCallbacks = (Hashtable) callbacks.get(element);
		if (elementCallbacks == null) {
			elementCallbacks = new Hashtable();
			callbacks.put(element, elementCallbacks);
		}
		elementCallbacks.put(event, genCallback(callback.intValue(), null));
	}

	private long fingerPressedTime;
	private int pointPressedX, pointPressedY, pointDraggedY;
	private boolean pointerDragged = false;
	private int scrollYSpeed;

	public void handleFingerEvent(MFingerEvent finger) {
		if (windows.size() > 0) {
			getTopMostScene().handleFingerEvent(finger);
			return;
		}

		if (finger.getType() == Adaptor.POINTER_PRESSED) {// pressed
			fingerPressedTime = System.currentTimeMillis();
			pointPressedX = finger.getX() - contentRect.x;
			pointPressedY = finger.getY() - contentRect.y;

			lastHitElement = page.hit(pointPressedX, pointPressedY);
			if (lastHitElement != null && lastHitElement.canFocus()) {
				lastHitElement.setFocusedWithEvent(true);
			}

			pointDraggedY = 0;
			pointerDragged = false;
			scrollYSpeed = 0;
			if (windowMenu != null)
				windowMenu.pointerPressed(finger.getX(), finger.getY());

		} else if (finger.getType() == Adaptor.POINTER_DRAGGED) {
			pointerDragged = true;
			// System.out.println(" window->handleFingerEvent()->POINTER_DRAGGED:");
			int dy = 0;
			if (pointDraggedY == 0) {
				pointDraggedY = finger.getY() - contentRect.y;
				dy = pointDraggedY - pointPressedY;
			} else {
				dy = finger.getY() - pointDraggedY;
				pointDraggedY = finger.getY();
			}
			page.setScrollYSafely(page.getScrollY() - dy, contentRect.height);
			// Core.getInstance().wakeUp();
		} else if (finger.getType() == Adaptor.POINTER_RELEASED) {
			int releaseX = finger.getX() - contentRect.x;
			int releaseY = finger.getY() - contentRect.y;
            int dragDistanceY=Math.abs(releaseY-pointPressedY);
			boolean hit = false;
			Sv3Element hitElement = null;

			if (!pointerDragged || dragDistanceY < Adaptor.getInstance().getConfigHeight()/20) {

				hitElement = page.hit(releaseX, releaseY);
				hit = lastHitElement == hitElement && hitElement != null;
				if (lastHitElement != hitElement && lastHitElement != null
						&& lastHitElement.canFocus()) {
					lastHitElement.setFocusedWithEvent(false);
					lastHitElement = null;
				} else {
					if (lastHitElement != null && lastHitElement != hitElement) {
						lastHitElement.setFocusedWithEvent(false);
					}
				}
			}

			if (hit) {
				hitElement.trigger();
			} else {
				long timeTake = (System.currentTimeMillis() - fingerPressedTime);
				if (timeTake < 2000) {
					int dy = releaseY - pointPressedY;
					int speedY = -(int) (dy * 1000 / timeTake);
					// System.out.println(" window->handleFingerEvent()->speedY:"
					// + speedY / 10);
					if (Math.abs(speedY / 10) >= 20) {
						scrollYSpeed = speedY / 5;
					}
				}
			}
		}
	}

	private void update() {
		if (layoutTimer > 0 && System.currentTimeMillis() > layoutTimer) {
			doLayout();
		}
		if (windows.size() > 0) {
			int count = windows.size();
			for (int i = 0; i < count; i++) {
				Scene window = (Scene) windows.elementAt(i);
				if (window instanceof Window) {
					((Window) window).update();
				}
			}
		}
		if (windowEvents.size() > 0) {
			MWindowEvent we = (MWindowEvent) windowEvents.elementAt(0);
			windowEvents.removeElementAt(0);
			this.runCallback(we.element, we.event);
		}
		if (scrollYSpeed != 0) {
			// System.out.println(" window->update()->scrollYSpeed:"
			// + scrollYSpeed);
			int realySpeed = scrollYSpeed / 10;
			page.setScrollYSafely(page.getScrollY() + realySpeed,
					contentRect.height);
			if (scrollYSpeed > 0)
				scrollYSpeed -= 5;
			else {
				scrollYSpeed += 5;
			}
			if (Math.abs(scrollYSpeed) <= 3) {
				scrollYSpeed = 0;
			}
			// Core.getInstance().wakeUp();
		}
	}

	public void handleKeyEvent(MKeyEvent keyEvent) {
		if (isMenuOpen()) {
			windowMenu.onKeyEvent(keyEvent);
			return;
		}
		int keyCode = keyEvent.getCode();
		if (keyEvent.getType() == Adaptor.KEYSTATE_PRESSED) {

			if (keyCode >= Adaptor.KEY_RIGHT && keyCode <= Adaptor.KEY_UP) {
				Sv3Element nextFocus = page.navigate((byte) keyCode);
				boolean shouldScroll = false;
				int scrollLength = contentRect.height / 8;
				if (nextFocus != null) {
					if (keyCode == Adaptor.KEY_UP
							|| keyCode == Adaptor.KEY_DOWN) {
						int nextFocusY = nextFocus.getAbsoluteRect().y
								- page.getScrollY();
						Rect temp = new Rect(0, nextFocusY, 1,
								nextFocus.getRect().height);
						Rect screen = new Rect(0, 0, contentRect.width,
								contentRect.height);
						if (temp.overlaps(screen) || screen.contains(temp)) {
							nextFocus.setFocusedWithEvent(true);
							if (!screen.contains(temp)) {
								shouldScroll = true;
							}
						} else {
							shouldScroll = true;
						}
					} else {
						nextFocus.setFocusedWithEvent(true);
					}

				} else {
					if (keyCode == Adaptor.KEY_UP
							|| keyCode == Adaptor.KEY_DOWN) {
						shouldScroll = true;
					}
				}

				if (shouldScroll) {
					if (keyCode == Adaptor.KEY_UP) {// up
						page.setScrollYSafely(page.getScrollY() - scrollLength,
								contentRect.height);
					} else if (keyCode == Adaptor.KEY_DOWN) {// down
						page.setScrollYSafely(page.getScrollY() + scrollLength,
								contentRect.height);
					}
				}

			}
			if (keyCode == Adaptor.KEY_FIRE) {
				if (page.getCurrentFocus() != null) {
					page.getCurrentFocus().trigger();
				}
			} else {
				if (windowMenu != null)
					windowMenu.keyPressed(keyCode);
			}
		} else if (keyEvent.getType() == Adaptor.KEYSTATE_RELEASED) {

		}
	}

	void handleResourceEvent(MResourceEvent resourceEvent) {
		if (this.onResourceLoaded != null) {
			prepareCallParams();
			addCallParam(resourceEvent.src);
			try {
				execute(this.onResourceLoaded);
			} catch (Exception t) {
			}
		}
		MilkImageUtil iu = (MilkImageUtil) Sv3Element.imageUtil;
		if (iu != null) {
			iu.imageArrive(resourceEvent.src, resourceEvent.width,
					resourceEvent.height, resourceEvent.sourceHash);
		}
	}

	public boolean willTrigger(Sv3Element element) {
		windowEvents.addElement(new MWindowEvent(element, new Integer(
				Window.event_trigger)));
		return false;
	}

	public boolean willFocus(Sv3Element element) {
		windowEvents.addElement(new MWindowEvent(element, new Integer(
				Window.event_will_focus)));
		return false;
	}

	public void didFocus(Sv3Element element) {

	}

	public boolean willEnable(Sv3Element elment) {
		// TODO Auto-generated method stub
		return false;
	}

	public void didEnable(Sv3Element element) {
		// TODO Auto-generated method stub

	}

	public boolean willDisable(Sv3Element elment) {
		// TODO Auto-generated method stub
		return false;
	}

	public void didChangeAttrib(Sv3Element elem, String name,
			boolean needsRepaint, boolean needsReplayout) {
		if (needsReplayout) {
			layoutTimer = System.currentTimeMillis() + 50;
			// if (layoutTask != null) {
			// layoutTask.cancel();
			// }
			// layoutTask = Adaptor.uiFactory.createMilkTask(this);
			// Adaptor.milk.scheduleTask(layoutTask, 50);
		}
	}

	void layoutWindow() {
		this.doLayout();
		layoutTimer = System.currentTimeMillis() + 50;
		// if (layoutTask != null) {
		// layoutTask.cancel();
		// }
		// layoutTask = Adaptor.uiFactory.createMilkTask(this);
		// Adaptor.milk.scheduleTask(layoutTask, 50);
	}

	public void didDisable(Sv3Element element) {
		// TODO Auto-generated method stub

	}

	public boolean willUnfocus(Sv3Element element) {
		windowEvents.addElement(new MWindowEvent(element, new Integer(
				Window.event_will_unfocus)));
		return false;
	}

	public void didUnfocus(Sv3Element element) {
		// TODO Auto-generated method stub

	}

	public boolean willChangeValue(Sv3Element element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void didChangeValue(Sv3Element element) {

	}

	public void didFinishInput(Sv3Input element) {
		// TODO Auto-generated method stub

	}

	protected void doMore() {
		super.doMore();
		update();
	}

	public void hideNotify() {
		scrollYSpeed = 0;
	}

	public void doTask() {
		this.doLayout();
	}

}
