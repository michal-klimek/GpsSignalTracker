package com.binartech.gpssignaltracker.core.tests;

import android.content.Context;
import android.os.Handler;

public interface LogAdapter
{
	void writeLog(String line);
	Context getContext();
	Handler getHandler();
}
