package com.verizon.ssp.roctool.helper;

import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.helper.EnsureFtpHelper;
import com.verizon.ssp.roctool.object.RocTicket;
import com.verizon.ssp.roctool.controller.Controller;
import com.verizon.ssp.roctool.db.DBConn;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.db.DBQueries;
import com.verizon.ssp.roctool.db.MSSqlConn;
// import com.verizon.ssp.roctool.daemon.AssignmentDaemon;

public class AssignmentDaemonHelper {
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	private static HashMap<Integer, RocTicket> ticketsToInsert = null;
	private static MSSqlConn msConn = MSSqlConn.getInstance();
	private static DBDriver driver = new DBDriver();
	private static EnsureFtpHelper ftpHelper = new EnsureFtpHelper();
	
	/**
	 * get's the tickets from the ROC DB
	 */
	public static void getROCTicketsFromRocDB() {			
		CallableStatement cStatement = null;
		ArrayList<RocTicket> ticketsFromRocDB = new ArrayList<RocTicket>();
		ResultSet rs = null;
		Connection con = null;
		
		try {		
			logger.debug("getting tickets from roc db");
			
			con = msConn.getConnection();			
			cStatement = con.prepareCall("{call dbo.usp_FeedSmartBill2(?, ?)}");
			cStatement.setObject(1, null, java.sql.Types.NULL);
			cStatement.setObject(2, 100.0, java.sql.Types.DOUBLE);
			rs = cStatement.executeQuery();
			
			int count = 0;
			while (rs.next()) { 
				try {					
					RocTicket r = new RocTicket();
					r.setRocTicketId(Integer.parseInt(rs.getString("Ticket")));
					r.setTicketCategory(rs.getString("Category"));
					r.setPriority(Utils.givePriorityNumber(rs.getString("Priority")));
					r.setReferredTo(rs.getString("referredto"));
					r.setTicketStatus(rs.getString("Status"));
					r.setLastUpdated(Utils.convertStringToDate(rs.getString("LastUpdate")));
					r.setRocDateReferred(Utils.convertStringToDate(rs.getString("DateReferred")));					
					r.setLastItReferredDate(Utils.convertStringToDate(rs.getString("DateReferred")));
					r.setIsBusiness(rs.getInt("Business"));
					r.setAccount(rs.getString("account"));
					r.setPcan(rs.getString("pcan"));
					r.setCmisIR(rs.getString("cmisir"));
					r.setOffice(rs.getString("office"));
					r.setDateOpened(Utils.convertStringToDate(rs.getString("DateOpened")));
					r.setBtn(rs.getString("btn"));
					r.setApplication(rs.getString("application"));					
					r.setComment(rs.getString("comment"));
					
					if (Utils.focusReferredTo.contains(r.getReferredTo())) {						
						if (count % 600 == 0) {
							logger.debug("Adding ticket from roc " + r.getRocTicketId() + " to ticketsFromRocDB " + count);
						}
						count++;
						ticketsFromRocDB.add(r);
					}
				} catch (Exception e) {
					logger.error("Exception in getROCTicketsFromRocDB: ", e);
					continue;
				}
			}
			persistROCTicketsLocally(ticketsFromRocDB);
		}
		catch (Exception e) {
			logger.error("Exception in getROCTicketsFromRocDB: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, cStatement, con);
			MSSqlConn.closeMSConnection();
		}
	}

	private static void persistROCTicketsLocally(ArrayList<RocTicket> ticketsFromRocDB) {
		PreparedStatement insertTicketPstmt = null;
		logger.debug("placing " + ticketsFromRocDB.size() + " ROC tickets into roc_ticket_info");
		
		ArrayList<RocTicket> ticketsToUpdate = new ArrayList<RocTicket>();
		setTicketsToInsert(new HashMap<Integer, RocTicket>());
		int batchId = getNextBatchID();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		HashMap<Integer, RocTicket> allTicketIds = DBDriver.getAllTicketIds("System");
		logger.debug("got all tickets: " + allTicketIds.size());
		try {			
			String query = DBQueries.dbQueriesMap.get("INSERT_INTO_TICKET_INFO");
			insertTicketPstmt = conn.prepareStatement(query);
			logger.debug("about to start iteration through ticketsFromRocDB size: " + ticketsFromRocDB.size());
			int localTicketsCount = 0;
			for (RocTicket r: ticketsFromRocDB) {				
				if (allTicketIds.containsKey(r.getRocTicketId()) ) {
					RocTicket r2 = allTicketIds.get(r.getRocTicketId());
					
					if (r2.getTicketStatus().equals("On Hold")) {
						r.setTicketStatus("On Hold");
					}
					
					if (null != r2.getVzId()) {
						r.setVzId(r2.getVzId());
					}
					
					if (!r.equals(r2)) {
						ticketsToUpdate.add(r);
					}
				}				
				else {
					if (!ticketsToInsert.containsKey(r.getRocTicketId())) {
						ticketsToInsert.put(r.getRocTicketId(), r);
					}
				}
				putTicketInPreparedStatment(insertTicketPstmt, r, localTicketsCount, batchId, conn);

				if (localTicketsCount % 10000 == 0) {
					logger.debug("executing batch");
					insertTicketPstmt.executeBatch();
				}
				localTicketsCount++;
			}
			insertTicketPstmt.executeBatch();
			logger.debug("after persisting tickets locally");
			insertIntoTicketMaster();
			insertIntoTicketHist("Y");
			updateTicketMaster(ticketsToUpdate);
			
			if ("true".equals(Controller.propertiesMap.get("ENSURE_FTP")))
				ftpHelper.checkEachTablesStatus(ticketsFromRocDB, allTicketIds);
			
			
			logger.debug("done with everything");
			getTicketsToInsert().clear();
			allTicketIds.clear();
		}
		catch (Exception e) {
			logger.error("Exception in persistROCTicketsLocally: ", e);
		}
		finally {
			Utils.safeCloseResources(null, insertTicketPstmt, conn);
			conn = null;
		}
	}

	private static int getNextBatchID() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			String query = DBQueries.dbQueriesMap.get("GET_BATCH_ID");
			
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getNextBatchID: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return 0;
	}

