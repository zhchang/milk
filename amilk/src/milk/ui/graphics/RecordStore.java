package milk.ui.graphics;

import java.util.Hashtable;

import milk.ui.UIHelper;

import android.database.sqlite.SQLiteDatabase;

public final class RecordStore {

	private boolean writable=true;

	private String recordStoreName;

	private DataBaseHelper dataBaseHelper = null;

	private boolean cheakDataBase() {
		if (dataBaseHelper == null) {
			return false;
		} 
		else {
			SQLiteDatabase sqliteDatabase=dataBaseHelper.getReadableDatabase();
			if (sqliteDatabase.isOpen()) {
				return true;
			}
			return false;
		}
	}

	public int addRecord(byte[] data, int offset, int numBytes) {
		if (!cheakDataBase()) {
			return 0;
		}
		if (!writable) {
			return 0;
		}
		synchronized (dataBaseHelper) {
			return (int)dataBaseHelper.insert(data);
		}
		
	}

	public int insertRecord(byte[] data,int recordId) {
		if (!cheakDataBase()) {
			return 0;
		}
		if (!writable) {
			return 0;
		}
		synchronized (dataBaseHelper) {
			return (int)dataBaseHelper.insert(recordId,data);
		}
	}
	
	public void closeRecordStore() {
		dataBaseHelper.close();
	}

	public void deleteRecord(int recordId) {
		dataBaseHelper.delete(recordId);
	}

	public Hashtable getAllRecord()
	{
//		return dataBaseHelper.getAllData();
		return null;
	}
	
	public String getName() {
		return recordStoreName;
	}


	public byte[] getRecord(int recordId) {
		return dataBaseHelper.select(recordId);
	}

	public int getRecordSize(int recordId) {
		return (int) dataBaseHelper.getReadableDatabase().getPageSize();
	}

	public int getSize() {
		return (int) dataBaseHelper.getReadableDatabase().getMaximumSize();
	}

	public int getSizeAvailable() {
		return (int) dataBaseHelper.getReadableDatabase().getMaximumSize();
	}

	public int getVersion() {
		return dataBaseHelper.getReadableDatabase().getVersion();
	}

	public String getPath() {
		return dataBaseHelper.getReadableDatabase().getPath();
	}

	public static RecordStore openRecordStore(String recordStoreName,
			boolean createIfNecessary) {
		RecordStore recordStore = new RecordStore(recordStoreName);
		return recordStore;
	}

	public void setRecord(int recordId, byte[] newData, int offset, int numBytes) {
		synchronized (dataBaseHelper) {
			dataBaseHelper.update(recordId, newData);
		}
	}

	private RecordStore(String recordStoreName) {
		this.recordStoreName = recordStoreName;
		dataBaseHelper = new DataBaseHelper(UIHelper.milk,recordStoreName);
	}
	
	
}
