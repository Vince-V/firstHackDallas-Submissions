package com.verizon.ssp.roctool.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.common.Utils;
import com.verizon.ssp.roctool.daemon.FTPDaemon;
import com.verizon.ssp.util.Logger;

public class FTPServlet  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FTPServlet() {
        super();
        if ("true".equals(Utils.getPropertyValue(Utils.loadPropertyFile("config/AdminPage.properties"), "ftp_enabled"))) {
        	logger.debug("Pulling from ROC");
        	FTPDaemon.startDaemon();
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FTPDaemon.startDaemon();
	}
	
	public void destroy() {
		FTPDaemon.stopDaemon();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    doGet(request,response);
	    return;
	}
}