	/**
	 * updates tickets in roc_ticket_master table
	 * @param ticketsToUpdate 
	 */
	private static void updateTicketMaster(ArrayList<RocTicket> ticketsToUpdate) {
		PreparedStatement pstmtCount = null;
		logger.debug("updating ticket master");
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			String queryPlusCount = DBQueries.dbQueriesMap.get("UPDATE_TICKET_IN_MASTER_TABLE");
			
			pstmtCount = conn.prepareStatement(queryPlusCount);
			
			for (RocTicket r : ticketsToUpdate) {
				putIntoUpdateMasterCountPstmt(pstmtCount, r, conn);				
			}
			
			pstmtCount.executeBatch();
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketMaster: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmtCount, conn);
			conn = null;
		}
	}

	private static void putIntoUpdateMasterCountPstmt(
			PreparedStatement pstmt, RocTicket r, Connection conn) throws SQLException {
		pstmt.setInt(1, r.getRocTicketId());
		pstmt.setString(2, r.getTicketStatus());
		pstmt.setString(3, r.getBtn());
		pstmt.setString(4, r.getAccount());
		pstmt.setString(5, r.getApplication());
		pstmt.setDate(6, r.getLastItReferredDate());
		pstmt.setString(7, r.getPcan());
		pstmt.setString(8, r.getReferredTo());		
		pstmt.setClob(9, Utils.convertStringToClob(r.getComment(), conn));
		pstmt.setInt(10, r.getRocTicketId());
		
		pstmt.addBatch();
	}

	/**
	 * inserts new tickets into roc_ticket_master table
	 * @param ticketsFromRocDB 
	 */
	private static void insertIntoTicketMaster() {
		PreparedStatement pstmt = null;
		logger.debug("inserting into ticket master");
		
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			String query = DBQueries.dbQueriesMap.get("INSERT_TICKET_INTO_MASTER_TABLE");
			
			pstmt = conn.prepareStatement(query);
			logger.debug("getTicketsToInsert " + getTicketsToInsert().size());
			HashMap<Integer, Integer> already = new HashMap<Integer, Integer>();
			
			int count = 0;
			for (RocTicket r: getTicketsToInsert().values()) {
				if (!already.containsKey(r.getRocTicketId())) {
					if (count % 500 == 0)
						logger.debug("adding ticket with vzId=" + r.getVzId() + " and status=" + r.getTicketStatus());
	
					pstmt.setInt(1, r.getRocTicketId());
					pstmt.setString(2, r.getVzId());
					pstmt.setDate(3, r.getDateOpened());
					pstmt.setDate(4, r.getLastItReferredDate());
					pstmt.setString(5, r.getTicketStatus());				
					
					Clob c = Utils.convertStringToClob(r.getComment(), conn);
					
					if (null != c)
						pstmt.setClob(6, c);
					else
						pstmt.setClob(6, conn.createClob());
					
					pstmt.setString(7, r.getReferredTo());
					pstmt.setString(8, r.getBtn());
					pstmt.setString(9, r.getAccount());
					pstmt.setString(10, r.getPcan());
					pstmt.setInt(11, r.getIsBusiness());
					pstmt.setString(12, r.getTicketCategory());
					pstmt.setString(13, r.getPriority());
					count++;
					
					already.put(r.getRocTicketId(), r.getRocTicketId());
					pstmt.addBatch();
				}
			}
			pstmt.executeBatch();
		}
		catch (Exception e) {
			logger.error("Exception in insertIntoTicketMaster: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}

	private static void putTicketInPreparedStatment(PreparedStatement insertTicketPstmt, 
			RocTicket r, int ticketsCount, int batchId, Connection conn) throws SQLException {
		insertTicketPstmt.setInt(1, r.getRocTicketId());
		insertTicketPstmt.setString(2, r.getBtn());
		insertTicketPstmt.setString(3, r.getAccount());
		insertTicketPstmt.setString(4, r.getApplication());
		insertTicketPstmt.setString(5, r.getPcan());
		insertTicketPstmt.setString(6, r.getCmisIR());
		insertTicketPstmt.setString(7, r.getOffice());
		insertTicketPstmt.setString(8, r.getReferredTo());
		insertTicketPstmt.setString(9, r.getTicketCategory());
		insertTicketPstmt.setString(10, r.getPriority());					
		insertTicketPstmt.setString(11, r.getTicketStatus());					
		insertTicketPstmt.setDate(12, r.getDateOpened());
		insertTicketPstmt.setDate(13, r.getLastUpdated());
		insertTicketPstmt.setDate(14, r.getRocDateReferred());
		
		Clob c = Utils.convertStringToClob(r.getComment(), conn);
		if (null != c)
			insertTicketPstmt.setClob(15, c);
		else
			insertTicketPstmt.setClob(15, conn.createClob());
		
		insertTicketPstmt.setInt(16, r.getIsBusiness());
		insertTicketPstmt.setInt(17, batchId);
		insertTicketPstmt.addBatch();		
		
		if (ticketsCount % 1500 == 0) {
			logger.debug("ticket has been added to pstmt " + ticketsCount);
		}
	}

	/**
	 * inserts tickets from roc db into ticket_hist table if they aren't already TODO: ask about this
	 * @param ticketsToInsert2 
	 */
	private static void insertIntoTicketHist(String rocInflow) {
		logger.debug("initial insert into ticket history - daemon: line 315; getTicketsToInsert: " + getTicketsToInsert().size());
		
		String query = DBQueries.dbQueriesMap.get("INSERT_TICKET_INTO_TICKET_HISTORY");
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			pstmt = conn.prepareStatement(query);
			int count = 0;
			
			for (RocTicket r: getTicketsToInsert().values()) {
				if (count % 500 == 0)
					logger.debug(r.getRocTicketId());
				pstmt.setInt(1, r.getRocTicketId());
				pstmt.setString(2, r.getVzId());
				pstmt.setString(3, r.getTicketStatus());
				pstmt.setString(4, r.getReferredTo());
				pstmt.setString(5, rocInflow);
				pstmt.setString(6, r.getOnHoldStatus());
				pstmt.setString(7, r.getUserComment());
				
				pstmt.addBatch();
				count++;
			}
			pstmt.executeBatch();
		}
		catch (Exception e) {
			logger.error("Exception in insertIntoTicketHist: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}		

		driver.getHistIdsForMasterTable(getTicketsToInsert().values(), "System");	
	}

	public static HashMap<Integer, RocTicket> getTicketsToInsert() {
		return ticketsToInsert;
	}

	public static void setTicketsToInsert(HashMap<Integer, RocTicket> ticketsToInsert) {
		logger.debug("setting ticketsToInsert");
		AssignmentDaemonHelper.ticketsToInsert = ticketsToInsert;
	}	
}
