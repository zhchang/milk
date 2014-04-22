package milk.ui.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import milk.implement.Adaptor;
import milk.implement.MD5;
import milk.implement.MilkInputStream;
import milk.implement.MilkOutputStream;
import milk.ui.graphics.RecordStore;
import milk.ui.internal.SDCardUtil;
import milk.utils.IOUtil;
import android.util.Log;

public class SQLiteStore
{

	private static String tag = SQLiteStore.class.getName();

	// private static MD5 md5 = new MD5();

	private static FileMapping fileMapping;
	private static RecordStore rescourceRecordStores;
	private static RecordStore fileMappingRecordStores;

	private static final String RESOURCE_DB = "ResourceDB";
	private static final String FILE_MAPPING_DB = "MappingDB";

	static
	{
		fileMapping = new FileMapping(new Hashtable());
		rescourceRecordStores = RecordStore.openRecordStore(RESOURCE_DB, true);
		fileMappingRecordStores = RecordStore.openRecordStore(FILE_MAPPING_DB, true);
		try
		{
			byte[] bytes = null;
			// bytes = SDCardUtil.getFileBytes(SD_PATH, FILE_MAPPING_DB);

			if (bytes == null)
			{

				bytes = fileMappingRecordStores.getRecord(1);

				// if (bytes != null)
				// {
				// boolean isSaveSDCard= SDCardUtil.setFileBytes(SD_PATH,
				// FILE_MAPPING_DB, bytes);
				// if(isSaveSDCard)
				// {
				// Log.i(tag, "SDCardUtil save len "+bytes.length);
				// }
				// else
				// {
				// Log.i(tag, "SDCardUtil not save len "+bytes.length);
				// }
				// }
			} else
			{
				// Log.i(tag, "SDCardUtil bytes len "+bytes.length);
			}

			if (bytes != null)
			{
				MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(bytes));
				while (dis.available() > 0)
				{
					String fileName = Adaptor.readVarChar(dis);
					int id = dis.readInt();
					Log.i(tag, "fileName " + fileName + "  " + id);
					fileMapping.files.put(fileName, new Integer(id));
				}
			} else
			{
				Log.i(tag, "static bytes is null");
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			Adaptor.infor("util Throwable");
		}

	}

	public static void release()
	{
		if (fileMappingRecordStores != null)
		{
			fileMappingRecordStores.closeRecordStore();
			fileMappingRecordStores = null;
		}

		if (rescourceRecordStores != null)
		{
			rescourceRecordStores.closeRecordStore();
			rescourceRecordStores = null;
		}

		if (fileMapping != null)
		{
			fileMapping = null;
		}

	}

	private static void saveFileMapping(FileMapping fileMapping)
	{
		try
		{
			fileMapping.needUpdate = true;
			fileMapping.lastRequested = System.currentTimeMillis();
		} catch (Exception t)
		{
			Adaptor.exception(t);
		}
	}

	public static void updateAllMappings()
	{

		if (fileMapping != null)
		{
			if (fileMapping.needUpdate)
			{
				if (System.currentTimeMillis() - fileMapping.lastRequested > 500)
				{
					try
					{
						byte[] temp = fileMapping.getBytes();
						// boolean isSuccess = SDCardUtil.setFileBytes(SD_PATH,
						// FILE_MAPPING_DB, temp);
						// if(isSuccess)
						// {
						// Log.i(tag,"SDCardUtil setFileBytes");
						// }
						// if (!isSuccess || !SDCardUtil.checkSDCARD())

						{
							byte[] bytes = fileMappingRecordStores.getRecord(1);
							if (bytes == null)
							{
								fileMappingRecordStores.addRecord(temp, 0, temp.length);
								Log.i(tag, "fileMappingRecordStores updateAllMappings addRecord");
							} else
							{
								fileMappingRecordStores.setRecord(1, temp, 0, temp.length);
								Log.i(tag, "fileMappingRecordStores updateAllMappings setRecord");
							}
							Log.i(tag, "sqlite setFile");
						}

						Log.i(tag, "fileMappingRecordStores updateAllMappings");
						fileMapping.needUpdate = false;
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static void doSaveFile(String fileName, byte[] bytes)
	{

		byte[] data = PrepackResource.getStoreByte(fileName, bytes);
		Log.i("Store", "fileName " + fileName);
		Log.i("Store", "fileName " + data.length);

		if (fileMapping == null)
		{
			fileMapping = new FileMapping(new Hashtable());
		}
		try
		{
			if (fileMapping.files.containsKey(fileName))
			{
				rescourceRecordStores.setRecord(((Integer) fileMapping.files.get(fileName)).intValue(), data, 0, data.length);
			} else
			{
				int id = rescourceRecordStores.addRecord(data, 0, data.length);
				fileMapping.files.put(fileName, new Integer(id));
				saveFileMapping(fileMapping);
			}
		} catch (Exception t)
		{
			doDeleteFile(fileName);
		}
	}

	public static void doDeleteFile(String fileName)
	{
		if (fileMapping == null)
		{
			return;
		}
		if (fileMapping.files.containsKey(fileName))
		{
			Integer temp = (Integer) fileMapping.files.get(fileName);
			try
			{
				rescourceRecordStores.deleteRecord(temp.intValue());
				fileMapping.files.remove(fileName);
				saveFileMapping(fileMapping);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}




	public static byte[] doReadFile(String fileName)
	{

		byte[] bytes = null;
		if (fileMapping != null)
		{
			if (fileMapping.files.containsKey(fileName))
			{
				Integer temp = (Integer) fileMapping.files.get(fileName);
				try
				{
					{
						bytes = rescourceRecordStores.getRecord(temp.intValue());
					}

					if (bytes == null)
					{
						return null;
					}

					MilkInputStream dis = new MilkInputStream(new ByteArrayInputStream(bytes));
					String fl = Adaptor.readIntStr(dis);
					if (fl.trim().equals(fileName.trim()))
					{
						byte[] dst = IOUtil.readBytes(dis);
						if (fileName.contains("database"))
						{
							Log.i("asd", "database  bytes" + dst.length);
						}
						// if(dst!=null&&dst.length>0)
						// {
						// dis.read(dst);
						return dst;
						// }
					}
					// else
					{
						Log.i(tag, "file name is error " + fileName);
						Log.i(tag, "file name fl is error " + fl);
						fileMapping.files.remove(fileName);
						fileMapping.needUpdate = true;
						rescourceRecordStores.deleteRecord(temp);
						Log.i(tag, "filename " + fileName + " file is not right");
					}

				} catch (Exception t)
				{
					t.printStackTrace();
				}
			}
		}
		return null;
	}

	public static boolean isExistFile(String fileName)
	{
		if (fileMapping == null)
		{
			return false;
		}
		if (fileMapping.files.containsKey(fileName))
		{
			return true;
		}
		return false;
	}

	// public static String hash(String input)
	// {
	//
	// StringBuffer buffer = new StringBuffer();
	// byte[] bytes = md5.getHash(input);
	// int len = bytes.length;
	// for (int i = 0; i < len; i++)
	// {
	// buffer.append((char) bytes[i]);
	// }
	// String result = buffer.toString();
	// return result;
	// }

}
