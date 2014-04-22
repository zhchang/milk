package milk.ui.internal;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;

import milk.implement.MD5;
import milk.implement.mk.MMap;
import milk.ui.UIHelper;
import milk.ui.secret.AESHelper;
import milk.ui2.MilkApp;
import milk.utils.StringUtils;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class ScreenControl
{
	public final static int LANDSCAPE = 1;
	public final static int PORTRAIT = 2;

	public static void setOrientation(int type)
	{
		if (type == LANDSCAPE)
		{
			UIHelper.milk.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		} else if (type == PORTRAIT)
		{
			UIHelper.milk.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		}
	}

	public static void init()
	{
		UIHelper.milk.requestWindowFeature(Window.FEATURE_NO_TITLE);
		UIHelper.milk.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// UIHelper.milk.getWindow().setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
		// | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		// UIHelper.milk
		// .getWindow()
		// .setSoftInputMode(
		// WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
	}

	private static int width;
	private static int height;
	
	public static int getScreenWidth()
	{
		if(width>0)
		{
			return width;
		}
		if (dm == null)
		{
			dm = new DisplayMetrics();
		}
		UIHelper.milk.getWindowManager().getDefaultDisplay().getMetrics(dm);

		Log.i(MilkApp.class.getName(), "screen w " + dm.widthPixels + "screen h" + dm.heightPixels + " density " + dm.density);
		
		width=dm.widthPixels;
		
		return width;
	}
	static DisplayMetrics dm;

	public static int getScreenHeight()
	{
		if(height>0)
		{
			return height;
		}
		
		if (dm == null)
		{
			dm = new DisplayMetrics();
		}
		UIHelper.milk.getWindowManager().getDefaultDisplay().getMetrics(dm);
		Log.i(MilkApp.class.getName(), "screen w " + dm.widthPixels + "screen h" + dm.heightPixels + " density " + dm.density);

		height=dm.heightPixels;
		return height;
	}

	public static String getIMIE()
	{
		return ((TelephonyManager) UIHelper.milk.getSystemService(UIHelper.milk.TELEPHONY_SERVICE)).getDeviceId();
	}

	public static String getIMSI()
	{
		String imsi=null;
		try
		{
			imsi = ((TelephonyManager) UIHelper.milk.getSystemService(UIHelper.milk.TELEPHONY_SERVICE)).getSubscriberId();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return imsi;
	}

	public static String getMACAddress()
	{
		try
		{
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			kgen.init(256);
		} catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		WifiManager wifiManager = (WifiManager) UIHelper.milk.getSystemService(UIHelper.milk.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled())
		{
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String mResult = wifiInfo.getMacAddress();
		return mResult;
	}

	public static String getAndroidId()
	{
		String androidID =null;
		try
		{
			androidID= android.provider.Settings.System.getString(UIHelper.milk.getContentResolver(), "android_id");
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return androidID;
	}

	public static MMap getAndroidUID()
	{
		MMap map = null;
		String IMSI = getIMSI();
		String IMIE = getIMIE();
		String systemTime = String.valueOf(System.currentTimeMillis());
		String value = null;
		String token = null;
		if (!StringUtils.isEmptyOrNull(IMSI) || !StringUtils.isEmptyOrNull(IMIE))
		{
			token = IMSI + IMIE;
		} else
		{
			token = getAndroidId();

		}
		Log.i("token", token);
		
		String v = systemTime + token + systemTime;
		MD5 md5 = new MD5();
		String AESStr = md5.getHashString(AESHelper.encryptAES(v));
		Log.i("AESStr md5", AESStr);
		if (!StringUtils.isEmptyOrNull(token) || !StringUtils.isEmptyOrNull(AESStr))
		{
			map = new MMap();
			map.set("time", systemTime);
			map.set("token", token);
			map.set("aes", AESStr);
			SDCardUtil.setFileBytes(SDCardUtil.SD_PATH, SDCardUtil.SYSTEM_ANDROID_UUID, AESHelper.encrypt(map.toString(),AESHelper.KEY));
			return map;
		}
		return map;
	}

	public static void checkUUIDBySDCard()
	{
		if(!SDCardUtil.isHasSDCard)
		{
			return;
		}
		
		if(!SDCardUtil.isExistFile(SDCardUtil.SD_PATH, SDCardUtil.SYSTEM_ANDROID_UUID))
		{
			getAndroidUID();
		}
	}
	
}
