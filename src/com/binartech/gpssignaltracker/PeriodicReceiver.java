package com.binartech.gpssignaltracker;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;

import com.binartech.gpssignaltracker.services.GpsHotStartTest;

public class PeriodicReceiver extends BroadcastReceiver
{
	private static final int ONGOING_ID = 0x484;
	private static final String ACTION_AWAKE = GlobalApp.APP_PACKAGE + ".action_awake_periodic";
	private static final String ACTION_DISABLE = GlobalApp.APP_PACKAGE+".action_awake_disable";
	//#ifdef DEBUG
	private static final String TAG = PeriodicReceiver.class.getSimpleName();
	//#endif
	private static WakeLock sWakeLock;
	
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(ACTION_AWAKE.equals(intent.getAction()))
		{
			if(sWakeLock == null)
			{
				sWakeLock = ((PowerManager)context.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
				sWakeLock.setReferenceCounted(false);
			}
			sWakeLock.acquire();
			Intent service = new Intent(context, GpsHotStartTest.class);
			context.startService(service);
		}
		else if(ACTION_DISABLE.equals(intent.getAction()))
		{
			setPeriodicWork(context, null);
		}
	}

	public static void releaseWakeLock()
	{
		if(sWakeLock != null)
		{
			sWakeLock.release();
		}
	}
	
	/**
	 * Uruchamia cykliczne w³aczanie us³ugi testu hot start
	 * @param context
	 * @param period okres w milisekundach lub {@code null}, ¿eby wy³¹czyæ
	 */
	public static void setPeriodicWork(Context context, Long period)
	{
		final PendingIntent pintent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_AWAKE), PendingIntent.FLAG_UPDATE_CURRENT);
		final AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		manager.cancel(pintent);
		if(period != null)
		{
			notifyPeriodicCheckState(context, true);
			manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+3000, period, pintent);
		}
		else
		{
			notifyPeriodicCheckState(context, false);
		}
	}
	
	private static void notifyPeriodicCheckState(Context context,  boolean enabled)
	{
		final NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (enabled)
		{
			Notification ntf = new Notification(com.binartech.gpssignaltracker.R.drawable.ic_gps_tester, "GPS hot start test started!", System.currentTimeMillis());
			Intent acc = new Intent(context, PeriodicReceiver.class);
			acc.setAction(ACTION_DISABLE);
			ntf.setLatestEventInfo(context, "GPS hot start test active", "Click here to cancel", PendingIntent.getBroadcast(context, 0, acc, PendingIntent.FLAG_UPDATE_CURRENT));
			ntf.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
			manager.notify(ONGOING_ID, ntf);
		}
		else
		{
			manager.cancel(ONGOING_ID);
		}
	}
}
