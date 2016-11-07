package com.verizon.ssp.roctool.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Properties;

import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.util.Logger;

public class DBQueries {
	public static HashMap<String, String> dbQueriesMap = new HashMap<String, String>();
	private static Properties prop = new Properties();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);

	static {
		loadQueries();
	}
	
	private static void loadQueries() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		if (dbQueriesMap.size() == 0) {
			try {
				prop = Utils.loadPropertyFile("config/AdminPage.properties");
				String query = Utils.getPropertyValue(prop, "GET_QUERIES");
				logger.debug(query);
				dbConn.connect();
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					dbQueriesMap.put(rs.getString("QUERY_NAME"), rs.getString("QUERY_TEXT"));
				}
			}
			catch (Exception e) {
				logger.error("Exception in loadQueries: ", e);
			}
			finally {
				Utils.safeCloseResources(rs, pstmt, conn);
				conn = null;
			}
		}
	}
}
