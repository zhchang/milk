package milk.ui.gesture;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class Gesture {
	
	private String tag="Gesture";
	private GestureDetector detector;
	private ArrayList<GestureListener> listenerList;
	private float screenWidth;
	private float screenHeight;
	private static Gesture gesture;
	
	public static Gesture getInstance() {
		if (gesture == null)
			gesture = new Gesture();
		return gesture;
	}
	
	private Gesture(){
		listenerList = new ArrayList<GestureListener>();
	}
	
	public void init(Context context) {
		detector = new GestureDetector(context, innerCallback);
	}

	public void setScreenSizePixels(float width,float height){
		this.screenWidth=width;
		this.screenHeight=height;
		System.out.println("------screenWidth:"+ screenWidth+"/ screenHeight"+this.screenHeight);
	}
	
	public void addGestureListener(GestureListener listener) {
		if (!listenerList.contains(listener) && listener != null) {
			listenerList.add(listener);
		}
	}
	
	public void removeGestureListener(GestureListener listener) {
		if (listenerList.contains(listener) && listener != null)
		    listenerList.remove(listener);
	}
	
	public void dispatchTouchEvent(MotionEvent ev) {
		if (detector != null && listenerList.size() > 0)
			detector.onTouchEvent(ev);
	}
	
	private GestureDetector.OnGestureListener innerCallback = new GestureDetector.SimpleOnGestureListener() {
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) {
			final float distanceX = e2.getX() - e1.getX();
			final float distanceY = e2.getY() - e1.getY();
			final float detectMinValue = screenWidth / 20;
			Log.i(tag, "-------------onFling----------velocityX:"+velocityX+"/ velocityY:"+velocityY);
			if (Math.abs(distanceX) > Math.abs(distanceY)) {
				if (distanceX > detectMinValue || distanceX < -detectMinValue && !onScrollCall) {
					if (distanceX > 0) {// right
//						Log.i(tag, "-------------onFling right----------");
						handleSlide(GestureListener.SLIDE_RIGHT,Math.abs(distanceX),Math.abs(velocityX));
					} else if (distanceX < 0) {
//						Log.i(tag, "-------------onFling left-----------");
						handleSlide(GestureListener.SLIDE_LEFT,Math.abs(distanceX),Math.abs(velocityX));
					}
				}
			} else {
				if (distanceY > detectMinValue || distanceY < -detectMinValue&& !onScrollCall) {
					if (distanceY > 0) {
						handleSlide(GestureListener.SLIDE_DOWN,Math.abs(distanceY),Math.abs(velocityY));
					} else {
						handleSlide(GestureListener.SLIDE_UP,Math.abs(distanceY),Math.abs(velocityY));
					}
				}
			}
//			onScrollCall=false;
//			scrollX = 0;
//			scrollY = 0;
			return true;    
		}
		
//		private float scrollX,scrollY;
		private final boolean onScrollCall=false;
//		public boolean onScroll(MotionEvent e1, MotionEvent e2,
//				float distanceX, float distanceY) {
//			scrollX+=distanceX;
//			scrollY+=distanceY;
//			
//			final float detectMinValue = screenWidth / 5;
//			Log.i(tag, "-------------onScroll-------------");
//			if (Math.abs(scrollX) > Math.abs(scrollY)) {
//				if (scrollX > detectMinValue || scrollX < -detectMinValue) {
//					float x = -scrollX;
//					if (x > 0) {// right
////						Log.i(tag, "-------------onScroll right--------");
//						handleSlide(GestureListener.SLIDE_RIGHT,Math.abs(scrollX),0);
//					} else if (x < 0) {
////						Log.i(tag, "-------------onScroll left----------");
//						handleSlide(GestureListener.SLIDE_LEFT,Math.abs(scrollX),0);
//					}
//					scrollX = 0;
//					scrollY = 0;
//					onScrollCall = true;
//				} else {
//					onScrollCall = false;
//				}
//			}
//			else{
//				if (scrollY > detectMinValue || scrollY < -detectMinValue) {
//					float y = -scrollY;
//					if (y > 0) {
//						handleSlide(GestureListener.SLIDE_DOWN,Math.abs(scrollY),0);
//					} else {
//						handleSlide(GestureListener.SLIDE_UP,Math.abs(scrollY),0);
//					}
//					scrollX = 0;
//					scrollY = 0;
//					onScrollCall = true;
//				} else {
//					onScrollCall = false;
//				}
//			}
//			return true;
//		} 
		
		
	};
	
	private void handleSlide(int direct,float distance,float speed) {
		for (int i = listenerList.size() - 1; i >= 0; i--) {
			listenerList.get(i).onSlide(direct,distance,speed);
		}
	}


}
