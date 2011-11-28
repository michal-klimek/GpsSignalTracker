package com.binartech.gpssignaltracker.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.location.GpsSatellite;

public class PrnFrame
{
	public static void writePrnFrame(DataOutput dos, List<GpsSatellite> sats, long time) throws IOException
	{
		dos.writeLong(time);
		dos.writeInt(sats.size());
		for(GpsSatellite sat : sats)
		{
			dos.writeInt(sat.getPrn());
		}
	}
	
	public static void writePrnFrame(DataOutput dos, ArrayList<SatelliteInfo> sats, long time) throws IOException
	{
		dos.writeLong(time);
		dos.writeInt(sats.size());
		for(SatelliteInfo sat : sats)
		{
			dos.writeInt(sat.getPrn());
		}
	}
	
	private final long time;
	private final int[] prns;
	
	public PrnFrame(DataInput dis) throws IOException
	{
		time = dis.readLong();
		final int count = dis.readInt();
		prns = new int[count];
		for(int i = 0; i < count; ++i)
		{
			prns[i] = dis.readInt();
		}
	}

	public long getTime()
	{
		return time;
	}

	public int[] getPrns()
	{
		return prns;
	}
	
	
}
