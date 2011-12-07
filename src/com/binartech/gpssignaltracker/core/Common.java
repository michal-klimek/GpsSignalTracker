package com.binartech.gpssignaltracker.core;

import java.io.File;
import java.text.SimpleDateFormat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;

public class Common
{
	public static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	public static final SimpleDateFormat LOG_FORMAT = new SimpleDateFormat("HH:mm:ss");
	public static File getGpsStartTesterDir()
	{
		return new File(Environment.getExternalStorageDirectory(), "Binartech/GpsStartTester");
	}
	
	public static File getGpsHotStartTestDir()
	{
		return new File(Environment.getExternalStorageDirectory(), "Binartech/GpsHotStartTester");
	}
	
	public static boolean isAirplaneModeOn(Context context)
	{
		return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	public static void turnGPSOn(Context context)
	{
		String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (!provider.contains("gps"))
		{
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			context.sendBroadcast(poke);
		}
	}

	public static void turnGPSOff(Context context)
	{
		String provider = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

		if (provider.contains("gps"))
		{
			final Intent poke = new Intent();
			poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
			poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
			poke.setData(Uri.parse("3"));
			context.sendBroadcast(poke);
		}
	}
	

}
