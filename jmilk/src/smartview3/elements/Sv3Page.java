package smartview3.elements;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;

import java.util.Hashtable;
import java.util.Vector;

import mobon.MobonException;
import mobon.MobonReader;
import smartview3.layout.LayoutContext;
import smartview3.layout.Rect;
import smartview3.render.IRenderContext;
import smartview3.utils.IEventHandler;

/**
 * 
 * @author luyx
 */
public class Sv3Page implements IEventHandler {
	public static final byte TAG_Root = 1, TAG_ByteCode = 2, TAG_PageHash = 3,
			TAG_Url = 4, TAG_Title = 5, TAG_CacheLevel = 6,
			TAG_CacheImages = 7, TAG_Debug = 9, TAG_DisableScrolling = 10,
			TAG_ResourceIntensive = 11, TAG_BgColor = 12, TAG_FocusColor = 13,
			TAG_FocusBgColor = 14, TAG_FocusBgColor2 = 15,
			TAG_FocusBdColor = 16, TAG_FocusBdSize = 17;
	public static final byte CACHE_NONE = 0, CACHE_IN_MEM = 1,
			CACHE_ON_DISK = 2;
	public static final int DEFAULT_BG_COLOR = 0xffffff;

	// attributes
	private Sv3Div root;
	private byte[] byteCode;
	private byte[] pageHash;
	private String url;
	private String title;
	private byte cacheLevel = CACHE_ON_DISK;
	private boolean cacheImages = true;
	private boolean debug;
	private boolean disableScrolling;
	private boolean resourceIntensive;
	private int bgColor = DEFAULT_BG_COLOR;
	private int focusColor = -1;
	private int focusBgColor = -1;
	private int focusBgColor2 = -1;
	private int focusBdColor = -1;
	private short focusBdSize = 0;

	private final Hashtable idTable = new Hashtable();

	protected Rect scrollRect = new Rect();

	// ============
	private int scrollY = 0;
	private IEventHandler eventHandler;

	public void clearMem() {
		eventHandler = null;
		root.clearMem();
		root = null;
	}

	public void readAttrFromMobon(MobonReader r, int key) throws IOException,
			MobonException {
		switch (key) {
		case TAG_Root:
			setRoot((Sv3Div) r.read());
			break;
		case TAG_ByteCode:
			byteCode = r.readBytes();
			break;
		case TAG_PageHash:
			pageHash = r.readBytes();
			break;
		case TAG_Url:
			url = r.readStringOrNull();
			break;
		case TAG_Title:
			title = r.readStringOrNull();
			break;
		case TAG_CacheLevel:
			cacheLevel = (byte) r.readInt();
			break;
		case TAG_CacheImages:
			cacheImages = r.readBoolean();
			break;
		case TAG_Debug:
			debug = r.readBoolean();
			break;
		case TAG_DisableScrolling:
			disableScrolling = r.readBoolean();
			break;
		case TAG_ResourceIntensive:
			resourceIntensive = r.readBoolean();
			break;
		case TAG_BgColor:
			setBgColor(r.readInt());
			break;
		case TAG_FocusColor:
			setFocusColor(r.readInt());
			break;
		case TAG_FocusBgColor:
			setFocusBgColor(r.readInt());
			break;
		case TAG_FocusBgColor2:
			setFocusBgColor2(r.readInt());
			break;
		case TAG_FocusBdColor:
			setFocusBdColor(r.readInt());
			break;
		case TAG_FocusBdSize:
			setFocusBdSize((short) r.readInt());
			break;
		default:
			r.read(); // read the value no matter what it is, for forward
						// compatibility
			break;
		}
	}

	public boolean setStrAttrib(String key, String value) {
		key = key.toLowerCase();
		if ("title".equals(key)) {
			setTitle(value);
		} else if ("bgcolor".equals(key)) {
			setBgColor(Sv3Element.strToColor(value));
		} else if ("focuscolor".equals(key)) {
			setFocusColor(Sv3Element.strToColor(value));
		} else if ("focusbgcolor".equals(key)) {
			setFocusBgColor(Sv3Element.strToColor(value));
		} else if ("focusbgcolor2".equals(key)) {
			setFocusBgColor2(Sv3Element.strToColor(value));
		} else {
			return false;
		}
		return true;
	}

