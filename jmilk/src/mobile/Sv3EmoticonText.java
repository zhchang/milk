package mobile;

import java.util.Hashtable;

import milk.ui2.MilkFont;

import smartview3.elements.Sv3Element;
import smartview3.elements.Sv3Page;
import smartview3.elements.Sv3Text;
import smartview3.layout.EmoticonSegment;
import smartview3.layout.Rect;
import smartview3.layout.TextSegment;
import smartview3.render.IRenderContext;

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

	protected void renderSegmentText(TextSegment seg, IRenderContext ctx, String fullText, MilkFont font, Rect r,
			int color, int spacing) {
		if (seg instanceof EmoticonSegment) {
			Object image = getSharedEmoticonImage();
			ctx.drawImage(image, r.x + ((EmoticonSegment) seg).getEmoticon().getXSpacing(), r.y);
		} else {
			super.renderSegmentText(seg, ctx, fullText, font, r, color, spacing);
		}
	}

	public Object getImageForEmoticon(Emoticon emo) {
		Object image = emo.getImage();
		if (image == null) {
			// TODO:
//			int warning_need_to_fix_the_path;
			image = imageUtil.loadLocalImage("emoticon" + emo.getId() + ".png");
			emo.setImage(image);
		}
		return image;
	}

	public Object getSharedEmoticonImage() {
		Object image = Emoticon.getSharedImage();
		if (image == null) {
			// TODO:
//			int warning_need_to_fix_the_path;
			image = imageUtil.loadLocalImage("emoticon.png");
			Emoticon.setSharedImage(image);
		}
		return image;
	}
}
