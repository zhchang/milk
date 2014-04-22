package milk.ui.store;

import milk.ui.internal.SDCardUtil;

public class Store
{

	private static final String tag = Store.class.getSimpleName();

	static
	{
	}

	public static void release()
	{
		if (SDCardUtil.isHasSDCard)
		{
			SDStore.release();
		} else
		{
			SQLiteStore.release();
		}
	}

	public static void updateAllMappings()
	{
		if (SDCardUtil.isHasSDCard)
		{
			SDStore.updateAllMappings();
		} else
		{
			SQLiteStore.updateAllMappings();
		}
		fl.doSaveFileList();
	}

	public static FileList fl = new FileList();

	public static void doSaveFile(String fileName, byte[] bytes)
	{
		fileName=PrepackResource.getPrepackResName(fileName);
		if (SDCardUtil.isHasSDCard)
		{
			SDStore.doSaveFile(fileName, bytes);
		} else
		{
			SQLiteStore.doSaveFile(fileName, bytes);
		}
		fl.add(fileName);
		fl.setNeedSave(true);
	}

	

	public static void doDeleteFile(String fileName)
	{
		fileName=PrepackResource.getPrepackResName(fileName);
		if (SDCardUtil.isHasSDCard)
		{
			SDStore.doDeleteFile(fileName);
		} else
		{
			SQLiteStore.doDeleteFile(fileName);
		}
	}

	public static byte[] doReadFile(String fileName)
	{
		fileName=PrepackResource.getPrepackResName(fileName);
		if (SDCardUtil.isHasSDCard)
		{
			return SDStore.doReadFile(fileName);
		} else
		{
			return SQLiteStore.doReadFile(fileName);
		}
	}

	public static boolean isExistFile(String fileName)
	{
		fileName=PrepackResource.getPrepackResName(fileName);
		
		if (SDCardUtil.isHasSDCard)
		{
			return SDStore.isExistFile(fileName);
		} else
		{
			return SQLiteStore.isExistFile(fileName);
		}
	}

}