	public boolean setIntAttrib(String key, int value) {
		key = key.toLowerCase();
		if ("bgcolor".equals(key)) {
			setBgColor(value);
		} else if ("focuscolor".equals(key)) {
			setFocusColor(value);
		} else if ("focusbgcolor".equals(key)) {
			setFocusBgColor(value);
		} else if ("focusbgcolor2".equals(key)) {
			setFocusBgColor2(value);
		} else if ("focusbdcolor".equals(key)) {
			setFocusBdColor(value);
		} else if ("focusbdsize".equals(key)) {
			setFocusBdSize((short) value);
		} else {
			return false;
		}
		return true;
	}

	public Object getAttrib(String key) {
		key = key.toLowerCase();
		Integer intResult = null;
		String strResult = null;
		if ("title".equals(key)) {
			strResult = title;
		} else if ("bgcolor".equals(key)) {
			intResult = new Integer(bgColor);
		} else if ("focuscolor".equals(key)) {
			intResult = new Integer(focusColor);
		} else if ("focusbgcolor".equals(key)) {
			intResult = new Integer(focusBgColor);
		} else if ("focusbgcolor2".equals(key)) {
			intResult = new Integer(focusBgColor2);
		} else if ("focusbdcolor".equals(key)) {
			intResult = new Integer(focusBdColor);
		} else if ("focusbdsize".equals(key)) {
			intResult = new Integer(focusBdSize);
		}
		return intResult != null ? (Object) intResult : strResult;
	}

	public void layout(LayoutContext ctx) {
		root.layout(ctx);
	}

	public Sv3Div getRoot() {
		return root;
	}

	public void setRoot(Sv3Div root) {
		root._setPage(this);
		this.root = root;
	}

	public Rect getScrollRect(int canvasWidth, int canvasHeight) {
		Rect r = root.getRect();
		scrollRect.x = (short) (canvasWidth - 7);
		scrollRect.width = 4;
		if (canvasHeight < r.height) {
			scrollRect.y = scrollY * canvasHeight / r.height;
			scrollRect.height = canvasHeight * canvasHeight / r.height;
			if (scrollRect.height < 3) {
				scrollRect.height = 3;
			}

		} else {
			scrollRect.y = 0;
			scrollRect.height = canvasHeight - 1;
		}
		return scrollRect;
	}

	public void render(IRenderContext ctx) {
		root.render(ctx);
	}

	public int getMaxScroll(int canvasHeight) {
		int max = 0;
		if (root != null) {
			Rect rect = root.getRect();
			if (rect != null) {
				max = rect.height - canvasHeight;
				if (max < 0)
					max = 0;
				return max;
			}
		}
		return max;
	}

	public void setScrollYSafely(int scroll, int canvasHeight) {
		if (scroll < 0)
			scroll = 0;
		int max = this.getMaxScroll(canvasHeight);
		if (scroll > max)
			scroll = max;
		scrollY = scroll;
	}

	public int getScrollY() {
		return scrollY;
	}

	public Sv3Element hit(int x, int y) {
		if (root == null) {
			return null;
		} else {
			return root.hit(x, y + scrollY);
		}
	}

	public byte[] getByteCode() {
		return byteCode;
	}

	public void setByteCode(byte[] byteCode) {
		this.byteCode = byteCode;
	}

	public byte[] getPageHash() {
		return pageHash;
	}

	public void setPageHash(byte[] pageHash) {
		this.pageHash = pageHash;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public byte getCacheLevel() {
		return cacheLevel;
	}

	public void setCacheLevel(byte cacheLevel) {
		this.cacheLevel = cacheLevel;
	}

	public boolean isCacheImages() {
		return cacheImages;
	}

	public void setCacheImages(boolean cacheImages) {
		this.cacheImages = cacheImages;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public boolean isDisableScrolling() {
		return disableScrolling;
	}

	public void setDisableScrolling(boolean disableScrolling) {
		this.disableScrolling = disableScrolling;
	}

	public boolean isResourceIntensive() {
		return resourceIntensive;
	}

	public void setResourceIntensive(boolean resourceIntensive) {
		this.resourceIntensive = resourceIntensive;
	}

	public int getBgColor() {
		return bgColor;
	}

	public void setBgColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public int getFocusColor() {
		return focusColor;
	}

	public void setFocusColor(int focusColor) {
		this.focusColor = focusColor;
	}

	public int getFocusBgColor() {
		return focusBgColor;
	}

	public void setFocusBgColor(int focusBgColor) {
		this.focusBgColor = focusBgColor;
	}

	public int getFocusBgColor2() {
		return focusBgColor2;
	}

	public void setFocusBgColor2(int focusBgColor2) {
		this.focusBgColor2 = focusBgColor2;
	}

	public int getFocusBdColor() {
		return focusBdColor;
	}

	public void setFocusBdColor(int focusBdColor) {
		this.focusBdColor = focusBdColor;
	}

	public short getFocusBdSize() {
		return focusBdSize;
	}

	public void setFocusBdSize(short focusBdSize) {
		this.focusBdSize = focusBdSize;
	}

	public final void setScrollY(int scrollY) {
		this.scrollY = scrollY;
	}

	public Sv3Element getElementById(String id) {
		return (Sv3Element) idTable.get(id);
	}

	public void registerElement(Sv3Element elem) {
		idTable.put(elem.getId(), elem);
	}

	public void deregisterElement(Sv3Element elem) {
		idTable.remove(elem.getId());
	}

	public IEventHandler getEventHandler() {
		return eventHandler;
	}

	public void setEventHandler(IEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public boolean willTrigger(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willTrigger(element);
		} else {
			return false;
		}
	}

	public boolean willFocus(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willFocus(element);
		} else {
			return false;
		}
	}

	public void didFocus(Sv3Element element) {
		if (eventHandler != null) {
			eventHandler.didFocus(element);
		}
	}

	public boolean willUnfocus(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willUnfocus(element);
		} else {
			return false;
		}
	}

