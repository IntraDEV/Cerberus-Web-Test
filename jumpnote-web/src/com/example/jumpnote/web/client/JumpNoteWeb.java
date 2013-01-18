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

package com.example.jumpnote.web.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.jumpnote.allshared.JsonRpcClient;
import com.example.jumpnote.allshared.JsonRpcClient.Call;
import com.example.jumpnote.allshared.JsonRpcException;
import com.example.jumpnote.allshared.JumpNoteProtocol;
import com.example.jumpnote.web.client.code.EncodedNote;
import com.example.jumpnote.web.client.code.NoteDecoderFactory;
import com.example.jumpnote.web.client.screens.KeyPassRequestPopupScreen;
import com.example.jumpnote.web.client.screens.KeypassRequestScreen;
import com.example.jumpnote.web.client.screens.NoteEditor;
import com.example.jumpnote.web.client.screens.NotesList;
import com.example.jumpnote.web.client.screens.WelcomeScreen;
import com.example.jumpnote.web.jsonrpc.gwt.JsonRpcGwtClient;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * The JumpNote web client entry point.
 */
public class JumpNoteWeb implements EntryPoint {
	public static final int TRANSIENT_MESSAGE_HIDE_DELAY = 5000;
	public static final int MIN_PASSCODE_LENGTH = 4;
    
    private final ScreenContainer mScreenContainer = new ScreenContainer();
    public static RootPanel sMessagePanel = null;

    public static String sLoginUrl = "";
    public static final JsonRpcGwtClient sJsonRpcClient = new JsonRpcGwtClient("/jumpnoterpc");
    public static Map<String, EncodedNote> sNotes = new HashMap<String, EncodedNote>();
    public static ModelJso.UserInfo sUserInfo = null;
    
    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        showMessage("Loading...", false);

        runStateMachine();

    }
    
    


    private class PostLoginCallback implements JsonRpcClient.Callback
    {

		@Override
		public void onSuccess(Object data) {
			final RootPanel loginPanel = RootPanel.get("loginPanel");
			// Process userInfo RPC call results
            JSONObject userInfoJson = (JSONObject) data;
            if (userInfoJson.containsKey(JumpNoteProtocol.UserInfo.RET_USER)) {
                JumpNoteWeb.sUserInfo = (ModelJso.UserInfo) userInfoJson.get(
                        JumpNoteProtocol.UserInfo.RET_USER).isObject().getJavaScriptObject();
                InlineLabel label = new InlineLabel();
                label.getElement().setId("userNameLabel");
                label.setText(sUserInfo.getNick());
                loginPanel.add(label);

                loginPanel.add(new InlineLabel(" | "));

                Anchor anchor = new Anchor("Sign out",
                        userInfoJson.get(JumpNoteProtocol.UserInfo.RET_LOGOUT_URL).isString()
                        .stringValue());
                loginPanel.add(anchor);
            } else {
                sLoginUrl = userInfoJson.get(JumpNoteProtocol.UserInfo.RET_LOGIN_URL).isString().stringValue();
                Anchor anchor = new Anchor("Sign in", sLoginUrl);
                loginPanel.add(anchor);
            }
            
            runStateMachine();
			
		}

		@Override
		public void onError(JsonRpcException caught) {
			showMessage("Error: " + caught.getMessage(), false);
		}
    }
    
    private class PostDataLoadedCallback implements JsonRpcClient.Callback
    {

		@Override
		public void onSuccess(Object data) {
            // Process notesList RPC call results
            JSONObject notesListJson = (JSONObject) data;
            if (notesListJson != null) {
                JSONArray notesJson = notesListJson.get(JumpNoteProtocol.NotesList.RET_NOTES).isArray();
                for (int i = 0; i < notesJson.size(); i++) {
                    ModelJso.Note note = (ModelJso.Note) notesJson.get(i).isObject()
                            .getJavaScriptObject();
                    sNotes.put(note.getId(), new EncodedNote(note));
                }
            }
                      
            runStateMachine();
		}
		
		@Override
		public void onError(JsonRpcException caught) {
			showMessage("Error: " + caught.getMessage(), false);
		}
    }
    
    
    private class PostKeyPassRequestCallback implements KeyPassRequestPopupScreen.PopupCompleteCallback
    {
		@Override
		public void popupComplete() {
			runStateMachine();					
		}
    }
    
    public final int STATE_INITIAL = 0;
    public final int STATE_REQUESTING_USERINFO = 1;
