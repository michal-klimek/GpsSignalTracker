package com.binartech.gpssignaltracker;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import com.binartech.gpssignaltracker.services.SnrFrame;
import com.binartech.gpssignaltracker.services.Tracker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ActivityMain extends Activity
{
	//#ifdef DEBUG
	private static final String TAG = ActivityMain.class.getSimpleName();
	//#endif
	private Button mStart, mStop;
	private EditText mEdit;
	private TextView mText;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mStart = (Button)findViewById(R.id.main_button_start);
		mStop = (Button)findViewById(R.id.main_button_stop);
		mEdit = (EditText)findViewById(R.id.main_edittext);
		mText = (TextView)findViewById(R.id.main_textview_status);
		mStart.setEnabled(!Tracker.isRunning);
		mStop.setEnabled(Tracker.isRunning);
		registerReceiver(mReceiver, new IntentFilter(Tracker.ACTION_NEW_DATA));
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			byte[] marshal = intent.getByteArrayExtra(Tracker.KEY_BYTE_ARRAY_DATA);
			try
			{
				DataInputStream dis = new DataInputStream(new ByteArrayInputStream(marshal));
				SnrFrame frame = new SnrFrame(dis);
				mText.setText(frame.toString());
			}
			catch (Exception e)
			{
				//#ifdef DEBUG
				Log.w(TAG, e);
				//#endif
			}
		}
	};
	
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.main_button_start:
			{
				String name = mEdit.getText().toString();
				if(name.length() == 0)
				{
					name = "Unnamed";
				}
				Intent intent = new Intent(this, Tracker.class);
				intent.putExtra(Tracker.KEY_STRING_DESCRIPTION, name);
				startService(intent);
			}break;
			case R.id.main_button_stop:
			{
				stopService(new Intent(this, Tracker.class));
			}break;
		}
	}
}