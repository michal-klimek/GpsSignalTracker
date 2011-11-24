package com.binartech.gpssignaltracker.core;

import java.io.DataInput;
import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;

public class PrnAnalyzer
{
	private ArrayList<PrnFrame> mPrnChanges = new ArrayList<PrnFrame>();
	private HashSet<Integer> mUniquePrns = new HashSet<Integer>();
	
	public PrnAnalyzer(DataInput dis) throws IOException
	{
		try
		{
			while(true)
			{
				final PrnFrame prnFrame = new PrnFrame(dis);
				for(int prn : prnFrame.getPrns())
				{
					mUniquePrns.add(prn);
				}
				mPrnChanges.add(prnFrame);
			}
		}
		catch (EOFException e)
		{
			
		}
	}
	
	public void printCSV(PrintStream pr)
	{
		final int prnsCount = mUniquePrns.size();
		for(PrnFrame frame : mPrnChanges)
		{
			final int[] prns = frame.getPrns();
			final int length = prns.length;
			pr.print(frame.getTime());
			pr.print(';');
			for(int i = 0; i < prnsCount; ++i)
			{
				if(i < length)
				{
					pr.printf("%02d;", prns[i]);
				}
				else
				{
					pr.print("00;");
				}
			}
			pr.println();
		}
	}
}
