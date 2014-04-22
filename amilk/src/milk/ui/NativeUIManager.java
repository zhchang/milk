package milk.ui;

import java.util.Timer;

import java.util.TimerTask;

import milk.implement.Adaptor;
import milk.implement.EditorSetting;
import milk.ui.graphics.Font;
import milk.ui.internal.ScreenControl;
import milk.ui2.InputListener;
import milk.ui2.MilkDisplayable;
import milk.utils.StringUtils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AbsoluteLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class NativeUIManager {

	private static NativeUIManager instance = null;
	private static String tag = NativeUIManager.class.getName();

	private EditText edit;

	public EditorSetting es;

	private EditText textBox;

	private InputListener inputListener;

	private TextWatcher tw;

	public static NativeUIManager getInstance() {
		if (instance == null) {
			instance = new NativeUIManager();
		}
		return instance;
	}

	private NativeUIManager() {
	}

	public void updateInputListener(milk.ui2.InputListener inputListener) {
		this.inputListener = inputListener;
	}

	public void updateEditorSetting(EditorSetting es) {
		this.es = es;
	}

	public void createInputBox() {
		if (edit == null) {
			AbsoluteLayout.LayoutParams params = new AbsoluteLayout.LayoutParams(
					0, 0, 0, 0);
			edit = new EditText(UIHelper.milk);
			edit.setFocusable(true);
			// edit.setSingleLine();
			edit.setMaxLines(6);
			edit.setVisibility(View.GONE);
			edit.setLayoutParams(params);
			edit.setAutoLinkMask(Linkify.ALL);
			edit.setPadding(0, 0, 0, 0);

		}

		OnEditorActionListener editLInistener = new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// es.receiver.updateInput(edit.getText().toString());
				Log.i("tag", "actionid " + actionId);

				if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
						|| actionId == EditorInfo.IME_ACTION_DONE) {
					Log.i(tag, "click enter");
					Adaptor.getInstance().onKey(Adaptor.KEY_MENU,
							Adaptor.KEYSTATE_PRESSED);
					Adaptor.getInstance().onKey(Adaptor.KEY_MENU,
							Adaptor.KEYSTATE_RELEASED);
					HandlerMsg.hideSoftInput();
					AbsoluteLayout.LayoutParams l = (AbsoluteLayout.LayoutParams) edit
							.getLayoutParams();
					Log.i(tag, "editLInistener x " + l.x + " y " + l.y);
				}
				edit.clearFocus();
				edit.requestLayout();
				edit.invalidate();
				return false;
			}
		};

		edit.setTextColor(Color.BLACK);
		edit.setOnEditorActionListener(editLInistener);

		tw = new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// edit.invalidate();
				edit.requestLayout();
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				edit.requestLayout();
			}

			@Override
			public void afterTextChanged(Editable s) {
				edit.requestLayout();
				if (edit.getText() != null) {
					if (es.receiver != null) {
						es.receiver.updateInput(edit.getText().toString());
					} else {
					}
				}
			}
		};
		if (es != null) {
			if (es.maxlength > 0) {
				edit.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
						es.maxlength) });
			}
			if (es.height > 0) {
				edit.setTextSize(es.height);
			}
		}
		edit.addTextChangedListener(tw);

		((ViewGroup) UIHelper.milk.canvas.getParent()).addView(edit);

	}

	private int getHeightInterval(int i) {
		if (UIHelper.milk.screenType == ScreenControl.LANDSCAPE) {
			return i * Adaptor.getInstance().height / 240;
		} else {
			return i * Adaptor.getInstance().height / 320;
		}

	}

	// private float editSize=Font.SIZE_SMALL*UIHelper.milk.heightPer;
	public void updateInputBox(int x, int y, int w, int h, int bg, String text) {
		// Log.i(tag, "updateInputBox x " + x + " y " + y + " w " + w + " h " +
		// h + " bg " + bg + " text " + text);
		if (edit == null) {
			createInputBox();
		}

		hideInputBox();

		if (edit != null) {
			// Log.i("sda", "editSize "+Font.SIZE_SMALL);
			// edit.setTextSize(Font.SIZE_SMALL);

			edit.setTextColor(Color.BLACK);
//			if (UIHelper.milk.isScale) {
//				x *= UIHelper.milk.widthPer;
//				y *= UIHelper.milk.heightPer;
//				w *= UIHelper.milk.widthPer;
//				h *= UIHelper.milk.heightPer;
//			}
			w=ViewAdapter.getInstance().toDeviceViewWidth(w);
            h=ViewAdapter.getInstance().toDeviceViewHeight(h);
			if (h < Font.SIZE_SMALL) {
				edit.setTextSize(h - getHeightInterval(4));
			} else {
				edit.setTextSize(Font.SIZE_SMALL - getHeightInterval(4));
			}

			if (es != null && es.setTextSize && es.textSize > 0) {
				edit.setTextSize(es.textSize);
			}

			AbsoluteLayout.LayoutParams params = ((AbsoluteLayout.LayoutParams) edit
					.getLayoutParams());
//			params.x = x + UIHelper.milk.xOffset;
			params.x = ViewAdapter.getInstance().toDeviceViewX(x);
//			params.y = y + UIHelper.milk.yOffset;
			params.y = ViewAdapter.getInstance().toDeviceViewY(y);
			params.width = w;
			params.height = h;

			if (bg != 0) {
				edit.setBackgroundColor(Color.rgb(Color.red(bg),
						Color.green(bg), Color.blue(bg)));
			}

			// Log.i(tag, "updateInputBox1 ---1");
			if (StringUtils.isEmptyOrNull(text)) {
				edit.setText("");
			} else {
				int index = edit.getSelectionStart();
				Editable e = edit.getEditableText();
				if (index < 0 || index >= edit.length()) {
					e.append(text);
				} else {
					e.insert(index, text);
				}
			}
			// Log.i(tag, "updateInputBox1 ---22");
			edit.addTextChangedListener(tw);
			edit.setHeight(h);
			showInputBox();
		}
	}

	public static void showAlert(String message) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(UIHelper.milk);
		dialog.setMessage(message);
		dialog.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == Dialog.BUTTON_POSITIVE) {
				}
			}
		});
		dialog.create().show();
	}

	public void showEditDialog(String title, String inputText) {
		AlertDialog.Builder dialog = new AlertDialog.Builder(UIHelper.milk);
		if (title == null || title == "") {
			dialog.setTitle("Please Input");
		} else {
			dialog.setTitle(title);
		}

		textBox = new EditText(UIHelper.milk);
		textBox.setHeight(UIHelper.milk.getCanvasHeight() / 10);
		textBox.setWidth(UIHelper.milk.getCanvasWidth() / 2);
		if (title != null && title != "") {
			textBox.setText(inputText);
		}

		dialog.setView(textBox);
		dialog.setPositiveButton("OK", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == Dialog.BUTTON_POSITIVE) {
					Log.i(tag, textBox.getText().toString());
					String content = textBox.getText().toString();
					MilkDisplayable md = new MilkDisplayableImpl(
							UIHelper.milk.canvas);
					UIHelper.milk.switchDisplay(md);
					if (inputListener != null) {
						// Log.i(tag, "inputlistener has");
						if (content == null) {
							content = "";
						}
						inputListener.onInput(false, content);
					}
				}
			}
		});
		dialog.setNegativeButton("cancel", new OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				MilkDisplayable md = new MilkDisplayableImpl(
						(View) UIHelper.milk.canvas);
				UIHelper.milk.switchDisplay(md);
				String content = textBox.getText().toString();
				if (inputListener != null) {
					if (content == null) {
						content = "";
					}
					inputListener.onInput(true, content);
				}
			}

		});
		dialog.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				MilkDisplayable md = new MilkDisplayableImpl(
						(View) UIHelper.milk.canvas);
				UIHelper.milk.switchDisplay(md);
				String content = textBox.getText().toString();
				if (inputListener != null) {
					if (content == null) {
						content = "";
					}
					inputListener.onInput(true, content);
				}
			}
		});
		dialog.create().show();
	}

	public void showInputBox() {
		// Log.i(tag, "-----------------showInputBox");
		if (edit != null) {
			// showSoftInput();
			edit.setFocusable(true);
			edit.setCursorVisible(true);
			edit.requestFocus();
			// edit.setSelection(0);
			edit.setVisibility(View.VISIBLE);
		}
	}

	public void hideInputBox() {
		// Log.i(tag, "----------------hideInputBox");
		if (edit != null) {
			edit.setVisibility(View.GONE);
			hideSoftInput();
			edit.removeTextChangedListener(tw);
			edit.setText("");
		}
	}

	public void setEditFocus() {
		// Log.i(tag, "----------------setEditFocus");
		if (edit != null) {
			try {
				edit.setFocusable(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// public void showSoftInput()
	// {
	// ((InputMethodManager)
	// UIHelper.milk.getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(edit,
	// 0);
	// }

	public void hideSoftInput() {
		Log.i("asd", "hidesoftinput");
		InputMethodManager imm = (InputMethodManager) UIHelper.milk
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(edit.getWindowToken(), 0);
	}

	public void insertInputText(String text) {
		if (edit != null) {
			if (text != null) {
				try {
					// edit.setText(edit.getText() + text);
					// edit.append(text);
					// edit.requestFocus();
					int index = edit.getSelectionStart();
					Editable e = edit.getEditableText();
					if (index < 0 || index >= edit.length()) {
						e.append(text);
					} else {
						e.insert(index, text);
					}
					edit.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	public void setInputText(String text) {
		if (edit != null) {
			setEditFocus();
			edit.setText(text);
			try {
				if (text != null && text.length() > 0)
					edit.setSelection(text.length());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void layoutInputBox() {
		// System.out.println("-------- layoutInputFrame--------");
		if (edit != null) {
			// edit.requestFocus();
			edit.requestLayout();
			edit.invalidate();
		}
	}

	public static void showBrowser(String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		final Uri uri = Uri.parse(url);
		final Intent it = new Intent(Intent.ACTION_VIEW, uri);

		// Intent intent= new Intent();
		// intent.setAction("android.intent.action.VIEW");
		// Uri content_url = Uri.parse(url);
		// intent.setData(content_url);
		// Runnable r=new Runnable(){
		// public void run()
		// {
		// UIHelper.milk.startActivity(it);
		// }
		// };
		// UIHelper.milk.startOnUiThread(r);

		Timer timer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				UIHelper.milk.startActivity(it);
			}
		};
		timer.schedule(task, 1);
	}

	public static void showBrowserByRect(int width, int height, String url) {
		if (!url.startsWith("http://")) {
			url = "http://" + url;
		}
		Dialog dialog = new Dialog(UIHelper.milk);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		final WebView webView = new WebView(UIHelper.milk);
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webView.loadUrl(url);
		dialog.addContentView(webView, new LayoutParams(width, height));
		dialog.show();
	}

}
