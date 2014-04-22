package milk.ui.internal;

import java.io.IOException;
import java.io.InputStream;

import milk.utils.ParseUtils;

import android.content.Context;

public class AssetsUtil {

	/**
	 *
	 * @param assetPath
	 * @return
	 */
	public static boolean isAssetExistent(Context context, String assetPath) {
		InputStream is = null;
		try {
	        is = context.getAssets().open(assetPath);
	        return is != null;
        } catch (IOException e) {
        	return false;
        } finally {
        	try {
	            if(is != null)
	            	is.close();
            } catch (IOException e) {
            }
        }
	}
	
	/**
	 *
	 * @param assetPath
	 * @return
	 */
	public static InputStream openAssetPostion(Context context, String assetPath) {
		InputStream is = null;
		try {
			is = context.getAssets().open(assetPath);
			return is;
		} catch (IOException e) {
			return null;
		}
	}
	
	public static byte[] getBytesFromAssets(Context context, String assetPath) {
		InputStream is = openAssetPostion(context,assetPath);
		byte[] bytes=null;
		if(is!=null)
		{
			try
			{
				bytes= ParseUtils.input2byte(is);
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return bytes;
	}
}