package com.verizon.ssp.roctool.object;

import java.io.Serializable;
import java.sql.Date;

public class RocTicket implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int rocTicketId;
	private int sspRouteCycles;
	private int isBusiness;
	private int historyId;
	private String referredTo;
	private String ticketCategory;
	private String priority;
	private String ticketStatus;
	private String btn;
	private String account;
	private String pcan;
	private String cmisIR;
	private String office;
	private String application;
	private String comment;
	private String userComment;
	private String vzId;
	private String onsiteReferred;
	private String referredCategory;
	private String onHoldStatus;
	private String maskedComment;
	private Date rocDateReferred;
	private Date dateOpened;
	private Date firstSspReferreDate;
	private Date lastItReferredDate;
	private Date lastUpdated;

	
	
	public int getHistoryId() {
		return historyId;
	}
	public void setHistoryId(int historyId) {
		this.historyId = historyId;
	}
	public String getOnHoldStatus() {
		return onHoldStatus;
	}
	public void setOnHoldStatus(String onHold) {
		this.onHoldStatus = onHold;
	}
	public String getMaskedComment() {
		return maskedComment;
	}
	public void setMaskedComment(String maskedComment) {
		this.maskedComment = maskedComment;
	}
	public int getRocTicketId() {
		return rocTicketId;
	}
	public void setRocTicketId(int roc_ticket_id) {
		this.rocTicketId = roc_ticket_id;
	}
	public Date getRocDateReferred() {
		return rocDateReferred;
	}
	public void setRocDateReferred(Date roc_date_referred) {
		this.rocDateReferred = roc_date_referred;
	}
	public String getReferredTo() {
		return referredTo;
	}
	public void setReferredTo(String reffered_to) {
		this.referredTo = reffered_to;
	}
	public String getTicketCategory() {
		return ticketCategory;
	}
	public void setTicketCategory(String ticket_category) {
		this.ticketCategory = ticket_category;
	}
	public String getPriority() {
		return priority;
	}
	public void setPriority(String priority) {
		this.priority = priority;
	}
	
	public String getTicketStatus() {
		return ticketStatus;
	}
	public void setTicketStatus(String status) {
		this.ticketStatus = status;
	}
	
	
	public String getVzId() {
		return vzId;
	}
	public void setVzId(String vz_id) {
		this.vzId = vz_id;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String clobAsString) {
		this.comment = clobAsString;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public Date getDateOpened() {
		return dateOpened;
	}
	public void setDateOpened(Date date_opened) {
		this.dateOpened = date_opened;
	}
	public Date getFirstSspReferredDate() {
		return firstSspReferreDate;
	}
	public void setFirstSspReferredDate(Date first_ssp_referred_date) {
		this.firstSspReferreDate = first_ssp_referred_date;
	}
	public Date getLastItReferredDate() {
		return lastItReferredDate;
	}
	public void setLastItReferredDate(Date last_it_referred_date) {
		this.lastItReferredDate = last_it_referred_date;
	}
	public int getSspRouteCycles() {
		return sspRouteCycles;
	}
	public void setSspRouteCycles(int ssp_route_cycles) {
		this.sspRouteCycles = ssp_route_cycles;
	}
	
	public String getUserComment() {
		return userComment;
	}
	public void setUserComment(String user_comment) {
		this.userComment = user_comment;
	}
	public Date getLastUpdated() {
		return lastUpdated;
	}
	public void setLastUpdated(Date last_updated) {
		this.lastUpdated = last_updated;
	}
	public int getIsBusiness() {
		return isBusiness;
	}
	public void setIsBusiness(int is_business) {
		this.isBusiness = is_business;
	}
		
	
	public String getBtn() {
		return btn;
	}
	public void setBtn(String string) {
		this.btn = string;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getPcan() {
		return pcan;
	}
	public void setPcan(String pcan) {
		this.pcan = pcan;
	}
	public String getCmisIR() {
		return cmisIR;
	}
	public void setCmisIR(String cmisIR) {
		this.cmisIR = cmisIR;
	}
	public String getOffice() {
		return office;
	}
	public void setOffice(String office) {
		this.office = office;
	}
	
	public String getOnsiteReferred() {
		return onsiteReferred;
	}
	public void setOnsiteReferred(String onsiteReferred) {
		this.onsiteReferred = onsiteReferred;
	}
	public String getReferredCategory() {
		return referredCategory;
	}
	public void setReferredCategory(String referredCategory) {
		this.referredCategory = referredCategory;
	}
	public boolean equals(RocTicket r) {
		return  getTicketCategory().equals(r.getTicketCategory())	
				&& ( getTicketStatus().equals(r.getTicketStatus()) || getTicketStatus().equals("On Hold") )
				&& getLastItReferredDate() == r.getLastItReferredDate()
				&& getReferredTo().equals(r.getReferredTo());
	}
	
	@Override
	public String toString() {
		return "RocTicket [roc_ticket_id=" + rocTicketId + ", referred_to="
				+ referredTo + ", ticket_category=" + ticketCategory
				+ ", priority=" + priority + ", status=" + ticketStatus
				+ ", date_opened=" + dateOpened + ", first_ssp_referred_date="
				+ firstSspReferreDate + ", last_it_referred_date="
				+ lastItReferredDate + ", ssp_route_cycles="
				+ sspRouteCycles + ", last_updated=" + lastUpdated
				+ ", is_business=" + isBusiness + ", btn=" + btn
				+ ", account=" + account + ", pcan=" + pcan + ", cmisIR="
				+ cmisIR + ", office=" + office + ", application="
				+ application + ", user_comment="
				+ userComment + ", vz_id=" + vzId + ", roc_date_referred="
				+ rocDateReferred + "]";
	}	
}