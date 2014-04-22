package milk.ui.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import milk.implement.Adaptor;
import milk.implement.MD5;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;
import milk.ui.internal.SDCardUtil;
import milk.utils.IOUtil;
import android.util.Log;

public class SDStore {
	private static final String DIR = SDCardUtil.SD_PATH + "/RESOURCE116";

	private static final String tag = SDStore.class.getSimpleName();

	private static MD5 md5 = new MD5();

	static {
		SDCardUtil.createPath2SDCard(DIR);
	}

	public static void release() {

	}

	public static void updateAllMappings() {

	}

	public static void doSaveFile(String fileName, byte[] bytes) {

		Log.i(tag, "doSaveFile " + fileName);

		fileName = md5.getHashString(fileName);

		byte[] data = PrepackResource.getStoreByte(fileName, bytes);
		SDCardUtil.setFileBytes(DIR, fileName, data);
	}

	public static void doDeleteFile(String fileName) {
		fileName = md5.getHashString(fileName);
		SDCardUtil.deleteFile(DIR, fileName);
	}

	public static byte[] doReadFile(String fileName) {
		Log.i(tag, "doReadFile " + fileName);

		fileName = md5.getHashString(fileName);

		byte[] bytes = SDCardUtil.getFileBytes(DIR, fileName);

		if (bytes == null) {
			Log.i(tag, "null");
			return null;
		}
		Log.i(tag, "byte size " + bytes.length);

		try {
			MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(
					bytes));
			String fl = Adaptor.readIntStr(dis);
			Log.i(tag, "fl " + fl);
			Log.i(tag, "fileName " + fileName);
			if (fl.trim().equals(fileName.trim())) {
				byte[] dst = IOUtil.readBytes(dis);
				return dst;
			}
		} catch (Exception t) {
			t.printStackTrace();
		}
		return null;
	}

	public static boolean isExistFile(String fileName) {
		fileName = md5.getHashString(fileName);

		return SDCardUtil.isExistFile(DIR, fileName);
	}

}
