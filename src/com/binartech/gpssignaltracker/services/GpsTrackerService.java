package com.binartech.gpssignaltracker.services;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.binartech.gpssignaltracker.ActivityMain;
import com.binartech.gpssignaltracker.core.PrnFrame;
import com.binartech.gpssignaltracker.core.SatelliteInfo;
import com.binartech.gpssignaltracker.core.SnrFrame;

public class GpsTrackerService extends Service
{
	private static final int NTF_ID = 0x0BABCA;
	public static final String KEY_STRING_DESCRIPTION = "com.binartech.gpssignaltracker.string_desc";
	public static final String ACTION_NEW_DATA = "com.binartech.gpssignaltracker.action_new_data";
	public static final String KEY_BYTE_ARRAY_DATA = "com.binartech.gpssignaltracker.key_marshalled_data";
	private static final String TAG = GpsTrackerService.class.getSimpleName();
	public static boolean isRunning;
	private WakeLock mWakeLock;
	private File mLogDir;
	private final SimpleDateFormat mFileTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private LocationManager mLocationManager;
	private DataOutputStream mSnrChanges, mPrnChanges;
	private boolean isSetUp;
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
					//#ifdef USE_NMEA
//@					mLocationManager.addNmeaListener(mNmeaListener);
					//#else
					mLocationManager.addGpsStatusListener(mGpsStatusListener);
					//#endif
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
		Notification ntf = new Notification(com.binartech.gpssignaltracker.R.drawable.ic_launcher, "Gps signal tracking is active.", System.currentTimeMillis());
		ntf.flags = Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT;
		Intent intent = new Intent(this, ActivityMain.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		ntf.setLatestEventInfo(this, "Tracking enabled", "Gps signal tracking is active.", PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
		startForeground(NTF_ID, ntf);
		isRunning = true;
	}

	@Override
	public void onDestroy()
	{
		//#ifdef DEBUG
		Log.v(TAG, "onDestroy()");
		//#endif
		isSetUp = false;
		mLocationManager.removeUpdates(mLocationListener);
		//#ifdef USE_NMEA
//@		mLocationManager.removeNmeaListener(mNmeaListener);
		//#else
		mLocationManager.removeGpsStatusListener(mGpsStatusListener);
		//#endif
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
		isRunning = false;
		stopForeground(true);
		mWakeLock.release();
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}
	//#ifdef USE_NMEA
//@	private final NmeaListener mNmeaListener = new NmeaListener()
//@	{
//@		private final Pattern regex = Pattern.compile("\\$GPGSV,(\\d*),(\\d*),(\\d*)((?:,\\d*,\\d*,\\d*,\\d*)+)");
//@		private final Pattern satRegex = Pattern.compile(",(\\d*),(\\d*),(\\d*),(\\d*)");
//@		private final ArrayList<SatelliteInfo> mSats = new ArrayList<SatelliteInfo>();
//@		@Override
//@		public void onNmeaReceived(long now, String nmea)
//@		{
//@			Matcher m;
//@			m = regex.matcher(nmea);
//@			if(m.find())
//@			{
//@				try
//@				{
//@					final int messageCount = Integer.parseInt(m.group(1));
//@					final int messageNumber = Integer.parseInt(m.group(2));
//@					final int satellites = Integer.parseInt(m.group(3));
					//#ifdef DEBUG
//@					Log.v(TAG, String.format("Messages: %d, Message number: %d, Satellites in view: %d", messageCount, messageNumber, satellites));
					//#endif
//@					if(messageNumber == 1)
//@					{
//@						mSats.clear();
//@					}
//@					final String sats = m.group(4);
//@					Matcher s = satRegex.matcher(sats);
//@					while(s.find())
//@					{
//@						try
//@						{
//@							final String sprn = s.group(1);
//@							final String selev = s.group(2);
//@							final String sazimuth = s.group(3);
//@							final String ssnr = s.group(4);
//@							final int prn = Integer.parseInt(sprn);
//@							final int elev = selev.length() > 0 ? Integer.parseInt(selev) : -1;
//@							final int azimuth = sazimuth.length() > 0 ? Integer.parseInt(sazimuth) : -1;
//@							final int snr = ssnr.length() > 0 ? Integer.parseInt(ssnr) : -1;
//@							mSats.add(new SatelliteInfo(prn, azimuth, elev, snr));
//@						}
//@						catch (Exception e)
//@						{
							//#ifdef DEBUG
//@							Log.w(TAG, e);
							//#endif
//@						}
//@					}
//@					if(messageNumber == messageCount)
//@					{
//@						try
//@						{
//@							PrnFrame.writePrnFrame(mPrnChanges, mSats, now);
//@						}
//@						catch (Exception e)
//@						{
							//#ifdef DEBUG
//@							Log.w(TAG, e);
							//#endif
//@						}
//@						try
//@						{
//@							SnrFrame.writeSnrChangeFrame(mSnrChanges, mSats, mLastLocation, now);
//@						}
//@						catch (Exception e)
//@						{
							//#ifdef DEBUG
//@							Log.w(TAG, e);
							//#endif
//@						}
//@						byte[] data = SnrFrame.marshalSnrChangeFrame(mSats, mLastLocation, now);
//@						Intent intent = new Intent(ACTION_NEW_DATA);
//@						intent.putExtra(KEY_BYTE_ARRAY_DATA, data);
//@						sendBroadcast(intent);
//@					}
//@				}
//@				catch (Exception e)
//@				{
					//#ifdef DEBUG
//@					Log.w(TAG, e);
					//#endif
//@				}
//@			}
//@		}
//@	};
	//#else
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
						final Iterable<GpsSatellite> satellites = status.getSatellites();
						for(GpsSatellite sat : satellites)
						{
							list.add(sat);
						}
						try
						{
							PrnFrame.writePrnFrame(mPrnChanges, list, now);
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
	//#endif
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
					mLastLocation = null;
				}break;
				case LocationProvider.TEMPORARILY_UNAVAILABLE:
				{
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
			mLastLocation = location;
		}
	};
	
	private OutputStream openFileOutput(File file) throws IOException
	{
		return new BufferedOutputStream(new FileOutputStream(file), 16 * 1024);
	}	
}
