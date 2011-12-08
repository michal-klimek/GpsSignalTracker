package com.binartech.gpssignaltracker.core.tests;

import android.content.Context;
import android.location.LocationManager;

public abstract class SimpleTest
{
	private final LogAdapter mLog;
	private boolean mIsExecuting;
	private final LocationManager mLocationManager;
	
	public SimpleTest(LogAdapter adapter)
	{
		mLog = adapter;
		mLocationManager = (LocationManager)mLog.getContext().getSystemService(Context.LOCATION_SERVICE);
	}

	
	public final void execute()
	{
		mIsExecuting = onExecute();
	}
	
	public final void cancel()
	{
		onCancel();
		mIsExecuting = false;
	}
	
	public final boolean isExecuting()
	{
		return mIsExecuting;
	}
	
	protected final void notifyExecutionDone()
	{
		mIsExecuting = false;
	}
	
	protected final void postDelayed(Runnable runnable, int delayMs)
	{
		mLog.getHandler().postDelayed(runnable, delayMs);
	}
	
	protected final void cancelExecutionOf(Runnable runnable)
	{
		mLog.getHandler().removeCallbacks(runnable);
	}
	
	protected abstract boolean onExecute();
	
	protected abstract void onCancel();
	
	protected final Context getContext()
	{
		return mLog.getContext();
	}
	
	protected final void writeLog(String line)
	{
		mLog.writeLog(line);
	}
	
	protected final LocationManager getLocationManager()
	{
		return mLocationManager;
	}
}
