package milk.ui.graphics;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
public abstract class GameView extends View
{
	public GameView(Context context)
	{
		super(context);
	}
	protected abstract void onDraw(Canvas canvas);
	public abstract boolean onKeyDown(int keyCode);
	public abstract boolean onKeyUp(int keyCode);
	public abstract boolean dispatchTouchEvent(MotionEvent ev);
}

