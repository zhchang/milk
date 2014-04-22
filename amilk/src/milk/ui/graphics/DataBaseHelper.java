package milk.ui.graphics;

import java.util.Hashtable;

import milk.ui.model.SqLitePool;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseHelper extends SQLiteOpenHelper {

	private String DB_NAME = "newsdb";
	private static final String TB_NAME = "sqliteTable";
	private static final String COLUMNS_ID = "id";
	private static final String COLUMNS_DATA = "data";
	private String sql;

	private static final String tag=DataBaseHelper.class.getName();
	
	public DataBaseHelper(Context context, String DBName) {
		super(context, DBName, null, 1);
		DB_NAME = DBName;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(tag, "DataBaseHelper create");
		if (this.sql == null || this.sql == "") {
			sql = "CREATE TABLE IF NOT EXISTS " + TB_NAME + " (" + COLUMNS_ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMNS_DATA
					+ " BLOB)";
		}

		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(tag, "DataBaseHelper onUpgrade");
	}

	private Cursor selectAll() {
//		SQLiteDatabase db = this.getReadableDatabase();
//		return db.query(TB_NAME, new String[] { COLUMNS_ID, COLUMNS_DATA },
//				null, null, null, null, null);
		return null;
	}

	public Hashtable<Integer, byte[]> getAllData() {
//		Hashtable<Integer, byte[]> table = null;
//		Cursor c = null;
//		try {
//			c = selectAll();
//			if (c != null && c.moveToFirst()) {
//				table = new Hashtable<Integer, byte[]>();
//				table.put(Integer.valueOf(c.getInt(0)), c.getBlob(1));
//			}
//			while (c.moveToNext()) {
//				table.put(Integer.valueOf(c.getInt(0)), c.getBlob(1));
//			}
//		} finally {
//			if (c != null) {
//				c.close();
//			}
//
//		}
//		return table;
		return null;
	}

	private Cursor selectById(int id) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(true, TB_NAME, new String[] { COLUMNS_ID,
				COLUMNS_DATA }, COLUMNS_ID + "=" + id, null, null, null, null,
				null);
		return cursor;
	}
//program
	public byte[] select(int id) {
		Cursor cursor = null;
		byte[] ret = null;
		try {
			cursor = selectById(id);
			if (cursor != null && cursor.getCount() > 0
					&& cursor.getColumnCount() > 0 && cursor.moveToFirst()) {
				ret = cursor.getBlob(1);
			}

		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (cursor != null) {
				cursor.close();
				cursor=null;
			}
		}
		try
		{
			Thread.sleep(3);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	public long insert(int recordId, byte[] data) {
		SQLiteDatabase db = null;
		long row = 0;
		try {
			db = this.getWritableDatabase();
//			ContentValues cv = new ContentValues();
			ContentValues cv=SqLitePool.getContentValues();
			cv.put(COLUMNS_DATA, data);
			cv.put(COLUMNS_ID, recordId);
			row = db.insert(TB_NAME, null, cv);
			SqLitePool.recovery(cv);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (db != null) {
				db.close();
			}
		}
		return row;

	}

	public long insert(byte[] data) {
		SQLiteDatabase db = null;
		long row = -1;
		try {
			db = this.getWritableDatabase();
//			ContentValues cv = new ContentValues();
			ContentValues cv=SqLitePool.getContentValues();
			cv.put(COLUMNS_DATA, data);
			row = db.insert(TB_NAME, null, cv);
			SqLitePool.recovery(cv);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (db != null) {
				db.close();
			}
		}
		return row;

	}

	public void delete(int index) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			String where = COLUMNS_ID + "=?";
			String[] whereValue = { Integer.toString(index) };
			db.delete(TB_NAME, where, whereValue);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public void update(int id, byte[] data) {
		SQLiteDatabase db = null;
		try {
			db = this.getWritableDatabase();
			String where = COLUMNS_ID + "=?";
			String[] whereValue = { Integer.toString(id) };
//			ContentValues cv = new ContentValues();
			ContentValues cv=SqLitePool.getContentValues();
			cv.put(COLUMNS_DATA, data);
			db.update(TB_NAME, cv, where, whereValue);
			SqLitePool.recovery(cv);
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if (db != null) {
				db.close();
			}
		}
	}
}
