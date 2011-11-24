package com.binartech.gpssignaltracker.core;

import java.io.DataInput;
import java.io.IOException;

public class SatelliteInfo implements Comparable<SatelliteInfo>
{
	private final int prn;
	private final float azimuth, elevation, snr;
	private final boolean hasAlmanac, usedInFix, hasEphemeris;
	
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
