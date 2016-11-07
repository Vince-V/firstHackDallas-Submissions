package com.verizon.ssp.roctool.daemonservlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.verizon.ssp.roctool.common.Utils;
// import com.verizon.ssp.roctool.daemon.GrabFromRocDaemon;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.daemon.AssignmentDaemon;
import com.verizon.ssp.roctool.controller.Controller;


/**
 * Servlet implementation class DaemonServlet
 */
public class DaemonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	Controller controller = Controller.getInstance();
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DaemonServlet() {
        super();
        if (0 == Controller.propertiesMap.size()) {
        	logger.debug("getting properties");
        	DBDriver.getProperties();
        	logger.debug("after getting properties");
        }
        
        logger.debug("there");
//        String grabFromRoc = Utils.getPropertyValue(Utils.loadPropertyFile("config/AdminPage.properties"), "GRAB_FROM_ROC");
        String grabFromRoc = Controller.propertiesMap.get("GRAB_FROM_ROC");
        logger.debug("grabFromRoc: " + grabFromRoc);
        
        if ("".equals(Controller.filterHTML)) {
        	logger.debug("populating filters");
        	controller.getFilterHtml("System");
        }
        
        if ("true".equals(grabFromRoc)) {
        	if (null == Utils.focusReferredTo) {
        		controller.getReferredToList("System");
        	}
        	
        	AssignmentDaemon.startDaemon();
    	}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AssignmentDaemon.startDaemon();
	}
	
	public void destroy() {
		AssignmentDaemon.stopDaemon();
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
