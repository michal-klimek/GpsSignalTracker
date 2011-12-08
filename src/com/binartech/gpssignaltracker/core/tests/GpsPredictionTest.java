package com.binartech.gpssignaltracker.core.tests;

import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.binartech.gpssignaltracker.core.Common;

public class GpsPredictionTest extends SimpleTest
{

	public GpsPredictionTest(LogAdapter adapter)
	{
		super(adapter);
	}

	@Override
	protected boolean onExecute()
	{
		writeLog("Gps Fix Prediction Test");
		if(!Common.isAirplaneModeOn(getContext()))
		{
			getLocationManager().addGpsStatusListener(mGpsStatusListener);
			getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mLocationListener);
			return true;
		}
		else
		{
			writeLog("Error: Airplane mode is turned on");
		}
		return false;
	}

	@Override
	protected void onCancel()
	{
		getLocationManager().removeUpdates(mLocationListener);
		getLocationManager().removeGpsStatusListener(mGpsStatusListener);
	}

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
			// TODO Auto-generated method stub
			
		}
	};
	
	private final GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener()
	{
		private boolean mEventsDisabled;
		@Override
		public void onGpsStatusChanged(int event)
		{
			final GpsStatus gpsStatus = getLocationManager().getGpsStatus(null);
			switch(event)
			{
				case GpsStatus.GPS_EVENT_FIRST_FIX:
				{
					writeLog("First fix in: "+gpsStatus.getTimeToFirstFix()+"ms");
					mEventsDisabled = true;
					postDelayed(new Runnable()
					{
						@Override
						public void run()
						{
							cancel();
						}
					}, 2000);
				}break;
				case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				{
					if (!mEventsDisabled)
					{
						int compatibleSatelittes = 0;
						int reachableSatelittes = 0;
						int trackedSatelittes = 0;
						int untrackedSatelittes = 0;
						for (GpsSatellite sat : gpsStatus.getSatellites())
						{
							if (sat.hasAlmanac() && sat.hasEphemeris())
							{
								trackedSatelittes++;
								if (sat.getSnr() >= 20.0f)
								{
									compatibleSatelittes++;
								}
								else if (sat.getSnr() >= 16.0f)
								{
									reachableSatelittes++;
								}
							}
							else
							{
								untrackedSatelittes++;
							}
						}
						writeLog("Tracked: "+trackedSatelittes+", untracked: "+untrackedSatelittes+", compatible: "+compatibleSatelittes+", reachable: "+reachableSatelittes);
						if (compatibleSatelittes > 4)
						{
							writeLog("Prediction: fix under 10sec");
						}
						else if (compatibleSatelittes == 4)
						{
							writeLog("Prediction: fix under 30sec");
						}
						else if (compatibleSatelittes == 3)
						{
							if (reachableSatelittes >= 2)
							{
								writeLog("Prediction: fix under 60sec");
							}
							else
							{
								writeLog("Prediction: fix unknown");
							}
						}
						else
						{
							writeLog("Prediction: fix unknown");
						}
					}
				}break;
			}
		}
	};
}
