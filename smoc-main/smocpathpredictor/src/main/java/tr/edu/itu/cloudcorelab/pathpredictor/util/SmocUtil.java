package tr.edu.itu.cloudcorelab.pathpredictor.util;

import java.util.Calendar;

public class SmocUtil
{
	public static long measure(Runnable runnable)
	{
		final long start = System.currentTimeMillis();
		runnable.run();
		return System.currentTimeMillis() - start;
	}
	
	public static String getTimestamp()
	{
		final Calendar now = Calendar.getInstance();
		final int year = now.get(Calendar.YEAR);
		final int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
		final int day = now.get(Calendar.DAY_OF_MONTH);
		final int hour = now.get(Calendar.HOUR_OF_DAY);
		final int minute = now.get(Calendar.MINUTE);
		final int second = now.get(Calendar.SECOND);
		final int ms = now.get(Calendar.MILLISECOND);
		
		return year + "." + month + "." + day + "_" + hour + "." + minute + "." + second + "." + ms;
	}
}
