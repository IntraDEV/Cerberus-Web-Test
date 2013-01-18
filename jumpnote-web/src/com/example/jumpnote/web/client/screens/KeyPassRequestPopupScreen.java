package com.example.jumpnote.web.client.screens;

import com.example.jumpnote.web.client.JumpNoteWeb;
import com.example.jumpnote.web.client.code.EncodedNote;
import com.example.jumpnote.web.client.code.NoteDecoderFactory;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class KeyPassRequestPopupScreen {
	
	 private MyPopup popup;
	 private VerticalPanel popUpPanelContents;
	 private HTML message;
	 private ClickHandler listener;
	 private Button button;
	 private SimplePanel holder;
	 private PasswordTextBox ptb;
	 private final PopupCompleteCallback callback;
	
	private class MyPopup extends PopupPanel {

        public MyPopup() {
          // PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
          // If this is set, the panel closes itself automatically when the user
          // clicks outside of it.
          super(false);

          // PopupPanel is a SimplePanel, so you have to set it's widget property to
          // whatever you want its contents to be.
          //setWidget(new Label("Click outside of this popup to close it"));
          
          setGlassEnabled(true);
        }
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
    
    private void handleClick() {
    	String passcode = ptb.getValue();
		if (passcode.length() < JumpNoteWeb.MIN_PASSCODE_LENGTH) {
			JumpNoteWeb.showMessage("Cannot have an empty or short passcode",true);
		} else {
			if (JumpNoteWeb.sNotes.size() == 0) {
				NoteDecoderFactory.installNoteDecoder(passcode);
				callback.popupComplete();
				popup.hide();
			} else {
				NoteDecoderFactory.installNoteDecoder(passcode);
				EncodedNote first=JumpNoteWeb.sNotes.entrySet().iterator().next().getValue();
				if (first.isDecodable()) {
					callback.popupComplete();
					popup.hide();
				} else {
					//Stay in this state
					JumpNoteWeb.showMessage("The passcode does not allow you to decode your notes",true);
					ptb.setText("");						
				}
			}	
		}
    }
    
    //My attempts to get a passcode from the user
    public KeyPassRequestPopupScreen(final PopupCompleteCallback callback)
    {
    	this.callback = callback;
        // Create a PopUpPanel with a button to close it
        popup = new MyPopup();
        popup.setStyleName("demo-PopUpPanel");
        popup.setGlassStyleName("demo-PopupPanel-glass");
        popUpPanelContents = new VerticalPanel();
        //popup.setText("PopUpPanel");
        
        if (JumpNoteWeb.sNotes.size() == 0) {
        	popup.setTitle("Enter the keycode you will use to unlock your password store");
        	message = new HTML("Enter password store keycode");
        } else {
        	popup.setTitle("Enter the keycode to unlock your password store");
        	message = new HTML("Enter password store keycode");
        }
       
        message.setStyleName("demo-PopUpPanel-message");

        SubmitListener sl = new SubmitListener();
        ptb = new PasswordTextBox();
        ptb.addKeyDownHandler((KeyDownHandler) sl);
        ptb.setStyleName("demo-PopUpPanel-password");
        
        listener = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				handleClick();
			}
        };
        
        button = new Button("Open", listener);
        holder = new SimplePanel();
        holder.add(button);
        holder.setStyleName("demo-PopUpPanel-footer");
        
        popUpPanelContents.add(message);
        popUpPanelContents.add(ptb);
        popUpPanelContents.add(holder);
       
        popup.setWidget(popUpPanelContents);

        
        popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          public void setPosition(int offsetWidth, int offsetHeight) {
            int left = (Window.getClientWidth() - offsetWidth) / 2;
            int top = (Window.getClientHeight() - offsetHeight) / 2;
            popup.setPopupPosition(left, top);                           
          }
        });
        
    }
}
