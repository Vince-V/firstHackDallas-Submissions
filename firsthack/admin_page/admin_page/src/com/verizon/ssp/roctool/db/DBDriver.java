package com.verizon.ssp.roctool.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.db.DBConn;
import com.verizon.ssp.roctool.common.TextMasking;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.controller.ClosureCodesController;
import com.verizon.ssp.roctool.controller.Controller;
import com.verizon.ssp.roctool.object.FilterOption;
import com.verizon.ssp.roctool.object.NewUser;
import com.verizon.ssp.roctool.object.RocTicket;
import com.verizon.ssp.roctool.object.RocTicketHistory;
import com.verizon.ssp.roctool.object.Signature;
import com.verizon.ssp.roctool.object.User;
import com.verizon.ssp.roctool.object.CSVObj;

import java.sql.Connection;

public class DBDriver {
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	public User validateUser(String username, String password) {
		User u = null;
		try {
			u = getUser(username, username);			
			if (null != u) {
				if (password.equals(u.getPassword()) && u.getVzId().equals(username)) {
					return u;
				}
				else {
					u = null;
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in validateUser: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, username, 0);
		}
		return u;
	}
	
	public User getUser(String vzId, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_USER");
		User u = new User();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, vzId);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					u.setFirstname(rs.getString("first_name"));
					u.setVzId(rs.getString("vz_id"));
					u.setLastname(rs.getString("last_name"));
					u.setUsergroup(rs.getInt("user_group"));
					u.setTicketsPerDay(rs.getInt("tickets_per_day"));
					u.setRole(Utils.getRoleType(rs.getInt("user_group")));
					u.setPassword(rs.getString("vz_password"));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUser: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		if (null != u && null != u.getVzId() && !u.getVzId().equals(""))
			return u;
		else 
			return null;
	}

