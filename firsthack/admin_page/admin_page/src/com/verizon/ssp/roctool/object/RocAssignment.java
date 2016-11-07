package com.verizon.ssp.roctool.object;

import com.verizon.ssp.roctool.object.RocTicket;

// Inherited RocTicket class
public class RocAssignment extends RocTicket {
	private static final long serialVersionUID = 1L;
	private String vzId;
	private java.sql.Date assigned_date;
	private String ticketStatus;
	private java.sql.Date updateDate;

	public String getVzId() {
		return vzId;
	}
	public void setVzId(String vz_id) {
		this.vzId = vz_id;
	}
	public java.sql.Date getAssignedDate() {
		return assigned_date;
	}
	public void setAssignedDate(java.sql.Date assigned_date) {
		this.assigned_date = assigned_date;
	}
	public String getTicketStatus() {
		return ticketStatus;
	}
	public void setTicketStatus(String ticket_status) {
		this.ticketStatus = ticket_status;
	}
	public java.sql.Date getUpdateDate() {
		return updateDate;
	}
	public void setUpdateDate(java.sql.Date update_date) {
		this.updateDate = update_date;
	}
	
	@Override
	public String toString() {
		return "RocAssignment [vz_id=" + vzId
				+ ", assigned_date=" + assigned_date + ", ticket_status="
				+ ticketStatus + ", update_date=" + updateDate + "]";
	}
}
