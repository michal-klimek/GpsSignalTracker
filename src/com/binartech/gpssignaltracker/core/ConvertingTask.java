package com.binartech.gpssignaltracker.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class ConvertingTask extends AsyncTask<Void, Void, Integer>
{
	//#ifdef DEBUG
	private static final String TAG = ConvertingTask.class.getSimpleName();
	//#endif
	private final Activity mParent;
	private ProgressDialog mDialog;

	public ConvertingTask(Activity parent)
	{
		mParent = parent;
	}

	@Override
	protected Integer doInBackground(Void... params)
	{
		//#ifdef DEBUG
		Log.i(TAG, "Conversion started");
		//#endif
		int filesConverted = 0;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))
		{
			File logDir = new File(Environment.getExternalStorageDirectory(), "Binartech");
			logDir = new File(logDir, "GpsSignalTracker");
			File[] dirs = logDir.listFiles(new DirFilter());
			//#ifdef DEBUG
			Log.d(TAG, "Compatible directories"+dirs.length);
			//#endif
			for (File dir : dirs)
			{
				final File prnchanges = new File(dir, "prnchanges.bin");
				final File snrchanges = new File(dir, "snrchanges.bin");
				final File prnCsv = new File(dir, "prnchanges.csv");
				final File snrCsv = new File(dir, "snrchanges.csv");
				PrnAnalyzer prnAnalyzer = null;
				SnrAnalyzer snrAnalyzer = null;
				DataInputStream dis = null;
				try
				{
					dis = new DataInputStream(new BufferedInputStream(new FileInputStream(prnchanges), 16 * 1024));
					prnAnalyzer = new PrnAnalyzer(dis);
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
						dis.close();
					}
					catch (Exception e2)
					{

					}
					dis = null;
				}
				try
				{
					dis = new DataInputStream(new BufferedInputStream(new FileInputStream(snrchanges), 16 * 1024));
					snrAnalyzer = new SnrAnalyzer(dis);
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
						dis.close();
					}
					catch (Exception e2)
					{

					}
					dis = null;
				}
				PrintStream pr = null;
				try
				{
					pr = new PrintStream(new BufferedOutputStream(new FileOutputStream(prnCsv), 512 * 1024), false);
					prnAnalyzer.printCSV(pr);
					filesConverted++;
				}
				catch (Exception e)
				{
					//#ifdef DEBUG
					Log.w(TAG, e);
					//#endif
				}
				finally
				{
					if (pr != null)
					{
						pr.close();
						pr = null;
					}
				}
				try
				{
					pr = new PrintStream(new BufferedOutputStream(new FileOutputStream(snrCsv), 512 * 1024), false);
					snrAnalyzer.printCSV(pr);
					filesConverted++;
				}
				catch (Exception e)
				{
					//#ifdef DEBUG
					Log.w(TAG, e);
					//#endif
				}
				finally
				{
					if (pr != null)
					{
						pr.close();
						pr = null;
					}
				}
			}
		}
		return filesConverted;
	}

	private static class DirFilter implements FileFilter
	{

		@Override
		public boolean accept(File path)
		{
			if (path.isDirectory())
			{
				File prnchanges = new File(path, "prnchanges.bin");
				File snrchanges = new File(path, "snrchanges.bin");
				return prnchanges.exists() && snrchanges.exists();
			}
			else
			{
				return false;
			}
		}

	}

	@Override
	protected void onPostExecute(Integer result)
	{
		mDialog.dismiss();
		Builder b = new Builder(mParent);
		b.setTitle("Converting completed");
		b.setIcon(android.R.drawable.ic_dialog_info);
		b.setMessage(String.format("Converted files: %d", result));
		b.setPositiveButton("OK", null);
		AlertDialog dlg = b.create();
		dlg.setOwnerActivity(mParent);
		dlg.show();
	}

	@Override
	protected void onPreExecute()
	{
		mDialog = ProgressDialog.show(mParent, "", "Converting binary files into CSV.\nPlease wait...", true, false);
	}

}
