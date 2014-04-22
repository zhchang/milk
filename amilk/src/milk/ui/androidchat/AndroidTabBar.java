package milk.ui.androidchat;

import milk.implement.Adaptor;
import milk.implement.IMEvent.MKeyEvent;
import milk.ui2.MilkGraphics;

public class AndroidTabBar {
	
	private AndroidTab tab[];
	private int focus=0;
	private AndroidTabBarListener tabListener;
	private int tabBarY;
	
	AndroidTabBar(int tabY) {
		tab = new AndroidTab[4];
		tabBarY=tabY;
		init();
		focusChanged();
	}
	
	
	private void init(){
		for (int i = 0; i < tab.length; i++) {
			tab[i]=new AndroidTab(AndroidResourceManager.chat[i],AndroidResourceManager.chatFocus[i]);
			tab[i].setPosition(i, tabBarY);
		}
	}
	
	void setNameList(String name[]){
		for (int i = 0; i < tab.length&&i<name.length; i++) {
			tab[i].setTabName(name[i]);
		}
	}

	void setTabListener(AndroidTabBarListener l) {
		tabListener = l;
	}

	void setFocus(int focus) {
		this.focus = focus;
		focusChanged();
	}

	void draw(MilkGraphics g) {
		for (int i = 0; i < tab.length; i++) {
			if (i != focus)
				tab[i].draw(g);
		}
		tab[focus].draw(g);
	}

	void handleKeyEvent(MKeyEvent key) {
		int type = key.getType();
		int keyCode = key.getCode();
		if (type == Adaptor.KEYSTATE_PRESSED) {
			switch (keyCode) {
			case Adaptor.KEY_LEFT:
				if (focus > 0) {
					focus--;
					tabListener.focusTabChange(focus);
					focusChanged();
				}
				break;
			case Adaptor.KEY_RIGHT:
				if (focus < tab.length - 1) {
					focus++;
					tabListener.focusTabChange( focus);
					focusChanged();
				}
				break;
			}
		}
	}

	void pointerPressed(int x, int y) {
		lastPointerDraggedX = 0;
		for (int i = 0; i < tab.length; i++) {
			if (tab[i].isTouch(x, y)) {
				focus = i;
				tabListener.focusTabChange(focus);
				focusChanged();
				return;
			}
		}
	}
	
	boolean onMoveFocus(int dx) {
		if (focus + dx >= 0 && focus + dx < tab.length) {
			focus = focus + dx;
			tabListener.focusTabChange(focus);
			focusChanged();
			return true;
		}
		return false;
	}

	private int lastPointerDraggedX;

	void pointerDragged(int x, int y) {
		if (y > 0 && y < tabBarY + AndroidTab.getTabHeight()) {
			if (lastPointerDraggedX == 0) {
				lastPointerDraggedX = x;
			} else if (Math.abs(lastPointerDraggedX - x) >= AndroidTab.getTabWidth()/2) {
				if (x > lastPointerDraggedX) {// right
					if (focus < tab.length - 1) {
						focus++;
						tabListener.focusTabChange( focus);
					}
				} else {
					if (focus > 0) {
						focus--;
						tabListener.focusTabChange(focus);
					}
				}
				focusChanged();
				lastPointerDraggedX = x;
			}
		}
	}

	void pointerReleased(int x, int y) {
		lastPointerDraggedX = 0;
	}

	private void focusChanged(){
		for (int i = 0; i < tab.length; i++)
			tab[i].setFocus(false);
		tab[focus].setFocus(true);
	}

}
