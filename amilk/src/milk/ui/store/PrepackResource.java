package milk.ui.store;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;
import milk.ui.UIHelper;
import milk.utils.IOUtil;
import milk.utils.ParseUtils;
import milk.utils.StringUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class PrepackResource
{

	private static final String ANDROID_FOLD = "drawable";

	public static Bitmap getRes(String name)
	{
		int resID = UIHelper.milk.getResources().getIdentifier(getPrepackResName(name), ANDROID_FOLD, UIHelper.milk.getApplicationInfo().packageName);
		return BitmapFactory.decodeResource(UIHelper.milk.getResources(), resID);
	}

	public static InputStream getPrepackResInputStream(String name)
	{
		// InputStream is=null;
		// if (AssetsUtil.isAssetExistent(MilkApp.milk,
		// getPrepackResName(name)))
		// {
		// is = AssetsUtil.openAssetPostion(MilkApp.milk,
		// getPrepackResName(name));
		// }
		String fileName = getPrepackResName(name);
		if (!StringUtils.isEmptyOrNull(fileName))
		{
			int resId = getResValue(fileName);
			if (resId > 0)
			{
				InputStream is = UIHelper.milk.getResources().openRawResource(resId);
				return is;
			}

		}
		return null;
	}

	public static byte[] getPrepackResBytes(String name)
	{
		byte[] bytes = null;
		try
		{
			InputStream is = PrepackResource.getPrepackResInputStream(name);
			if (is != null)
			{
				bytes = ParseUtils.input2byte(is);
				is.close();
			}
		} catch (Exception t)
		{
			Adaptor.infor("midlet Throwable" + t.getMessage());
		}
		return bytes;
	}

	private static int getResValue(String name)
	{
		int resID = UIHelper.milk.getResources().getIdentifier(name, ANDROID_FOLD, UIHelper.milk.getApplicationInfo().packageName);
		return resID;
	}

	public static String getPrepackResName(String key)
	{
		if (key != null)
		{
			return key.replaceAll("-", "_").toLowerCase();
		}
		return "";
	}

	public static byte[] getStoreByte(String fileName, byte[] bytes)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MilkOutputStream dos = new MilkOutputStream(bos);
		try
		{
			Adaptor.writeIntStr(dos, fileName);
			IOUtil.writeBytes(dos, bytes);

			dos.close();
			bos.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}

		return bos.toByteArray();
	}
}
