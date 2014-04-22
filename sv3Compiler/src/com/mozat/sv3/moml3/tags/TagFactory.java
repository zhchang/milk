package com.mozat.sv3.moml3.tags;

import com.mozat.sv3.smartview3.elements.Sv3Button;
import com.mozat.sv3.smartview3.elements.Sv3Checkbox;
import com.mozat.sv3.smartview3.elements.Sv3Div9Patch;
import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.elements.Sv3Factory;
import com.mozat.sv3.smartview3.elements.Sv3Image;
import com.mozat.sv3.smartview3.elements.Sv3Input;
import com.mozat.sv3.smartview3.elements.Sv3Page;
import com.mozat.sv3.smartview3.elements.Sv3Select;
import com.mozat.sv3.smartview3.elements.Sv3Text;

public class TagFactory extends Sv3Factory {

	@Override
	public Sv3Button createButton() {
		return new Button("button");
	}

	@Override
	public Sv3Checkbox createCheckbox() {
		return new Checkbox("checkbox");
	}

	@Override
	public Sv3Div9Patch createDiv() {
		return new Div("div");
	}

	@Override
	public Sv3Element createElement() {
		return new Element("general");
	}

	@Override
	public Sv3Image createImage() {
		return new Img("img");
	}

	@Override
	public Sv3Select createSelect() {
		return new Select("select");
	}

	@Override
	public Sv3Text createText() {
		return new Span("span");
	}

	@Override
	public Sv3Input createInput() {
		return new Input("input");
	}

	@Override
	public Sv3Page createPage() {
		return new Page();
	}
}
