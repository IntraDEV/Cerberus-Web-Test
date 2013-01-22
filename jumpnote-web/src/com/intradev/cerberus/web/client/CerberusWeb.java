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

/*
 * This file is heavily modified from the original google code.
 */

package com.intradev.cerberus.web.client;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.RootPanel;
import com.intradev.cerberus.allshared.JsonRpcClient;
import com.intradev.cerberus.allshared.JsonRpcException;
import com.intradev.cerberus.allshared.CerberusProtocol;
import com.intradev.cerberus.web.client.code.EncodedPassword;
import com.intradev.cerberus.web.client.screens.KeypassRequestScreen;
import com.intradev.cerberus.web.client.screens.PasswordEditor;
import com.intradev.cerberus.web.client.screens.PasswordList;
import com.intradev.cerberus.web.client.screens.WelcomeScreen;
import com.intradev.cerberus.web.jsonrpc.gwt.JsonRpcGwtClient;


/**
 * The Cerberus web client entry point.
 */
public class CerberusWeb implements EntryPoint {
	public static final int TRANSIENT_MESSAGE_HIDE_DELAY = 5000;
	public static final int MIN_PASSCODE_LENGTH = 4;
    
	//TODO: these are probably static to simplify the sample code. Need to remake them non-static and private
    private final ScreenContainer mScreenContainer = new ScreenContainer();
    public static RootPanel sMessagePanel = null;

    private String sLoginUrl = "";
    public static final JsonRpcGwtClient sJsonRpcClient = new JsonRpcGwtClient("/cerberusrpc");
    public static Map<String, EncodedPassword> sPasswords = new HashMap<String, EncodedPassword>();
    public static ModelJso.UserInfo sUserInfo = null;

    /*
     * These are the states for the UI state machine 
     */
    public final static int STATE_INITIAL = 0;
    public final static int STATE_REQUESTING_USERINFO = 1;
//        STATE_NEED_LOGIN = 2;
    public final static int STATE_REQUESTING_LOGIN = 3;
    public final static int STATE_FETCHING_PASSWORDS = 4;
    public final static int STATE_REQUESTING_KEYCODE = 5;
    public final static int STATE_APPLICATION_RUNNING = 6;
    
    private int currentApplicationState = STATE_INITIAL;
      
    
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
            if (userInfoJson.containsKey(CerberusProtocol.UserInfo.RET_USER)) {
                CerberusWeb.sUserInfo = (ModelJso.UserInfo) userInfoJson.get(
                        CerberusProtocol.UserInfo.RET_USER).isObject().getJavaScriptObject();
                InlineLabel label = new InlineLabel();
                label.getElement().setId("userNameLabel");
                label.setText(sUserInfo.getNick());
                loginPanel.add(label);

                loginPanel.add(new InlineLabel(" | "));

                Anchor anchor = new Anchor("Sign out",
                        userInfoJson.get(CerberusProtocol.UserInfo.RET_LOGOUT_URL).isString()
                        .stringValue());
                loginPanel.add(anchor);
            } else {
                sLoginUrl = userInfoJson.get(CerberusProtocol.UserInfo.RET_LOGIN_URL).isString().stringValue();
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
            // Process passwordsList RPC call results
            JSONObject passwordsListJson = (JSONObject) data;
            if (passwordsListJson != null) {
                JSONArray passwordsJson = passwordsListJson.get(CerberusProtocol.PasswordsList.RET_PASSWORDS).isArray();
                for (int i = 0; i < passwordsJson.size(); i++) {
                    ModelJso.Password password = (ModelJso.Password) passwordsJson.get(i).isObject()
                            .getJavaScriptObject();
                    sPasswords.put(password.getId(), new EncodedPassword(password));
                }
            }
                      
            runStateMachine();
		}
		
		@Override
		public void onError(JsonRpcException caught) {
			showMessage("Error: " + caught.getMessage(), false);
		}
    }
    
    
    private class PostKeyPassRequestCallback implements KeypassRequestScreen.PopupCompleteCallback
    {
		@Override
		public void popupComplete() {
			runStateMachine();				
		}
    }
    
    //TODO make me private
    public void displayRequestKeypassScreen() {
    	RootPanel.get("screenPanel").add(new KeypassRequestScreen(this,new PostKeyPassRequestCallback()));
    	//History.newItem("settings");
    }
    

    private void performPostFetchProcessing() {
    	hideMessage();
    	switch (currentApplicationState) {
    	case STATE_REQUESTING_USERINFO:
    		RootPanel.get("screenPanel").add(new WelcomeScreen(sLoginUrl));
    		break;
    	case STATE_FETCHING_PASSWORDS:
//    		mScreenContainer.addScreen("settings", new KeypassRequestScreen(this,new PostKeyPassRequestCallback()));
//    		mScreenContainer.setDefault("settings");
//    		mScreenContainer.install(RootPanel.get("screenPanel"));    		
    		displayRequestKeypassScreen();
    		break;
    	case STATE_REQUESTING_KEYCODE:
    		mScreenContainer.addScreen("home", new PasswordList(this));
    		mScreenContainer.addScreen("password", new PasswordEditor(this));
    		//mScreenContainer.addScreen("settings", new KeypassRequestScreen(this,new PostKeyPassRequestCallback()));
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
    		if (CerberusWeb.sUserInfo == null) {
    			//We aren't logged in
    			//assert(sLoginUrl != null);    			
    			performPostFetchProcessing();
    			currentApplicationState = STATE_REQUESTING_LOGIN;
    		} else {
    			//assert(sLoginUrl == null);
    			currentApplicationState = STATE_FETCHING_PASSWORDS;
    			loadData(new PostDataLoadedCallback());
    		}
    		break;
//    	case STATE_NEED_LOGIN:
//    		break;
    	case STATE_REQUESTING_LOGIN:
    		//Do nothing, we don't allow the user to go any further
    		break;
    	case STATE_FETCHING_PASSWORDS:    		
    		performPostFetchProcessing();
    		currentApplicationState = STATE_REQUESTING_KEYCODE;
    		break;
    	case STATE_REQUESTING_KEYCODE:
    		//TODO: need some logic to inform the user to create a keypass if there are no passwords
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
        userInfoParams.put(CerberusProtocol.UserInfo.ARG_LOGIN_CONTINUE,
                new JSONString(Window.Location.getHref()));

        sJsonRpcClient.call(CerberusProtocol.UserInfo.METHOD, userInfoParams, callback);
    }
    
    public void loadData(final JsonRpcClient.Callback callback) {
        sJsonRpcClient.call(CerberusProtocol.PasswordsList.METHOD, null, callback);
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
