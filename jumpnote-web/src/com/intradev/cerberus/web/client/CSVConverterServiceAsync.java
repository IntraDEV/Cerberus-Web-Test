package com.intradev.cerberus.web.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface CSVConverterServiceAsync {

 

   /**
    * Prepares a csv download and return the blobkey for the create file
    * @param layout  the table layout to parse to csv
    * @return blobkey created blob key
    */
   void prepareCSVDownload(String[][] mappu, AsyncCallback<String> async);
}
