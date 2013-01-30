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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.web.client.CerberusWeb;
import com.intradev.cerberus.web.client.Screen;

/**
 * The welcome screen, containing a simple message indicating that the user needs to sign in
 * to see their password.
 */
public class LegalAndInstructionsScreen extends Screen {

    private static LegalAndInstructionsScreenUiBinder uiBinder = GWT.create(LegalAndInstructionsScreenUiBinder.class);

    interface LegalAndInstructionsScreenUiBinder extends UiBinder<Widget, LegalAndInstructionsScreen> {
    }
    
    
    @UiField
    PopupPanel popup;
        
    @UiField
   	Label header;
        
    @UiField
    PushButton okButton;
        

   
    private ClickHandler listener;
    private HandlerRegistration handlerRegistration;
    /*private CerberusWeb masterInstance;*/
    
    
    public static LegalAndInstructionsScreen instanciateLegalAndInstructionsScreen(CerberusWeb instance) {
    	LegalAndInstructionsScreen newInstance= new LegalAndInstructionsScreen(instance);
    	
        
        return newInstance;
    }
    

    private LegalAndInstructionsScreen(CerberusWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        /*this.masterInstance = instance;*/


        listener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				handleClick();
			}
        };
                
        okButton.addClickHandler(listener);

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
    
    public interface PopupCompleteCallback {
    	
    	void popupComplete();
    }
    
    private void shutdownPopup() {
		handlerRegistration.removeHandler();
		popup.hide();
		//Remove ourselves from the parent
		this.removeFromParent();
    }
    
    private void handleClick() {

    	shutdownPopup();

    }


	@Override
	public Screen fillOrReplace(List<String> args) {
		// TODO Auto-generated method stub
		return this;
	}
    

}