	/**
	* gets all the tickets to put into the csv file
	*/
	public static HashMap<Integer, CSVObj> getCSVvalues() {
		HashMap<Integer, CSVObj> CSVs = new HashMap<Integer, CSVObj>();
		String query = DBQueries.dbQueriesMap.get("GET_CSV_VALUES");
		ArrayList<Integer> ftpTickets = new ArrayList<Integer>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while(rs.next()) {
					CSVObj csv = new CSVObj();
					
					ftpTickets.add(rs.getInt("ROC_TICKET_ID"));
					csv.setTicketId(rs.getInt("ROC_TICKET_ID"));
					csv.setVzId(rs.getString("VZ_ID"));
					csv.setStatus(rs.getString("TICKET_STATUS"));
					csv.setComment(rs.getString("TICKET_COMMENT"));
					csv.setFtpId(rs.getInt("FTP_ID"));
					
					if (!CSVs.containsKey(csv.getTicketId())) {
						CSVs.put(csv.getTicketId(), csv);
					}			
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getCSVvalues: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return CSVs;
	}

	public static void updateCSVTable(HashMap<Integer, CSVObj> csvs) {
		String query = DBQueries.dbQueriesMap.get("UPDATE_CSV_TABLE");
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		int ftpBatchId = getFtpBatchId();
		logger.debug("in updateCSVTable ftpbatchId: " + ftpBatchId);
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				for (Integer tktId : csvs.keySet()) {
					pstmt.setInt(1, ftpBatchId);
					pstmt.setInt(2, csvs.get(tktId).getFtpId());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateCSVTable: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
		logger.debug("getting out of updateCSVTable");
	}

	/**
	 * get's all the users in the db
	 */
	public static void getUsers(String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_ALL_USERS");
		ResultSet uRS = null;
		PreparedStatement uPstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		logger.debug("getting all users");
		try {
			if (null != conn) {
				uPstmt = conn.prepareStatement(query);
				uRS = uPstmt.executeQuery();
				
				while (uRS.next()) {
					User u = new User();
					u.setFirstname(uRS.getString("first_name"));
					u.setLastname(uRS.getString("last_name"));
					u.setUsergroup(uRS.getInt("user_group"));
					u.setTicketsPerDay(uRS.getInt("tickets_per_day"));
					u.setTicketsPending(uRS.getInt("pending_count"));
					u.setVzId(uRS.getString("vz_id"));
					u.setActive((uRS.getString("is_active").equals("Y")) ? true : false);
					u.setRole(Utils.getRoleType(uRS.getInt("user_group")));
					u.setUsersBestCategory(uRS.getString("users_best_ticket_category"));
					u.setTicketsTaken(uRS.getInt("taken_count"));
					u.setOffShore(uRS.getString("is_offshore"));
					Controller.userList.put(u.getVzId(), u);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUsers: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(uRS, uPstmt, conn);
			conn = null;
		}
	}

	
	
	/**
	 * get's tickets applied to the specific user
	 * @param id - the vz_id of the user
	 */
	public ArrayList<RocTicket> getTickets(String id, String sessionVzId) {
		logger.debug("getting " + id + "'s tickets");
		String query;
		
		ArrayList<RocTicket> ticketList = new ArrayList<RocTicket>();
		
		query = DBQueries.dbQueriesMap.get("GET_PENDING_TICKETS_FOR_SPECIFIC_USER");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				if (null != id && !id.equals(""))
					pstmt.setString(1, id);
				
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					RocTicket rt = populateRocTicket(rs, conn);
					rt.setIsBusiness(rs.getInt("is_business"));
					ticketList.add(rt);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTickets: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		return ticketList;
	}
	
	/**
	 * populates the roc ticket from the values from the DB
	 * @param rs
	 * @param conn
	 * @return - the roc ticket
	 * @throws SQLException
	 */
	private static RocTicket populateRocTicket(ResultSet rs, Connection conn)
			throws SQLException {		
		RocTicket rt = new RocTicket();
		rt.setVzId(rs.getString("vz_id"));
		rt.setPriority(rs.getString("priority"));
		rt.setReferredTo(rs.getString("referred_to"));
		rt.setRocTicketId(rs.getInt("roc_ticket_id"));
		rt.setTicketCategory(rs.getString("ticket_category"));
		rt.setSspRouteCycles(rs.getInt("ssp_route_cycles"));
		rt.setTicketStatus(rs.getString("ticket_status"));
		rt.setLastItReferredDate(rs.getDate("last_it_referred_date"));
		rt.setDateOpened(rs.getDate("date_opened"));
		rt.setFirstSspReferredDate(rs.getDate("first_ssp_referred_date"));
		rt.setLastUpdated(rs.getDate("last_updated"));
		rt.setOnsiteReferred(rs.getString("onsite_referred"));
		rt.setReferredCategory(rs.getString("onsite_referred_category"));
		rt.setHistoryId(getTicketHistoryIdFromMasterTable(rt.getRocTicketId(), conn));
		
		ArrayList<String> codeAndComment = getHoldCode(rt.getHistoryId(), conn);
		
		if (!codeAndComment.isEmpty()) {
			if (null != codeAndComment.get(0)) {
				rt.setOnHoldStatus(codeAndComment.get(0));
			}
			if (null != codeAndComment.get(1)) {
				rt.setUserComment(codeAndComment.get(1));
			}
		}
		return rt;
	}

	private static ArrayList<String> getHoldCode(int histId, Connection connection) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_HOLD_CODE_FOR_TICKET");
		ArrayList<String> codeAndComment = new ArrayList<String>();
		
		try {
			if (null != connection) {
				pstmt = connection.prepareStatement(query);
				pstmt.setInt(1, histId);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					codeAndComment.add(null == rs.getString("on_hold_code") ? "" : rs.getString("on_hold_code"));
					codeAndComment.add(null == rs.getString("user_comment") ? "" : rs.getString("user_comment"));				
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getHoldCode: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, null);
		}
		return codeAndComment;
	}

	/**
	 * updating the is_active field for the given user in the DB
	 * @param vzId - the vz_id of the user being deactivated or activated
	 * @param chcked - checks whether to activate or deactivate the user
	 * @return  1 passed; -1 error
	 */
	public String deactivateUser(String vzId, boolean chcked) {
		logger.debug("checked in deactivateUser DBDriver " + chcked);
		String query;
		
		if (chcked) // if the checkbox is checked we set the user to active
			query = DBQueries.dbQueriesMap.get("ACTIVATE_USER");
		else // else deactivate user
			query = DBQueries.dbQueriesMap.get("DEACTIVATE_USER");

		runSimpleQuery(vzId, query);
		return "1";
	}

	/**
	 * adds a user to the DB; initializes the user's is_active field as: 'Y' (active)
	 * @param fname
	 * @param lname
	 * @param id
	 * @param pwd
	 * @param tktperday
	 * @param bestcat - the ROC ticket category the user is best at
	 * @param ugroup - the usergroup the user belongs to
	 * @param isoffshore 
	 * @return 1 pass; -1 error
	 */
	public String addUser(String fname, String lname, String id, String pwd,
			int tktperday, String bestcat, String ugroup, String isoffshore) {
		String query = DBQueries.dbQueriesMap.get("INSERT_USER_INTO_TICKET_USERS");
		
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, id);
				pstmt.setString(2, fname);
				pstmt.setString(3, lname);
				pstmt.setInt(4, tktperday);
				pstmt.setString(5, pwd);
				pstmt.setInt(6, Integer.parseInt(ugroup));
				pstmt.setString(7, bestcat);
				pstmt.setString(8, (isoffshore.equalsIgnoreCase("true")) ? "Y" : "N");
				pstmt.executeQuery();
				Utils.safeCloseResources(null, pstmt, conn);
			}
		}
		catch (Exception e) {
			logger.error("Exception in addUser: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, id, 0);
			return "-1";
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
			addUserToUserAssignmentHistTable(id);
		}
		return "1";
	}


	/**
	 * adds the new user to the assignment_hist table
	 * @param vzId - the vz_id of the user to be added.
	 */
	private void addUserToUserAssignmentHistTable(String vzId) {
		String query = DBQueries.dbQueriesMap.get("INSERT_USER_INTO_USER_ASSIGNMENT");
		logger.debug("inserting user: " + vzId + " into user assignment hist table");
		runSimpleQuery(vzId, query);
	}
	
	private void runSimpleQuery(String vzId, String query) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, vzId);
				pstmt.execute();
			}
		}
		catch(Exception e) {
			logger.error("Exception in runSimpleQuery: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	/**
	 * updates the given ticket in the db and the cache to be ftpd later on.
	 * @param RocTicketHistory
	 * @param vzId
	 * @param sessionVzId 
	 */
	public void driverUpdateTicket(RocTicketHistory tkt, String vzId, String sessionVzId) {
		logger.debug("updating roc_ticket_info id: " + tkt.getId());

		String query = DBQueries.dbQueriesMap.get("INSERT_INTO_FTP_CSV");
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		ArrayList<RocTicket> historyTickets = null;

		try {
			if (null != conn) {
				int tktId = Integer.parseInt(tkt.getId());
				boolean tktOnHold = tkt.getStatus().equals("On Hold");
				
				if (!tktOnHold) {
					pstmt = conn.prepareStatement(query);
					pstmt.setInt(1, tktId);
					pstmt.setString(2, tkt.getComment());
					pstmt.setString(3, tkt.getStatus());
					pstmt.setString(4, vzId);
					
					pstmt.execute();
				}
				
				RocTicket r = getTicket(tktId, false, null, sessionVzId);				
				r.setTicketStatus(tkt.getStatus());
				r.setOnHoldStatus(tkt.getOnHoldStatus());
				r.setReferredTo(tkt.getReferredTo());
				r.setUserComment(tkt.getComment());
				r.setOnsiteReferred(booleanValue(tkt.getOnSiteReferral()));
				
				// checking if it's a new user working on the ticket
				// if it is the user the ticket was assigned to do an update to history table
				if (null != r.getVzId() && vzId.equals(r.getVzId())) {
					updateTicketHistory(tkt, conn, vzId);
					updateTicketAssignmentHist(r.getHistoryId(), tkt.getStatus(), vzId, conn);
				}
				else { // else a new user has worked on the ticket, so do an insert to history table
					historyTickets = new ArrayList<RocTicket>();					
					historyTickets.add(r);
					insertIntoTicketHistory(vzId, historyTickets, conn, sessionVzId);
					updateTicketHistory(tkt, conn, vzId);
					String tkts = tkt.getId() + ",";
					insertIntoAssignmentHistTable(vzId, "System", tkts, conn, sessionVzId);
				}
				
				r.setVzId(vzId);
				int ftpId = getTktFtpId(r.getRocTicketId());
				updateTicketMaster(r, ftpId, conn, tktOnHold);
			}
		}
		catch (Exception e) {
			logger.error("Exception in driverUpdateTicket: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, Integer.parseInt(tkt.getId()));
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}	
	
	private int getTktFtpId(int rocTicketId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String query = DBQueries.dbQueriesMap.get("GET_MAX_FTP_ID");
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, rocTicketId);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					return rs.getInt(1);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTktFtpId: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", rocTicketId);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return 0;
	}
	
	private static int getFtpBatchId() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String query = DBQueries.dbQueriesMap.get("GET_FTP_BATCH_ID");		
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					return rs.getInt(1);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getFtpBatchId: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return 0;
	}

	private void updateTicketMaster(RocTicket r, int ftpId, Connection conn, boolean keepAssignment) {	
		String query = "";
		
		if (keepAssignment) 
			query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_MASTER_DATE");
		else
			query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_MASTER_CLEAR_VZID");
		
		logger.debug(query);
		logger.debug(keepAssignment);
		
		PreparedStatement pstmt = null;
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				pstmt.setString(1, r.getTicketStatus());
				pstmt.setString(2, r.getOnsiteReferred());
				pstmt.setString(3, r.getReferredTo());
				
				if (keepAssignment) {
					pstmt.setString(4, r.getVzId());
					pstmt.setInt(5, ftpId);
					pstmt.setInt(6, r.getRocTicketId());
				}
				else {
					pstmt.setInt(4, ftpId);
					pstmt.setInt(5, r.getRocTicketId());
				}
				
				pstmt.execute();
			}
		}
		catch(Exception e) {
			logger.error("Exception in updateTicketMaster: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, r.getVzId(), r.getRocTicketId());
		}
		finally {
			Utils.safeCloseResources(null, pstmt, null);
		}
	}

	private void updateTicketHistory(RocTicketHistory tkt, Connection conn, String vzId) {
		
		int histId = getTicketHistoryIdFromMasterTable(Integer.parseInt(tkt.getId()), conn);
		
		String mdirect = booleanValue(tkt.getMisdirect());
		String rroute = booleanValue(tkt.getReroute());
		String rdirect = booleanValue(tkt.getRedirect());
		String onsiteReferred = booleanValue(tkt.getOnSiteReferral());

		String query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_HISTORY");
		PreparedStatement pstmt = null;
		
		try {		
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				pstmt.setString(1, tkt.getStatus());
				pstmt.setString(2, tkt.getCat());
				pstmt.setString(3, tkt.getTrack());
				pstmt.setString(4, tkt.getIssue());
				pstmt.setString(5, tkt.getOrdering());
				pstmt.setString(6, tkt.getProv());
				pstmt.setString(7, tkt.getBilling());
				pstmt.setString(8, mdirect);
				pstmt.setString(9, rroute);
				pstmt.setString(10, rdirect);
				pstmt.setString(11, tkt.getResolution());
				pstmt.setString(12, tkt.getComment());
				pstmt.setString(13, onsiteReferred);
				pstmt.setString(14, tkt.getReferredTo());
				pstmt.setString(15, tkt.getOnHoldStatus());
				pstmt.setString(16, vzId);
				pstmt.setInt(17, histId);
				
				pstmt.execute();
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketHistory: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", Integer.parseInt(tkt.getId()));
		}
		finally {
			Utils.safeCloseResources(null, pstmt, null);
		}
	}

	private static int getTicketHistoryIdFromMasterTable(int tkt_id, Connection conn) {
		String query = DBQueries.dbQueriesMap.get("GET_MAX_HIST_ID_FROM_MASTER");
		int id = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, tkt_id);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					id = rs.getInt("HIST_ID");
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketHistoryId: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", tkt_id);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, null);
		}
		
		return id;
	}

	private String booleanValue(String bool) {
		return "false".equals(bool.toLowerCase()) ? "N" : "Y";
	}

	private int getTicketHistoryId(int tkt_id, Connection conn) {
		String query = DBQueries.dbQueriesMap.get("GET_MAX_HIST_ID_FOR_TICKET");
		int id = 0;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, tkt_id);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					id = rs.getInt("HISTORY_ID");
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketHistoryId: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", tkt_id);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, null);
		}
		
		return id;
	}

	private void updateTicketAssignmentHist(int histId, String status, String vz_id, Connection conn) {
		logger.debug("Updating ticket assignment history id: " + histId);		
		String query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_ASSIGNMENT_HIST");
		PreparedStatement pstmt = null;
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, status);
				pstmt.setInt(2, histId);
				pstmt.execute();
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketAssignmentHist: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vz_id, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, null);
		}
	}

	public ArrayList<String> getStatuses() {		
		String query = DBQueries.dbQueriesMap.get("GET_STATUS_DESCRIPTIONS");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		ArrayList<String> statuses = new ArrayList<String>();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					statuses.add(rs.getString(1));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getStatuses: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return statuses;
	}

	public HashMap<String, RocTicket> getAllTickets() {
		logger.debug("getting all tickets");
		String query = DBQueries.dbQueriesMap.get("GET_PENDING_TICKETS_FOR_ALL_USERS");
		PreparedStatement ticketPstmt = null;
		ResultSet ticketRs = null;
		HashMap<String, RocTicket> allTicketList = new HashMap<String, RocTicket>();
		
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				ticketPstmt = conn.prepareStatement(query);
				ticketRs = ticketPstmt.executeQuery();
				
				while (ticketRs.next()) {
					RocTicket rt = populateRocTicket(ticketRs, conn);					
					allTicketList.put(rt.getRocTicketId() + "", rt);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllTickets: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(ticketRs, ticketPstmt, conn);
		}
		return allTicketList;
	}

	public ArrayList<String> getClosureCategories() {
		String query = DBQueries.dbQueriesMap.get("GET_CLOSURE_CATEGORIES");
		Controller.setClosureCats(new ArrayList<String>());
		PreparedStatement pstmt = null;
		ResultSet rs = null;		
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					Controller.getClosureCats().add(rs.getString(1));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getClosureCategories: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return Controller.getClosureCats();
	}

	public ArrayList<String> getTracking(String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_TRACKING_CATEGORIES");
		
		ArrayList<String> tracks = new ArrayList<String>();
		PreparedStatement pstmt = null; 
		ResultSet rs = null;	
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
				
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					tracks.add(rs.getString(1));
				}
			}
		}
		catch (Exception e) {
			logger.error("Error in getTracking: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return tracks;
	}

	public ArrayList<String> getIssues(int track, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_ISSUE_STAGES");
		return addToArrayList(6, "issues", query, sessionVzId);
	}

	public ArrayList<String> getOrderings(int track, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_ORDERING_STAGES");
		return addToArrayList(6, "orderings", query, sessionVzId);
	}

	public ArrayList<String> getBillings(int track, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_BILLING_STAGES");
		return addToArrayList(6, "billings", query, sessionVzId);
	}
	
	public ArrayList<String> getProvisionings(int track, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_PROVISION_STAGES");
		return addToArrayList(6, "provisioning", query, sessionVzId);
	}
	
	private ArrayList<String> addToArrayList(int track, String stage, String query, String sessionVzId) {	
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		ArrayList<String> stageDescs = new ArrayList<String>();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, track);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {			
					stageDescs.add(rs.getString(1));
				}
			}
		}
		catch (Exception e){
			logger.error("Exception in addToArrayList: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		return stageDescs;
	}

	public static String getCommentSignature(String vz_id) {
		String signature = "";
		PreparedStatement pstmt = null; 
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			String query = DBQueries.dbQueriesMap.get("GET_USERS_SIGNATURE");
			
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, vz_id);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					signature = rs.getString(1);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getCommentSignature: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vz_id, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		return signature;
	}

	public static void updateSignature(Signature sig, String vz_id) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			String query = DBQueries.dbQueriesMap.get("UPDATE_USERS_SIGNATURE");
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, sig.getSignature());
				pstmt.setString(2, vz_id);
				pstmt.execute();
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateSignature: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vz_id, 0);
		}	
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	public static RocTicket getTicket(int tkt_id, boolean showBtn, Connection connection, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("GET_SPECIFIC_TICKET");
		
		RocTicket r = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
//		logger.debug(showBtn);
		Connection conn = null;

		try {		
			if (null == connection) {
				DBConn dbConn = new DBConn();
				conn = dbConn.connect();
			}
			else {
				conn = connection;
			}
			
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setInt(1, tkt_id);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					r = new RocTicket();
					r.setRocTicketId(tkt_id);
					if (showBtn)
						r.setBtn(rs.getString("btn"));
					r.setPcan(rs.getString("pcan"));
					r.setAccount(rs.getString("account"));
					r.setVzId(rs.getString("vz_id"));
					
					logger.debug("tkt_id: " + tkt_id);
					r.setHistoryId(getTicketHistoryIdFromMasterTable(tkt_id, conn));
					logger.debug("history id: " + r.getHistoryId());
					ArrayList<String> codeAndcomment = getHoldCode(r.getHistoryId(), conn);
					logger.debug("codeAndComment.size() = " + codeAndcomment.size());
					
					if (!codeAndcomment.isEmpty()) {
						if (null != codeAndcomment.get(0)) {
							r.setOnHoldStatus(codeAndcomment.get(0));
//							logger.debug(codeAndcomment.get(0));
						}
						if (null != codeAndcomment.get(1)) {
							r.setUserComment(codeAndcomment.get(1));
//							logger.debug(codeAndcomment.get(1));
						}
					}
					
					
					String clobAsString = Utils.convertClobToString(rs).toString();
					
					if (showBtn)
						r.setComment(clobAsString);
					else
						r.setComment("");			
					
					r.setMaskedComment(TextMasking.maskText(clobAsString));
					r.setReferredTo(rs.getString("referred_to"));
					r.setTicketStatus(rs.getString("ticket_status"));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicket: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			if (null == connection)
				Utils.safeCloseResources(rs, pstmt, conn);
		}
//		logger.debug("returning " + r);
		return r;
	}

	public boolean updateUsersEmail(String email, String vz_id, String sessionVzId) {
		String query = DBQueries.dbQueriesMap.get("UPDATE_USERS_EMAIL");
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();		

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, email);
				pstmt.setString(2, vz_id);
				boolean updated = pstmt.execute();
				return updated;
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateUsersEmail: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
		return false;
	}

	public static String getEmail(String vz_id) {
		String query = DBQueries.dbQueriesMap.get("GET_USERS_EMAIL");
		
		String email = "";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				pstmt.setString(1, vz_id);
				rs = pstmt.executeQuery();
				
				if (rs.next())
					email = rs.getString(1);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getEmail: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vz_id, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		return email;
	}
	
	public void getReferredToList(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		if (null == Utils.focusReferredTo) {
			DBConn dbConn = new DBConn();
			Connection conn = dbConn.connect();
			Utils.focusReferredTo = new ArrayList<String>();
			try {
				String query = DBQueries.dbQueriesMap.get("GET_REFERRED_TO_LIST");
				
				if (null != conn) {
					pstmt = conn.prepareStatement(query); 
					rs = pstmt.executeQuery();
					
					while (rs.next()) {
						Utils.focusReferredTo.add(rs.getString("referred_name"));
					}
				}
			}
			catch (Exception e) {
				logger.error("Exception in getReferredToList: ", e);
				String error = Utils.stackTraceToString(e);
				writeToErrorLog(error, sessionVzId, 0);
			}
			finally {
				Utils.safeCloseResources(rs, pstmt, conn);
			}
		}
	}
	
	public static HashMap<String,Integer> getTicketCompleteCount(String sessionVzId){	
		HashMap<String,Integer> countMap = new HashMap<String,Integer>();
		String query = DBQueries.dbQueriesMap.get("GET_TOTAL_TICKETS_SUMMARY");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if(null != conn){
				pstmt = conn.prepareStatement(query);
				if(null != pstmt){
					rs = pstmt.executeQuery();
					while (rs.next()){
						countMap.put(rs.getString("first_name"),rs.getInt("finished"));
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
//		logger.debug(countMap);
		return countMap;
	}
	
	public ArrayList<Integer> getTicketsDone(String vzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_TICKETS_DONE_BY_USER");
		ArrayList<Integer> ticketIds = new ArrayList<Integer>();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if(null != conn){
				pstmt = conn.prepareStatement(query);
				if(null != pstmt){
					rs = pstmt.executeQuery();
					while (rs.next()){
						ticketIds.add(rs.getInt("roc_ticket_id"));
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return ticketIds;
	}

	public String updateUser(NewUser u, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = DBQueries.dbQueriesMap.get("UPDATE_USER");
		String done = "";
//		logger.debug("is active:  " + u.getIsActive());
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if(null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, u.getUsersBestCategory());
				if (u.getTicketsPerDay() != "")
					pstmt.setInt(2, Integer.parseInt(u.getTicketsPerDay()));
				else
					pstmt.setInt(2, Controller.userList.get(u.getVzId()).getTicketsPerDay());
				pstmt.setString(3, ("true".equals(u.getIsActive()) ? "Y" : "N"));			
				pstmt.setString(4, u.getVzId());
				
				if(null != pstmt){
//					logger.debug(query);
//					logger.debug(("true".equals(u.getIsActive()) ? "Y" : "N"));
//					logger.debug(u.getTicketsPerDay());
//					logger.debug(u.getUsersBestCategory());
//					logger.debug(u.getVzId());
					pstmt.execute();
					done = "1";
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateUser: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
		return done;
	}

	public User getUsersInfo(String vzId) {
		User u = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_USERS_INFO");
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if(null != conn){
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, vzId);
				if(null != pstmt){
					rs = pstmt.executeQuery();
					if (rs.next()){
						u = new User();
						u.setUsersBestCategory(rs.getString("users_best_ticket_category"));
						u.setTicketsPerDay(rs.getInt("tickets_per_day"));
						u.setActive("Y".equals(rs.getString("is_active")) ? true : false);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, vzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return u;
	}

	public String reassignTickets(String assignTo, String assignee, String tkts, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = DBQueries.dbQueriesMap.get("MAN_ASSIGN");
		ArrayList<RocTicket> historyTickets = new ArrayList<RocTicket>();
		String assignedResponse = "not assigned";
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		if (!assignTo.equals("undefined")) {
			try {
				if (null != conn) {
					pstmt = conn.prepareStatement(query);
					for (String tkt : tkts.split(",")) {
						pstmt.setString(1, assignTo);					
						pstmt.setString(2, tkt);
						
						RocTicket r = getTicket(Integer.parseInt(tkt), false, conn, sessionVzId);
						r.setVzId(assignTo);
						historyTickets.add(r);
						pstmt.execute();
					}
					logger.debug("executed reassign batch");
					assignedResponse = "assigned";
				}
			}
			catch (Exception e) {
				logger.error("Exception in reassignTickets: ", e);
				String error = Utils.stackTraceToString(e);
				writeToErrorLog(error, sessionVzId, 0);
			}
			finally {
				Utils.safeCloseResources(null, pstmt, conn);
				conn = null;
			}
		}

		insertIntoTicketHistory(assignTo, historyTickets, null, sessionVzId);
		insertIntoAssignmentHistTable(assignTo, assignee, tkts, null, sessionVzId);
		
		return assignedResponse;
	}

	/**
	 * here i need to insert everytime we make a change to the assignment
	 * once i'm done inserting here, i need to update the history id in master table
	 * @param historyTickets2 
	 * @param vzId 
	 * @param conn2 
	 */
	private void insertIntoTicketHistory(String vzId, ArrayList<RocTicket> historyTickets, Connection conn2, String sessionVzId) {
		logger.debug("in insertIntoTicketHistory");
		PreparedStatement pstmt = null;
		String query = DBQueries.dbQueriesMap.get("INSERT_TICKET_INTO_TICKET_HISTORY");
		Connection conn = null;
		
		if (null == conn2) {
			DBConn dbConn = new DBConn();
			conn = dbConn.connect();
		}
		else {
			conn = conn2;
		}

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				for (RocTicket tkt : historyTickets) {
//					logger.debug("inserting " + tkt.getRocTicketId() + " into history table");
					pstmt.setInt(1, tkt.getRocTicketId());
					pstmt.setString(2, vzId);
					pstmt.setString(3, tkt.getTicketStatus());
					pstmt.setString(4, tkt.getReferredTo());
					pstmt.setString(5, "N");
					pstmt.setString(6, tkt.getOnHoldStatus());
					pstmt.setString(7, tkt.getUserComment());
					pstmt.addBatch();
				}
				pstmt.executeBatch();		
			}
		}
		catch (Exception e) {
			logger.error("Exception in insertIntoTicketHistory: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			if (null == conn2) {
				Utils.safeCloseResources(null, pstmt, conn);
				conn = null;
			}
		}
		
		getHistIdsForMasterTable(historyTickets, sessionVzId);
	}

	public void getHistIdsForMasterTable(Collection<RocTicket> collection, String sessionVzId) {
		logger.debug("in getHistIdsForMasterTable");
		HashMap<Integer, Integer> histIds = new HashMap<Integer, Integer>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				for(RocTicket r : collection) {
					int histId =  getTicketHistoryId(r.getRocTicketId(), conn);
					if (!histIds.containsKey(histId)) {
						histIds.put(r.getRocTicketId(), histId);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getHistIdForMasterTable: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		updateRocTicketMasterWithHistId(histIds, sessionVzId);
	}

	private void updateRocTicketMasterWithHistId(HashMap<Integer, Integer> histIds, String sessionVzId) {
		logger.debug("in updateRocTicketMasterWithHistId");
		PreparedStatement pstmt = null;
		String query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_MASTER_WITH_HIST_ID");
		logger.debug(query);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				for(Integer r : histIds.keySet()) {
					pstmt.setInt(1, histIds.get(r));
					pstmt.setInt(2, r);
					pstmt.execute();
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateRocTicketMasterWithHistId: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	private void insertIntoAssignmentHistTable(String assignedTo, String assignee, String tkts, Connection conn2, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = DBQueries.dbQueriesMap.get("INSERT_TICKET_INTO_ASSIGNMENT_HIST");
		logger.debug("assigning to: " + assignedTo);
		logger.debug("tktString: "+ tkts);
		logger.debug("assignee: " + assignee);
		Connection conn = null;
		
		if (null == conn2) {
			DBConn dbConn = new DBConn();
			conn = dbConn.connect();
		}
		else {
			conn = conn2;
		}

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				for (String tkt : tkts.split(",")) {
					int tktId = Integer.parseInt(tkt);
					logger.debug("tkt: " + tkt);
					pstmt.setInt(1, tktId);
					pstmt.setString(2, assignedTo);					
					pstmt.setString(3, assignee);
					
					RocTicket r = getTicket(tktId, false, conn, sessionVzId);
							
					pstmt.setString(4, r.getTicketStatus());
					pstmt.setInt(5, r.getHistoryId());
					
					if(null != pstmt){
						pstmt.addBatch();
					}
				}
				pstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			if (null == conn2) {
				Utils.safeCloseResources(null, pstmt, conn);
				conn = null;
			}
		}		
	}

	public synchronized HashMap<String, RocTicket> getUnassigned(String sessionVzId) {
		logger.debug("getting unassigned tickets");
		
		HashMap<String, RocTicket> ticketList = new HashMap<String, RocTicket>();		
		String query = DBQueries.dbQueriesMap.get("GET_ALL_TICKETS");
		logger.debug(query);
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);			
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					RocTicket rt = populateRocTicket(rs, conn);					
					ticketList.put(rt.getRocTicketId() + "", rt);
				}
				
				logger.debug("after getting unassigned tickets for ticketList has: " +  ticketList.size() + " tickets");
			}
			
		}
		catch (Exception e) {
			logger.error("Exception in getTickets: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		
		return ticketList;
	}

	public ArrayList<ArrayList<String>> getPendingTicketsForUsers(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_PENDING_COUNTS");
		ArrayList<ArrayList<String>> counts = new ArrayList<ArrayList<String>>();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while(rs.next()) {
					ArrayList<String> inner = new ArrayList<String>();
					inner.add(rs.getString("first_name"));
					
					if (null != rs.getString("referred_to"))
						inner.add(rs.getString("referred_to"));
					else
						inner.add("0");
					
					if (null != rs.getString("assigned"))
						inner.add(rs.getString("assigned"));
					else
						inner.add("0");
					
					counts.add(inner);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return counts;
	}

	public HashMap<String, Integer> getCountsPerReferredTo(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_COUNT_PER_REFERRED_TO");
		HashMap<String, Integer> counts = new HashMap<String, Integer>();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while(rs.next()) {
					counts.put(rs.getString("referred_to"), rs.getInt("amount"));				
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketCompleteCount: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		return counts;
	}
	
	public ArrayList<String> getReroutes(String sessionVzId) {
		ArrayList<String> routes = new ArrayList<String>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		logger.debug("in dbDriver's getReroutes()");
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			String query = DBQueries.dbQueriesMap.get("GET_REROUTE_STATUSES");
			
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while(rs.next()) {
					String status = rs.getString("REROUTE_STATUS");
					routes.add(status);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getRoutes: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		logger.debug("returning : " + routes);
		return routes;
	}
		
	public ArrayList<FilterOption> getFilterOptions(HashMap<String, String> labels, String sessionVzId) {
		logger.debug("about to get every option from unassigned tickets");
		HashMap<String, RocTicket> tickets = getUnassigned(sessionVzId);
		logger.debug("got all options from unassigned tickets");
		
		ArrayList<FilterOption> options = new ArrayList<FilterOption>();
		
		for (String label : labels.values()) {
			FilterOption fo = new FilterOption();
			fo.setOptionLabel(label);
			options.add(fo);
		}
		
		if (null == Controller.userList || Controller.userList.size()  == 0) {
			logger.debug("grabbing users");
			getUsers(sessionVzId);
		}
		
		if (null != Controller.userList) {
			for (User u : Controller.userList.values()) {
				options.get(0).addToOptionIds(u.getVzId());
				options.get(0).addToLables(u.getFirstname());
			}
		}
		
		options.get(0).addToOptionIds("null");
		options.get(0).addToLables("Unassigned");
		
		options.get(7).addToOptionIds("1");
		options.get(7).addToLables("Y");

		options.get(7).addToOptionIds("0");
		options.get(7).addToLables("N");
		
		ArrayList<String> categories = getMainCategories(sessionVzId);
		
		for (String ref : categories) {
			options.get(1).addToOptionIds(ref);
			options.get(1).addToLables(ref);
		}
		
		for (String tktId : tickets.keySet()) {			
			if (null != tickets.get(tktId).getTicketCategory()) {
				if (!options.get(2).getLabels().contains(tickets.get(tktId).getTicketCategory())) {
					options.get(2).addToOptionIds(tickets.get(tktId).getTicketCategory());
					options.get(2).addToLables(tickets.get(tktId).getTicketCategory());
				}
			}
			
			if (null != tickets.get(tktId).getPriority()) {
				if (!options.get(3).getLabels().contains(tickets.get(tktId).getPriority())) {
					options.get(3).addToOptionIds(tickets.get(tktId).getPriority());
					options.get(3).addToLables(tickets.get(tktId).getPriority());
				}
			}
			
			if (null != tickets.get(tktId).getTicketStatus()) {
				if (!options.get(4).getLabels().contains(tickets.get(tktId).getTicketStatus())) {
					options.get(4).addToOptionIds(tickets.get(tktId).getTicketStatus());
					options.get(4).addToLables(tickets.get(tktId).getTicketStatus());
				}
			}
			
			if (null != tickets.get(tktId).getOnsiteReferred()) { 
				if (!options.get(5).getLabels().contains(tickets.get(tktId).getOnsiteReferred())) {
					options.get(5).addToOptionIds(tickets.get(tktId).getOnsiteReferred());
					options.get(5).addToLables(tickets.get(tktId).getOnsiteReferred());
				}
			}
			
			if (null != tickets.get(tktId).getReferredCategory()) {
				if (!options.get(6).getLabels().contains(tickets.get(tktId).getReferredCategory())) {
					options.get(6).addToOptionIds(tickets.get(tktId).getReferredCategory());
					options.get(6).addToLables(tickets.get(tktId).getReferredCategory());
				}
			}
		}
		
		return options;
	}

	private ArrayList<String> getMainCategories(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		ArrayList<String> categories = new ArrayList<String>();
		try {
			if (null != conn) {
				String query = DBQueries.dbQueriesMap.get("GET_MAIN_CATEGORIES");
				
				if (null != query) {
					pstmt = conn.prepareStatement(query);
					rs = pstmt.executeQuery();
					
					while (rs.next()) {
						categories.add(rs.getString("category_name"));
					}
				}
			}
		}
		catch(Exception e) {
			logger.error("Exception in getMainCategories: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return categories;
	}

	public HashMap<String, RocTicket> getFilteredTickets(String clauses, String sessionVzId) {
		
		HashMap<String, RocTicket> tickets = new HashMap<String, RocTicket>();
		
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		String query = DBQueries.dbQueriesMap.get("GET_FILTERED_TICKETS");
		String[] sql = clauses.split("`");
		
		int i = 1;
		int sqlLength = sql.length;
		
		for (; i < sqlLength - 1; i++) {
			if (sql[i].contains("null") || sql[i].contains("NULL")) {
				query += "" + " AND ";
 			}
			else {
				query += sql[i] + " AND ";
			}
		}
		
		if (null != sql[i])
			query += sql[i];
		else
			query += query.subSequence(0, query.length() - 5);
		
		logger.debug(query);
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					RocTicket rt = new RocTicket();
					rt.setRocTicketId(rs.getInt("roc_ticket_id"));
					rt.setVzId(rs.getString("vz_id"));
					rt.setReferredTo(rs.getString("referred_to"));
					rt.setTicketCategory(rs.getString("ticket_category"));
					rt.setPriority(rs.getString("priority"));
					rt.setSspRouteCycles(rs.getInt("ssp_route_cycles"));
					rt.setLastItReferredDate(rs.getDate("last_it_referred_date"));
					rt.setTicketStatus(rs.getString("ticket_status"));
					rt.setOnsiteReferred(rs.getString("onsite_referred"));
					rt.setReferredCategory(rs.getString("onsite_referred_category"));
					rt.setIsBusiness(rs.getInt("is_business"));
					rt.setHistoryId(getTicketHistoryIdFromMasterTable(rt.getRocTicketId(), conn));
					
					ArrayList<String> codeAndComment = getHoldCode(rt.getHistoryId(), conn);
					
					if (!codeAndComment.isEmpty()) {
						if (null != codeAndComment.get(0))
							rt.setOnHoldStatus(codeAndComment.get(0));
						if (null != codeAndComment.get(1))
							rt.setUserComment(codeAndComment.get(1));
					}
					
					tickets.put(rt.getRocTicketId() + "", rt);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getFilteredTickets: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
			conn = null;
		}
		return tickets;
	}

	public String addClosureCodes(String codes, String sessionVzId) {
		String[] splitted = codes.split("`");
		
		if (splitted[0].split(Pattern.quote("|")).length > 1) {
			addClosureCategory(splitted[0].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[1].split(Pattern.quote("|")).length > 1) {
			addTracking(splitted[1].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[2].split(Pattern.quote("|")).length > 1) {
			addIssueStage(splitted[2].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[3].split(Pattern.quote("|")).length > 1) {
			addOrderingStage(splitted[3].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[4].split(Pattern.quote("|")).length > 1) {
			addProvisionStage(splitted[4].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[5].split(Pattern.quote("|")).length > 1) {
			addBillingStage(splitted[5].split(Pattern.quote("|"))[1], sessionVzId);
		}	
		
		updateClosureCats(sessionVzId);
		
		return "1";
	}

	private void updateClosureCats(String sessionVzId) {
		Controller.setClosureStages(null);
		ClosureCodesController ccc = ClosureCodesController.getInstance();
		ccc.getClosureCategories();
		ccc.getTrackingCategories(sessionVzId);
		ccc.getIssues(6,sessionVzId);
		ccc.getOrdering(6,sessionVzId);
		ccc.getBilling(6,sessionVzId);
		ccc.getProvisioning(6,sessionVzId);
	}

	private void addBillingStage(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_BILLING_STAGE (BILLING_STAGE_ID, BILLING_STAGE_DESC, PARENT_TRACKING_ID) VALUES (roc_billing_seq.nextval,?,6)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addBillingStage: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}

	private void addProvisionStage(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_PROVISION_STAGE (PROVISION_STAGE_ID, PROVISION_STAGE_DESC, PARENT_TRACKING_ID) VALUES (roc_provision_seq.nextval,?,6)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addProvisionStage: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}		
	}

	private void addOrderingStage(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_ORDERING_STAGE (ORDERING_STAGE_ID, ORDER_STAGE_DESC, PARENT_TRACKING_ID) VALUES (roc_ordering_seq.nextval,?,6)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addOrderingStage: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	private void addIssueStage(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_ISSUE_STAGE (ISSUE_STAGE_ID, ISSUE_STAGE_DESC, PARENT_TRACKING_ID) VALUES (roc_issue_seq.nextval,?,6)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addIssueStage: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}

	private void addTracking(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_INTERNAL_TRACKING (TRACKING_ID, TRACKING_CATEGORY, PARENT_TRACKING_ID) VALUES (roc_internal_seq.nextval,?,6)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addTracking: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	private void addClosureCategory(String string, String sessionVzId) {
		PreparedStatement pstmt = null;
		String query = "INSERT INTO ROC_CLOSURE_CATEGORY (CATEGORY_ID, CATEGORY_DESC) VALUES (roc_closure_seq.nextval,?)";
		logger.debug("adding: " + string);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, string);
			pstmt.execute();
		}
		catch (Exception e) {
			logger.error("Exception in addClosureCategory: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
			conn = null;
		}
	}

	public ArrayList<String> getHoldCodes(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_HOLD_CODES");
		ArrayList<String> codes = new ArrayList<String>();		
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		try {			
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				codes.add(rs.getString("onhold_code"));
			}
		}
		catch (Exception e) {
			logger.error("Exception in getHoldCodes: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return codes;
	}

	/**
	 * updates the history table's ftp status
	 * @param csvs
	 */
	public static void updateTicketHistoryFTPStatus(HashMap<Integer, CSVObj> csvs) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String query = DBQueries.dbQueriesMap.get("UPDATE_HISOTRY_FTP");
				
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				for(Integer tktId: csvs.keySet()) {
					pstmt.setInt(1, tktId);
					pstmt.addBatch();
				}
				pstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketHistoryFTPStatus: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "System", 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}

	public static HashMap<Integer, RocTicket> getAllTicketIds(String sessionVzId) {
		HashMap<Integer, RocTicket> ids = new HashMap<Integer, RocTicket>();
		
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		ResultSet rs = null;
		Connection conn = dbConn.connect();
		String query = DBQueries.dbQueriesMap.get("GET_TICKET_IDS");
				
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);				
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					RocTicket rt = new RocTicket();
					rt.setRocTicketId(rs.getInt("roc_ticket_id"));
					rt.setVzId(rs.getString("vz_id"));
					rt.setReferredTo(rs.getString("referred_to"));
					rt.setTicketCategory(rs.getString("ticket_category"));
					rt.setPriority(rs.getString("priority"));
					rt.setSspRouteCycles(rs.getInt("ssp_route_cycles"));
					rt.setLastItReferredDate(rs.getDate("last_it_referred_date"));
					rt.setTicketStatus(rs.getString("ticket_status"));
					rt.setOnsiteReferred(rs.getString("onsite_referred"));
					rt.setReferredCategory(rs.getString("onsite_referred_category"));
					rt.setIsBusiness(rs.getInt("is_business"));
					rt.setHistoryId(rs.getInt("hist_id"));
					
					ArrayList<String> codeAndComment = getHoldCode(rt.getHistoryId(), conn);
					
					if (!codeAndComment.isEmpty()) {
						if (null != codeAndComment.get(0))
							rt.setOnHoldStatus(codeAndComment.get(0));
						if (null != codeAndComment.get(1))
							rt.setUserComment(codeAndComment.get(1));
					}
					
					if (null != rt) {	
						ids.put(rt.getRocTicketId(), rt);
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketHistoryFTPStatus: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return ids;
	}

	public static String getFTPStatus(Integer rocTkt) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String status = "";
		String query = DBQueries.dbQueriesMap.get("GET_FTP_STATUS");
		
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);	
				pstmt.setInt(1, rocTkt);
				rs = pstmt.executeQuery();
				
				if (rs.next()) {
					status = rs.getString("ticket_status");
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketHistoryFTPStatus: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "", rocTkt);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return status;	
	}

	public static void resendTickets(ArrayList<Integer> ticketsToResend, String sessionVzId) {
		PreparedStatement pstmt = null;
		DBConn dbCon = new DBConn();
		Connection conn = dbCon.connect();
		String query = DBQueries.dbQueriesMap.get("RESEND_TICKETS");
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				for (Integer tktId: ticketsToResend) {
					pstmt.setInt(1, tktId);
					pstmt.addBatch();
				}
				pstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in resendTickets: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}
	
	public static void writeToErrorLog(String error, String vzId, int tktId) {
		String query = DBQueries.dbQueriesMap.get("INSERT_INTO_ERROR_LOGS");
		PreparedStatement pstmt = null;
		DBConn dbconn = new DBConn();
		Connection conn = dbconn.connect();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, error);
				pstmt.setInt(2, tktId);
				pstmt.setString(3, vzId);
				pstmt.execute();
			}
		}
		catch (Exception e) {
			logger.error("Exception in writeToErrorLogs: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}	
	}

	public static void getProperties() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		String query = DBQueries.dbQueriesMap.get("GET_PROPERTIES");
		logger.debug(query);
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					Controller.propertiesMap.put(rs.getString("PROPERTY_NAME"), rs.getString("PROPERTY_VALUE"));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getProperties: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, "System", 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
	}

	public ArrayList<String> getAllQueues(String sessionVzId) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();

		String query = DBQueries.dbQueriesMap.get("GET_ALL_QUEUES");
		ArrayList<String> queues = new ArrayList<String>();
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				rs = pstmt.executeQuery();
				
				while (rs.next()) {
					queues.add(rs.getString("referred_name"));
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllQueues: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return queues;
	}

	public String removeQueue(String which, String queue, String sessionVzId) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String removed = "false";
		String query = "";
		
		if (which.equals("status"))
			query = DBQueries.dbQueriesMap.get("REMOVE_STATUS_QUEUE");
		else if (which.equals("reroute"))
			query = DBQueries.dbQueriesMap.get("REMOVE_REROUTE_STATUS");
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, queue);
				
				pstmt.execute();
				removed = "true";
			}
		}
		catch (Exception e) {
			logger.error("Exception in removeQueue: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
		
		reloadStatuses(sessionVzId);
		return removed;
	}

	private void reloadStatuses(String sessionVzId) {
		getStatuses();
	}

	public String addQueue(String which, String queue, String sessionVzId) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		String added = "false";
		String query = "";
		
		if (which.equals("status"))
			query = DBQueries.dbQueriesMap.get("ADD_STATUS_QUEUE");
		else if (which.equals("reroute"))
			query = DBQueries.dbQueriesMap.get("ADD_REROUTE_QUEUE");
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				
				if (which.equals("reroute")) {
					String[] queueSplit = queue.split("`");
					String queueName = queueSplit[0];
					String queueMap = queueSplit[1];
					pstmt.setString(1, queueName);
					pstmt.setString(2, queueMap);
				}
				else {
					pstmt.setString(1, queue);
				}
				
				pstmt.execute();
				added = "true";
			}
		}
		catch (Exception e) {
			logger.error("Exception in addQueue: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
		
		reloadStatuses(sessionVzId);
		return added;
	}

	public String removeClosureCodes(String codes, String sessionVzId) {
		String[] splitted = codes.split("`");
		
		if (splitted[0].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("closure" ,splitted[0].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[1].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("tracking", splitted[1].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[2].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("issue", splitted[2].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[3].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("ordering", splitted[3].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[4].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("provision", splitted[4].split(Pattern.quote("|"))[1], sessionVzId);
		}
		if (splitted[5].split(Pattern.quote("|")).length > 1) {
			removeClosureCode("billing", splitted[5].split(Pattern.quote("|"))[1], sessionVzId);
		}
		
		updateClosureCats(sessionVzId);
		
		return "removed";
	}

	private void removeClosureCode(String whichType, String desc, String sessionVzId) {
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		logger.debug("removing closure code: " + whichType + " with desc: " + desc);
		String query = "";
		
		if ("closure".equals(whichType))
			query = "DELETE FROM ROC_CLOSURE_CATEGORY WHERE CATEGORY_DESC=?";
		else if ("tracking".equals(whichType))
			query = "DELETE FROM ROC_INTERNAL_TRACKING WHERE TRACKING_CATEGORY=?";
		else if ("issue".equals(whichType))
			query = "DELETE FROM ROC_ISSUE_STAGE WHERE ISSUE_STAGE_DESC=?";
		else if ("ordering".equals(whichType))
			query = "DELETE FROM ROC_ORDERING_STAGE WHERE ORDER_STAGE_DESC=?";
		else if ("provision".equals(whichType))
			query = "DELETE FROM ROC_PROVISION_STAGE WHERE PROVISION_STAGE_DESC=?";
		else if ("billing".equals(whichType))
			query = "DELETE FROM ROC_BILLING_STAGE WHERE BILLING_STAGE_DESC=?";
		
		try {
			if (null != conn) {
				pstmt = conn.prepareStatement(query);
				pstmt.setString(1, desc);
				pstmt.execute();
			}
		}
		catch (Exception e) {
			logger.error("Exception in addQueue: ", e);
			String error = Utils.stackTraceToString(e);
			writeToErrorLog(error, sessionVzId, 0);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}
}