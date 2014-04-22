package milk.ui.internal;

import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.jar.Attributes.Name;

import milk.implement.Adaptor;
import milk.implement.MilkOutputStream;
import milk.utils.IOUtil;

import android.os.Environment;
import android.util.Log;

public class SDCardUtil {
	private static final String tag = SDCardUtil.class.getName();

	public final static String SDPATH;

	public static final String SD_PATH = "OceanAgePro";

	public static final String SYSTEM_ANDROID_UUID = "system";

	public static final boolean isHasSDCard;

	static {
		SDPATH = Environment.getExternalStorageDirectory() + "/";
		SDCardUtil.createPath2SDCard(SDCardUtil.SD_PATH);
		isHasSDCard = checkSDCARD();
		System.out.println("------------isHasSDCard:"+isHasSDCard);
	}

	public static String getSDPATH() {

		return SDPATH;
	}

	private static boolean checkSDCARD() {

		String status = Environment.getExternalStorageState();

		if (status.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}

		return false;
	}

	public static boolean createPath2SDCard(String path) {
		if (!isHasSDCard) {
			return false;
		}

		File dir = new File(getSDPATH() + path);

		if (!dir.exists()) {
			dir.mkdirs();
		}

		return true;
	}

	public static byte[] getFileBytes(String path, String fileName) {
		if (!isHasSDCard) {
			return null;
		}
		File file = new File(getSDPATH() + path + "/" + fileName);
		if (file.exists()) {
			try {
				if (file != null) {
					FileInputStream fis = new FileInputStream(file);
					if (fis != null) {
						int len = fis.available();
						byte[] bytes = new byte[len];
						fis.read(bytes);
						return bytes;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean setFileBytes(String path, String fileName,
			byte[] bytes) {
		if (!isHasSDCard) {
			return false;
		}

		File file = new File(getSDPATH() + path + "/" + fileName);
		FileOutputStream fos = null;
		try {
			if (file.exists()) {
				file.delete();
			}
			fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.flush();
			fos.close();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean deleteFile(String path, String fileName) {
		if (!isHasSDCard) {
			return false;
		}
		File file = new File(getSDPATH() + path + "/" + fileName);
		if (file.exists()) {
			file.delete();
		}
		return true;
	}

	public static File[] findSDCardFile(String path, final String fileType) {

		File dir = new File(getSDPATH() + path);

		if (dir.isDirectory()) {
			File[] files = dir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String filename) {

					return (filename.endsWith(fileType));
				}
			});

			Arrays.sort(files, new Comparator<File>() {
				@Override
				public int compare(File str1, File str2) {

					return str2.getName().compareTo(str1.getName());
				}
			});

			return files;
		}

		return null;
	}

	public static boolean isExistFile(String path, String fileName) {
		if (!isHasSDCard) {
			return false;
		}

		Log.i(tag, "path " + path + "/" + fileName);
		File f = new File(SDPATH + path + "/" + fileName);
		return f.exists();
	}

}