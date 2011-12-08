package com.binartech.gpssignaltracker;

import java.io.File;
import java.io.PrintWriter;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.binartech.gpssignaltracker.core.Common;
import com.binartech.gpssignaltracker.core.tests.GpsStartTest;
import com.binartech.gpssignaltracker.core.tests.GpsStartTest.Option;
import com.binartech.gpssignaltracker.core.tests.GpsPredictionTest;
import com.binartech.gpssignaltracker.core.tests.LogAdapter;
import com.binartech.gpssignaltracker.core.tests.SimpleTest;

public class ActivityGpsTest extends Activity implements LogAdapter
{
	private static final int DLG_AIRPLANE_MODE = 0x01;
	
	private TextView mTextViewLog;
	private ScrollView mScroller;
	private PrintWriter mLog;
	private SimpleTest mGpsTest;
	private Handler mHandler;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		mHandler = new Handler();
		setContentView(R.layout.activity_gps_test);
		mTextViewLog = (TextView)findViewById(R.id.gtest_textview_log);
		mScroller = (ScrollView)findViewById(R.id.gtest_scrollview);
		File dir = Common.getGpsStartTesterDir();
		try
		{
			dir.mkdirs();
			mLog = new PrintWriter(new File(dir, Common.FILE_FORMAT.format(new Date())+".txt"));
		}
		catch (Exception e)
		{
			Toast.makeText(getApplicationContext(), "Can not create log file. Make sure you have enough space on your SD card.", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onDestroy()
	{
		if(mGpsTest != null && mGpsTest.isExecuting())
		{
			mGpsTest.cancel();
		}
		mLog.close();
		super.onDestroy();
	}

	@Override
	public Context getContext()
	{
		return this;
	}

	@Override
	public Handler getHandler()
	{
		return mHandler;
	}

	public void writeLog(String line)
	{
		final String logLine = Common.LOG_FORMAT.format(new Date())+" "+line;
		mLog.println(logLine);
		mTextViewLog.append(logLine+"\n");
		mScroller.post(new Runnable()
		{
			@Override
			public void run()
			{
				mScroller.fullScroll(View.FOCUS_DOWN);
			}
		});
	}
	
	public void onButtonClick(View view)
	{
		switch(view.getId())
		{
			case R.id.gtest_button_coldstart:
			{
				runTest(Option.COLD_START);
			}break;
			case R.id.gtest_button_warmstart:
			{
				runTest(Option.WARM_START);
			}break;
			case R.id.gtest_button_normalstart:
			{
				runTest(Option.NORMAL);
			}break;
			case R.id.gtest_button_hotstart_test_periodic:
			{
				PeriodicReceiver.setPeriodicWork(this, AlarmManager.INTERVAL_HOUR);
			}break;
			case R.id.gtest_gps_prediction_test:
			{
				runTest(new GpsPredictionTest(this));
			}break;
		}
	}

	private void runTest(SimpleTest test)
	{
		if(mGpsTest == null || !mGpsTest.isExecuting())
		{
			mGpsTest = test;
			mGpsTest.execute();
		}
		else
		{
			mGpsTest.cancel();
			mGpsTest = null;
		}
	}
	
	/**
	 * 
	 */
	private void runTest(Option option)
	{
		runTest(new GpsStartTest(this, option));
	}

}
