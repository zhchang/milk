package milk.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStore;

import milk.implement.Adaptor;
import milk.implement.MD5;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;
import net.rim.device.api.io.File;
import net.rim.device.api.io.FileInputStream;
import net.rim.device.api.io.FileOutputStream;

public class Util {

	private static MD5 md5 = new MD5();

	private static final int rCount = 10;
	private static FileMapping[] fileMappings = new FileMapping[rCount];
	private static RecordStore[] recordStores = new RecordStore[rCount];

	// private static Vector fileCache = new Vector();

	private static Vector files = new Vector();

	private static int currentRecordIndex = 0;

	// private static Runnable fileWriter = new Runnable() {
	//
	// public void run() {
	//
	// while (true) {
	// String fileName = null;
	// byte[] bytes = null;
	// synchronized (fileCache) {
	// if (fileCache.size() == 0) {
	// try {
	// fileCache.wait();
	// } catch (Exception e) {
	// }
	// }
	// }
	// synchronized (fileCache) {
	// if (fileCache.size() > 0) {
	// fileName = (String) fileCache.elementAt(0);
	// fileCache.removeElementAt(0);
	// bytes = (byte[]) fileCache.elementAt(0);
	// fileCache.removeElementAt(0);
	// } else {
	// break;
	// }
	// }
	// if (fileName != null && bytes != null) {
	// // Adaptor.debug("writing " + fileName + " [" + bytes.length
	// // + "]bytes");
	// doSaveFile(fileName, bytes);
	// try {
	// Thread.sleep(20);
	// } catch (Exception e) {
	// }
	// }
	//
	// }
	// }
	//
	// };

	static {
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			FileInputStream fileIn = null;
			MilkInputStream mis = null;
			try {
				fileIn = new FileInputStream(File.FILESYSTEM_PATRIOT,
						"file-list");
				mis = new MilkInputStream(fileIn);
				files.addElement(Adaptor.readVarChar(mis));
			} catch (IOException ioe) {
			} finally {
				if (fileIn != null) {
					try {
						fileIn.close();
					} catch (Exception e) {

					}
				}
				if (mis != null) {
					try {
						mis.close();
					} catch (Exception e) {
					}
				}
			}
		} else {
			for (int i = 0; i < 10; i++) {
				RecordStore rs = openRecordStore(i, false);
				if (rs == null) {
					break;
				} else {
					try {
						int count = rs.getNumRecords();
						if (count > 0) {
							byte[] bytes = rs.getRecord(1);
							if (bytes != null) {
								FileMapping fileMapping = new FileMapping(
										new Hashtable());
								MilkInputStream dis = new MilkInputStream(
										new ByteArrayInputStream(bytes));
								while (dis.available() > 0) {
									String fileName = Adaptor.readVarChar(dis);
									int id = dis.readInt();
									fileMapping.files.put(fileName,
											new Integer(id));
								}
								fileMappings[i] = fileMapping;
							}
						}
					} catch (Exception t) {
						Adaptor.infor("util Throwable");
					}
				}
			}
		}

