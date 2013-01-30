package com.intradev.cerberus.web.server;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * User: AnAmuser
 * Date: 03-06-11
 * <p/>
 * Handling export get requests
 */
@SuppressWarnings("serial")
public class Export extends HttpServlet {

	@Override
   protected void doGet(
         HttpServletRequest request,
         HttpServletResponse response) throws ServletException, IOException {
      try {
         String type = request.getParameter("type");
         String key = request.getParameter("key");
         String filename = request.getParameter("filename");
         if ((filename == null) || (filename.compareTo("") == 0)) {
        	 filename = "download.txt";
         }
         BlobKey blobKey = new BlobKey(key);
         BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();

         if (type.equals("xls")) {
            response.setContentType("application/vnd.ms-excel");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
         } else if (type.equals("csv")) {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
         }

         blobStoreService.serve(blobKey, response);
         
         
         //Delete the blob in one hour
         Queue queue = QueueFactory.getDefaultQueue();         
         TaskOptions taskOptions = TaskOptions.Builder.withPayload(new FileDeferredTask(blobKey.getKeyString()));
         //In one minute
         taskOptions.countdownMillis(60 * 1000);          
         //taskOptions.countdownMillis(60);
         queue.add(taskOptions);
        
      } catch (Exception e) {
         throw new ServletException("File was not generated", e);
      }
   }
	
	@Override
   protected void doDelete(
	         HttpServletRequest request,
	         HttpServletResponse response) throws ServletException, IOException {
	      try {
	        
	         String key = request.getParameter("key");
	         
	         BlobKey blobKey = new BlobKey(key);
	         BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();

	         //Delete the blob, can only serve this once
	         blobStoreService.delete(blobKey);
	         
	      } catch (Exception e) {
	         throw new ServletException("File was not generated", e);
	      }
	   }
   
   
	public class FileDeferredTask implements DeferredTask {
		 
	
		private String _blobKey;
	 
		public FileDeferredTask(String blobKey){
			
			_blobKey = blobKey;
		}
	 
		@Override
		public void run() {
	 
			BlobKey blob = new BlobKey(_blobKey);
	 
			BlobstoreService blobstoreService = 
				BlobstoreServiceFactory.getBlobstoreService();
	 
			blobstoreService.delete(blob);
	 

		}
	 
	 
	}
	
	
   
}
