package milk.implement.sv3;

import milk.implement.Adaptor;
import milk.ui2.MilkUiFactory;
import smartview3.elements.Sv3Div;
import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Factory;
import smartview3.elements.Sv3Image;
import smartview3.elements.Sv3Input;
import smartview3.elements.Sv3Page;
import smartview3.elements.Sv3Text;

public class MilkFactory extends Sv3Factory {

	static {
		Sv3Element.imageUtil = MilkImageUtil.getInstance();
	}

	public Sv3Page createPage() {
		return new MilkPage();
	}

	public Sv3Text createText() {
		return new Sv3Text(null);
	}

	public Sv3Input createInput() {
		return new MilkInput(null);
	}

	public Sv3Image createImage() {
		return new Sv3Image(null);
	}

	public Sv3Div createDiv() {
		return Adaptor.uiFactory.createMilkDiv9(null);
	}

}
