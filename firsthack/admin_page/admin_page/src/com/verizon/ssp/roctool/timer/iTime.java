package com.verizon.ssp.roctool.timer;

public interface iTime {
	// Shows the user the time to start the daemon
	void seeStartTime();
	
	// Gets the start time from the config file for the daemon
	void getStartTime();
}	
