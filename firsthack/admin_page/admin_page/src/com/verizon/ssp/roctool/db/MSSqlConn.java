package com.verizon.ssp.roctool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.util.Logger;

public class MSSqlConn {	
	private static MSSqlConn msConn = null;
	private static String db_url;
	private static String username;
	private static String password;
	private static Connection conn;
	private Properties prop = new Properties();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	public MSSqlConn() {
		loadPropertyFile();
	}
	
	private void loadPropertyFile() {
		try {
			prop = Utils.loadPropertyFile("config/AdminPage.properties");
			
			db_url = Utils.getPropertyValue(prop, Constants.MS_URL);
			username = Utils.getPropertyValue(prop, Constants.MS_USERNAME);
			password = Utils.getPropertyValue(prop, Constants.MS_PASSWORD);
		}
		catch (Exception e) {
			logger.error("Exception in loadPropertyFile: ", e);
		}
	}
	
	public Connection getConnection() {		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			conn = DriverManager.getConnection(db_url, username, password);
		}
		catch (Exception e) {
			logger.error("Eception in getMSConnection: ", e);
		}
		
		return conn;
	}
	
	public static void closeMSConnection() {
		try {
			conn.close();
		} 
		catch (Exception e) {
			logger.error("Eception in closeMSConnection: ", e);
		}
	}
	
	
	public static MSSqlConn getInstance() {
		if (null == msConn)
			msConn = new MSSqlConn();
		return msConn;
	}
}
