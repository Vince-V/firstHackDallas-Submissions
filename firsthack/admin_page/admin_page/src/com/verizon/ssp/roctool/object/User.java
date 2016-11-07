package com.verizon.ssp.roctool.object;

public class User {
	private String firstname;
	private String lastname;
	private String vzId;
	private String role;
	private String usersBestCategory;
	private String password;
	private String offShore;
	private int usergroup;
	private int ticketsPerDay;
	private int ticketsTaken;
	private int ticketsPending;
	private int ticketsClosed;
	private boolean isActive;
	
	public User() {}
	
	
	public String getOffShore() {
		return offShore;
	}
	public void setOffShore(String offShore) {
		this.offShore = offShore;
	}
	public String getFirstname() {
		return firstname;
	}
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}	
	
	public int getTicketsPending() {
		return ticketsPending;
	}
	public void setTicketsPending(int tickets_pending) {
		this.ticketsPending = tickets_pending;
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
	public void setVzId(String user_id) {
		this.vzId = user_id;
	}
	public int getUsergroup() {
		return usergroup;
	}
	public void setUsergroup(int privlages) {
		this.usergroup = privlages;
	}
	public int getTicketsPerDay() {
		return ticketsPerDay;
	}
	public void setTicketsPerDay(int tickets_per_day) {
		this.ticketsPerDay = tickets_per_day;
	}
		
	public int getTicketsTaken() {
		return ticketsTaken;
	}
	public void setTicketsTaken(int taken3) {
		this.ticketsTaken = taken3;
	}	
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}	
	public int getTicketsClosed() {
		return ticketsClosed;
	}
	public void setTicketsClosed(int tickets_closed) {
		this.ticketsClosed = tickets_closed;
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

	@Override
	public String toString() {
		return "User [vzid=" + vzId + "]";
	}
	
	
}
