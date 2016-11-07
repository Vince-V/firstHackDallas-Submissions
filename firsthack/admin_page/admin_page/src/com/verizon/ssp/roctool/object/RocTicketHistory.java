package com.verizon.ssp.roctool.object;

public class RocTicketHistory {
	private String id;
	private String status;
	private String cat;
	private String track;
	private String issue;
	private String ordering;
	private String prov;
	private String billing;
	private String misdirect;
	private String reroute;
	private String redirect;
	private String resolution;
	private String comment;
	private String referredTo;
	private String onSiteReferral;
	private String onHoldStatus;
		
	
	public RocTicketHistory() {
	}
	
	
	public String getOnHoldStatus() {
		return onHoldStatus;
	}
	public void setOnHoldStatus(String onHold) {
		this.onHoldStatus = onHold;
	}
	public String getReferredTo() {
		return referredTo;
	}
	public void setReferredTo(String referredTo) {
		this.referredTo = referredTo;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getCat() {
		return cat;
	}
	public void setCat(String cat) {
		this.cat = cat;
	}
	public String getTrack() {
		return track;
	}
	public void setTrack(String track) {
		this.track = track;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getOrdering() {
		return ordering;
	}
	public void setOrdering(String ordering) {
		this.ordering = ordering;
	}
	public String getProv() {
		return prov;
	}
	public void setProv(String prov) {
		this.prov = prov;
	}
	public String getBilling() {
		return billing;
	}
	public void setBilling(String billing) {
		this.billing = billing;
	}
	public String getMisdirect() {
		return misdirect;
	}
	public void setMisdirect(String misdirect) {
		this.misdirect = misdirect;
	}
	public String getReroute() {
		return reroute;
	}
	public void setReroute(String reroute) {
		this.reroute = reroute;
	}
	public String getRedirect() {
		return redirect;
	}
	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	public String getOnSiteReferral() {
		return onSiteReferral;
	}
	public void setOnSiteReferral(String onSiteReferral) {
		this.onSiteReferral = onSiteReferral;
	}


	@Override
	public String toString() {
		return "RocTicketHistory [id=" + id + ", status=" + status + ", cat="
				+ cat + ", track=" + track + ", issue=" + issue + ", ordering="
				+ ordering + ", prov=" + prov + ", billing=" + billing
				+ ", misdirect=" + misdirect + ", reroute=" + reroute
				+ ", redirect=" + redirect + ", resolution=" + resolution
				+ ", comment=" + comment + "]";
	}
}
