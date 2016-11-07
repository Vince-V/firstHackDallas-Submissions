package com.verizon.ssp.roctool.servlet;

import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.controller.ClosureCodesController;
import com.verizon.ssp.roctool.controller.Controller;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.object.NewUser;
import com.verizon.ssp.roctool.object.RocTicket;
import com.verizon.ssp.roctool.object.RocTicketHistory;
import com.verizon.ssp.roctool.object.Signature;
import com.verizon.ssp.roctool.object.User;

@Path("/servlet")
public class ROCServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Controller controller = Controller.getInstance();
	private ClosureCodesController ccController = ClosureCodesController.getInstance();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	@Context 
	private HttpServletRequest request;
	
	@Context 
	private HttpServletResponse response;
	
	@GET
	@Path("/validateUser/{vzId}-{password}")
	@Produces(MediaType.TEXT_PLAIN)
	public String validateUser(@PathParam("vzId") String vzId, @PathParam("password") String pw) {
		logger.debug("id: " + vzId + " pw: " + pw);
		response.addHeader("Cache-Control", "no-cache");
		
		User u = controller.validateUser(vzId, pw);
		logger.debug(u);
		
		if (null != u) {
			if (u.getVzId().equals(vzId) && u.getPassword().equals(pw)) {
				HttpSession session = request.getSession();
				session.setAttribute("vzId", vzId);
				session.setAttribute("loggedIn", true);
				logger.debug("the group of the user is this right here -> " + u.getRole());
				session.setAttribute("userGroup", u.getRole());
				return "1";
			}
			else
				return "0";
		}
		else {
			return "0";
		}
	}
	
	@GET
	@Path("/getStatuses")
	@Produces(MediaType.TEXT_HTML)
	public String getStatuses() {
		logger.debug("getting statuses");
		return controller.getStatuses();
	}
	
	@GET
	@Path("/getTicket={ticketId}")
	@Produces(MediaType.APPLICATION_JSON)
	public RocTicket getTicket(@PathParam("ticketId") int tktId) {
		logger.debug("tktId: " + tktId);
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
		response.addHeader("Cache-Control", "no-cache");
		
		if (null != session) {
			if (null != vzId) {
				boolean showBtn = controller.showBtn(vzId);
				RocTicket r = DBDriver.getTicket(tktId, showBtn, null, vzId);			
				logger.debug(r);			
				return r;
			}
		}
		return null;
	}
	
	@GET
	@Path("/getTickets")
	@Produces(MediaType.TEXT_HTML)
	public String getTickets() {		
		HttpSession session = request.getSession(false);
		response.addHeader("Cache-Control", "no-cache");
		if (null == session)
			return "1337";
		
		String id = (String) session.getAttribute("vzId");
		String group = (String) session.getAttribute("userGroup");
		
		logger.debug("vzId: " + id + " group: " + group);
		logger.debug("getting tickets");
		String passer = controller.getTickets(id, group);
		logger.debug("after getting tickets");
		
		return passer;
	}
	
	@GET
	@Path("/getusergroup")
	@Produces(MediaType.TEXT_HTML)
	public String userGroup() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
	
		return (String) session.getAttribute("userGroup"); 
	}
	
	@GET
	@Path("/getAllTickets")
	@Produces(MediaType.TEXT_HTML)
	public String getAllTickets() {
		HttpSession session = request.getSession(false);
		response.addHeader("Cache-Control", "no-cache");
		if (null == session)
			return "1337";
		
		String id = (String) session.getAttribute("vzId");
		String group = (String) session.getAttribute("userGroup");
		
		logger.debug("vzId: " + id + " group: " + group);
		return controller.getAllTickets(id, group);
	}
	
	@POST
	@Path("/addUser")
	public String addUser(NewUser u) {
		logger.debug("adding new user to db: "  + u);
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
		return controller.addUser(u.getFirstname(), u.getLastname(), u.getVzId(), u.getPassword(), 
				Integer.parseInt(u.getTicketsPerDay()), u.getUsersBestCategory(), u.getRole(), u.getIsOffshore(), vzId);
	}

	@GET
	@Path("/getTicketCount")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<String> getTicketCountByUser() {
		HttpSession session = request.getSession(false);
		ArrayList<String> htmls = new ArrayList<String>();
		response.addHeader("Cache-Control", "no-cache");
		if (null == session) {
			htmls.add("1337");
		}
		else {		
			String vzId = (String) session.getAttribute("vzId");
			htmls.add("<tbody>" + controller.getCompleteCountbyUser(vzId) + "</tbody>");
			htmls.add("<tbody>" + controller.getPendingTicketsForUsers(vzId) + "</tbody>");
			htmls.add("<tbody>" + controller.getCountsPerReferredTo(vzId) + "</tbody>");
		}
		
		return htmls;
	}	
	
	@POST
	@Path("/updateTicket")
	@Produces(MediaType.TEXT_HTML)
	public String updateTicket(RocTicketHistory hist) {
		logger.debug("updating ticket: " + hist);
		
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
				
		controller.controllerUpdateTicket(hist, vzId);
		
		return "done";
	}
	
	@GET
	@Path("/getRerouteMap")
	@Produces(MediaType.TEXT_HTML)
	public String rerouteMap() {
		HttpSession session = request.getSession(false);
		logger.debug("getting reroute map");
		if (null == session)
			return "1337";
		String vzId = (String) session.getAttribute("vzId");
		return controller.getReroutes(vzId);
	}
	
	@POST
	@Path("/updateSignature")
	public void updateSignature(Signature sig) {
		HttpSession session = request.getSession(false);
		String vzId=(String) session.getAttribute("vzId");
		
		logger.debug("updating signature to: " + sig);
		DBDriver.updateSignature(sig, vzId);
	}
	
	@GET
	@Path("/getRole")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<String> getRole() {
		HttpSession session = request.getSession(false);
		ArrayList<String> role = new ArrayList<String>();
		response.addHeader("Cache-Control", "no-cache");
		if (null != session)
			role.add((String) session.getAttribute("userGroup"));	
		
		String pending = "-1";		
		if (null != Controller.userList.get((String) session.getAttribute("vzId")) &&
				null != Controller.userticketList) {
			pending = Controller.userticketList.size() + "";
		}
		
		String name = "-1";
		if (null != Controller.userList.get((String) session.getAttribute("vzId"))) 
			name = Controller.userList.get((String) session.getAttribute("vzId")).getFirstname();
		
		role.add(pending);
		role.add(name);
		
		logger.debug(pending);
		
		
		return role;
	}
	
	@GET
	@Path("/getClosureCategories")
	@Produces(MediaType.TEXT_HTML)
	public String getClosureCategories() {
		logger.debug("getting closure cats");
		response.addHeader("Cache-Control", "no-cache");
		return ccController.getClosureCategories();
	}
	
	@GET
	@Path("/getTracking")
	@Produces(MediaType.TEXT_HTML)
	public String getTrackingCategories() {
		logger.debug("inside tracking");
		HttpSession session = request.getSession(false);
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return ccController.getTrackingCategories(vzId);
	}
	
	@GET
	@Path("/getIssue/{track}")
	@Produces(MediaType.TEXT_HTML)
	public String getIssues(@PathParam("track") int track) {
		logger.debug("track is: " + track);
		
		HttpSession session = request.getSession(false);
		session.setAttribute("trackingCat", track);
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return ccController.getIssues(track,vzId);
	}
	
	@GET
	@Path("/getOrdering")
	@Produces(MediaType.TEXT_HTML)
	public String getOrdering() {
		logger.debug("getting ordering");
		HttpSession session = request.getSession(false);
		Integer track = (Integer) session.getAttribute("trackingCat");
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return ccController.getOrdering(track,vzId);
	}
	
	@GET
	@Path("/getBilling")
	@Produces(MediaType.TEXT_HTML)
	public String getBilling() {
		HttpSession session = request.getSession(false);
		Integer track = (Integer) session.getAttribute("trackingCat");
		response.addHeader("Cache-Control", "no-cache");
		logger.debug("track is : " + track);

		String vzId = (String) session.getAttribute("vzId");
		return ccController.getBilling(track,vzId);
	}
	
	@GET
	@Path("/getProv")
	@Produces(MediaType.TEXT_HTML)
	public String getProvisioning() {
		HttpSession session = request.getSession(false);
		int track = (Integer) session.getAttribute("trackingCat");
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return ccController.getProvisioning(track,vzId);
	}
	
	@GET
	@Path("/getUsers")
	@Produces(MediaType.TEXT_HTML)
	public String getUsers() {
		logger.debug("Getting all users");
		HttpSession session = request.getSession(false);
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return controller.getUsers((String) session.getAttribute("userGroup"), vzId);
	}
	
	@GET
	@Path("/getName")
	@Produces(MediaType.TEXT_HTML)
	public String getName() {
		logger.debug("Getting name");
		HttpSession session = request.getSession(false);
		
		return DBDriver.getCommentSignature((String) session.getAttribute("vzId"));
	}
	
	@GET
	@Path("/getEmail")
	@Produces(MediaType.TEXT_HTML)
	public String getEmail() {
		logger.debug("Getting name");
		HttpSession session = request.getSession(false);
		
		return DBDriver.getEmail((String) session.getAttribute("vzId"));
	}
	
	@GET
	@Path("/deactivateUser/{vzId}-{checked}")
	@Produces(MediaType.TEXT_HTML)
	public String deactivateUser(@PathParam("vzId") String id, @PathParam("checked") boolean chcked) {
		logger.debug("Trying to deactivate user " + id + " " + chcked);
		
		String foo = controller.deactivate(id, chcked);

		logger.debug(foo);
		return foo;
	}
	
	
	@GET
	@Path("/signout")
	@Produces(MediaType.TEXT_HTML)
	public String signOut() {
		HttpSession session = request.getSession(false);		
		logger.debug("signing " + session.getAttribute("vzId") + " out");
		session.removeAttribute("vzId");
		session.setAttribute("loggedIn", false);
		session.removeAttribute("trackingCat");
		session.removeAttribute("userGroup");	
		session.invalidate();
		
		response.addHeader("Cache-Control", "no-cache");
		return "";
	}
	
	@GET
	@Path("/getUserList/{vzId}")
	@Produces(MediaType.TEXT_HTML)
	public String getUserList(@PathParam("vzId") String vzId) {
		logger.debug("getting user list");
		response.addHeader("Cache-Control", "no-cache");
		return controller.getUserList(vzId);
	}
	
	@GET
	@Path("/updateUsersEmail/{email}")
	@Produces(MediaType.APPLICATION_JSON)
	public String updateUsersEmail(@PathParam("email") String email) {
		logger.debug("email: " + email);
		HttpSession session = request.getSession(false);
		boolean updated = controller.updateUsersEmail(email, (String) session.getAttribute("vzId")); 
		
		String updtd = "false";
		if (updated)
			updtd = "true";
		
		return updtd;
	}
	
	@GET
	@Path("/getReferredToList")
	@Produces(MediaType.TEXT_HTML)
	public String getReferredToList() {
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
		return controller.getReferredToList(vzId);
	}
	
	@GET
	@Path("/showUnmaskedCheckbox")
	@Produces(MediaType.TEXT_PLAIN)
	public String showUnmaskedCheckbox() {
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
		return controller.showBtn(vzId) ? "true" : "false";
	}
	
	@GET
	@Path("/getTicketsAsLinks")
	@Produces(MediaType.TEXT_HTML)
	public String getTicketsAsLinks() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return null;
		String vzId = (String) session.getAttribute("vzId");
		String tickets = controller.getTicketsAsHrefs(vzId);
		return tickets;
	}
	
	@GET
	@Path("/getDNDCats/{vzId}")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<ArrayList<String>> getDNDCategories(@PathParam("vzId") String vzId) {
		HttpSession session = request.getSession(false);
		logger.debug(vzId);
		if (null == session)
			return null;
		String group = (String) session.getAttribute("userGroup");
		ArrayList<ArrayList<String>> dndCats = null;
		logger.debug(group);
		
		if (group.equals("admin"))
			dndCats = controller.dragNDropCategories(vzId, vzId);
		
		return dndCats;
	}
	
	@POST
	@Path("/updateUser")
	public String updateUser(NewUser u) {
		logger.debug("updateing user in db: "  + u);
		HttpSession session = request.getSession(false);
		String vzId = (String) session.getAttribute("vzId");
		return controller.updateUser(u, vzId);
	}
	
	@POST
	@Path("/manualAssignment/{vzId}/{tkts}")
	public String updateUser(@PathParam("vzId") String assignTo, @PathParam("tkts") String tkts) {
		HttpSession session = request.getSession(false);
		String assignee = (String) session.getAttribute("vzId");
		logger.debug("assigning tkts: " + tkts + "\nto: " + assignTo + " by: "  + assignee);
		return controller.reassignTicket(assignTo, assignee, tkts, assignee);
	}
	
	@POST
	@Path("/getVzIdsForFooter")
	@Produces(MediaType.APPLICATION_JSON)
	public ArrayList<String> getVzIdsForFooter() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return null;
		return controller.getVzIdsHtml((String) session.getAttribute("vzId"));
	}
	
	@POST
	@Path("/allUnassignedTickets")
	public String getUnassignedTickets() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return null;
		response.addHeader("Cache-Control", "no-cache");
		String vzId = (String) session.getAttribute("vzId");
		return controller.getUnassigned(vzId);
	}
	
	@POST
	@Path("/getFilterHtml")
	public String getFilterHtml() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		String vzId = (String) session.getAttribute("vzId");
		return controller.getFilterHtml(vzId);
	}
	
	/**
	 * should get the object from the front end.
	 * @return
	 */
	@POST
	@Path("/doFilter")
	@Produces(MediaType.TEXT_HTML)
	public String doFilter(String clauses) {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		String[] splitted = clauses.split("`");
		
		if (clauses.contains("home.html") && splitted.length == 1) {
			return getTickets();
		}
		else if (clauses.contains("allTickets.html") && splitted.length == 1) {
			return getAllTickets();
		}
		
		logger.debug(clauses);
		String vzId = (String) session.getAttribute("vzId");
		String html = controller.getFilteredTickets(clauses, vzId); 
		logger.debug("in dofilter method:\n" + html);
		return html;
	}
	
	@POST
	@Path("/addClosureCodes")
	@Produces(MediaType.TEXT_HTML)
	public String addClosureCodes(String codes) {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		String vzId = (String) session.getAttribute("vzId");
		String added = controller.addClosureCodes(codes,vzId);
		
		return added;
	}
	
	@POST
	@Path("/removeClosureCodes")
	@Produces(MediaType.TEXT_HTML)
	public String removeClosureCodes(String codes) {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		String vzId = (String) session.getAttribute("vzId");
		String added = controller.removeClosureCodes(codes,vzId);
		
		return added;
	}
	
	@POST
	@Path("/getHoldCodes")
	@Produces(MediaType.TEXT_HTML)
	public String getHoldCodes() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		String vzId = (String) session.getAttribute("vzId");
		return controller.getHoldCodes(vzId);
	}
	
	@POST
	@Path("/getAllQueueOptions")
	@Produces(MediaType.TEXT_HTML)
	public String getAllQueues() {
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		
		String vzId = (String) session.getAttribute("vzId");
		return controller.getAllQueues(vzId);
	}
	
	/**
	 * remove the queue
	 * @return
	 */
	@POST
	@Path("/removeQueue/{which}")
	@Produces(MediaType.TEXT_HTML)
	public String removeQueue(String queue, @PathParam("which") String which) {
		logger.debug("Removing the " + which + " queue: " + queue);
		
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		
		String vzId = (String) session.getAttribute("vzId");
		String html = controller.removeQueue(which, queue, vzId);
		return html;
	}
	
	/**
	 * add the queue
	 * @return
	 */
	@POST
	@Path("/addQueue/{which}")
	@Produces(MediaType.TEXT_HTML)
	public String addQueue(String queue, @PathParam("which") String which) {
		logger.debug("Adding the " + which + " queue: " + queue);
		
		HttpSession session = request.getSession(false);
		if (null == session)
			return "1337";
		
		String vzId = (String) session.getAttribute("vzId");
		String html = controller.addQueue(which, queue, vzId);
		return html;
	}
	
	@GET
	@Path("/test")
	@Produces(MediaType.TEXT_PLAIN)
	public String Test() {
		return "Hello World!!!";
	}
}