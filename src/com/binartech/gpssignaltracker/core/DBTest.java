package com.binartech.gpssignaltracker.core;

import java.util.Random;

import android.content.Context;
import android.database.DatabaseUtils.InsertHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class DBTest extends SQLiteOpenHelper
{
	private static final String TAG = DBTest.class.getSimpleName();
	public DBTest(Context context)
	{
		super(context, "sectors", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE sectors(id INTEGER PRIMARY KEY ASC, sector_id INTEGER NOT NULL, sector_hash INTEGER NOT NULL, threat_file_index INTEGER NOT NULL, threat_offset INTEGER NOT NULL, lat INTEGER NOT NULL, lng INTEGER NOT NULL, dir INTEGER NOT NULL, speed INTEGER NOT NULL, flags INTEGER NOT NULL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO Auto-generated method stub
	}
	
	public void testInsertion()
	{
		InsertTask task = new InsertTask();
		task.execute();
	}
	
	private void insertTest()
	{
		Random rand = new Random();
		SQLiteDatabase db = getWritableDatabase();
		db.execSQL("delete from sectors");
		final long start = SystemClock.elapsedRealtime();
		final int recordCount = 10000;
		boolean result = false;
		db.beginTransaction();
		try
		{
			
			InsertHelper ih = new InsertHelper(db, "sectors");
			final int cSectorId = ih.getColumnIndex("sector_id");
			final int cSectorHash = ih.getColumnIndex("sector_hash");
			final int cThreatFileIndex = ih.getColumnIndex("threat_file_index");
			final int cThreatFileOffset = ih.getColumnIndex("threat_offset");
			final int cLat = ih.getColumnIndex("lat");
			final int cLng = ih.getColumnIndex("lng");
			final int cDir = ih.getColumnIndex("dir");
			final int cSpeed = ih.getColumnIndex("speed");
			final int cFlags = ih.getColumnIndex("flags");
			for(int i = 0; i < recordCount; ++i)
			{
				ih.prepareForInsert();
				ih.bind(cSectorId, i+1);
				ih.bind(cSectorHash, rand.nextInt());
				ih.bind(cThreatFileIndex, rand.nextInt());
				ih.bind(cThreatFileOffset, rand.nextInt());
				ih.bind(cLat, rand.nextInt());
				ih.bind(cLng, rand.nextInt());
				ih.bind(cDir, rand.nextInt());
				ih.bind(cSpeed, rand.nextInt());
				ih.bind(cFlags, rand.nextInt());
				ih.execute();
			}
			db.setTransactionSuccessful();
			result = true;
		}
		catch (Exception e)
		{
			//#ifdef DEBUG
			Log.w(TAG, e);
			//#endif
		}
		db.endTransaction();
		if(result)
		{
			try
			{
				db.execSQL("CREATE INDEX IF NOT EXISTS sectors_index ON sectors(sector_id)");
			}
			catch (Exception e)
			{
				//#ifdef DEBUG
				Log.w(TAG, e);
				//#endif
			}
		}
		final long diff = SystemClock.elapsedRealtime() - start;
		//#ifdef DEBUG
		Log.v(TAG, String.format("Inserted %d records in %dms", recordCount, diff));
		//#endif
	}

	private class InsertTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params)
		{
			insertTest();
			return null;
		}
		
	}
}