		// new Thread(fileWriter).start();
	}

	private static RecordStore openRecordStore(int index, boolean create) {
		if (recordStores[index] != null) {
			return recordStores[index];
		}
		String name = hash("milk" + index);

		RecordStore result = null;
		try {
			result = RecordStore.openRecordStore(name, create);
			if (result != null) {
				recordStores[index] = result;
			}
		} catch (Exception t) {
			// Adaptor.exception(t);
		}
		return result;
	}

	private static void saveFileMapping(FileMapping fileMapping, RecordStore rs) {
		try {
			int count = rs.getNumRecords();

			if (count == 0) {
				byte[] temp = fileMapping.getBytes();
				rs.addRecord(temp, 0, temp.length);
			} else {
				fileMapping.needUpdate = true;
				fileMapping.lastRequested = System.currentTimeMillis();
			}
		} catch (Exception t) {
			Adaptor.exception(t);
		}
	}

	public static void updateAllMappings() {
		for (int i = 0; i < 10; i++) {
			FileMapping fileMapping = fileMappings[i];
			if (fileMapping != null) {
				if (fileMapping.needUpdate) {
					if (System.currentTimeMillis() - fileMapping.lastRequested > 500) {
						try {
							byte[] temp = fileMapping.getBytes();
							recordStores[i].setRecord(1, temp, 0, temp.length);
							fileMapping.needUpdate = false;
						} catch (Exception e) {

						}
					}
				}
			} else {
				break;
			}
		}
	}

	static void saveFileList() {
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(File.FILESYSTEM_PATRIOT, "file-list");
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				MilkOutputStream mos = new MilkOutputStream(bos);
				int count = files.size();
				for (int i = 0; i < count; i++) {
					String name = (String) files.elementAt(i);
					Adaptor.writeVarChar(mos, name);
				}
				// Write raw bytes to file
				fos.write(bos.toByteArray());
				bos.close();
				mos.close();
				fos.flush();
				fos.close();
			} catch (IOException ieo) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {

					}
				}
			}
		}
	}

	static void saveFile(String fileName, byte[] bytes) {
		// synchronized (fileCache) {
		// fileCache.addElement(fileName);
		// fileCache.addElement(bytes);
		// fileCache.notifyAll();
		// }
		fileName = Adaptor.replaceAll(fileName, "-", "_").toLowerCase();
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(File.FILESYSTEM_PATRIOT, fileName);

				// Write raw bytes to file
				fos.write(bytes);
				fos.flush();
				fos.close();

			} catch (IOException ieo) {
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (Exception e) {

					}
				}
			}
			saveFileList();
		} else {
			if (fileName != null && bytes != null) {
				doSaveFile(fileName, bytes);
			}
		}
	}

	static void doSaveFile(String fileName, byte[] bytes) {

		for (int i = currentRecordIndex; i < 10; i++) {
			FileMapping fileMapping = fileMappings[i];
			if (fileMapping == null) {
				fileMapping = new FileMapping(new Hashtable());
				fileMapping.files.put(fileName, new Integer(2));
				fileMappings[i] = fileMapping;
				RecordStore rs = openRecordStore(i, true);
				saveFileMapping(fileMapping, rs);
				try {
					rs.addRecord(bytes, 0, bytes.length);
				} catch (Exception t) {
				}
				break;
			} else {

				RecordStore rs = openRecordStore(i, false);

				boolean ok = false;
				try {
					if (fileMapping.files.containsKey(fileName)) {
						rs.setRecord(
								((Integer) fileMapping.files.get(fileName))
										.intValue(), bytes, 0, bytes.length);
					} else {
						int id = rs.addRecord(bytes, 0, bytes.length);
						fileMapping.files.put(fileName, new Integer(id));
						saveFileMapping(fileMapping, rs);
					}
					ok = true;
				} catch (Exception t) {
					Util.deleteFile(fileName);
				}
				fileMappings[i] = fileMapping;
				if (ok) {
					break;
				}

			}
		}

	}

	static void deleteFile(String fileName) {
		fileName = Adaptor.replaceAll(fileName, "-", "_").toLowerCase();
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {

			try {

				if (files.contains(fileName)) {
					files.removeElement(fileName);
					saveFileList();
					File.delete(File.FILESYSTEM_PATRIOT, fileName);
				}

			} catch (Exception e) {

			}
		} else {
			boolean ok = false;
			// synchronized (fileCache) {
			// int count = fileCache.size();
			// for (int i = 0; i < count; i += 2) {
			// String thing = (String) fileCache.elementAt(i);
			// if (thing.equals(fileName)) {
			// ok = true;
			// break;
			// }
			// }
			// }
			if (!ok) {
				doDeleteFile(fileName);
			}
		}
	}

	static void doDeleteFile(String fileName) {

		for (int i = 0; i < 10; i++) {
			FileMapping fileMapping = fileMappings[i];
			if (fileMapping == null) {
				break;
			}
			if (fileMapping.files.containsKey(fileName)) {
				RecordStore rs = openRecordStore(i, false);
				try {
					fileMapping.files.remove(fileName);
					saveFileMapping(fileMapping, rs);
				} catch (Exception e) {
				}
				break;
			}
		}
	}

	static byte[] readFile(String fileName) {
		fileName = Adaptor.replaceAll(fileName, "-", "_").toLowerCase();
		byte[] bytes = null;
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			if (files.contains(fileName)) {
				FileInputStream fileIn = null;
				try {
					fileIn = new FileInputStream(File.FILESYSTEM_PATRIOT,
							fileName);
					bytes = new byte[fileIn.available()];
					fileIn.read(bytes);
				} catch (IOException ioe) {
				} finally {
					if (fileIn != null) {
						try {
							fileIn.close();
						} catch (Exception e) {

						}
					}
				}
			}
		} else {

			// synchronized (fileCache) {
			// int count = fileCache.size();
			// for (int i = 0; i < count; i += 2) {
			// String thing = (String) fileCache.elementAt(i);
			// if (thing.equals(fileName)) {
			// bytes = (byte[]) fileCache.elementAt(i + 1);
			// break;
			// }
			// }
			// }
			if (bytes == null) {
				bytes = doReadFile(fileName);
			}
		}
		return bytes;
	}

	static byte[] doReadFile(String fileName) {

		byte[] bytes = null;
		for (int i = 0; i < 10; i++) {
			FileMapping fileMapping = fileMappings[i];
			if (fileMapping == null) {
				break;
			}
			if (fileMapping.files.containsKey(fileName)) {
				Integer temp = (Integer) fileMapping.files.get(fileName);
				RecordStore rs = openRecordStore(i, false);
				try {
					bytes = rs.getRecord(temp.intValue());
				} catch (Exception t) {
				}
				break;
			}
		}
		return bytes;
	}

	static boolean fileExists(String fileName) {
		fileName = Adaptor.replaceAll(fileName, "-", "_").toLowerCase();
		boolean has = false;
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			try {
				has = File.findFirst(File.FILESYSTEM_PATRIOT, fileName) != null;
			} catch (Exception e) {
				has = true;
			}
		} else {

			// synchronized (fileCache) {
			// int count = fileCache.size();
			// for (int i = 0; i < count; i += 2) {
			// String thing = (String) fileCache.elementAt(i);
			// if (thing.equals(fileName)) {
			// has = true;
			// break;
			// }
			// }
			// }
			if (!has) {
				has = doFileExists(fileName);
			}
		}
		return has;
	}

	static boolean doFileExists(String fileName) {
		for (int i = 0; i < 10; i++) {
			FileMapping fileMapping = fileMappings[i];
			if (fileMapping == null) {
				break;
			}
			if (fileMapping.files.containsKey(fileName)) {
				return true;
			}
		}
		return false;
	}

	static String hash(String input) {

		StringBuffer buffer = new StringBuffer();
		byte[] bytes = md5.getHash(input);
		int len = bytes.length;
		for (int i = 0; i < len; i++) {
			buffer.append((char) bytes[i]);
		}
		String result = buffer.toString();
		return result;
	}

	static Vector getFileList() {
		if (File.isFileSystemSupported(File.FILESYSTEM_PATRIOT)) {
			return files;
		} else {
			Vector thing = new Vector();
			for (int i = 0; i < 10; i++) {
				FileMapping fileMapping = fileMappings[i];
				if (fileMapping == null || fileMapping.files == null) {
					break;
				}
				Enumeration e = fileMapping.files.keys();
				while (e.hasMoreElements()) {
					thing.addElement((String) e.nextElement());
				}
			}
			return thing;
		}
	}

}
