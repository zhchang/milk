package milk.ui.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;

import milk.implement.Adaptor;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;
import milk.ui.internal.SDCardUtil;
import android.util.Log;

public class FileList {
	private Vector fileList = new Vector();
	private static final String tag = FileList.class.getName();
	public static final String FILE_LIST_NAME = "filelist";
	private long saveListTime;
	private boolean isNeedSave;
	private Vector prepackFileList;

	public void doSaveFileList() {
		if (fileList != null && fileList.size() > 0 && isNeedSave) {
			long currentTime = System.currentTimeMillis();
			if (currentTime - saveListTime > 1000) {
				if (SDCardUtil.isHasSDCard) {
					try {
						SDStore.doSaveFile(FILE_LIST_NAME,
								VectorToBytes(fileList));
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					try {
						SQLiteStore.doSaveFile(FILE_LIST_NAME,
								VectorToBytes(fileList));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				isNeedSave = false;
				saveListTime = currentTime;
				Log.i(tag, "isNeedSave " + isNeedSave + " saveListTime "
						+ saveListTime);
			}
		}

	}

	byte[] VectorToBytes(Vector fileList) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		MilkOutputStream dos = new MilkOutputStream(bos);
		int size = fileList.size();
		for (int i = 0; i < size; i++) {
			Adaptor.writeVarChar(dos, fileList.get(i).toString());
		}
		byte[] temp = bos.toByteArray();
		return temp;
	}

	public void add(String name) {
		if (!fileList.contains(name)) {
			fileList.add(name);
		}
	}

	public void delete(String name) {
		if (fileList.contains(name)) {
			fileList.remove(name);
		}
	}

	public boolean isExistFile(String name) {
		if (fileList != null && fileList.contains(name)) {
			return true;
		}
		if (prepackFileList != null && prepackFileList.contains(name)) {
			return true;
		}
		return false;
	}

	public void setNeedSave(boolean isNeed) {
		isNeedSave = isNeed;
	}

	public Vector getFileList() {
		return fileList;
	}

	public void getStoreFileList() {
		String key = FILE_LIST_NAME;
		byte[] bytes = null;
		if (SDCardUtil.isHasSDCard) {
			bytes = SDStore.doReadFile(key);
		} else {
			bytes = SQLiteStore.doReadFile(key);
		}
		if (bytes != null && bytes.length > 0) {
			MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(
					bytes));
			try {
				while (dis.available() > 0) {
					String fileName = Adaptor.readVarChar(dis);
					fileList.add(fileName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Vector getPrepackFileList() {
		String key = FILE_LIST_NAME;
		byte[] bytes = null;
		prepackFileList = new Vector();
		bytes = PrepackResource.getPrepackResBytes(key);
		if (bytes != null && bytes.length > 0) {
			MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(
					bytes));
			try {
				while (dis.available() > 0) {
					String fileName = Adaptor.readVarChar(dis);
					prepackFileList.add(fileName);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return prepackFileList;
	}

	public Vector getAllFileList() {
		getStoreFileList();
		Vector v = getPrepackFileList();
		if (fileList != null) {
			int size = fileList.size();
			for (int i = 0; i < size; i++) {
				v.add(fileList.get(i));
			}
		}

		return v;
	}
}
