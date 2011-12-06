package com.binartech.gpssignaltracker.core.tests;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.binartech.gpssignaltracker.core.Common;

public class GpsStartTest extends SimpleTest
{
	public static enum Option
	{
		COLD_START, WARM_START, WARM_START_WITH_INJECT, NORMAL
	}

	private final LocationManager mManager;
	private long mGpsStarted;
	private final Option mOption;

	public GpsStartTest(LogAdapter adapter, Option option)
	{
		super(adapter);
		mOption = option;
		mManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	protected boolean onExecute()
	{
		switch (mOption)
		{
			case COLD_START:
			{
				writeLog("Cold start");
				if (Common.isAirplaneModeOn(getContext()))
				{
					return startTest();
				}
				else
				{
					writeLog("Error: Airplane mode is turned off");
					return false;
				}
			}
			case WARM_START:
			{
				writeLog("Warm start");
				if (!Common.isAirplaneModeOn(getContext()))
				{
					
					return startTest();
				}
				else
				{
					writeLog("Error: Airplane mode is turned on");
					return false;
				}
			}
			case NORMAL:
			{
				writeLog("Normal start");
				if (!Common.isAirplaneModeOn(getContext()))
				{
					
					if (mManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
					{
						mThirdStage.run();
						return true;
					}
					else
					{
						writeLog("Error: GPS disabled in phone settings");
						return false;
					}
				}
				else
				{
					writeLog("Error: Airplane mode is turned on");
					return false;
				}
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	private boolean startTest()
	{
		if (mManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
		{
			writeLog("Clearing AGPS data.");
			mManager.sendExtraCommand(LocationManager.GPS_PROVIDER, "delete_aiding_data", null);
			writeLog("Data cleared");
			writeLog("Turn GPS setting OFF");
			Common.turnGPSOff(getContext());
			writeLog("GPS turned off");
			writeLog("Waiting 2sec");
			postDelayed(mSecondStage, 2000);
			return true;
		}
		else
		{
			writeLog("Error: GPS disabled in phone settings");
			return false;
		}
	}

	private final Runnable mSecondStage = new Runnable()
	{

		@Override
		public void run()
		{
			writeLog("Turn GPS setting ON");
			Common.turnGPSOn(getContext());
			writeLog("Waiting 2sec");
			postDelayed(mThirdStage, 2000);
		}
	};

	private final Runnable mThirdStage = new Runnable()
	{

		@Override
		public void run()
		{
			writeLog("Turning GPS on");
			mManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
			mGpsStarted = System.currentTimeMillis();
			writeLog("GPS started. Waiting for first location");
		}
	};

	private final LocationListener mLocationListener = new LocationListener()
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
			writeLog("GPS has fix");
			writeLog("Time: " + (System.currentTimeMillis() - mGpsStarted) + "ms");
			cancel();
		}
	};

	@Override
	protected void onCancel()
	{
		cancelExecutionOf(mThirdStage);
		cancelExecutionOf(mSecondStage);
		mManager.removeUpdates(mLocationListener);
	}

}
