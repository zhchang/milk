package milk.ui;

import milk.implement.EditorSetting;

import milk.ui.model.AndroidPool;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class HandlerMsg
{
	private static final int REFRESH = 1;
	private static final int SHOW_EDIT_DIALOG = 2;
	private static final int INPUTBOX = 3;
	private static final int HIDE_INPUT = 4;
	private static final int FOCUS_INPUTBOX = 5;
	private static final int INSERT_FACE = 6;
	private static final int ALERT_DIALOG = 7;
	
	private static final int HIDE_SOFT_INPUTBOX = 8;
	private static final int SET_INPUT = 9;
	private static final int LAYOUT_INPUTBOX = 10;

	public static void handler(Message msg)
	{
		Bundle b=null;
		switch (msg.what)
		{
		case REFRESH:
			if (UIHelper.milk.canvas != null)
			{
//				NativeUIManager.getInstance().layoutEdit();
				UIHelper.milk.canvas.postInvalidate();
				//.invalidate();
			}
			break;

		case SHOW_EDIT_DIALOG:
			b=msg.getData();
			NativeUIManager.getInstance().showEditDialog(b.getString("title"), b.getString("text"));
			AndroidPool.recovery(b);
			break;
		case INPUTBOX:
			b=msg.getData();
			NativeUIManager.getInstance().updateInputBox(b.getInt("x"), b.getInt("y"), b.getInt("w"), b.getInt("h"), b.getInt("bgColor"),b.getString("text"));
			AndroidPool.recovery(b);
			break;
		case HIDE_INPUT:
			NativeUIManager.getInstance().hideInputBox();
			break;
		case FOCUS_INPUTBOX:
			NativeUIManager.getInstance().setEditFocus();
		case INSERT_FACE:
			b=msg.getData();
			NativeUIManager.getInstance().insertInputText(b.getString("face"));
			AndroidPool.recovery(b);
			break;
		case ALERT_DIALOG:
			b=msg.getData();
			NativeUIManager.showAlert(b.getString("text"));
			AndroidPool.recovery(b);
			break;
		case SET_INPUT:
			b=msg.getData();
			NativeUIManager.getInstance().setInputText(b.getString("text"));
			AndroidPool.recovery(b);
			break;
		case HIDE_SOFT_INPUTBOX:
			NativeUIManager.getInstance().hideSoftInput();
//			UIHelper.milk.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			break;
		case LAYOUT_INPUTBOX:
			NativeUIManager.getInstance().layoutInputBox();
			break;
		}

	}


	
	public static void setInputText(String inputText)
	{
		if (inputText == null)
		{
			inputText = "";
		}

		Message m = Message.obtain();
		Bundle b = AndroidPool.getBundle();
		m.what = SET_INPUT;
		b.putString("text", inputText);
		m.setData(b);
		UIHelper.milk.getHandler().sendMessage(m);
	}

	public static void showEditDialog(String title, String text)
	{
		Message message = Message.obtain();
		message.what = HandlerMsg.SHOW_EDIT_DIALOG;
		Bundle b = AndroidPool.getBundle();
		b.putString("title", title);
		b.putString("text", text);
		message.setData(b);
		UIHelper.milk.getHandler().sendMessage(message);
	}

	public static void refresh()
	{
		UIHelper.milk.getHandler().sendEmptyMessage(REFRESH);
	}

	public static void hideSoftInput()
	{
		UIHelper.milk.getHandler().sendEmptyMessage(HIDE_SOFT_INPUTBOX);
		
	}
	
	public static void layoutInputFrame()
	{
		UIHelper.milk.getHandler().sendEmptyMessage(LAYOUT_INPUTBOX);
	}

	
	public static void showInput(EditorSetting setting)
	{
		NativeUIManager.getInstance().updateEditorSetting(setting);
		Message message = Message.obtain();
		message.what = HandlerMsg.INPUTBOX;
		Bundle b = AndroidPool.getBundle();
		b.putInt("x", setting.x);
		b.putInt("y", setting.y);
		b.putInt("w", setting.width);
		b.putInt("h", setting.height);
		b.putInt("bgColor", setting.bgColor);
		if(setting.receiver!=null&&setting.receiver.getInitText()!=null)
		{
			b.putString("text", setting.receiver.getInitText());
		}
		else {
			b.putString("text", "");
		}
		
		message.setData(b);
		UIHelper.milk.getHandler().sendMessage(message);
	}

	public static void hideInput()
	{
		UIHelper.milk.getHandler().sendEmptyMessage(HandlerMsg.HIDE_INPUT);
	}

	public static void focusInput()
	{
		UIHelper.milk.getHandler().sendEmptyMessage(HandlerMsg.FOCUS_INPUTBOX);
	}

	public static void insertFace(String faceName)
	{
		Message message = Message.obtain();
		message.what = HandlerMsg.INSERT_FACE;
		Bundle b = AndroidPool.getBundle();
		b.putString("face", faceName);
		message.setData(b);
		UIHelper.milk.getHandler().sendMessage(message);
	}

	public static void showAlert(String text)
	{
		Message msg = Message.obtain();
		msg.what = HandlerMsg.ALERT_DIALOG;
		Bundle b = AndroidPool.getBundle();
		b.putString("text", text);
		msg.setData(b);
		UIHelper.milk.getHandler().sendMessage(msg);
	}
}