	public boolean willEnable(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willEnable(element);
		} else {
			return false;
		}
	}

	public void didEnable(Sv3Element element) {
		if (eventHandler != null) {
			eventHandler.didEnable(element);
		}
	}

	public boolean willDisable(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willDisable(element);
		} else {
			return false;
		}
	}

	public void didDisable(Sv3Element element) {
		if (eventHandler != null) {
			eventHandler.didDisable(element);
		}
	}

	public void didUnfocus(Sv3Element element) {
		if (eventHandler != null) {
			eventHandler.didUnfocus(element);
		}
	}

	public boolean willChangeValue(Sv3Element element) {
		if (eventHandler != null) {
			return eventHandler.willChangeValue(element);
		} else {
			return false;
		}
	}

	public void didChangeValue(Sv3Element element) {
		if (eventHandler != null) {
			eventHandler.didChangeValue(element);
		}
	}

	public void didFinishInput(Sv3Input element) {
		if (eventHandler != null) {
			eventHandler.didFinishInput(element);
		}
	}

	public void didChangeAttrib(Sv3Element elem, String name,
			boolean needsRepaint, boolean needsRelayout) {
		if (eventHandler != null) {
			eventHandler.didChangeAttrib(elem, name, needsRepaint,
					needsRelayout);
		}
	}

	public void translate(Hashtable units) {
		Sv3Div root = getRoot();
		if (root != null) {
			translate(root, units);
		}
	}

	private void translate(Sv3Element elem, Hashtable units) {
		if (elem instanceof Sv3Text) {
			String translated = getTranslatedText(units,
					((Sv3Text) elem).getText());
			((Sv3Text) elem).setText(translated);
		} else if (elem instanceof Sv3Select) {
			String[] options = ((Sv3Select) elem).getOptions();
			// only translate text (odd index) but not value (even index)
			for (int i = 1; i < options.length; i += 2) {
				options[i] = getTranslatedText(units, options[i]);
			}
		} else if (elem instanceof Sv3Button) {
			String translated = getTranslatedText(units,
					((Sv3Button) elem).getText());
			((Sv3Button) elem).setText(translated);
		} else if (elem instanceof Sv3Input) {
			String translated = getTranslatedText(units,
					((Sv3Input) elem).getText());
			((Sv3Input) elem).setText(translated);
			translated = getTranslatedText(units,
					((Sv3Input) elem).getTextOnEmpty());
			((Sv3Input) elem).setTextOnEmpty(translated);
		} else if (elem instanceof Sv3Image) {
			String translated = getTranslatedText(units,
					((Sv3Image) elem).getAlt());
			((Sv3Image) elem).setAlt(translated);
		}
		String translated = getTranslatedText(units, elem.getTips());
		elem.setTips(translated);
		translated = getTranslatedText(units, elem.getConfirmation());
		elem.setConfirmation(translated);

		if (elem instanceof Sv3Div) {
			Vector children = ((Sv3Div) elem).getChildren();
			int count = children.size();
			for (int i = 0; i < count; ++i) {
				Object c = children.elementAt(i);
				translate((Sv3Element) c, units);
			}
		}
	}

	private String getTranslatedText(Hashtable units, String text) {
		if (text == null) {
			return null;
		} else {
			String trans = (String) units.get(text);
			if (trans == null) {
				return text;
			} else {
				return trans;
			}
		}
	}
}
