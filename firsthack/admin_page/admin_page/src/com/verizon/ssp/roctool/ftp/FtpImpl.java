package com.verizon.ssp.roctool.ftp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.net.ftp.FTPClient;
// import org.apache.commons.vfs2.FileObject;
// import org.apache.commons.vfs2.FileSystemOptions;
// import org.apache.commons.vfs2.Selectors;
// import org.apache.commons.vfs2.impl.StandardFileSystemManager;
// import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;


import com.verizon.ssp.roctool.common.Constants;
import com.verizon.ssp.roctool.db.DBDriver;
import com.verizon.ssp.roctool.object.CSVObj;
import com.verizon.ssp.util.Logger;
import com.verizon.ssp.roctool.common.Utils;

// import org.apache.commons.vfs2.FileObject;
// import org.apache.commons.vfs2.FileSystemOptions;
// import org.apache.commons.vfs2.Selectors;
// import org.apache.commons.vfs2.impl.StandardFileSystemManager;
// import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;

import com.verizon.ssp.roctool.controller.Controller;

import java.util.Date;

public class FtpImpl implements iFtp{	
	private HashMap<Integer, CSVObj> csvs = DBDriver.getCSVvalues();
	private static Logger logger = Logger.getLogger(Constants.ROC_LOGGER);
	private static String writeBkCsv = "";
	
	/**
	 * creates the csv file
	 */
	public void createCSVFile() {		
		Writer writer = null;
		Writer bkWriter = null;
		logger.debug("csvs size: " + csvs.size());
		
		try {
			if (writeBkCsv.equals(""))
				writeBkCsv = Utils.getPropertyValue(Utils.loadPropertyFile("config/AdminPage.properties"), "write_bk_csv");

			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ftpFile.csv")));

			if ("true".equals(writeBkCsv)) {
				bkWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("ftpFile_" + convertDateToString(new Date()) + ".csv")));
				bkWriter.write("Issue,Comment,Status,User_ID\n");
			}
			writer.write("Issue,Comment,Status,User_ID\n");
			
			for (CSVObj c: csvs.values()) {
				logger.debug(c.getTicketId());
				writer.write(c.getTicketId() + ",\"" + c.getComment().replaceAll("\"", "") + "\"," + c.getStatus() + "," + c.getVzId() + "\n");

				if ("true".equals(writeBkCsv)) {
					bkWriter.write(c.getTicketId() + ",\"" + c.getComment() + "\"," + c.getStatus() + "," + c.getVzId() + "\n");
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception in createCSVFile: ", e);
		}
		finally {
			try {
				writer.close();

				if ("true".equals(writeBkCsv))
					bkWriter.close();
			}
			catch (Exception ex) {
				 logger.error("Exception in createCSVFile: ", ex);
			}
		}
	}

	// will display the date in the filename so we can check to see if the csv file is
	// being written correctly...
	private static String convertDateToString(Date d) {
		String strDate = "";
		DateFormat df = new SimpleDateFormat("yyyy_MM_dd-HH_mm");
		strDate = df.format(d);	
		return strDate;
	}
	
	/**
	 * ftp's the csv file to ROC
	 */
	public void ftpFile() {
		logger.debug("in ftpFIle send_csv: " + Controller.propertiesMap.get("SEND_CSV"));
		
		if ("true".equals(Controller.propertiesMap.get("SEND_CSV"))) {
			FTPClient client = new FTPClient();
			FileInputStream fis = null;
			
			createCSVFile();
			logger.debug("ftping csv");
			
			try {
				client.connect("143.91.12.178");
				client.login("roc", "4close.me");
				
				fis = new FileInputStream("ftpFile.csv");
				client.storeFile("/ROC/VDSI_ROCTkts.csv", fis);
			} catch (MalformedURLException e) {
				logger.error("Exception in ftpFile: ", e);
			} catch (IOException e) {
				logger.error("Exception in ftpFile: ", e);
			}	
			finally {
				try {
					if (fis != null)
						fis.close();
					client.logout();
					client.disconnect();
				}
				catch (Exception ex) { 
					logger.error("Exception in ftpFile: ", ex);
				}
			}
		}
		DBDriver.updateCSVTable(csvs);
		logger.debug("GETTING OUT OF FTP PART");
	}
	
	// public void sftpFile(){
		
	// 	createCSVFile();
	// 	logger.debug("ftping csv");
		
	// 	StandardFileSystemManager manager = new StandardFileSystemManager();
		 
	// 	try {
			  
	// 		String fileToFTP = "ftpFile.csv";
	// 		String serverAddress = "143.91.12.178";
	// 		String userId = "roc";
	// 		String password = "4close.me";
	// 		String remoteDirectory = "/ROC/";
	// 		String remoteName = "VDSI_ROCTkts.csv";
			
	// 		File file = new File(fileToFTP);
	// 		if (!file.exists())
	// 			throw new RuntimeException("Error. Local file not found");
			 
	// 		manager.init();
			    
	// 		FileSystemOptions opts = new FileSystemOptions();
	// 		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
	// 		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
	// 		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000);
			  
	// 		String sftpUri = "sftp://" + userId + ":" + password +  "@" + serverAddress + "/" + 
	// 		   remoteDirectory + remoteName;
			    
	// 		FileObject localFile = manager.resolveFile(file.getAbsolutePath());
	// 		FileObject remoteFile = manager.resolveFile(sftpUri, opts);
			 
	// 		remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);
	// 		logger.debug("FTP SUCCESSFUL");
		 
	// 	}
	// 	catch (Exception ex) {
	// 		logger.error("Exception in ftpFile: ", ex);
	// 	}
	// 	finally {
	// 		manager.close();
	// 		DBDriver.updateCSVTable(csvs);
	// 	}
	// }
	
}
