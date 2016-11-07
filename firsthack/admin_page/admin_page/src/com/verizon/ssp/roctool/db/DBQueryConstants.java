package com.verizon.ssp.roctool.db;

public class DBQueryConstants {

	public static final String INSERT_TICKET_INTO_MASTER_TABLE = "INSERT INTO ROC_TICKET_MASTER "
			+ "(ROC_TICKET_ID, "
			+ "VZ_ID, "
			+ "DATE_OPENED, "
			+ "FIRST_SSP_REFERRED_DATE, "
			+ "SSP_ROUTE_CYCLES, "
			+ "LAST_IT_REFERRED_DATE, "
			+ "TICKET_STATUS, "
			+ "TICKET_COMMENT, "
			+ "REFERRED_TO, "
			+ "LAST_UPDATED, "
			+ "BTN, "
			+ "ACCOUNT, "
			+ "PCAN, "
			+ "IS_BUSINESS, "
			+ "TICKET_CATEGORY, "
			+ "PRIORITY) "
			+ "VALUES (?,?,?,?,1,sysdate,?,?,?,sysdate,?,?,?,?,?,?)"; 
	
	public static final String INSERT_TICKET_INTO_ASSIGNMENT_HIST = "INSERT INTO ROC_TICKET_ASSIGNMENT_HIST (ASSIGNMENT_ID, ROC_TICKET_ID, VZ_ID, ASSIGNED_DATE, TICKET_STATUS, UPDATE_DATE) "
			   + "VALUES (ssp_dashboard.assignment_hist_seq.nextval, ?, ?, ?, ?, ?)";
	
	public static final String INSERT_USER_TAKEN_COUNT = "insert into ROC_USER_ASSIGNMENT_HIST "
			 + "(taken_count, pending_count, vz_id, assignment_date) "
			 + "values (?,?,?,sysdate)";
	
	/**
	 * inserts the new user to the user_assignment_hist table
	 */
	public static final String INSERT_USER_INTO_USER_ASSIGNMENT = "insert into roc_user_assignment_hist "
			+ "(hist_id, vz_id, pending_count, taken_count, assignment_date) "
			+ "values (ssp_dashboard.assignment_hist_seq.nextval,?,0,0,sysdate)";

	/**
	 * inserts the new user to the roc_ticket_users table
	 */
	public static final String INSERT_USER_INTO_RICKET_USERS = "Insert into SSP_DASHBOARD.ROC_TICKET_USERS (VZ_ID,FIRST_NAME, LAST_NAME, "
			+ "TICKETS_PER_DAY, IS_ACTIVE, VZ_PASSWORD,USER_GROUP, "
			+ "USERS_BEST_TICKET_CATEGORY, IS_OFFSHORE) Values (?,?,?,?,'Y',?,?,?,?)";

	/**
	 * inserts new tickets into roc_ticket_info table
	 */
	public static final String INSERT_INTO_TICKET_INFO = "Insert into roc_ticket_info (INFO_ID, "
			+ "ROC_TICKET_ID, "
			+ "BTN, "
			+ "ACCOUNT, "
			+ "APPLICATION, "
			+ "PCAN, "
			+ "CMIS_IR, "
			+ "OFFICE, "
			+ "REFERRED_TO, "
			+ "TICKET_CATEGORY,"
			+ "PRIORITY, "
			+ "TICKET_STATUS, "
			+ "DATE_OPENED, "
			+ "LAST_UPDATED, "
			+ "DATE_REFERRED, "
			+ "TICKET_COMMENT, "
			+ "IS_BUSINESS) "
			+ "Values (ssp_dashboard.roc_ticket_info_seq.nextval,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	
	public static final String INSERT_TICKET_INTO_TICKET_HISTORY = "insert into roc_ticket_history "
			+ "(HISTORY_ID, "
			+ "ROC_TICKET_ID, "
			+ "VZ_ID, "
			+ "TICKET_STATUS, "
			+ "REFERRED_TO, "
			+ "update_date) "
			 + "values(ssp_dashboard.roc_ticket_history_seq.nextval,?,?,?,?,sysdate)";

	/**
	 * updates the user's pending count (subtracts one from number)
	 */
	public static final String SUBTRACT_ONE_FROM_ROC_USER_ASSINGMENT = "update roc_user_assignment_hist set "
			+ "pending_count=((select pending_count from roc_user_assignment_hist where vz_id=? and trunc(assignment_date)=trunc(sysdate)) - 1) "
			+ "where vz_id=? and trunc(assignment_date)=trunc(sysdate)";

	/**
	 * updates the user's ticket assignment (tkt_id->vz_id)
	 */
	public static final String UPDATE_TICKET_ASSIGNMENT_HIST = "update roc_ticket_assignment_hist "
			+ "set ticket_status=?, update_date=sysdate "
			+ "where roc_ticket_id=? and vz_id=?";
	
	public static final String UPDATE_TICKET_HISTORY = "UPDATE ROC_TICKET_HISTORY "
			+ "SET "
				+ "TICKET_STATUS=?, "
				+ "CLOSURE_CAT=?, "
				+ "INTERNAL_TRACKING=?, "
				+ "ISSUE_STAGE=?, "
				+ "ORDER_STAGE=?, "
				+ "PROVISION_STAGE=?, "
				+ "BILLING_STAGE=?, "
				+ "UPDATE_DATE=sysdate, "
				+ "IS_MISDIRECT=?, "
				+ "IS_RE_ROUTE=?, "
				+ "IS_REDIRECT=?, "
				+ "RESOLUTION=?, "
				+ "USER_COMMENT=? "
			+ "WHERE HISTORY_ID=?";
	
	
	public static final String UPDATE_TICKET_IN_MASTER_TABLE = "UPDATE ROC_TICKET_MASTER "
			+ "SET SSP_ROUTE_CYCLES=(SELECT SSP_ROUTE_CYCLES FROM ROC_TICKET_MASTER WHERE ROC_TICKET_ID=?) + 1, "
			+ "TICKET_STATUS=?, "
			+ "trunc(LAST_IT_REFERRED_DATE)=trunc(sysdate) "
			+ "WHERE ROC_TICKET_ID=?";

