package com.verizon.ssp.roctool.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import com.verizon.ssp.util.Logger;

import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.common.Utils;

public class DBConn {
	private static DBConn dbConn = null;
	private static String db_url;
	private static String username;
	private static String password;
	private static Properties prop = new Properties();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	public DBConn() {
		loadPropertyFile();
	}
	
	private void loadPropertyFile() {
		try {
			prop = Utils.loadPropertyFile("config/AdminPage.properties");
			
			db_url = Utils.getPropertyValue(prop, Constants.ORACLE_URL);
			username = Utils.getPropertyValue(prop, Constants.ORACLE_USERNAME);
			password = Utils.getPropertyValue(prop, Constants.ORACLE_PASSWORD);
		}
		catch (Exception e) {
			logger.error("Exception in loadPropertyFile: ", e);
		}
	}
	
	public Connection connect() {
		Connection conn;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
			
			if (Utils.getPropertyValue(prop, "USE_CONNECTION_POOL").equals("false")) {
				logger.debug("not using connection pool");
				conn = DriverManager.getConnection(db_url, username, password);
			}
			else
				conn = getConnectionFromPool();
			return conn;
		}
		catch (Exception e) {
			logger.error("Exception in connect: ", e);
		}
		
		return null;
	}
	
	public void closeConnection(Connection conn) {
		try {
			conn.close();
		}
		catch (Exception e) {
			logger.error("Exception in closeConnection: ", e);
		}
	} 

	public static DBConn getInstance() {
		if (null == dbConn)
			dbConn = new DBConn();
		return dbConn;
	}
	
	/**
	 * Fetching a db connection using connection pooling
	 * 
	 * @return Connection
	 */
	public static Connection getConnectionFromPool() {
		Properties env = new Properties();
		Connection conn = null;
		try {
			env.put(Context.INITIAL_CONTEXT_FACTORY, Utils.getPropertyValue(prop, Constants.WEBLOGICCONTEXTFACTORY));
			env.put(Context.PROVIDER_URL,Utils.getPropertyValue(prop, Constants.WEBLOGICURL));
			env.put(Context.SECURITY_AUTHENTICATION, "None");
			env.put(Context.SECURITY_PRINCIPAL,Utils.getPropertyValue(prop, Constants.WEBLOGICUSERID));
			env.put(Context.SECURITY_CREDENTIALS, Utils.getPropertyValue(prop, Constants.WEBLOGICPASSWORD));
			Context ic = new InitialContext(env);
			DataSource dataSource = (DataSource) ic.lookup(Utils.getPropertyValue(prop, Constants.WEBLOGICJNDI));
			conn = dataSource.getConnection();
		} catch (Exception ex) {
			logger.error("Connection Pool Exception : " + ex.toString(), ex);
		}
		return conn;
	} 	
}
