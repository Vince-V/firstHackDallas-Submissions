package com.verizon.ssp.roctool.common;

import java.util.regex.Pattern;

import com.verizon.ssp.util.Logger;

public class TextMasking {
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	
	public static String maskText(String comment) {
		String[] commentSplit = comment.split(Pattern.quote("\n"));
		String maskedComment = "";
		for (String str : commentSplit) {
			if (str.contains(Constants.TELEPHONE_NUMBER_1)) {
				maskedComment = maskTelephoneNumber(maskedComment, str, Constants.TELEPHONE_NUMBER_1);
				continue;
			}
			else if (str.contains(Constants.TELEPHONE_NUMBER_2)) {
				maskedComment = maskTelephoneNumber(maskedComment, str, Constants.TELEPHONE_NUMBER_2);
				continue;
			}
			else if (str.contains(Constants.CUSTOMER_CBR_1)) {
				maskedComment = maskTelephoneNumber(maskedComment, str, Constants.CUSTOMER_CBR_1);
				continue;
			}
			else if (str.contains(Constants.CUSTOMER_CBR_2)) {
				maskedComment = maskTelephoneNumber(maskedComment, str, Constants.CUSTOMER_CBR_2);
				continue;
			}
			else if (str.contains(Constants.BOLD_START_TAG)) {
				maskedComment = extractTimeStampWithinBoldTags(maskedComment, str);
				continue;
			}
			else if (!str.contains(Constants.ADDITIONAL_INFO) && !str.contains(Constants.AIMS_DISPOSISTION_CODE) 
				&& !str.contains(Constants.BOLD_END_TAG) && !str.contains(Constants.BOLD_START_TAG) 
				&& !str.contains(Constants.CHAT_ID) && !str.contains(Constants.CONFERENCE)
				&& !str.contains(Constants.CONTACT) && !str.contains(Constants.HANDLER_NAME)
				&& !str.contains(Constants.INITIATOR_NAME) && !str.contains(Constants.LOB)
				&& !str.contains(Constants.NOTES) && !str.contains(Constants.QUEUE_NAME)
				&& !str.contains(Constants.ROC_TICKET_NUMBER) && !str.contains(Constants.STATE)
				&& !str.contains(Constants.TELEPHONE_NUMBER_1) && !str.contains(Constants.TELEPHONE_NUMBER_2)
				&& !str.contains(Constants.TICKET_STATUS) && !str.contains(Constants.TRANSFERS)
				&& !str.contains(Constants.URL) && !str.contains(Constants.CUSTOMER_NAME)
				&& !str.contains(Constants.ADDRESS) && !str.contains(Constants.REFERRED_TO)
				&& !str.contains(Constants.HTTP) && !str.contains(Constants.HTTP.toLowerCase())
				&& !str.contains(Constants.CUST_EMAIL)) {
				if (str.contains(":"))
					maskedComment += boldTextBeforeColon(str);
				else 
					maskedComment += str + "</br></br>";
			}
		}
		logger.debug("after masking:\n" + maskedComment);
		return maskedComment;
	}


	private static String boldTextBeforeColon(String str) {
		String[] splitted = str.split(":");
		
		String boldedText = "<b>" + splitted[0] + ":</b>";
		
		if (splitted.length > 1)
			boldedText += splitted[1];
		
		return boldedText + "</br>";
	}


	private static String extractTimeStampWithinBoldTags(String newComment,
			String str) {
		String[] splitted = str.split(Constants.BOLD_START_TAG);
		try {			
			newComment += "<b>TimeStamp: " + splitted[0] + "</b></br>";
			
			if (splitted.length > 1) {
				if (splitted[1].contains(":"))
					newComment += splitted[1];
			}
		}
		catch (Exception e) {
			logger.error("Exception in extractTimeStampWithinBoldTags", e);
		}
		
		newComment += str.substring(str.indexOf(Constants.BOLD_END_TAG) + 4, str.length()) + "</br></br>";
		return newComment;
	}


	private static String maskTelephoneNumber(String newComment, String str, String TELEPHONE_NUMBER) {
		int indexOfColon = str.indexOf(TELEPHONE_NUMBER);
		
		if (indexOfColon < 0) {
			indexOfColon = str.indexOf(TELEPHONE_NUMBER) + TELEPHONE_NUMBER.length();
		}
		
		newComment += "<b>" + str.substring(str.indexOf(TELEPHONE_NUMBER), indexOfColon + 1) + "</b> ";
		String subString = str.substring(indexOfColon + TELEPHONE_NUMBER.length() + 2, indexOfColon + 10 + TELEPHONE_NUMBER.length());
		
		
		if (subString.matches("\\d+")) {
			for (int i = indexOfColon; i < subString.length(); i++) {
				newComment += "*";
			}
		}		
		return newComment + "</br></br>";
	}
}
