package milk.ui.graphics;

import milk.ui.MilkFontImpl;

import milk.ui.UIHelper;
import milk.ui.model.RectPool;
import milk.ui2.MilkFont;
import milk.ui2.MilkGraphics;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Graphics {


	Rect clipRect = new Rect();
	Paint paint = null;
	MilkFont font = null;
	Canvas canvas = null;
	FontMetrics fm=new FontMetrics();
	
	float[] fs;
	
	public int color=Font.DEFAULT_NUMBER;
	
	private Rect getRect(int x, int y, int width, int height) {
//		return new Rect(x, y, x + width, y + height);
		return RectPool.getRect(x, y, width, height);
	}

	public Paint getPaint() {
		if(paint==null)
		{
			paint=new Paint();
		}
		milkPaint.setPaint(paint, ((MilkFontImpl)font).font, color);
		return paint;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public Graphics(Canvas canvas) {
		this.canvas = canvas;

		font=MilkFontImpl.getDefaultFont();
		canvas.clipRect(getRect(0, 0, UIHelper.milk.getCanvasWidth(),
				UIHelper.milk.getCanvasWidth()));

		this.canvas.save(Canvas.CLIP_SAVE_FLAG);

		canvas.getClipBounds(clipRect);
//		this.clipRect = canvas.getClipBounds();
	}


	public void clipRect(int x, int y, int width, int height) {

		canvas.clipRect(x, y, x + width, y + height);

//		clipRect = 
		canvas.getClipBounds(clipRect);
	}

	public void copyArea(int x_src, int y_src, int width, int height,
			int x_dest, int y_dest, int anchor) {

	}

	public void drawArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		getPaint().setStyle(Style.STROKE);
		Rect rect=getRect(x, y, width, height);
		RectF rf=RectPool.getRectf(rect);
		canvas.drawArc(rf,startAngle,
				arcAngle, true, getPaint());
		RectPool.recovery(rect);
		RectPool.recovery(rf);
	}

	
//	public void drawChar(char character, int x, int y, int anchor) {
//		char[] chArr = new char[1];
//		chArr[0] = character;
//		
//		canvas.drawText(new String(chArr), x, y, getPaint());
//	}

	public void drawChars(char[] data, int offset, int length, int x, int y,
			int anchor) {
		canvas.drawText(data, offset, length, x, y, getPaint());
	}

	public void drawImage(Image img, int x, int y, int anchor) {
		int alpha = getPaint().getAlpha();
		getPaint().setAlpha(img.getAlpha());
		if (img != null && img.getImg() != null) {
			x = getXByAnchor(img, x, anchor);
			y = getYByAnchor(img, y, anchor);
			canvas.drawBitmap(img.getImg(), x, y, getPaint());
			getPaint().setAlpha(alpha);
		}

	}
	

	
	public void drawLine(int x1, int y1, int x2, int y2) {
		canvas.drawLine(x1, y1, x2, y2, getPaint());
	}

	public void drawRect(int x, int y, int width, int height) {
		Rect rect = getRect(x, y, width, height);
		getPaint().setStyle(Style.STROKE);
		canvas.drawRect(rect, getPaint());
		RectPool.recovery(rect);
	}

	static final float[]  mirrorY =new float[] { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
	
	public static Matrix getTransformMetrix(int transform, int width, int height) {
		Matrix mMatrix = new Matrix();
		Matrix temp=null;
//		float[] mirrorY=null;
		
		switch (transform) {
		case Sprite.TRANS_NONE:
			break;

		case Sprite.TRANS_ROT90:
			mMatrix.setRotate(90, width / 2, height / 2);
			break;

		case Sprite.TRANS_ROT180:
			mMatrix.setRotate(180, width / 2, height / 2);
			break;

		case Sprite.TRANS_ROT270:
			mMatrix.setRotate(270, width / 2, height / 2);
			break;

		case Sprite.TRANS_MIRROR:
			temp = new Matrix();
//			mirrorY =new float[] { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
			temp.setValues(mirrorY);
			mMatrix.postConcat(temp);
			break;

		case Sprite.TRANS_MIRROR_ROT90:
			temp = new Matrix();
//			mirrorY =new float[] { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
			temp.setValues(mirrorY);
			mMatrix.postConcat(temp);
			mMatrix.setRotate(90, width / 2, height / 2);
			break;

		case Sprite.TRANS_MIRROR_ROT180:
			temp = new Matrix();
//			mirrorY =new float[] { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
			mMatrix.postConcat(temp);
			mMatrix.setRotate(180, width / 2, height / 2);
			break;

		case Sprite.TRANS_MIRROR_ROT270:
			temp = new Matrix();
//			mirrorY =new float[] { -1, 0, 0, 0, 1, 0, 0, 0, 1 };
			mMatrix.postConcat(temp);
			mMatrix.setRotate(270, width / 2, height / 2);
			break;
		}
		return mMatrix;
	}

//	public void drawRegion(Image src, int x_src, int y_src, int width,
//			int height, int transform, int x_dst, int y_dst, int anchor) {
//
//		if (x_src + width > src.getWidth() || y_src + height > src.getHeight()
//				|| width < 0 || height < 0 || x_src < 0 || y_src < 0) {
//			throw new IllegalArgumentException("Area out of Image");
//		}
//
//		int alpha = paint.getAlpha();
//
//		paint.setAlpha(src.getAlpha());
//
//		Bitmap newBitmap = null;
//
//		Matrix tmp_matrix = new Matrix();
//
//		switch (transform) {
//
//		case Sprite.TRANS_NONE:
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_ROT90:
//			tmp_matrix.reset();
//			tmp_matrix.preRotate(90);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_ROT180:
//			tmp_matrix.reset();
//			tmp_matrix.preRotate(180);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_ROT270:
//			tmp_matrix.reset();
//			tmp_matrix.preRotate(270);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_MIRROR:
//			tmp_matrix.reset();
//			tmp_matrix.preScale(-1, 1);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_MIRROR_ROT90:
//			tmp_matrix.reset();
//			tmp_matrix.preScale(-1, 1);
//			tmp_matrix.preRotate(-90);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_MIRROR_ROT180:
//			tmp_matrix.reset();
//			tmp_matrix.preScale(-1, 1);
//			tmp_matrix.preRotate(-180);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//
//		case Sprite.TRANS_MIRROR_ROT270:
//			tmp_matrix.reset();
//			tmp_matrix.preScale(-1, 1);
//			tmp_matrix.preRotate(-270);
//			newBitmap = Bitmap.createBitmap(src.getImg(), x_src, y_src, width,
//					height, tmp_matrix, true);
//			break;
//		}
//
//		canvas.drawBitmap(newBitmap, x_dst, y_dst, paint);
//		paint.setAlpha(alpha);
//	}

	public void drawRGB(int[] rgbData, int offset, int scanlength, int x,
			int y, int width, int height, boolean processAlpha) {
		canvas.drawBitmap(rgbData, offset, scanlength, x, y, width, height,
				processAlpha, getPaint());
	}

	public void drawRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Rect rect = getRect(x, y, width, height);
		getPaint().setStyle(Style.STROKE);
		RectF rf=RectPool.getRectf(rect);
		canvas.drawRoundRect(rf, arcWidth, arcHeight, getPaint());
		RectPool.recovery(rect);
		RectPool.recovery(rf);
	}

	public void drawString(String str, int x, int y, int anchor) {
		x = getTextXByAnchor(str, x, anchor);
		y = getTextYByAnchor(str, y, anchor);
		canvas.drawText(str, x, y, getPaint());
	}

	public void drawSubstring(String str, int offset, int len, int x, int y,
			int anchor) {
		String tempStr = str.substring(offset, offset + len);
		x = getTextXByAnchor(tempStr, x, anchor);
		y = getTextYByAnchor(tempStr, y, anchor);
		canvas.drawText(tempStr, x, y, getPaint());
	}

	public void fillArc(int x, int y, int width, int height, int startAngle,
			int arcAngle) {
		getPaint().setStyle(Style.FILL);
		RectF rectf=RectPool.getRectf(x, y, width, height);
		canvas.drawArc(rectf, startAngle, arcAngle,
				true, getPaint());
		RectPool.recovery(rectf);
	}

	public void fillRect(int x, int y, int width, int height) {
		getPaint().setStyle(Style.FILL);
		Rect rect=getRect(x, y, width, height);
		RectF rf=RectPool.getRectf(rect);
		canvas.drawRect(rf, getPaint());
		RectPool.recovery(rect);
		RectPool.recovery(rf);
	}

	public void fillRoundRect(int x, int y, int width, int height,
			int arcWidth, int arcHeight) {
		Rect rect = getRect(x, y, width, height);
		getPaint().setStyle(Style.FILL);
		RectF rf=RectPool.getRectf(rect);
		canvas.drawRoundRect(rf, arcWidth, arcHeight, getPaint());
		RectPool.recovery(rect);
		RectPool.recovery(rf);
	}

	public void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3) {
		getPaint().setStyle(Style.FILL);
		Paint p=getPaint();
		canvas.drawLine(x1, y1, x2, y2, p);
		canvas.drawLine(x2, y2, x3, y3, p);
		canvas.drawLine(x3, y3, x1, y1, p);
	}

	public int getClipHeight() {
		return (int) clipRect.height();
	}

	public int getClipWidth() {
		return (int) clipRect.width();
	}

	public int getClipX() {
		return (int) clipRect.left;
	}

	public int getClipY() {
		return (int) clipRect.top;
	}

	public int getColor() {
		return getPaint().getColor();
	}

	public int getAlpha() {
		return getPaint().getAlpha();
	}


	public MilkFont getFont() {
		if (font == null) {
			font = MilkFontImpl.getDefaultFont();
		}
		return font;
	}



	private int getMatrixValue(int value) {
		if (fs == null) {
			fs = new float[9];
		}

		canvas.getMatrix().getValues(fs);
		return (int) fs[value];
	}

	public int getTranslateX() {
		return getMatrixValue(Matrix.MTRANS_X);
	}

	public int getTranslateY() {
		return getMatrixValue(Matrix.MTRANS_Y);
	}

	public void setClip(int x, int y, int width, int height) {
		Rect r=getRect(x, y, width, height);
		canvas.clipRect(r, Region.Op.REPLACE);
		canvas.getClipBounds(clipRect);
		RectPool.recovery(r);
	}

