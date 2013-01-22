/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intradev.cerberus.web.client.screens;

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
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.web.client.JumpNoteWeb;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedNote;
import com.intradev.cerberus.web.client.code.NoteDecoderFactory;

import java.util.List;

/**
 * The welcome screen, containing a simple message indicating that the user needs to sign in
 * to see her notes.
 */
public class KeypassRequestScreen extends Screen {

    private static KeypassRequestScreenUiBinder uiBinder = GWT.create(KeypassRequestScreenUiBinder.class);

    interface KeypassRequestScreenUiBinder extends UiBinder<Widget, KeypassRequestScreen> {
    }
    
    
    @UiField
    PopupPanel popup;
        
    @UiField
   	Label label;
    
    @UiField
   	Label body;
    
    @UiField
    PushButton button;
    
    @UiField
	PasswordTextBox ptb;

    
    private final PopupCompleteCallback callback;
    private ClickHandler listener;
    private HandlerRegistration handlerRegistration;

    public KeypassRequestScreen(final PopupCompleteCallback callback) {
        initWidget(uiBinder.createAndBindUi(this));
        
    	this.callback = callback;

        
    	
    	
        if (JumpNoteWeb.sNotes.size() == 0) {
        	popup.setTitle("Enter the keycode you will use to unlock your password store");
        } else {
        	popup.setTitle("Enter the keycode to unlock your password store");     	
        }
       

        SubmitListener sl = new SubmitListener();

        ptb.addKeyDownHandler((KeyDownHandler) sl);

        listener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				handleClick();
			}
        };
                
        button.addClickHandler(listener);

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

    private class SubmitListener implements KeyDownHandler {

		@Override
		public void onKeyDown(KeyDownEvent event) {
			if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
				handleClick();
			}	
		}
      }
    
    public interface PopupCompleteCallback {
    	
    	void popupComplete();
    }
    
    private void shutdownPopup() {
		callback.popupComplete();
		handlerRegistration.removeHandler();
		popup.hide();
    }
    
    private void handleClick() {
    	String passcode = ptb.getValue();
		if (passcode.length() < JumpNoteWeb.MIN_PASSCODE_LENGTH) {
			JumpNoteWeb.showMessage("Cannot have an empty or short passcode",true);
		} else {
			if (JumpNoteWeb.sNotes.size() == 0) {
				NoteDecoderFactory.installNoteDecoder(passcode);
				shutdownPopup();
			} else {
				NoteDecoderFactory.installNoteDecoder(passcode);
				EncodedNote first=JumpNoteWeb.sNotes.entrySet().iterator().next().getValue();
				if (first.isDecodable()) {
					shutdownPopup();
				} else {
					//Stay in this state
					JumpNoteWeb.showMessage("The passcode does not allow you to decode your notes",true);
					ptb.setText("");						
				}
			}	
		}
    }
    

}