	/**
	 * get's all the pending tickets for each user
	 */
	public static final String GET_PENDING_TICKETS_FOR_SPECIFIC_USER = "select m.roc_ticket_id, m.vz_id, m.ticket_comment, m.last_updated, m.date_opened, m.referred_to, m.priority, "
			+"m.ssp_route_cycles, m.last_it_referred_date, m.first_ssp_referred_date, m.ticket_status, m.ticket_category "
			+"from roc_ticket_master m "
			+"where m.roc_ticket_id in ( "
			  +"select rtm.roc_ticket_id "
			  +"from roc_ticket_master rtm left join ( "
			    +"select max(c.date_added) as latest_date, c.roc_ticket_id " 
			    +"from ftp_csv c "
			    +"group by c.roc_ticket_id "
			  +") d "
			  +"on rtm.roc_ticket_id=d.roc_ticket_id "
			  +"where (rtm.TICKET_STATUS not like '%losed' or rtm.TICKET_STATUS not like '%omplete' or rtm.ticket_status != 'To Test') "
			  +"and rtm.last_it_referred_date > d.latest_date "
			 +"union "
			    +"select rtm2.roc_ticket_id from roc_ticket_master rtm2 where (rtm2.TICKET_STATUS not like '%losed' and rtm2.TICKET_STATUS not like '%omplete' " 
			                                                                +"and rtm2.ticket_status != 'To Test') "
			    +"and rtm2.roc_ticket_id not in (select c.roc_ticket_id from ftp_csv c) "
			+") and m.vz_id=? and m.ticket_status != 'To Test' and m.ticket_status not like '%omplete'";

	/**
	 * get's all the pending tickets for a specific user
	 */
	public static final String 
	GET_PENDING_TICKETS_FOR_ALL_USERS = "select m.roc_ticket_id, m.vz_id, m.date_opened, m.first_ssp_referred_date," +
			"m.ssp_route_cycles, m.last_it_referred_date, m.ticket_status, m.priority, m.ticket_category, " + 
			"m.referred_to, m.last_updated, m.ticket_comment " +
			"from roc_ticket_master m " +
			"where m.roc_ticket_id in ( " +
			  "select rtm.roc_ticket_id " +
			  "from roc_ticket_master rtm left join ftp_csv c " +
			  "on (rtm.TICKET_STATUS not like '%losed' and rtm.TICKET_STATUS not like '%omplete' " +
			  "and rtm.ticket_status != 'To Test') " +
			  "and (c.roc_ticket_id is null or (rtm.roc_ticket_id = c.roc_ticket_id " +
			  "and c.date_added < rtm.last_it_referred_date) ) " +
			") " +
			"and m.vz_id is not null " +
			"and m.ticket_status != 'To Test' " +
			"and m.ticket_status not like '%omplete' " +
			"order by m.vz_id";

	/**
	 * gets all the tickets that are to be reassigned
	 */
	public static final String GET_TICKETS_TO_REASSIGN = "select m.roc_ticket_id, m.priority, m.referred_to, m.ticket_category, a.vz_id "
			+ "from roct_ticket_master m, roc_ticket_assignment_hist a where m.roc_ticket_id in ( "
				+ "select roc_ticket_id from roc_ticket_history "
				+ "where ssp_route_cycles > 1 "
				+ "and last_it_referred_date > sysdate "
			+ ") and m.roc_ticket_id=a.roc_ticket_id and a.ticket_status like '%losed' or a.ticket_status like '%omplete' and a.ticket_status != m.ticket_status";
	
	public static final String GET_DEACTIVATED_USERS_TICKETS = "SELECT * "
			+ "FROM ROC_TICKET_MASTER "
			+ "WHERE VZ_ID=? "
			+ "AND TICKET_STATUS NOT LIKE '%omplete' "
			+ "AND TICKET_STATUS NOT LIKE '%losed' "
			+ "AND TICKET_STTUS NOT LIKE '%o Test'";
	
	public static final String GET_UNASSIGNED_TICKETS = "select * "
			+ "from roc_ticket_info "
			+ "where roc_ticket_id not in ( "
			  + "select roc_ticket_id "
			  + "from roc_ticket_assignment_hist "
		    + ") and (ticket_status not like '%omplete' and ticket_status not like '%losed' and ticket_status not like 'To Test') and is_business=1";
	
	public static final String GET_USERS_THAT_CAN_TAKE_MORE_TICKETS = "select u.*, uah.pending_count, uah.taken_count "
			 + "from roc_ticket_users u, roc_user_assignment_hist uah "
			 + "where uah.vz_id=u.vz_id "
			 + "and trunc(assignment_date)=trunc(sysdate) and u.is_active != 'N'";

	
	public static final String UPDATE_USERS_TAKEN_COUNT = "update ROC_USER_ASSIGNMENT_HIST "
			 + "set taken_count=?, "
			 + "pending_count=? "
			 + "where vz_id=? "
			 + "and trunc(assignment_date)=trunc(sysdate)";
}