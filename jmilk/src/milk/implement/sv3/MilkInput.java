package milk.implement.sv3;

import milk.implement.Adaptor;
import milk.ui2.InputListener;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Input;
import smartview3.elements.Sv3Page;

public class MilkInput extends Sv3Input implements InputListener {

	// MobileFontUtil fu = new MobileFontUtil();

	public MilkInput(String id) {
		super(id);
		url = "#";
	}

	public MilkInput(String id, Sv3Input in, Sv3Page page) {
		super(id, in, page);
		url = "#";
	}

	public Sv3Element clone(String id, Sv3Page page) {
		return new MilkInput(id, this, page);
	}

	protected void triggerRaw() {

		// TODO: showup textbox and get user input
		int inputType = convertToJ2MEConstaints(this);

		String initText = getText();
		if (initText == null) {
			initText = this.getTextOnEmpty();
		}
		Adaptor.milk.getInput("", initText, getLength(), inputType, this);

	}

	private int convertToJ2MEConstaints(Sv3Input input) {
		int con;
		switch (input.getKeyboard()) {
		case Sv3Input.KEYBOARD_URL:
			con = 0x04;// TextField.URL;
			break;
		case Sv3Input.KEYBOARD_EMAIL:
			con = 1;// TextField.EMAILADDR;
			break;
		case Sv3Input.KEYBOARD_PHONE:
			con = 3;// TextField.PHONENUMBER;
			break;
		case Sv3Input.KEYBOARD_NUMERIC:
			con = 2;// TextField.NUMERIC;
			break;
		default: // all other cases maps to ANY
			con = 0;// TextField.ANY;
			break;
		}
		if (input.isSecure()) {
			con |= 65536;// TextField.PASSWORD;
		}
		switch (input.getCapitalization()) {
		case Sv3Input.CAPITALIZATION_WORDS:
			con |= 1048576;// TextField.INITIAL_CAPS_WORD;
			break;
		case Sv3Input.CAPITALIZATION_SENTENCES:
			con |= 2097152;// TextField.INITIAL_CAPS_SENTENCE;
			break;
		// case CAPITALIZATION_ALL: // not supported by J2ME
		// break;
		// case CAPITALIZATION_NONE:
		// break;
		}
		return con;
	}

	public void onInput(boolean cancelled, String input) {
		if (!cancelled && input != null) {
			setStrAttrib("text", input);
		}
	}

}
