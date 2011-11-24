package com.binartech.gpssignaltracker.core;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import android.location.Location;

public class SnrAnalyzer
{
	private final ArrayList<SnrFrame> mSnrChanges = new ArrayList<SnrFrame>();
	private final int[] mUniquePrns;
	
	public SnrAnalyzer(DataInput dis) throws IOException
	{
		final HashSet<Integer> uniquePrns = new HashSet<Integer>();
		try
		{
			
			while(true)
			{
				final SnrFrame frame = new SnrFrame(dis);
				for(SatelliteInfo sat : frame.getSatellites())
				{
					uniquePrns.add(sat.getPrn());
				}
				mSnrChanges.add(frame);
			}
		}
		catch (EOFException e)
		{
			
		}
		Integer[] tmp = uniquePrns.toArray(new Integer[uniquePrns.size()]);
		mUniquePrns = new int[tmp.length];
		for(int i = 0; i < mUniquePrns.length; ++i)
		{
			mUniquePrns[i] = tmp[i];
		}
		Arrays.sort(mUniquePrns);
	}
	
	public void printCSV(PrintStream pr)
	{
		final int count = mUniquePrns.length;
		for(int prn : mUniquePrns)
		{
			pr.printf("%02d;", prn);
		}
		pr.println();
		for(SnrFrame frame : mSnrChanges)
		{
			final SatelliteInfo[] sats = frame.getSatellites();
			final int length = sats.length;
			Arrays.sort(sats);
			pr.print(frame.getTime());
			int satIndex = 0;
			for(int i = 0; i < count; ++i)
			{
				final int prn = mUniquePrns[i];
				if(satIndex < length) //prn mo¿e byæ na liœcie
				{
					final int satprn = sats[satIndex].getPrn();
					if(prn == satprn)
					{
						pr.printf("%02.2f;", sats[satIndex].getSnr());
						satIndex++;
					}
					else
					{
						pr.print("00,00;");
					}
				}
				else
				{
					pr.print("00,00;");
				}
			}
			final Location loc = frame.getLocation();
			if(loc != null)
			{
				pr.printf("1;%02.2f;", loc.getAccuracy());
			}
			else
			{
				pr.print("0;00,00;");
			}
			pr.println();
		}
	}
}
