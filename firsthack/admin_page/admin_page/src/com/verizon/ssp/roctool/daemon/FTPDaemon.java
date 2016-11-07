package com.verizon.ssp.roctool.daemon;

import java.util.Calendar;
import java.util.Date;
//import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.controller.Controller;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.ftp.FtpImpl;
import com.verizon.ssp.roctool.timer.FTPTimeImpl;
import com.verizon.ssp.util.Logger;

public class FTPDaemon extends TimerTask {
	private static Timer timer = null;
	private static int START_HOUR;
	private static int START_MINUTE;
	private static String strWakeUpInterval = null;
//	private static Properties prop;
	private static FTPTimeImpl getTime = new FTPTimeImpl();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	// Gets the start time for the daemon
	static {
		getTime.getStartTime();
		START_HOUR = getTime.getStartTimeHour();
		START_MINUTE = getTime.getStartTimeMin();
		getTime.seeStartTime();
	}
	
	public static synchronized void startDaemon() {
		if (timer == null) {
			logger.debug("Inside Daemon!, start time: " + new Date());
			
			int wakeUpInterval = 0;
			
			try {				
//				prop = Utils.loadPropertyFile("config/AdminPage.properties");
//				strWakeUpInterval = Utils.getPropertyValue(prop, "FTP_WAKE_UP_INTERVAL");
				strWakeUpInterval = Controller.propertiesMap.get("FTP_WAKE_UP_INTERVAL");
				
				if (strWakeUpInterval != null && strWakeUpInterval.trim().length() > 0 ) {
					wakeUpInterval = Integer.parseInt(strWakeUpInterval) * 30 * 1000 * 6;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			logger.debug("WakeUp Interval (in ms): " + wakeUpInterval);

			if (wakeUpInterval <= 0) {
				logger.error("Invalid poll interval passed. Using default value: 15 min.");
				wakeUpInterval = 5 * 30 * 1000 * 6;
				logger.debug("wakeUpInterval (in ms): " + wakeUpInterval);
			}
			timer = new Timer();
			FTPDaemon task = new FTPDaemon();
			timer.schedule(task, getNextRunTime().getTime(),(long) wakeUpInterval);
		}
	}
	
	/**
	 * gets the next run time for the daemon
	 * (currently running every 12 hours starting at 5am
	 * @return
	 */
	public static Calendar getNextRunTime() {
		Calendar nextRun = Calendar.getInstance();
		nextRun.set(Calendar.HOUR_OF_DAY, START_HOUR);
		nextRun.set(Calendar.MINUTE, START_MINUTE);
		
		return nextRun;
	}
	
	/**
	 * Calculates the Stop time of the Daemon.
	 */
	public static synchronized void stopDaemon() {
		if (timer != null) {
			logger.debug("\n Shutting down AssignmentDaemon at " + (new Date()));
			timer.cancel();
		}
		logger.debug("Servlet is destory.");
	}
	
	@Override
	public void run() {
		DBDriver.getUsers("System");
		logger.debug("creating csv file");

		if ("true".equals(Utils.getPropertyValue(Utils.loadPropertyFile("config/AdminPage.properties"), "ftp_enabled"))) {
			FtpImpl ftp = new FtpImpl();
			ftp.ftpFile();
		}		
	}
}
