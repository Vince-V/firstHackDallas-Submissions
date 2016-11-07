package com.verizon.ssp.roctool.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.io.IOUtils;

import com.verizon.ssp.roctool.controller.Controller;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.util.Logger;

public class Utils {
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	private static Properties property = null;
	private static String writeComment = "";
	public static ArrayList<String> focusReferredTo = null;
	public static boolean initialInsertUsersToAssignmentHist = false;

	public static String convertDateToString(Date d) {
		String strDate = "";
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		strDate = df.format(d);	
		return strDate;
	}
	
	public static Date convertStringToDate(String strDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date utilDate;
		Date sqlDate = null;
		
		try {
			utilDate = format.parse(strDate);
			sqlDate = new Date(utilDate.getTime());
		} 
		catch (ParseException e) {
			logger.error("ParseException in convertStringToDate: ", e);
		}		
		
		return sqlDate;
	}
	
	public static Clob convertStringToClob(String comment, Connection connection) {
		Clob c = null;
		
		try {
			c = connection.createClob();
			c.setString(1, comment);
		}
		catch (Exception e) {
			logger.error("Exceptoin in convertStringToClob: ", e);
		}
		
		return c;
	}
	
	public static StringWriter convertClobToString(ResultSet rs) {
		StringWriter clobAsString = new StringWriter();
		InputStream in = null;
		try {
			in = rs.getClob("ticket_comment").getAsciiStream();
			
			if (null != in) {				
				IOUtils.copy(in, clobAsString);
			}
			else {
				clobAsString.getBuffer().setLength(0);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (null != in) {
				try {
					in.close();
				}
				catch (Exception e) {
					logger.error("Exception in convertClobToString: ", e);
				}
			}
		}
		return clobAsString;
	}
	
	public static String givePriorityNumber(String priority) {
		if (null != priority) {			
			if (priority.toLowerCase().equals("escalated"))
				priority = "0" + " Escalated";
			else if (priority.toLowerCase().equals("expedited"))
				priority = "1" + " Expedited";
			else if (priority.toLowerCase().equals("high"))
				priority = "2" + " High";
			else if (priority.toLowerCase().equals("medium"))
				priority = "3" + " Medium";
			else if (priority.toLowerCase().equals("low"))
				priority = "4" + " Low";
		}
		
		return priority;
	}
	
	public static String getRoleType(int role) {
		String type = "";
		switch(role) {
		case 1:
			type = "admin";
			break;
		case 2:
			type = "agent";
			break;
		case 3:
			type = "guest";
			break;
		default:
				break;
		}
		return type;
	}
	
	public static void safeCloseResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
		if (pstmt != null) {
			try {
				pstmt.close();
				pstmt = null;
			} catch (SQLException e) {
				logger.error("pstmt SQLException in safeCloseResources: ", e);
			}
		}

		if (rs != null) {
			try {
				rs.close();
				rs = null;
			} catch (SQLException e) {
				logger.error("rs SQLException in safeCloseResources: ", e);
			}
		}
		if (conn != null) {
			try {
				conn.close();
				conn = null;
			} catch (SQLException e) {
				logger.error("conn SQLException in safeCloseResources: ", e);
			}
		}
	}
	
	
	/**
	 * Used to load the property files and return property object.
	 * 
	 * @return Properties
	 */
	public static Properties loadPropertyFile(String filename) {
		if (property == null || property.isEmpty()) {
			property = new Properties();
			InputStream propFile = null;
			try {
				System.out.println(filename);
				propFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
				System.out.println(propFile.toString());
				property.load(propFile);
				
			} catch (Exception e) {
				logger.error("Exception in loadPropertyFile: ", e);
				
				if (null != propFile)
					IOUtils.closeQuietly(propFile);
			}
		}
		return property;
	}

	/**
	 * Used to retrieve the corresponding value in property file for a given
	 * key.
	 * 
	 * @param props
	 * @param strKey
	 * @return String
	 */
	public static String getPropertyValue(Properties props, String strKey) {
		String propertyValue = null;
		if (props != null) {
			if (props.containsKey(strKey) && props.getProperty(strKey) != null) {
				propertyValue = props.getProperty(strKey).trim();
			} 
			else {
				propertyValue = null;
			}
		} 
		else {
			propertyValue = null;
		}
		return propertyValue;
	}
	
	public static String stackTraceToString(Throwable e) {
		String retValue = null;
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			retValue = sw.toString();
		} finally {
			try {
				if (pw != null)
					pw.close();
				if (sw != null)
					sw.close();
			} catch (IOException ex) {
				logger.error("Exception in stackTraceToString: ", ex);
			}
		}
		return retValue;
	}
}