package com.binartech.gpssignaltracker.core;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import com.binartech.gpssignaltracker.services.GpsTrackerService;

import android.location.GpsSatellite;

public class SatelliteInfo implements Comparable<SatelliteInfo>
{
	private final int prn;
	private final float azimuth, elevation, snr;
	private final boolean hasAlmanac, usedInFix, hasEphemeris;
	
	
	
	public SatelliteInfo(int prn, float azimuth, float elevation, float snr)
	{
		this.prn = prn;
		this.azimuth = azimuth;
		this.elevation = elevation;
		this.snr = snr;
		hasAlmanac = false;
		hasEphemeris = false;
		usedInFix = false;
	}

	public SatelliteInfo(DataInput dis) throws IOException
	{
		prn = dis.readInt();
		snr = dis.readFloat();
		azimuth = dis.readFloat();
		elevation = dis.readFloat();
		hasAlmanac = dis.readBoolean();
		hasEphemeris = dis.readBoolean();
		usedInFix = dis.readBoolean();
	}
	
	public void writeToStream(DataOutput dos) throws IOException
	{
		dos.writeInt(prn);
		dos.writeFloat(snr);
		dos.writeFloat(azimuth);
		dos.writeFloat(elevation);
		dos.writeBoolean(hasAlmanac);
		dos.writeBoolean(hasEphemeris);
		dos.writeBoolean(usedInFix);
	}
	
	@Override
	public int compareTo(SatelliteInfo another)
	{
		return prn - another.prn;
	}
	
	@Override
	public String toString()
	{
		return String.format("Prn: %02d, Snr: %02.2f, A: %b, E: %b, F: %b", prn, snr, hasAlmanac, hasEphemeris, usedInFix);
	}

	public int getPrn()
	{
		return prn;
	}
	public float getAzimuth()
	{
		return azimuth;
	}
	public float getElevation()
	{
		return elevation;
	}
	public float getSnr()
	{
		return snr;
	}
	public boolean hasAlmanac()
	{
		return hasAlmanac;
	}
	public boolean isUsedInFix()
	{
		return usedInFix;
	}
	public boolean hasEphemeris()
	{
		return hasEphemeris;
	}
	
	
}