//        STATE_NEED_LOGIN = 2;
    public final int STATE_REQUESTING_LOGIN = 3;
    public final int STATE_FETCHING_NOTES = 4;
    public final int STATE_REQUESTING_KEYCODE = 5;
    public final int STATE_APPLICATION_RUNNING = 6;
    
    private int currentApplicationState = STATE_INITIAL;
    
    private void performPostFetchProcessing() {
    	hideMessage();
    	switch (currentApplicationState) {
    	case STATE_REQUESTING_USERINFO:
    		RootPanel.get("screenPanel").add(new WelcomeScreen());
    		break;
    	case STATE_FETCHING_NOTES:
    		RootPanel.get("screenPanel").add(new KeypassRequestScreen());
    		new KeyPassRequestPopupScreen(new PostKeyPassRequestCallback());
    		break;
    	case STATE_REQUESTING_KEYCODE:
    		mScreenContainer.addScreen("home", new NotesList());
    		mScreenContainer.addScreen("note", new NoteEditor());
    		mScreenContainer.setDefault("home");
    		mScreenContainer.install(RootPanel.get("screenPanel"));
    		break;
    	}

    }
    
    private void runStateMachine() {
    	
    	switch (currentApplicationState) {
    	case STATE_INITIAL:
    		currentApplicationState = STATE_REQUESTING_USERINFO;
    		getUserLoginCookie(new PostLoginCallback());
    		break;
    	case STATE_REQUESTING_USERINFO:
    		//Did we get the login info? E.g. are we already logged in?
    		if (JumpNoteWeb.sUserInfo == null) {
    			//We aren't logged in
    			//assert(sLoginUrl != null);    			
    			performPostFetchProcessing();
    			currentApplicationState = STATE_REQUESTING_LOGIN;
    		} else {
    			//assert(sLoginUrl == null);
    			currentApplicationState = STATE_FETCHING_NOTES;
    			loadData(new PostDataLoadedCallback());
    		}
    		break;
//    	case STATE_NEED_LOGIN:
//    		break;
    	case STATE_REQUESTING_LOGIN:
    		//Do nothing, we don't allow the user to go any further
    		break;
    	case STATE_FETCHING_NOTES:    		
    		performPostFetchProcessing();
    		currentApplicationState = STATE_REQUESTING_KEYCODE;
    		break;
    	case STATE_REQUESTING_KEYCODE:
    		//TODO: need some logic to know if the keycode was successful, and also to inform the user to create a keypass if there are no notes
    		performPostFetchProcessing();
    		currentApplicationState = STATE_APPLICATION_RUNNING;
    		break;
    	case STATE_APPLICATION_RUNNING:
    		break;
    	}
    }
    

    public void getUserLoginCookie(final JsonRpcClient.Callback callback) {
        // Get and populate login information
        JSONObject userInfoParams = new JSONObject();
        userInfoParams.put(JumpNoteProtocol.UserInfo.ARG_LOGIN_CONTINUE,
                new JSONString(Window.Location.getHref()));

        sJsonRpcClient.call(JumpNoteProtocol.UserInfo.METHOD, userInfoParams, callback);
        

    }
    
    public void loadData(final JsonRpcClient.Callback callback) {
        sJsonRpcClient.call(JumpNoteProtocol.NotesList.METHOD, null, callback);
    }

    public static void showMessage(String message, boolean isTransient) {
        if (sMessagePanel == null) {
            sMessagePanel = RootPanel.get("messagePanel");
        }

        sMessagePanel.setVisible(true);
        sMessagePanel.getElement().setInnerText(message);
        if (isTransient) {
            new Timer() {
                @Override
                public void run() {
                    sMessagePanel.setVisible(false);
                }
            }.schedule(TRANSIENT_MESSAGE_HIDE_DELAY);
        }
    }

    public static void hideMessage() {
        sMessagePanel.setVisible(false);
    }
}
