package com.verizon.ssp.roctool.assignmentservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.common.Utils;

import com.verizon.ssp.roctool.controller.AssignmentDaemonController;

public class AssignmentServlet extends HttpServlet {    
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public AssignmentServlet() {
        super();       
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		logger.debug("about to assign");
		AssignmentDaemonController ac = AssignmentDaemonController.getInstance();
		logger.debug("doing new assignmnet");
		ac.priorityAssignment();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	    doGet(request,response);
	    return;
	}
}