//	public void setAlign(int align) {
//		if (LEFT == align || (Graphics.LEFT | Graphics.TOP) == align
//				|| (Graphics.LEFT | Graphics.BOTTOM) == align) {
////			paint.setTextAlign(Align.LEFT);
//			((MilkFontImpl)font).font.textAlign=Font.TEXT_LEFT;
//		} else if (HCENTER == align
//				|| (Graphics.HCENTER | Graphics.TOP) == align) {
////			paint.setTextAlign(Align.CENTER);
//			((MilkFontImpl)font).font.textAlign=Font.TEXT_CENTER;
//		} else if (RIGHT == align || (Graphics.RIGHT | Graphics.TOP) == align) {
////			paint.setTextAlign(Align.RIGHT);
//			((MilkFontImpl)font).font.textAlign=Font.TEXT_RIGHT;
//		}
//	}

	
	
	public void setColor(int RGB) {
		color=RGB;
	}


	// android
	public void setFont(MilkFont font) {
		this.font=font;
		
	}

	public void setGrayScale(int value) {
	}

//	public void setStrokeStyle(int style) {
//		paint.setStrokeWidth(style);
//	}

	public void translate(int x, int y) {
		canvas.translate(x, y);
	}

	private int getTextXByAnchor(String text, int x, int anchor) {
		if ((anchor & MilkGraphics.LEFT) > 0) {
			// int textWidth = (int) paint.measureText(text);
			// x = x - textWidth / 2;
			// FontMetrics fm = paint.getFontMetrics();
			// int textHeight = (int) Math.ceil(fm.descent - fm.ascent);
		} else if ((anchor & MilkGraphics.RIGHT) > 0) {

		}
		return x;
	}

	private int getTextYByAnchor(String text, int y, int anchor) {
		
		
//		if ((anchor & MilkGraphics.BOTTOM) > 0) 
		{
			getPaint().getFontMetrics(fm);
			Paint p=getPaint();
			int textHeight = (int) p.getTextSize();
			y = (int)(y + textHeight+2-fm.bottom);
		}
		
		

		return y;
	}

	private int getXByAnchor(Image img, int x, int anchor) {
		if (anchor >= 1 && anchor <= 3) {
			x = x - img.getWidth() / 2;
		}
		return x;
	}

	private int getYByAnchor(Image img, int y, int anchor) {
		if (anchor >= 1 && anchor <= 3) {
			y = y - img.getHeight() / 2;
		}
		return y;
	}

}
