package com.verizon.ssp.roctool.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.object.NewUser;
import com.verizon.ssp.roctool.object.RocTicket;
import com.verizon.ssp.roctool.object.FilterOption;
import com.verizon.ssp.roctool.object.RocTicketHistory;
import com.verizon.ssp.roctool.object.User;

public class Controller {
	private static Controller controller = null;
	private DBDriver drive = new DBDriver();
	private String userHTML;
	private String userListHTML;
	private static String referredToListHTML;
	public static String filterHTML;
	
	public static HashMap<String, User> userList = new HashMap<String, User>();
	public static HashMap<String, HashMap<String, RocTicket>> userticketList = new HashMap<String, HashMap<String,RocTicket>>();
	private static boolean getUsers = true;
	private static ArrayList<String> closureCats;
	private static HashMap<String, HashMap<String, ArrayList<String>>> closureStages;
	public static HashMap<String, String> propertiesMap = new HashMap<String, String>();
	
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);	

	static {
		DBDriver.getProperties();
	}
	
	private Controller() {
		userHTML = "";
		userListHTML = "";	
		referredToListHTML = "";
		filterHTML = "";
		closureCats = null;
		closureStages = null;
	}
	
	/**
	 * validates the user
	 * @param vzId - the id to be bashed against
	 * @param password - the password to be bashed against
	 * @return - 0 (failed); 1 (passed)
	 */
	public User validateUser(String vzId, String password) {	
		return drive.validateUser(vzId, password);
	}
	
	/**
	 * gets the pending tickets for the specific user
	 * @param id - the id of the user to get tickets for
	 * @param group
	 * @return - table html 
	 */
	public String getTickets(String id, String group) {
		logger.debug("getting tickets for id: " + id + "; group: " + group);
		String ticketHTML = "";
		
		logger.debug("get users? " + getUsers);
		if (getUsers) {
			DBDriver.getUsers(id);
			getUsers = false;
		}
		
		try {
			ArrayList<RocTicket> usersTickets = getUsersTickets(id);
			
			if (null != usersTickets) {
				for (RocTicket r : usersTickets) {
					ticketHTML = populateTicketHTML(r, ticketHTML, id);
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTickets: ", e);
		}
		
		return ticketHTML;
	}
	
	private ArrayList<RocTicket> getUsersTickets(String id) {
		ArrayList<RocTicket> usersTickets = null;		
		usersTickets = drive.getTickets(id, id);
		
		return usersTickets;
	}
	
	public String getUnassigned(String sessionVzId) {
		logger.debug("getting unassigned Tickets");
		String ticketHTML = "";
		logger.debug("getting unassigned Tickets");
		ticketHTML = "";
		
		try {
			for (RocTicket r: drive.getUnassigned(sessionVzId).values()) {
				ticketHTML = populateTicketHTML(r, ticketHTML, sessionVzId);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUnassgined: ", e);
			String error = Utils.stackTraceToString(e);
			DBDriver.writeToErrorLog(error, sessionVzId, 0);
		}		
		return ticketHTML;
	}
	
	/**
	 * gets all the pending tickets
	 * @param id
	 * @param group
	 * @param tickets 
	 * @return
	 */
	public String getAllTickets(String id, String group) {
		logger.debug("getting tickets for id: " + id + "; group: " + group);
		String ticketHTML = "";
		HashMap<String, RocTicket> allTicketList = drive.getAllTickets();
		
		System.out.println("get users? " + getUsers);
		if (getUsers) {
			DBDriver.getUsers(id);
			getUsers = false;
		}
			
		logger.debug("allTicketList : : : : " + allTicketList.size());
		
		try {
			for (RocTicket r: allTicketList.values()) {
				ticketHTML = populateTicketHTML(r, ticketHTML, id);
			}
		}
		catch (Exception e) {
			logger.error("Exception in getAllTickets: ", e);
			String error = Utils.stackTraceToString(e);
			DBDriver.writeToErrorLog(error, id, 0);
		}
		logger.debug("returning now");
		return ticketHTML;
	}
	
	/**
	 * populates ticketHTML for the front end
	 * @param r
	 */
	private String populateTicketHTML(RocTicket r, String html, String sessionVzId) {
		if (userList.size() < 0) {
			DBDriver.getUsers(sessionVzId);
		}
		
		if (null != userList.get(r.getVzId())) {
			html += "<tr id=\"" + r.getRocTicketId() + "\" onclick='getTicketInfo(\"" + (r.getRocTicketId()) + "\")' isclicked='false' class=\"ticket\"><td>" + r.getRocTicketId() + "</td><td>" 
					  + (null == r.getVzId() ? "" : null == userList.get(r.getVzId()).getFirstname() ? "" : userList.get(r.getVzId()).getFirstname()) + "</td><td>" + 
						(null == r.getReferredTo() ? "" : r.getReferredTo()) + "</td><td>" + 
						(null == r.getTicketCategory() ? "" : r.getTicketCategory()) + "</td><td>" + 
						(null == r.getPriority() ? "" : r.getPriority()) + "</td><td>" + (r.getSspRouteCycles()) + "</td><td>" + 
						(null == Utils.convertDateToString(r.getLastItReferredDate()) ? "" : Utils.convertDateToString(r.getLastItReferredDate())) + "</td><td>" + 
						(null == r.getTicketStatus() ? "" : r.getTicketStatus()) + "</td><td>" +
						(null == r.getOnsiteReferred() ? "" : r.getOnsiteReferred()) + "</td><td>" +
						(null == r.getReferredCategory() ? "" : r.getReferredCategory()) + "</td><td>" +
						(1 == r.getIsBusiness() ? "Y" : "N") + "</td><td>" + 
						r.getOnHoldStatus() + "</td></tr>";
		}
		else {
			html += "<tr id=\"" + r.getRocTicketId() + "\" onclick='getTicketInfo(\"" + (r.getRocTicketId()) + "\")' isclicked='false' class=\"ticket\"><td>" + r.getRocTicketId() + "</td><td>" 
					  + ( "" ) + "</td><td>" + 
						(null == r.getReferredTo() ? "" : r.getReferredTo()) + "</td><td>" + 
						(null == r.getTicketCategory() ? "" : r.getTicketCategory()) + "</td><td>" + 
						(null == r.getPriority() ? "" : r.getPriority()) + "</td><td>" + (r.getSspRouteCycles()) + "</td><td>" + 
						(null == Utils.convertDateToString(r.getLastItReferredDate()) ? "" : Utils.convertDateToString(r.getLastItReferredDate())) + "</td><td>" + 
						(null == r.getTicketStatus() ? "" : r.getTicketStatus()) + "</td><td>" +
						(null == r.getOnsiteReferred() ? "" : r.getOnsiteReferred()) + "</td><td>" +
						(null == r.getReferredCategory() ? "" : r.getReferredCategory()) + "</td><td>" +
						(1 == r.getIsBusiness() ? "Y" : "N") + "</td><td>" + 
						r.getOnHoldStatus() + "</td></tr>";
		}
		
		return html;
	}
	
	/**
	 * generates the html for the user table
	 * @param id 
	 * @return - html string
	 */
	public String getUsers(String group, String sessionVzId) {
		userHTML = "";
		DBDriver.getUsers(sessionVzId);
		
		try {
			if (userHTML.equals("")) {
				for (User u: userList.values()) {
					userHTML += "<tr onclick='getCategories(\"" + u.getVzId() + "\")'><td>" + u.getVzId() 
								+ "</td><td>" + u.getFirstname() + "</td><td>"
								+ u.getLastname() + "</td><td>" + u.getTicketsPerDay() +"</td><td>" 
								+ (u.getTicketsTaken() - u.getTicketsClosed()) + "</td><td>" + u.getTicketsClosed() + "</td>";
					
					if (group.equals("admin")) {
						if (u.isActive()) {
							userHTML += "<td><form><input type=CHECKBOX id=\"activate_" + u.getFirstname() 
									+ "\" checked onclick='doSomething(\"" + u.getVzId() + "\", \"" 
									+ u.getFirstname() + "\")'></input></form></td></tr>";
						}
						else {
							userHTML += "<td><form><input type=CHECKBOX id=\"activate_" 
									+ u.getFirstname() + "\" onclick='doSomething(\"" + u.getVzId() + "\", \"" 
									+ u.getFirstname() + "\")'></input></form></td></tr>";
						}
					}				
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in getUsers: ", e);
		}
		return userHTML;
	}
	
	public String getCompleteCountbyUser(String sessionVzId){		
		String userCountHTML="";
		
		try {
			HashMap<String,Integer> completeCount = DBDriver.getTicketCompleteCount(sessionVzId);
			Iterator<?> it = completeCount.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				String firstName = (String) pairs.getKey();
				Integer count = (Integer) pairs.getValue();
				
				logger.debug(firstName + " " + count);
	
				userCountHTML += "<tr><td>" + (null!=firstName ? firstName : "") + "</td>" 
						+ "<td>" + (null!=count.toString() ? count.toString() : "") + "</td></tr>";
			}
		}
		catch (Exception e) {
			logger.error("Exception in getCompleteCountbyUser: ", e);
		}
		return userCountHTML;
	}
	
	public String deactivate(String id, boolean chcked) {
		userList.clear();
		return drive.deactivateUser(id, chcked);
	}
		
	public String addUser(String fname, String lname, String id, String pwd,
			int tktperday, String bestcat, String ugroup, String isoffshore, String sessionVzId) {
		userList.clear();
		String newU = drive.addUser(fname, lname, id, pwd, tktperday, bestcat, ugroup, isoffshore);
		DBDriver.getUsers(sessionVzId);
		return newU;
	}
	
	
	public String getUserList(String vz_id) {
		if (userList.size() == 0) 
			DBDriver.getUsers(vz_id);
		
		if (!userListHTML.equals(""))
			userListHTML = "";
		
		try {
			for (String id: userList.keySet()) {
				if (!id.equals(vz_id) && userList.get(id).isActive())
					userListHTML += "<a onclick=showChartByUser(\"" + userList.get(id).getVzId() + "\",\"" 
							+ userList.get(id).getFirstname() + "\")>" + userList.get(id).getFirstname() + "</a></br>";
			}
		}
		catch (Exception e) {
			logger.error("Exception in getCompleteCountbyUser: ", e);
		}
		return userListHTML;
	}
	
	public void controllerUpdateTicket(RocTicketHistory tkt, String vzId) {
		drive.driverUpdateTicket(tkt, vzId, vzId);		
	}


	public String getStatuses() {	
		logger.debug("getting statuses");
		String statusOptions = "<option value=\"null\">Status Queues</option>";
		ArrayList<String> statuses = drive.getStatuses();
		for (String s: statuses) {
			statusOptions += "<option value=\"" + s + "\">" + s + "</option>";
		}
	logger.debug(statusOptions);
		return statusOptions;
	}

	public boolean updateUsersEmail(String email, String vz_id) {
		return drive.updateUsersEmail(email, vz_id, vz_id);
	}
	
	public String getReferredToList(String sessionVzId) {
		if (null == Utils.focusReferredTo)
			drive.getReferredToList(sessionVzId);
		
		if (referredToListHTML.equals("")) {
			for (String name: Utils.focusReferredTo) {
				referredToListHTML += "<option value=\"" + name + "\">" + name + "</option>";
			}
		}
		logger.debug(referredToListHTML);
		
		return referredToListHTML;
	}

	public static void setClosureCats(ArrayList<String> catsList) {		
		logger.debug("making catslist");
		closureCats = catsList;
	}

	public static ArrayList<String> getClosureCats() {
		return closureCats;
	}
	
	public static void setClosureStages(HashMap<String, HashMap<String, ArrayList<String>>> catsList) {		
		logger.debug("making catslist");
		closureStages = catsList;
	}

	public static HashMap<String, HashMap<String, ArrayList<String>>> getClosureStages() {
		return closureStages;
	}
	
	public String getTicketsAsHrefs(String vzId) {
		String ticketsAsHrefs = "";
		
		try {
			for (int tktId : drive.getTicketsDone(vzId)) {
				ticketsAsHrefs += "<tr><td class='tickets-done'>"
						+ "<a href='/AdminPage/updateTicket.html?id=" + tktId + "'>" + tktId 
						+ "</a><td><tr>";
			}
		}
		catch (Exception e) {
			logger.error("Exception in getTicketsAsHrefs: ", e);
		}
		return ticketsAsHrefs;
	}
	
	public ArrayList<ArrayList<String>> dragNDropCategories(String vzId, String sessionVzId) {
		ArrayList<ArrayList<String>> dndCats = new ArrayList<ArrayList<String>>();	
		try {
			ArrayList<String> usersCats = new ArrayList<String>();
			ArrayList<String> compareCats = new ArrayList<String>();
	 		int count = 0;
	 		String cats = "";
	 		logger.debug("getting users categories");
	 		User u = drive.getUsersInfo(vzId);
	 		if (null != u) {
				for (String cat : u.getUsersBestCategory().split(",")) {
					cats += "<div draggable=\"true\" class=\'drag-option\' ondragstart=\"drag(event)\" id=\"drag" 
							+ count +"\">" + cat +"</div>";
					count++;
					compareCats.add(cat);
				}
	 		}
	 		usersCats.add(true == u.isActive() ? "Y" : "N");
	 		usersCats.add(u.getTicketsPerDay() + "");
	 		usersCats.add(cats);
			dndCats.add(usersCats);
			dndCats.add(getCategories(compareCats, count, sessionVzId));
			logger.debug("returning categories");
		}
		catch (Exception e) {
			logger.error("Exception in dragNDropCategories: ", e);
		}
		return dndCats;
	}
	
	public ArrayList<String> getCategories(ArrayList<String> usersCats, int count, String sessionVzId) {
		ArrayList<String> newCats = new ArrayList<String>();		
		try {
			String cats = "";
			if (null == Utils.focusReferredTo) 
				drive.getReferredToList(sessionVzId);
			
			for (String cat : Utils.focusReferredTo) {
				if (!usersCats.contains(cat)) {
					cats += "<div draggable=\"true\" class=\'drag-option\' ondragstart=\"drag(event)\" id=\"drag" 
							+ count +"\">" + cat +"</div>";
					count++;
				}
			}
			newCats.add(cats);
		}
		catch (Exception e) {
			logger.error("Exception in getCategories: ", e);
		}
		return newCats;
	}
	
	/**
	 * Instantiates one controller object for the whole servlet
	 * Singleton Implementation
	 * @return the controller object
	 */
	public static synchronized Controller getInstance(){
		if(null == controller)
			controller = new Controller();
		return controller;
	}

	public boolean showBtn(String vzId) {
		return ("n".equalsIgnoreCase(userList.get(vzId).getOffShore()));
	}

	public String updateUser(NewUser u,String sessionVzId) {
		return drive.updateUser(u, sessionVzId);
	}

	public String reassignTicket(String assignTo, String assignee, String tkts, String sessionVzId) {
		return drive.reassignTickets(assignTo, assignee, tkts, sessionVzId);
 
	}

	public ArrayList<String> getVzIdsHtml(String userId) {
		ArrayList<String> users = new  ArrayList<String>();
		try {
			String htmlVDSI = "<option value=\"foo\">VDSI</option>";
			String htmlONSHORE = "<option value=\"foo\">Onshore</option>";
			
			for (String vzId : userList.keySet()) {
				if (null != vzId || !vzId.isEmpty()) {
					if ("Y".equalsIgnoreCase(userList.get(vzId).getOffShore())) {
						htmlVDSI += "<option value=\"" + vzId + "\">" + userList.get(vzId).getFirstname() +"</option>";
					}
					else {
						htmlONSHORE += "<option value=\"" + vzId + "\">" + userList.get(vzId).getFirstname() +"</option>";
					}
				}
			}
			
			users.add(htmlVDSI);
			users.add(htmlONSHORE);
		}
		catch (Exception e) {
			logger.error("Exception in getVzIdsHtml: ", e);
		}
		return users;
	}

	public String getPendingTicketsForUsers(String sessionVzId) {
		String html = "";
		try {
			ArrayList<ArrayList<String>> counts = drive.getPendingTicketsForUsers(sessionVzId);
			logger.debug(counts);
			for (int i = 0; i < counts.size(); i++) {
				html += "<tr><td>" + counts.get(i).get(0) + "</td><td>" + counts.get(i).get(1) + "</td><td>" + counts.get(i).get(2) + "</td></tr>";
			}
		}
		catch (Exception e) {
			logger.error("Exception in getPendingTicketsForUsers: ", e);
		}
		return html;
	}

	public String getCountsPerReferredTo(String sessionVzId) {
		String html = "";
		try {
			HashMap<String, Integer> counts = drive.getCountsPerReferredTo(sessionVzId);
			int total = 0;
			for (String refer : counts.keySet()) {
				html += "<tr><td>" + refer + "</td><td>" + counts.get(refer) + "</td></tr>";
				total += counts.get(refer);
			}
			html += "<tr><td><b>Total</b></td><td><b>" + total + "</b></td></tr>";
		}
		catch (Exception e) {
			logger.error("Exception in dragNDropCategories: ", e);
		}
		return html;
	}

	public String getReroutes(String sessionVzId) {
		String reroutes = "";
		ArrayList<String> routes = drive.getReroutes(sessionVzId);
		reroutes += "<option value\"null\">Reroute Queues</option>";
		
		logger.debug("routes size: " + routes.size());
		for (String status: routes) {
			reroutes += "<option value=\"" + status + "\">" + status +"</option>";
//			logger.debug(reroutes);
		}
		return reroutes;
	}
	
	private HashMap<String, String> getFilterLabelsMap() {
		LinkedHashMap<String, String> labelsMap = new LinkedHashMap<String, String>();
		
		labelsMap.put("Assigned To", "VZ_ID");
		labelsMap.put("Referred To", "REFERRED_TO");
		labelsMap.put("Category", "TICKET_CATEGORY");
		labelsMap.put("Priority", "PRIORITY");
		labelsMap.put("Status", "TICKET_STATUS");
		labelsMap.put("Onsite Referral", "ONSITE_REFERRED");
		labelsMap.put("Referral Category", "ONSITE_REFERRED_CATEGORY");
		labelsMap.put("Business", "IS_BUSINESS");
		
		return labelsMap;
	}

	public String getFilterHtml(String sessionVzId) {
		if ("".equals(filterHTML)) {
			// initial div tag
			filterHTML += "<div class=\"filter\" id=\"1\"><div class=\"allLabels\">";
			
			// populating the labels
			HashMap<String, String> labels = getFilterLabelsMap();
			for (String label : labels.keySet()) {
				filterHTML += "<div class=\"filterLabel\" id=\"" + labels.get(label) + "\">" + label + "</div>";
			}
			
			// creating the options lists
			filterHTML += "</div><div class=\"allOptions\" data-col=\"options\" name=\"options\">";
			
			// populating the options
			ArrayList<FilterOption> options = drive.getFilterOptions(labels,sessionVzId);
			for (FilterOption fo: options) {
				String idLabel =  fo.getOptionLabel().replaceAll("\\s", "");
				filterHTML += "<div class=\"filterOptions\" id=\"" + idLabel + "\"><ul>";
				for (int i = 0; i < fo.getOptionIds().size(); i++) {
					filterHTML += "<li id=\"" + fo.getOptionIds().get(i) + "\">" + fo.getLabels().get(i) + "</li>";
				}
				filterHTML += "</ul></div>";
			}
			filterHTML += "</div></div>";
			filterHTML += "<div class=\"filterSearch\">" +
							"<span id=\"filterEl\">" +
							"<label>Filter: </label><input type=\"text\"  data-col=\"options\" name=\"options\" "
							+ "class=\"filterInput\" size=\"21\" maxlength=\"120\"> (Hit enter to add/remove)" +
							"</span>" +
						  "</div>" +
						  "<div class=\"filterSearchBottom\">" +
								"<div class=\"filterBtn\">Filter</div>" +
								"<div class=\"filterText\">" +
								"<div class=\"filterBy\">Filtering by:</div>" +
								"<div class=\"filterByList\"></div>" +
						  "</div>" +
						"</div>";
		}
		return filterHTML;
	}

	public String getFilteredTickets(String clauses, String sessionVzId) {
		HashMap<String, RocTicket> tickets = drive.getFilteredTickets(clauses,sessionVzId);
		String ticketHTML = "";
		for (RocTicket r: tickets.values()) {
			ticketHTML = populateTicketHTML(r, ticketHTML, sessionVzId);
		} 
//		logger.debug("after getting filtered tickets:\n" + ticketHTML);
		return ticketHTML;
	}

	public String addClosureCodes(String codes, String sessionVzId) {
		return drive.addClosureCodes(codes,sessionVzId);
	}

	public String getHoldCodes(String sessionVzId) {
		String codesHtml = "<option value=\"null\"></option>";
		for(String code : drive.getHoldCodes(sessionVzId)) {
			codesHtml += "<option value=\"" + code + "\">" + code + "</option>";
		}
		
		return codesHtml;
	}

	public String getAllQueues(String sessionVzId) {
		String html = "<option value=\"null\">All Queues</option>";
		for (String queue: drive.getAllQueues(sessionVzId)) {
			html += "<option value=\"" + queue + "\">" + queue + "</option>";
		}
		return html;
	}

	public String removeQueue(String which, String queue, String sessionVzId) {
		String removed = drive.removeQueue(which, queue, sessionVzId);
		getReroutes(sessionVzId);
		return removed;
	}

	public String addQueue(String which, String queue, String sessionVzId) {
		String added = drive.addQueue(which, queue, sessionVzId);
		getReroutes(sessionVzId);
		return added;
	}

	public String removeClosureCodes(String codes, String sessionVzId) {
		return drive.removeClosureCodes(codes, sessionVzId);
	}
}
