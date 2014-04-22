package com.mozat.sv3.extension;

import java.util.Hashtable;

import com.mozat.sv3.smartview3.elements.Sv3Element;
import com.mozat.sv3.smartview3.elements.Sv3Page;
import com.mozat.sv3.smartview3.elements.Sv3Text;
import com.mozat.sv3.smartview3.layout.EmoticonSegment;
import com.mozat.sv3.smartview3.layout.Rect;
import com.mozat.sv3.smartview3.layout.TextSegment;
import com.mozat.sv3.smartview3.render.IRenderContext;

public class Sv3EmoticonText extends Sv3Text {
	public Sv3EmoticonText(String id) {
		super(id);
	}

	public Sv3EmoticonText(String id, Sv3EmoticonText sv3EmoticonText, Sv3Page p) {
		super(id, sv3EmoticonText, p);
	}

	public Sv3Element clone(String id, Sv3Page p) {
		return new Sv3EmoticonText(id, this, p);
	}

	protected Hashtable getEmoticonTable() {
		return Emoticon.getEmoticonTable();
	}

	protected void renderSegmentText(TextSegment seg, IRenderContext ctx,
			String fullText, Object font, Rect r, int color, int spacing) {
		if (seg instanceof EmoticonSegment) {
			Emoticon emo = (Emoticon) ((EmoticonSegment) seg).getEmoticon();
			Object image = getImageForEmoticon(emo);
			ctx.drawImage(image, r.x + emo.getXSpacing(), r.y);
		} else {
			super.renderSegmentText(seg, ctx, fullText, font, r, color, spacing);
		}
	}

	public Object getImageForEmoticon(Emoticon emo) {
		Object image = emo.getImage();
		if (image == null) {
			image = imageUtil.loadLocalImage("e" + (emo.getId() + 1) + ".png");
			emo.setImage(image);
		}
		return image;
	}

	// public Object getSharedEmoticonImage() {
	// Object image = Emoticon.getSharedImage();
	// if (image == null) {
	// image = imageUtil.loadLocalImage("emoticon.png");
	// Emoticon.setSharedImage(image);
	// }
	// return image;
	// }
}
