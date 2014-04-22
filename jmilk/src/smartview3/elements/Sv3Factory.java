package smartview3.elements;

import java.io.IOException;

import mobon.MobonException;
import mobon.MobonReader;

public class Sv3Factory implements ISv3Factory {

	public Sv3Button createButton() {
		return new Sv3Button(null);
	}

	public Sv3Checkbox createCheckbox() {
		return new Sv3Checkbox(null);
	}

	public Sv3Div createDiv() {
		return new Sv3Div9Patch(null);
	}

	public Sv3Element createElement() {
		return new Sv3Div(null);
	}

	public Sv3Image createImage() {
		return new Sv3Image(null);
	}

	public Sv3Select createSelect() {
		return new Sv3Select(null);
	}

	public Sv3Text createText() {
		return new Sv3Text(null);
	}

	public Sv3Input createInput() {
		return new Sv3Input(null);
	}

	public Sv3Page createPage() {
		return new Sv3Page();
	}

	public Sv3Element elementFromMobon(MobonReader r) throws IOException,
			MobonException {
		Sv3Element e = null;
		int tag = r.readInt();
		switch (tag) {
		case Sv3Element.TYPE_ELEMENT:
			e = createElement();
			break;
		case Sv3Element.TYPE_DIV:
			e = createDiv();
			break;
		case Sv3Element.TYPE_TEXT:
			e = createText();
			break;
		case Sv3Element.TYPE_IMAGE:
			e = createImage();
			break;
		case Sv3Element.TYPE_BUTTON:
			e = createButton();
			break;
		case Sv3Element.TYPE_INPUT:
			e = createInput();
			break;
		case Sv3Element.TYPE_SELECT:
			e = createSelect();
			break;
		case Sv3Element.TYPE_CHECKBOX:
			e = createCheckbox();
			break;
		}

		int size = r.readMapSize(); // 1 or 5 bytes
		for (int i = 0; i < size; ++i) {
			int key = r.readInt();
			e.readAttrFromMobon(r, key);
		}

		return e;
	}

}
