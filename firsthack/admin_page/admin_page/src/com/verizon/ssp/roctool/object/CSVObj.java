package com.verizon.ssp.roctool.object;

public class CSVObj {
	private int ticket_id;
	private String comment;
	private String status;
	private String vzId;
	private int ftpId;
	
	public int getTicketId() {
		return ticket_id;
	}
	public void setTicketId(int ticketId) {
		this.ticket_id = ticketId;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getVzId() {
		return vzId;
	}
	public void setVzId(String vzId) {
		this.vzId = vzId;
	}
	public int getFtpId() {
		return ftpId;
	}
	public void setFtpId(int ftpId) {
		this.ftpId = ftpId;
	}
}
