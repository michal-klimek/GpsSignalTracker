package com.binartech.gpssignaltracker.services;

import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationManager;

public class SnrFrame
{
	private static final SimpleDateFormat TIME = new SimpleDateFormat("HH:mm:ss");
	public static void writeSnrChangeFrame(DataOutput dos, List<GpsSatellite> sats, Location loc, long time) throws IOException
	{
		dos.writeLong(time);
		final boolean hasFix = loc != null;
		dos.writeBoolean(hasFix);
		if(hasFix)
		{
			dos.writeDouble(loc.getLatitude());
			dos.writeDouble(loc.getLongitude());
			dos.writeFloat(loc.getSpeed());
			dos.writeFloat(loc.getBearing());
			dos.writeFloat(loc.getAccuracy());
		}
		dos.writeInt(sats.size());
		for(GpsSatellite sat : sats)
		{
			dos.writeInt(sat.getPrn());
			dos.writeFloat(sat.getSnr());
			dos.writeFloat(sat.getAzimuth());
			dos.writeFloat(sat.getElevation());
			dos.writeBoolean(sat.hasAlmanac());
			dos.writeBoolean(sat.hasEphemeris());
			dos.writeBoolean(sat.usedInFix());
		}
	}
	
	public static byte[] marshalSnrChangeFrame(List<GpsSatellite> sats, Location loc, long time)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try
		{
			DataOutputStream dos = new DataOutputStream(baos);
			writeSnrChangeFrame(dos, sats, loc, time);
			return baos.toByteArray();
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private final long time;
	private final Location location;
	private final SatelliteInfo[] satellites;
	
	public SnrFrame(DataInput dis) throws IOException
	{
		time = dis.readLong();
		final boolean hasFix = dis.readBoolean();
		if(hasFix)
		{
			location = new Location(LocationManager.GPS_PROVIDER);
			location.setLatitude(dis.readDouble());
			location.setLongitude(dis.readDouble());
			location.setSpeed(dis.readFloat());
			location.setBearing(dis.readFloat());
			location.setAccuracy(dis.readFloat());
		}
		else
		{
			location = null;
		}
		final int satCount = dis.readInt();
		satellites = new SatelliteInfo[satCount];
		for(int i = 0; i < satCount; ++i)
		{
			satellites[i] = new SatelliteInfo(dis);
		}
	}

	public long getTime()
	{
		return time;
	}

	public Location getLocation()
	{
		return location;
	}

	public SatelliteInfo[] getSatellites()
	{
		return satellites;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(TIME.format(new Date(time))).append('\n');
		sb.append("Location: ").append(location).append('\n');
		for(SatelliteInfo info : satellites)
		{
			sb.append(info).append('\n');
		}
		return sb.toString();
	}
	
	
}
