package com.intradev.cerberus.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("CSVConverterService")
public interface CSVConverterService extends RemoteService {


   /**
    * Prepares a csv download and return the blobkey for the create file
    * @param layout  the table layout to parse to csv
    * @return blobkey created blob key
    */
   String prepareCSVDownload(String[][] mappu);

   /**
    * Utility/Convenience class.
    * Use SampleApplicationService.App.getInstance() to access static instance of CSVConverterServiceAsync
    */
   public static class App {
      private static CSVConverterServiceAsync ourInstance = GWT.create(CSVConverterService.class);

      public static synchronized CSVConverterServiceAsync getInstance() {
         return ourInstance;
      }
   }
}
