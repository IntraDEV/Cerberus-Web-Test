package com.intradev.cerberus.web.client.screens;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.web.client.CerberusWeb;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedPassword;


/**
 * The welcome screen, containing a simple message indicating that the user needs to sign in
 * to see their password.
 */
public class ExportDataScreen extends Screen {

    private static ExportDataScreenUiBinder uiBinder = GWT.create(ExportDataScreenUiBinder.class);

    interface ExportDataScreenUiBinder extends UiBinder<Widget, ExportDataScreen> {
    }
    
    
    @UiField
    PopupPanel popup;
        
    @UiField
   	Label header;
    
    @UiField
   	Label deepInstructionsLabel;
 
    @UiField
    PushButton okButton;
    
    @UiField
    PushButton cancelButton;
        
    private ClickHandler listener;
    private HandlerRegistration handlerRegistration;
    /*private CerberusWeb masterInstance;*/
    
    
    public static ExportDataScreen instanciateExportDataScreen(CerberusWeb instance) {
    	ExportDataScreen newInstance= new ExportDataScreen(instance);
    	
        return newInstance;
    }
    

    private ExportDataScreen(CerberusWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        /*this.masterInstance = instance;*/
    	
        listener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				handleClick();
			}
        };
                
        okButton.addClickHandler(listener);
        
        cancelButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				shutdownPopup();				
			}
        	
        });

        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
           	popup.center();
          }
        });

        handlerRegistration = Window.addResizeHandler(new ResizeHandler() {           
            @Override
            public void onResize(ResizeEvent event) {
               	popup.center();
            }
        });
        
        
    }
    
    @Override
    public Screen fillOrReplace(List<String> args) {
        return this;
    }


    private void shutdownPopup() {
		
		handlerRegistration.removeHandler();
		popup.hide();
		//Remove ourselves from the parent
		this.removeFromParent();
    }
    
   
    private void handleClick() {
    	
		CerberusWeb.showMessage("Export complete",true);
	
		String [][] data= new String[CerberusWeb.sPasswords.size()][2];
		
			
		int i=0;
        for (String id : CerberusWeb.sPasswords.keySet()) {
        	EncodedPassword password = CerberusWeb.sPasswords.get(id);
            //passwords.add(password);
        	
        	String title=password.getTitle();
        	String body=password.getBody();
        	data[i][0]=title;
        	data[i][1]=body;
        	i++;
        }

//        Collections.sort(passwords, new Comparator<EncodedPassword>() {
//            public int compare(EncodedPassword o1, EncodedPassword o2) {
//                return o1.getTitle().compareTo(o2.getTitle());
//            }
//        });
//
//        for (EncodedPassword password : passwords) {
//            PasswordItem itemWidget = new PasswordItem(password, mPasswordItemActionCallback);
//            passwordList.add(itemWidget);
//        }
        
		
		com.intradev.cerberus.web.client.CSVConverterService.App.getInstance().prepareCSVDownload(
                data, new AsyncCallback<String>() {
                   public void onFailure(Throwable throwable) {
                	   throwable.printStackTrace();
                	   Window.alert("Error while preparing csv file \n" + throwable.getMessage());
                   }

                   public void onSuccess(String key) {
                     // Window.open("/cerberusweb/Export?type=csv&key=" + key, "_blank", "");
                      
                      Window.open("/cerberusweb/Export?type=csv&key=" + key + "&filename=passwords.csv", "_parent", "location=no");
                      
                      
                      
                   }
                });
       }
    
}
