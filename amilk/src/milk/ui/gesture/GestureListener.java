package milk.ui.gesture;

public interface GestureListener {
	
	int SLIDE_LEFT = 1;
	int SLIDE_RIGHT = 2;
	int SLIDE_UP = 3;
	int SLIDE_DOWN = 4;

	void onSlide(int direct,float distance,float velocity);
	
}
