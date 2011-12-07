package com.binartech.gpssignaltracker.services;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.binartech.gpssignaltracker.PeriodicReceiver;
import com.binartech.gpssignaltracker.core.Common;

public class GpsHotStartTest extends Service
{
	//#ifdef DEBUG
	public static final String TAG = GpsHotStartTest.class.getSimpleName();

	//#endif
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}

	private PrintWriter mLogWriter;
	private WakeLock mWakeLock;
	private LocationManager mLocationManager;
	private Handler mHandler;

	@Override
	public void onCreate()
	{
		mHandler = new Handler();
		mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		try
		{
			final File dir = Common.getGpsHotStartTestDir();
			dir.mkdirs();
			mLogWriter = new PrintWriter(new File(dir, Common.FILE_FORMAT.format(new Date()) + ".txt"));
			mLocationManager.addGpsStatusListener(mGpsStatusListener);
			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);

		}
		catch (Exception e)
		{
			//#ifdef DEBUG
			Log.w(TAG, e);
			//#endif
			stopSelf();
		}
		super.onCreate();
	}

	@Override
	public void onDestroy()
	{
		mLocationManager.removeGpsStatusListener(mGpsStatusListener);
		mLocationManager.removeUpdates(mLocationListener);
		if (mLogWriter != null)
		{
			mLogWriter.close();
		}
		mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		PeriodicReceiver.releaseWakeLock();
		super.onStart(intent, startId);
	}

	private void writeLog(String line)
	{
		mLogWriter.println(Common.LOG_FORMAT.format(new Date()) + " " + line);
	}

	private LocationListener mLocationListener = new LocationListener()
	{

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras)
		{
			// TODO Auto-generated method stub

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
			
		}
	};

	private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener()
	{

		@Override
		public void onGpsStatusChanged(int event)
		{
			final GpsStatus status = mLocationManager.getGpsStatus(null);
			switch (event)
			{
				case GpsStatus.GPS_EVENT_FIRST_FIX:
				{
					writeLog("First fix within: " + status.getTimeToFirstFix() + "ms");
					mHandler.postDelayed(new Runnable()
					{
						
						@Override
						public void run()
						{
							writeLog("GPS turned off");
							stopSelf();
						}
					}, 2000);
				}
				break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				{
					writeLog("Satelittes:");
					for (GpsSatellite sat : status.getSatellites())
					{			
						mLogWriter.println(String.format(Locale.FRANCE, "PRN: %02d; SNR: %02.2f; ELV: %05.2f; AZM: %06.2f; ALM: %d; EFE: %d; FIX: %d", sat.getPrn(), sat.getSnr(), sat.getElevation(), sat.getAzimuth(), sat.hasAlmanac() ? 1 : 0, sat.hasEphemeris() ? 1 : 0, sat.usedInFix() ? 1 : 0));
					}
				}
				break;
				case GpsStatus.GPS_EVENT_STARTED:
				{
					writeLog("GPS turned ON");
				}break;
				case GpsStatus.GPS_EVENT_STOPPED:
				{
					writeLog("GPS turned OFF");
				}break;
			}
		}
	};
}
