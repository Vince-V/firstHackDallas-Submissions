package com.verizon.ssp.roctool.object;

public class NewUser {
	private String firstname;
	private String lastname;
	private String vzId;
	private String role;
	private String usersBestCategory;
	private String password;
	private String usergroup;
	private String ticketsPerDay;
	private String ticketsTaken;
	private String ticketsPending;
	private String ticketsClosed;
	private String isActive;
	private String isOffshore;
	
	NewUser() {}
	
	
	public String getIsOffshore() {
		return isOffshore;
	}
	public void setIsOffshore(String isOffshore) {
		this.isOffshore = isOffshore;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getVzId() {
		return vzId;
	}
	public void setVzId(String vz_id) {
		this.vzId = vz_id;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getUsersBestCategory() {
		return usersBestCategory;
	}
	public void setUsersBestCategory(String users_best_category) {
		this.usersBestCategory = users_best_category;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsergroup() {
		return usergroup;
	}
	public void setUsergroup(String usergroup) {
		this.usergroup = usergroup;
	}
	public String getTicketsPerDay() {
		return ticketsPerDay;
	}
	public void setTicketsPerDay(String tickets_per_day) {
		this.ticketsPerDay = tickets_per_day;
	}
	public String getTicketsTaken() {
		return ticketsTaken;
	}
	public void setTicketsTaken(String tickets_taken) {
		this.ticketsTaken = tickets_taken;
	}
	public String getTicketsPending() {
		return ticketsPending;
	}
	public void setTicketsPending(String tickets_pending) {
		this.ticketsPending = tickets_pending;
	}
	public String getTicketsClosed() {
		return ticketsClosed;
	}
	public void setTicketsClosed(String tickets_closed) {
		this.ticketsClosed = tickets_closed;
	}
	public String getIsActive() {
		return isActive;
	}
	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}
	@Override
	public String toString() {
		return "NewUser [firstname=" + firstname + ", lastname=" + lastname
				+ ", vz_id=" + vzId + ", role=" + role
				+ ", users_best_category=" + usersBestCategory
				+ ", password=" + password + ", usergroup=" + usergroup
				+ ", tickets_per_day=" + ticketsPerDay + ", tickets_taken="
				+ ticketsTaken + ", tickets_pending=" + ticketsPending
				+ ", tickets_closed=" + ticketsClosed + ", isActive="
				+ isActive + "]";
	}
}