package com.verizon.ssp.roctool.helper;

import java.util.ArrayList;
import java.util.HashMap;

import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.object.RocTicket;

public class EnsureFtpHelper {
	public void checkEachTablesStatus(ArrayList<RocTicket> ticketsFromRocDB,
			HashMap<Integer, RocTicket> allTicketIds) {
		ArrayList<Integer> ticketsToResend = new ArrayList<Integer>();
		
		for (RocTicket rocTkt: ticketsFromRocDB) {
			String rocStatus = rocTkt.getTicketStatus();
			String ftpStatus = DBDriver.getFTPStatus(rocTkt.getRocTicketId());
			String masterStatus = allTicketIds.get(rocTkt.getRocTicketId()).getTicketStatus(); 
			
			if (null != ftpStatus) {
				if (!ftpStatus.equals(rocStatus)) {
					if (rocStatus.equals(masterStatus))
						ticketsToResend.add(rocTkt.getRocTicketId());
				}
			}
		}
		
		resendTickets(ticketsToResend);
	}

	private void resendTickets(ArrayList<Integer> ticketsToResend) {
		DBDriver.resendTickets(ticketsToResend, "System");		
	}
}
