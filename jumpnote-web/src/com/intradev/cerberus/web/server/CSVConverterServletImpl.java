package com.intradev.cerberus.web.server;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;

import au.com.bytecode.opencsv.CSVWriter;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class CSVConverterServletImpl extends RemoteServiceServlet implements com.intradev.cerberus.web.client.CSVConverterService {

	   public String prepareCSVDownload(String[][] mappu) {
		      FileService fileService = FileServiceFactory.getFileService();
		      AppEngineFile file = null;
		      try {
		         file = fileService.createNewBlobFile("text/csv");
		         FileWriteChannel writeChannel = fileService.openWriteChannel(file, true);

		         writeChannel.write(ByteBuffer.wrap(generateCSVFile(mappu)));
		         writeChannel.closeFinally();

		         BlobKey blobKey = fileService.getBlobKey(file);
		         return blobKey.getKeyString();
		      } catch (Exception e) {
		         throw new RuntimeException("Error creating csv file", e);
		      }
		   }
	   
   private byte[] generateCSVFile(String[][] mappu) throws Exception {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      Writer w=new OutputStreamWriter(outputStream);
      CSVWriter csvWriter=new CSVWriter(w);
      for (int i=0;i<mappu.length;i++) {
    	  csvWriter.writeNext(mappu[i]);
      }
      csvWriter.close();
      outputStream.flush();
      outputStream.close();
      return outputStream.toByteArray();
   }
}