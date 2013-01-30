package com.intradev.cerberus.web.client.screens;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.web.client.CerberusWeb;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedPassword;
import com.intradev.cerberus.web.client.code.PasswordDecoderFactory;

/**
 * The welcome screen, containing a simple message indicating that the user needs to sign in
 * to see their password.
 */
public class KeypassChangeScreen extends Screen {

    private static KeypassChangeScreenUiBinder uiBinder = GWT.create(KeypassChangeScreenUiBinder.class);

    interface KeypassChangeScreenUiBinder extends UiBinder<Widget, KeypassChangeScreen> {
    }
    
    
    @UiField
    PopupPanel popup;
        
    @UiField
   	Label header;
    
    @UiField
   	Label currentPasswordLabel;
    
    @UiField
   	Label newPasswordLabel;
    
    @UiField
    PushButton okButton;
    
    @UiField
    PushButton cancelButton;
        
    @UiField
	PasswordTextBox currentPassword;
    
    @UiField
	PasswordTextBox newPassword;

    
    private final KeypassRequestScreen.PopupCompleteCallback callback;
    private ClickHandler listener;
    private HandlerRegistration handlerRegistration;
    /*private CerberusWeb masterInstance;*/
    
    
    public static KeypassChangeScreen instanciateKeypassChangeScreen(CerberusWeb instance, final KeypassRequestScreen.PopupCompleteCallback callback) {
    	KeypassChangeScreen newInstance= new KeypassChangeScreen(instance,callback);
    	
    	//First check if there are passwords already in the store
        if (CerberusWeb.sPasswords.size() == 0) {
        	newInstance.popup.setTitle("Enter the keycode you will use to unlock your password store");
        } else {
        	newInstance.popup.setTitle("Enter the keycode to unlock your password store");     	
        }
        
        return newInstance;
    }
    

    private KeypassChangeScreen(CerberusWeb instance, final KeypassRequestScreen.PopupCompleteCallback callback) {
        initWidget(uiBinder.createAndBindUi(this));
        /*this.masterInstance = instance;*/
    	this.callback = callback;
    	
/*        if (CerberusWeb.sPasswords.size() == 0) {
        	popup.setTitle("Enter the keycode you will use to unlock your password store");
        } else {
        	popup.setTitle("Enter the keycode to unlock your password store");     	
        }*/
       

        SubmitListener sl = new SubmitListener();

        currentPassword.addKeyDownHandler((KeyDownHandler) sl);

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
           	currentPassword.getElement().focus();
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

    private class SubmitListener implements KeyDownHandler {

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				handleClick();
			}	
		}
      }
    
    //TODO: centralize since this is a duplicat of KeypassRequestScreen
    private void shutdownPopup() {
		callback.popupComplete();
		handlerRegistration.removeHandler();
		popup.hide();
		//Remove ourselves from the parent
		this.removeFromParent();
    }
    
    
    //TODO: centralize since this is a duplicat of KeypassRequestScreen
    private void handleClick() {
    	String passcode = currentPassword.getValue();
		if (passcode.length() < CerberusWeb.MIN_PASSCODE_LENGTH) {
			CerberusWeb.showMessage("Cannot have an empty or short passcode",true);
		} else {
			if (CerberusWeb.sPasswords.size() == 0) {
				PasswordDecoderFactory.installPasswordDecoder(passcode);
				shutdownPopup();
			} else {
				PasswordDecoderFactory.installPasswordDecoder(passcode);
				EncodedPassword first=CerberusWeb.sPasswords.entrySet().iterator().next().getValue();
				if (first.isDecodable()) {
					shutdownPopup();
				} else {
					//Stay in this state
					CerberusWeb.showMessage("The passcode does not allow you to decode your passwords",true);
					currentPassword.setText("");						
				}
			}	
		}
    }
    

}
