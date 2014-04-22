package milk.ui.graphics;

import java.io.IOException;
import java.io.InputStream;

import milk.ui.UIHelper;
import milk.ui.store.PrepackResource;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

public class Image {
	private Bitmap img;
	private int alpha;

	private Canvas canvas;
	private Graphics g;
	static Resources res;
	
    public Image() {
		img = null;
		alpha = 0xff;
	}
	
	public static void setResources(Resources resources){
		res=resources;
	}

	public void setImage(Bitmap b) {
		this.img = b;
	}

	private Image(Bitmap img, int alpha) {
		this.img = img;
		this.alpha = alpha;
	}

	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {

		// load the origial Bitmap
		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Bitmap resizedBitmap = resizeImage(BitmapOrg, scaleWidth, scaleHeight);
		return resizedBitmap;
		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		// return new BitmapDrawable(resizedBitmap);

	}

	public static Bitmap resizeImage(Bitmap bitmap, float scaleWidth,
			float scaleHeight) {

		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);
//		BitmapOrg.recycle();
//		BitmapOrg=null;
		return resizedBitmap;

	}

//	public static Bitmap getResizeImage(Bitmap bitmap) {
//		Bitmap resizeBitmap=null;
//		if (bitmap != null) {
//			if ((UIHelper.milk.widthPer != 1 || UIHelper.milk.heightPer != 1)
//					&& UIHelper.milk.isScale) {
//				resizeBitmap = resizeImage(bitmap, UIHelper.milk.widthPer,
//						UIHelper.milk.heightPer);
//			} else {
//
//			}
//
//		}
//		return resizeBitmap;
//	}

	private Image(Image source) {
		this.img = source.img;
		this.alpha = source.alpha;
	}

	public static Image createImage(String src) {
		if (src.startsWith("/")) {
			src = src.substring(1);
		}

//		Resources resources = UIHelper.milk.getResources();
//		AssetManager assetManager = resources.getAssets();
		
		
		InputStream in = null;
		Bitmap img = null;
		try {
//			in = assetManager.open(src);
			in=PrepackResource.getPrepackResInputStream(src);
			if(in!=null)
			{
				img = BitmapFactory.decodeStream(in);
				if(img!=null)
				{
					return new Image(img, 0xff);
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			if(in!=null)
			{
				try
				{
					in.close();
				} catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static Image createImage(byte[] imageData, int imageOffset,
			int imageLength) {

		Bitmap img = BitmapFactory.decodeByteArray(imageData, imageOffset,
				imageLength);
		return new Image(img, 0xff);

	}

	private static Image createImage(Image source) {
		return new Image(source);
	}

	//problem
	public static Image createImage(Image image, int x, int y, int width,
			int height, int transform) {
		Bitmap bmp = Bitmap.createBitmap(image.img, x, y, width, height);
		Paint paint = new Paint();
		paint.setAlpha(image.alpha);

		Image tImage = createImage(width, height);
		Canvas canvas = new Canvas(tImage.img);
		Matrix mMatrix = Graphics.getTransformMetrix(transform, width, height);
		canvas.drawBitmap(bmp, mMatrix, paint);

		return tImage;
	}

	private static Image createImage(InputStream stream) {
		Bitmap img = BitmapFactory.decodeStream(stream);
		return new Image(img, 0xff);
	}

	public static Image createImage(int width, int height) {
		Bitmap img = Bitmap
				.createBitmap(width, height, Bitmap.Config.ARGB_8888);
//		img.setDensity((int)(ScreenControl.dm.density*img.getDensity()));
		return new Image(img, 0xff);
	}

	public static Image createRGBImage(int[] rgb, int width, int height,
			boolean processAlpha) {
		Bitmap img;
		if (processAlpha) {
			img = Bitmap.createBitmap(rgb, width, height,
					Bitmap.Config.ARGB_8888);
		} else {
			img = Bitmap
					.createBitmap(rgb, width, height, Bitmap.Config.RGB_565);
		}
		return new Image(img, 0xff);
	}


	
	public Graphics getGraphics() {
		if(g!=null)
		{
			return g;
		}
		if(canvas==null)
		{
			canvas = new Canvas(img);
		}
		g=new Graphics(canvas);
		// Paint paint = new Paint();
		// paint.setAlpha(alpha);
		// return new Graphics(canvas, paint);
		return g;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

	public int getAlpha() {
		return alpha;
	}

	public Bitmap getImg() {
		return img;
	}

	public void getRGB(int[] rgbData, int offset, int scanlength, int x, int y,
			int width, int height) {
		img.getPixels(rgbData, offset, scanlength, x, y, width, height);
	}

	public int getHeight() {
		if (img != null) {
			return img.getHeight();
		}
		return 0;
	}

	public int getWidth() {
		if (img != null) {
			return img.getWidth();
		}
		return 0;
	}

	boolean isMutable() {
		return img.isMutable();
	}

	public void replaceColor(int newColor, int oldColor) {
		int width = img.getWidth();
		int height = img.getHeight();
		int[] pixels = new int[width * height];
		img.getPixels(pixels, 0, width, 0, 0, width, height);

		for (int i = 0; i < pixels.length; i++) {
			if ((pixels[i] | 0x00ffffff) == (oldColor | 0x00ffffff)) {
				pixels[i] = oldColor;
			}
		}
		img = Bitmap.createBitmap(pixels, 0, 0, height, height,
				Bitmap.Config.ARGB_8888);
	}
	
	public static Image createImage(int id){
		Image img=new Image();
		img.img=BitmapFactory.decodeResource(res, id);
		return img;
	}
	
	public void resizeImage(int w,int h){
		img=resizeImage(img, w, h);
	}
	
	public Image rotate(int degree) {
		// 获取这个图片的宽和高
		int width = getWidth();
		int height = getHeight();

		// 定义预转换成的图片的宽度和高度
//		int newWidth = 200;
//		int newHeight = 200;
//		// 计算缩放率，新尺寸除原始尺寸
//		float scaleWidth = ((float) newWidth) / width;
//		float scaleHeight = ((float) newHeight) / height;
		// 创建操作图片用的matrix对象
		Matrix matrix = new Matrix();
		// 缩放图片动作
//		matrix.postScale(scaleWidth, scaleHeight);
		// 旋转图片动作
		matrix.postRotate(degree);
		// 创建新的图片
		Bitmap resizedBitmap = Bitmap.createBitmap(this.img, 0, 0, width,height, matrix, true);
		Image ret=new Image();
		ret.img=resizedBitmap;
		return ret;
	}
	
	public static Bitmap rotate(Bitmap b, float degrees) {
		if (degrees != 0 && b != null) {
			Matrix m = new Matrix();
			m.setRotate(degrees,
			(float) b.getWidth() / 2, (float) b.getHeight() / 2);
			try {
				Bitmap b2 = Bitmap.createBitmap(
				b, 0, 0, b.getWidth(), b.getHeight(), m, true);
//				if (b != b2) {
//					b.recycle();
//					b = b2;
//				}
				return b2;
			} catch (OutOfMemoryError ex) {
			}
		}
		return b;
    }
}