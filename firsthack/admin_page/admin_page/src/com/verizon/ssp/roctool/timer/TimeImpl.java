package com.verizon.ssp.roctool.timer;

import java.util.Properties;

import com.verizon.ssp.util.Logger;

import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;

public class TimeImpl implements iTime {
	private static String startTimeHour;
	private static String startTimeMin;
	private static String howOften;
	private Properties prop;
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	@Override
	public void getStartTime() {
		prop = Utils.loadPropertyFile("config/AdminPage.properties");
		startTimeHour = Utils.getPropertyValue(prop, "DAEMON_START_TIME_HOUR");
		startTimeMin = Utils.getPropertyValue(prop, "DAEMON_START_TIME_MIN");
		howOften = Utils.getPropertyValue(prop, "WAKE_UP_INTERVAL");
	}
	
	@Override
	public void seeStartTime() {
		getStartTime();
		logger.debug("Starting Daemon at: " + startTimeHour + ":" + startTimeMin + ", running it every " + howOften + " hours");
	}	
	
	public int getStartTimeHour() {
		return Integer.parseInt(startTimeHour);
	}
	
	public int getStartTimeMin() {
		return Integer.parseInt(startTimeMin);
	}
	
	public int getHowOften() {
		return Integer.parseInt(howOften);
	}
}
