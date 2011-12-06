package com.binartech.gpssignaltracker.core.tests;

import android.content.Context;

public abstract class SimpleTest
{
	private final LogAdapter mLog;
	private boolean mIsExecuting;
	
	public SimpleTest(LogAdapter adapter)
	{
		mLog = adapter;
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
	
	public boolean isExecuting()
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
	
	protected Context getContext()
	{
		return mLog.getContext();
	}
	
	protected void writeLog(String line)
	{
		mLog.writeLog(line);
	}
}
