package com.verizon.ssp.roctool.controller;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
//import java.util.Date;
import java.util.HashMap;
//import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;

import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.db.DBConn;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.db.DBQueries;
import com.verizon.ssp.roctool.object.RocTicket;
import com.verizon.ssp.roctool.object.User;
import java.sql.Connection;

public class AssignmentDaemonController {
	private static AssignmentDaemonController controller = null;
	private DBDriver drive = new DBDriver();
	private HashMap<Integer, RocTicket> ticketsAlreadyInLocalDB = null;	
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	private AssignmentDaemonController() {
		ticketsAlreadyInLocalDB = new HashMap<Integer, RocTicket>();
		
		if (null == Utils.focusReferredTo) 
			drive.getReferredToList("System");
	}

	public void priorityAssignment() {
		insertUsersIntoAssignmentHist();
		logger.debug("inside priorityAssignment");
		int maxPriorities = 0;
		Queue<User> assignmentQueue = new LinkedList<User>();
		ArrayList<String> mainCategories = getMainCategories();
		HashMap<String, User> newAssignmentsUsers = new HashMap<String, User>();
		HashMap<Integer, RocTicket> newAssignmentsTickets = new HashMap<Integer, RocTicket>();

		getUsersThatCanTakeMoreTickets(assignmentQueue);

		if (!assignmentQueue.isEmpty()) {
			for (User u : assignmentQueue) {
				int numCats = u.getUsersBestCategory().split(",").length;
				if (maxPriorities < numCats) {
					maxPriorities = numCats;
				}
			}
		}

		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		
		String ahQuery = DBQueries.dbQueriesMap.get("INSERT_TICKET_INTO_ASSIGNMENT_HIST");		
		PreparedStatement ahPstmt = null;

		try {
			ahPstmt = conn.prepareStatement(ahQuery);
			int assignedCount = 0;
			for (int priority = 0; priority < maxPriorities; priority++) {
				for (String cat : mainCategories) {
					ArrayList<Queue<User>> usersWhosPriorityMatch = grabUsersForCat(cat, priority, assignmentQueue); // done
					if (usersWhosPriorityMatch.size() > 0) {
						ArrayList<RocTicket> ticketsGrabbed = grabTicketForCat(cat, conn); // done
						getNumOfBusiness(ticketsGrabbed, cat); // for debugging purposes
						for (RocTicket r : ticketsGrabbed) {
							while (!usersWhosPriorityMatch.get(0).isEmpty() || !usersWhosPriorityMatch.get(1).isEmpty()) {
								User u = usersWhosPriorityMatch.get(r.getIsBusiness()).poll();
								if (null != u && u.getTicketsTaken() < u.getTicketsPerDay() && null != r) {
									if (assignedCount % 100 == 0)
										logger.debug(u.getVzId() + " offshore="+u.getOffShore() + " tkt Busi="+r.getIsBusiness());
									u.setTicketsTaken(u.getTicketsTaken() + 1);
									u.setTicketsPending(u.getTicketsPending() + 1);
									r.setVzId(u.getVzId());
									if (u.getTicketsTaken() < u.getTicketsPerDay())
										usersWhosPriorityMatch.get(r.getIsBusiness()).add(u);

									logger.debug("gave " + u.getVzId() + " ticket: " + r.getRocTicketId() 
											+ " and has taken: " + u.getTicketsTaken());

									java.util.Date today = new java.util.Date();
									java.sql.Date sqlDate = new java.sql.Date(today.getTime());
									ahPstmt.setInt(1, r.getRocTicketId());
									ahPstmt.setString(2, u.getVzId());
									ahPstmt.setDate(3, sqlDate);
									ahPstmt.setString(4, r.getTicketStatus());
									ahPstmt.setDate(5, sqlDate);
									ahPstmt.addBatch();

									if (!newAssignmentsUsers.containsKey(u.getVzId())) {
										newAssignmentsUsers.put(u.getVzId(), u);
									}
									else {
										newAssignmentsUsers.get(u.getVzId()).setTicketsPending(u.getTicketsPending());
										newAssignmentsUsers.get(u.getVzId()).setTicketsTaken(u.getTicketsTaken());
									}										
									if (!newAssignmentsTickets.containsKey(r.getRocTicketId())) {
										newAssignmentsTickets.put(r.getRocTicketId(), r);
									}
									assignedCount++;
									break;
								}
								else
									break;
							}
						}
						if (newAssignmentsTickets.size() > 0)
							updateTicketMasterVzIds(newAssignmentsTickets, conn);
						newAssignmentsTickets.clear();
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in priorityAssignment: ", e);
		}	
		finally {
			Utils.safeCloseResources(null, ahPstmt, conn);
		}

		logger.debug("done with assignments");

		if (newAssignmentsUsers.size() > 0) {
			updateUsersTakenCount(newAssignmentsUsers);
		}
	}

	private void getNumOfBusiness(ArrayList<RocTicket> tickets, String category) {
		int business = 0;
		int consumer = 0;

		for (RocTicket r : tickets) {
			if (r.getIsBusiness() == 1)
				business++;
			else
				consumer++;
		}

		logger.debug("for " + category + " there are " + business + " business tickets and " + consumer + " consumer tickets");
	}

	private ArrayList<Queue<User>> grabUsersForCat(String cat, int priority, Queue<User> assignmentQueue) {
		ArrayList<Queue<User>> returnQ = new ArrayList<Queue<User>>();
		Queue<User> busiQueue = new LinkedList<User>();
		Queue<User> conQueue = new LinkedList<User>();
		returnQ.add(busiQueue);
		returnQ.add(conQueue);
		Queue<User> temp = new LinkedList<User>();

		logger.debug("in grabUsersForCat");
		try {
			if (null != assignmentQueue)
				temp.addAll(assignmentQueue);
			while(!temp.isEmpty()) {
				User u = temp.poll();
				if (null != u) {
					String[] usersCats = u.getUsersBestCategory().split(",");
					if (usersCats.length > priority) {
						if (usersCats[priority].equalsIgnoreCase(cat)) {
							if (u.getOffShore().equals("B")) {
								returnQ.get(0).add(u);
								returnQ.get(1).add(u);
							}
							else if (u.getOffShore().equals("Y"))
								returnQ.get(0).add(u);
							else
								returnQ.get(1).add(u);
						}
					}
				}
			}
		}		
		catch (Exception e) {
			logger.error("Exception in grabUsersForCat: ", e);
		}
		return returnQ;
	}

	private ArrayList<RocTicket> grabTicketForCat(String cat, Connection conn) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_TICKETS_BY_CATEGORY");
		ArrayList<RocTicket> tickets = new ArrayList<RocTicket>();

		logger.debug("in grabTicketForCat");
		logger.debug(query + "=" + cat);
		try {
			pstmt = conn.prepareStatement(query);
			pstmt.setString(1, cat);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				RocTicket rt = new RocTicket();
				rt.setRocTicketId(rs.getInt("roc_ticket_id"));
				rt.setLastItReferredDate(rs.getDate("last_it_referred_date"));
				rt.setTicketStatus(rs.getString("ticket_status"));
				rt.setReferredTo(rs.getString("referred_to"));
				rt.setIsBusiness(rs.getInt("is_business"));
									
				if (Utils.focusReferredTo.contains(rt.getReferredTo()) 
					&& !rt.getTicketStatus().toLowerCase().equals("complete") 
					&& !rt.getTicketStatus().toLowerCase().equals("closed")
					&& !rt.getTicketStatus().toLowerCase().equals("to test")) {
					
					tickets.add(rt);
				}
			}
		}
		catch(Exception e) {
			logger.error("Exception in grabTicketForCat: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, null);
		}	
		logger.debug("about to exit grabTicketForCat; numOfTicketsGrabbed: " + tickets.size());
		return tickets;
	}

	private ArrayList<String> getMainCategories() {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String query = DBQueries.dbQueriesMap.get("GET_MAIN_CATEGORIES");
		ArrayList<String> cats = new ArrayList<String>();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		logger.debug("in getMainCategories");
		try {
			 ;
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				cats.add(rs.getString("category_name"));
			}
		}
		catch(Exception e) {
			logger.error("Exception in getMainCategories: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		logger.debug("about to exit getMainCategories");
		logger.debug(cats);
		return cats;
	}

	

	private void updateTicketMasterVzIds(
			HashMap<Integer, RocTicket> newAssignmentsTickets, Connection conn) {
		PreparedStatement pstmt = null;
		
		try {
			String query = DBQueries.dbQueriesMap.get("UPDATE_TICKET_MASTER_VZ_ID");
			if (null == conn) {
				 ;
				pstmt = conn.prepareStatement(query);
			}
			else 
				pstmt = conn.prepareStatement(query);
			
			for (int tktId : newAssignmentsTickets.keySet()) {
				pstmt.setString(1, newAssignmentsTickets.get(tktId).getVzId());
				pstmt.setInt(2, tktId);
				pstmt.addBatch();
			}
			
			pstmt.executeBatch();
		}
		catch (Exception e) {
			logger.error("Exception in updateTicketMasterVzIds: ", e);
		}
		finally {
			if (null == conn)
				Utils.safeCloseResources(null, pstmt, conn);
			else
				Utils.safeCloseResources(null, pstmt, null);
		}
	}

	private void updateUsersTakenCount(HashMap<String, User> newAssignmentsUsers) {
		logger.debug("Updating users taken count");
		
		String query = DBQueries.dbQueriesMap.get("UPDATE_USERS_TAKEN_COUNT");
		logger.debug("newAssignments: " + newAssignmentsUsers);
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		PreparedStatement countPstmt = null;
		
		try {
			countPstmt = conn.prepareStatement(query);
			for (String u: newAssignmentsUsers.keySet()) {
				logger.debug("inside updateUsersTakenCount u=" + u);
				if (null != newAssignmentsUsers.get(u)) {
					logger.debug("user: " + newAssignmentsUsers.get(u).getVzId() + " has this many ticketsTaken: " + newAssignmentsUsers.get(u).getTicketsTaken());
					
					countPstmt.setInt(1, newAssignmentsUsers.get(u).getTicketsTaken());
					countPstmt.setInt(2, newAssignmentsUsers.get(u).getTicketsPending());
					countPstmt.setString(3, newAssignmentsUsers.get(u).getVzId());
					countPstmt.addBatch();
				}
			}
			countPstmt.executeBatch();
		}
		catch (Exception e) {
			logger.error("Exception in updateUsersTakenCount: ", e);
		}
		finally {
			Utils.safeCloseResources(null, countPstmt, conn);
		}			
	}

	/**
	 * gets users that can take more tickets
	 * @param assignmentQueue 
	 */
	private void getUsersThatCanTakeMoreTickets(Queue<User> assignmentQueue) {
		logger.debug("getting users that can take more tickets");
		
		// query 19
		String query = DBQueries.dbQueriesMap.get("GET_USERS_THAT_CAN_TAKE_MORE_TICKETS");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {				
				User u = new User();
				u.setTicketsPerDay(rs.getInt("tickets_per_day"));
				u.setVzId(rs.getString("vz_id"));
				u.setOffShore(rs.getString("is_offshore"));
				u.setTicketsPending(rs.getInt("pending_count"));
				u.setUsersBestCategory(rs.getString("users_best_ticket_category"));
				
				if (!assignmentQueue.contains(u)) {
					logger.debug("adding: " + u.getVzId() + " to the queue...");
					logger.debug("taken: " + u.getTicketsTaken() + " perday: " + u.getTicketsPerDay());
					
					assignmentQueue.add(u);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUsersThatCanTakeMoreTickets: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
	}
	
	/**
	 * inserting the active users into assignment_hist table
	 */
	private void insertUsersIntoAssignmentHist() {				
		logger.debug("putting users into assignment hist");
		String insertQuery = DBQueries.dbQueriesMap.get("INSERT_USER_INTO_USER_ASSIGNMENT_HIST");		
		ArrayList<String> activeUsers = getActiveUsers();
		ArrayList<String> usersInAlready = seeIfUserAlreadyInAssignmentHist(activeUsers);
		ArrayList<String> usersInToday = getUsersInAHToday();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		PreparedStatement initialPstmt = null;
		boolean initialInsert = false;
		
		try {
			if (null != activeUsers && activeUsers.size() > 0) {				
				logger.debug(activeUsers);
				initialPstmt = conn.prepareStatement(insertQuery);
				
				for (String u: activeUsers) {
					if (!usersInAlready.contains(u) && !usersInToday.contains(u)) {
						if (!initialInsert)
							initialInsert = true;
						logger.debug("inserting: "  + u + " to assignment_hist without previous pending_count");
						
						initialPstmt.setString(1, u);
						initialPstmt.addBatch();
					}
				}
				
				if (initialInsert)
					initialPstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in insertUsersIntoAssignmentHist: ", e);
		}
		finally {
			Utils.safeCloseResources(null, initialPstmt, conn);			
		}
		insertUsersIntoAssignmentHistWithCount(activeUsers, usersInAlready, usersInToday);
	}

	private ArrayList<String> getUsersInAHToday() {
		ArrayList<String> usersInToday = new ArrayList<String>();
		String query = DBQueries.dbQueriesMap.get("GET_USERS_ALREADY_IN_ASSIGNMENT_HIST_TODAY");	
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();

			while (rs.next()) {
				if (!usersInToday.contains(rs.getString("vz_id")))
					usersInToday.add(rs.getString("vz_id"));
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUsersInAHToday: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}

		return usersInToday;
	}
	
	
	private void insertUsersIntoAssignmentHistWithCount(ArrayList<String> activeUsers, ArrayList<String> usersInAlready,
		ArrayList<String> usersInToday) {
		
		String insertQueryPending = DBQueries.dbQueriesMap.get("INSERT_USER_INTO_ASSIGNMENT_HIST_WITH_PENDING_COUNT");		
		PreparedStatement pstmt = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		try {
			if (null != activeUsers && activeUsers.size() > 0) {				
				logger.debug(activeUsers);
				pstmt = conn.prepareStatement(insertQueryPending);
				
				for (String u: usersInAlready) {
					if (!usersInToday.contains(u)) {
						logger.debug("inserting: "  + u + " to assignment_hist with previous pending_count");
						
						pstmt.setString(1, u);
						pstmt.setString(2, u);
						pstmt.setString(3, u);
						pstmt.addBatch();
					}
				}
				pstmt.executeBatch();
			}
		}
		catch (Exception e) {
			logger.error("Exception in insertUsersIntoAssignmentHistWithCount: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
	}

	private ArrayList<String> seeIfUserAlreadyInAssignmentHist(ArrayList<String> activeUsers) {
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<String> usersInAssignHist = new ArrayList<String>();
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		try {
			String query = DBQueries.dbQueriesMap.get("GET_USERS_ALREADY_IN_ASSIGNMENT_HIST");
			pstmt = conn.prepareStatement(query);			
			rs = pstmt.executeQuery();
			
			while (rs.next()){
				logger.debug("adding " + rs.getString("vz_id") + " to usersInAssignHist");
				usersInAssignHist.add(rs.getString("vz_id"));
			}
		}
		catch (Exception e) {
			logger.error("Exception in seeIfsUserAlreadyInAssignmentHist: ", e);
		}
		finally {
			Utils.safeCloseResources(rs, pstmt, conn);
		}
		
		return usersInAssignHist;
	}

	private ArrayList<String> getActiveUsers() {
		ArrayList<String> activeUsers = new ArrayList<String>();
		String query = DBQueries.dbQueriesMap.get("GET_ACTIVE_USERS");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		DBConn dbConn = new DBConn();
		Connection conn = dbConn.connect();
		try {
			pstmt = conn.prepareStatement(query);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				activeUsers.add(rs.getString("vz_id"));
			}
		}
		catch (Exception e) {
			logger.error("Exception in getActiveUsers: ", e);
		}
		finally {
			Utils.safeCloseResources(null, pstmt, conn);
		}
		return activeUsers;
	}

	
	private User seeIfUserHasCategory(Queue<User> assignmentQueue, RocTicket r) {
		User u;
		User temp = null;
		Queue<User> tempQ = new LinkedList<User>();
		logger.debug("inside seeIfUserHasCategory: " + assignmentQueue);
		logger.debug("referred_to: " + r.getReferredTo());
		tempQ.addAll(assignmentQueue);
		
		while (!tempQ.isEmpty()) {
			u = tempQ.poll();
			
			if (null != u.getUsersBestCategory() && !u.getUsersBestCategory().equals("")
					&& null != r.getReferredTo()) {
				for (String c: u.getUsersBestCategory().split(",")) {
					if (null != c && c.equals(r.getReferredTo()) 
							&& u.getTicketsTaken() < u.getTicketsPerDay()) {
						temp = u;
						break;
					}
				}
			}

			if (null != temp) 
				break;
		}
		if (null != temp) {
			logger.debug("returning " + temp + " from seeIfUserHasCategory");
		}
		return temp;
	}
	
	public static synchronized AssignmentDaemonController getInstance(){
		if(null == controller)
			controller = new AssignmentDaemonController();
		return controller;
	}
		
	public HashMap<Integer, RocTicket> getTicketsAlreadyInLocalDB() {
		return this.ticketsAlreadyInLocalDB;
	}
}
