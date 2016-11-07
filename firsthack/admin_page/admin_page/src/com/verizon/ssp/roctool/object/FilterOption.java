package com.verizon.ssp.roctool.object;

import java.util.ArrayList;

public class FilterOption {
	private String optionLabel;
	private ArrayList<String> optionId = new ArrayList<String>();
	private ArrayList<String> labels = new ArrayList<String>();
	
	public String getOptionLabel() {
		return optionLabel;
	}
	public void setOptionLabel(String optionLabel) {
		this.optionLabel = optionLabel;
	}
	
	public String getOptionId() {
		return optionLabel;
	}
	public void addToOptionIds(String optionId) {
		this.optionId.add(optionId);
	}
	
	public ArrayList<String> getOptionIds() {
		return optionId;
	}
	
	public ArrayList<String> getLabels() {
		return labels;
	}
	
	public void addToLables(String label) {
		this.labels.add(label);
	}
}
