package com.binartech.gpssignaltracker.services;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class Tracker extends Service
{
	public static final String KEY_STRING_DESCRIPTION = "com.binartech.gpssignaltracker.string_desc";
	public static final String ACTION_NEW_DATA = "com.binartech.gpssignaltracker.action_new_data";
	public static final String KEY_BYTE_ARRAY_DATA = "com.binartech.gpssignaltracker.key_marshalled_data";
	private static final String TAG = Tracker.class.getSimpleName();
	public static boolean isRunning;
	private WakeLock mWakeLock;
	private File mLogDir;
	private final SimpleDateFormat mFileTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private int mLastPrnIndex;
	private HashMap<Integer, Integer> mPrnToIndex = new HashMap<Integer, Integer>();
	private LocationManager mLocationManager;
	private DataOutputStream mSnrChanges, mPrnChanges;
	private boolean isSetUp;
	private boolean mHasFix;
	private Location mLastLocation;
	
	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		if (!isSetUp)
		{
			String desc = intent.getStringExtra(KEY_STRING_DESCRIPTION);
			if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
			{
				mLogDir = new File(Environment.getExternalStorageDirectory(), "Binartech");
				mLogDir = new File(mLogDir, "GpsSignalTracker");
				mLogDir = new File(mLogDir, mFileTime.format(new Date()));
				if (mLogDir.exists() || mLogDir.mkdirs())
				{
					PrintStream ps = null;
					try
					{
						ps = new PrintStream(new File(mLogDir, "info.txt"));
						ps.println(desc);
					}
					catch (Exception e)
					{
						//#ifdef DEBUG
						Log.w(TAG, e);
						//#endif
					}
					finally
					{
						try
						{
							ps.close();
						}
						catch (Exception e)
						{

						}
					}
					try
					{
						mSnrChanges = new DataOutputStream(openFileOutput(new File(mLogDir, "snrchanges.bin")));
					}
					catch (Exception e)
					{
						stopSelf();
						return;
					}
					try
					{
						mPrnChanges = new DataOutputStream(openFileOutput(new File(mLogDir, "prnchanges.bin")));
					}
					catch (Exception e)
					{
						stopSelf();
						return;
					}
					mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
					mLocationManager.addGpsStatusListener(mGpsStatusListener);
				}
				else
				{
					stopSelf();
				}
			}
			else
			{
				stopSelf();
			}
			isSetUp = true;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		//#ifdef DEBUG
		Log.v(TAG, "onCreate()");
		//#endif
		mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		PowerManager power = (PowerManager)getSystemService(POWER_SERVICE);
		mWakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
	}

	@Override
	public void onDestroy()
	{
		//#ifdef DEBUG
		Log.v(TAG, "onDestroy()");
		//#endif
		isSetUp = false;
		mLocationManager.removeUpdates(mLocationListener);
		mLocationManager.removeGpsStatusListener(mGpsStatusListener);
		try
		{
			mSnrChanges.close();
		}
		catch (Exception e)
		{
			//#ifdef DEBUG
			Log.w(TAG, e);
			//#endif
		}
		try
		{
			mPrnChanges.close();
		}
		catch (Exception e)
		{
			//#ifdef DEBUG
			Log.w(TAG, e);
			//#endif
		}
		mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	
	private final GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener()
	{
		private final ArrayList<GpsSatellite> list = new ArrayList<GpsSatellite>();
		@Override
		public void onGpsStatusChanged(int event)
		{
			if (isSetUp)
			{
				switch (event)
				{
					case GpsStatus.GPS_EVENT_FIRST_FIX:
					{

					}
					break;
					case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
					{
						final long now = System.currentTimeMillis();
						GpsStatus status = mLocationManager.getGpsStatus(null);
						list.clear();
						for(GpsSatellite sat : status.getSatellites())
						{
							list.add(sat);
						}
						try
						{
							writePrnFrame(mPrnChanges, list, now);
						}
						catch (Exception e)
						{
							//#ifdef DEBUG
							Log.w(TAG, e);
							//#endif
						}
						try
						{
							SnrFrame.writeSnrChangeFrame(mSnrChanges, list, mLastLocation, now);
						}
						catch (Exception e)
						{
							//#ifdef DEBUG
							Log.w(TAG, e);
							//#endif
						}
						byte[] data = SnrFrame.marshalSnrChangeFrame(list, mLastLocation, now);
						Intent intent = new Intent(ACTION_NEW_DATA);
						intent.putExtra(KEY_BYTE_ARRAY_DATA, data);
						sendBroadcast(intent);
					}
					break;
				}
			}
		}
	};
	
	private final LocationListener mLocationListener = new LocationListener()
	{
		
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			switch(status)
			{
				case LocationProvider.AVAILABLE:
				{

				}break;
				case LocationProvider.OUT_OF_SERVICE:
				{
					mHasFix = false;
					mLastLocation = null;
				}break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
				{
					mHasFix = false;
					mLastLocation = null;
				}break;
			}
		}
		
		@Override
		public void onProviderEnabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProviderDisabled(String provider)
		{
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onLocationChanged(Location location)
		{
			mHasFix = true;	
			mLastLocation = location;
		}
	};
	
	private OutputStream openFileOutput(File file) throws IOException
	{
		return new BufferedOutputStream(new FileOutputStream(file), 16 * 1024);
	}
	
	public static void writePrnFrame(DataOutput dos, List<GpsSatellite> sats, long time) throws IOException
	{
		dos.writeLong(time);
		dos.writeInt(sats.size());
		for(GpsSatellite sat : sats)
		{
			dos.writeInt(sat.getPrn());
		}
	}
	
	
}
