package com.verizon.ssp.roctool.controller;

import java.util.ArrayList;

import com.verizon.ssp.roctool.db.DBDriver;
public class ClosureCodesController {
	private static ClosureCodesController ccController = null;
	private DBDriver drive;
	
	public ClosureCodesController() {
		drive = new DBDriver();
	}
	
	public String getClosureCategories() {
		ArrayList<String> categories = drive.getClosureCategories();
		String cats = "<option value=\"\"></option>";
		for (String cat: categories) {
			cats += "<option value=\"" + cat + "\">" + cat + "</option>";
		}
		return cats;
	}

	public String getTrackingCategories(String sessionVzId) {
		ArrayList<String> tracks = drive.getTracking(sessionVzId);		
		String tra = "<option value=\"\"></option>";
		for (String track: tracks) {
			tra += "<option value=\"" + track + "\">" + track + "</option>";
		}
		return tra;
	}
	
	public String getIssues(int track, String sessionVzId) {
		ArrayList<String> issues = drive.getIssues(track,sessionVzId);
		String ish = populateClosureCodes(issues, 4);
		return ish;
	}

	public String getOrdering(int track, String sessionVzId) {
		ArrayList<String> orderings = drive.getOrderings(track,sessionVzId);
		String ord = populateClosureCodes(orderings, 1);
		return ord;
	}

	public String getBilling(int track, String sessionVzId) {
		ArrayList<String> billings = drive.getBillings(track,sessionVzId);
		String bill = populateClosureCodes(billings, 2);
		return bill;
	}

	public String getProvisioning(int track, String sessionVzId) {
		ArrayList<String> provisionings = drive.getProvisionings(track,sessionVzId);
		String prov = populateClosureCodes(provisionings, 3);
		return prov;
	}
	
	private String populateClosureCodes(ArrayList<String> codes, int whichOne) {
		String options = "<option value=\"\"></option>";		
		if (null != codes) {
			for (String code: codes) {
				options += "<option value=\"" + code + "\">" + code + "</option>";
			}			
			options += "<option value=\"add new\"'>Add New</option>";
			
		}
		return options;
	}
	
	/**
	 * Instantiates one controller object for the whole servlet
	 * Singleton Implementation
	 * @return the controller object
	 */
	public static synchronized ClosureCodesController getInstance(){
		if(null == ccController)
			ccController = new ClosureCodesController();
		return ccController;
	}
}
