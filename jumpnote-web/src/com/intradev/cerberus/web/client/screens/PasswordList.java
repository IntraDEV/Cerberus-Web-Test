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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.allshared.JsonRpcClient;
import com.intradev.cerberus.allshared.JsonRpcException;
import com.intradev.cerberus.allshared.CerberusProtocol;
import com.intradev.cerberus.web.client.CerberusWeb;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedPassword;
import com.intradev.cerberus.web.client.controls.PasswordItem;

/**
 * The notes list screen.
 */
public class PasswordList extends Screen {

    private static NotesListUiBinder uiBinder = GWT.create(NotesListUiBinder.class);

    interface NotesListUiBinder extends UiBinder<Widget, PasswordList> {
    }
    

    @UiField
    FlowPanel passwordList;

    @UiField
    PushButton createButton;
    
    @UiField
    PushButton setKeyCodeButton;
    
    private CerberusWeb masterInstance;

    public PasswordList(CerberusWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        masterInstance = instance;
        refreshNotes();
    }

    @UiHandler("createButton")
    void onClick(ClickEvent e) {
        History.newItem("password");
    }
    
    @UiHandler("setKeyCodeButton")
    void onSetKeyCodeButton(ClickEvent e) {
        //History.newItem("note");
    	CerberusWeb.showMessage("This will allow the user to change the keypass in the future", true);
    	masterInstance.displayRequestKeypassScreen();
    }

    public void refreshNotes() {
    	passwordList.clear();
        if (CerberusWeb.sNotes.keySet().isEmpty()) {
            Label emptyLabel = new Label();
            emptyLabel.setStyleName("empty");
            emptyLabel.setText("You haven't written any passwords, create one by clicking " +
                    "'Create Password' below!");
            passwordList.add(emptyLabel);
        } else {
            List<EncodedPassword> notes = new ArrayList<EncodedPassword>();
            for (String id : CerberusWeb.sNotes.keySet()) {
            	EncodedPassword note = CerberusWeb.sNotes.get(id);
                notes.add(note);
            }

            Collections.sort(notes, new Comparator<EncodedPassword>() {
                public int compare(EncodedPassword o1, EncodedPassword o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });

            for (EncodedPassword note : notes) {
                PasswordItem itemWidget = new PasswordItem(note, mNoteItemActionCallback);
                passwordList.add(itemWidget);
            }
        }
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        refreshNotes();
        return this;
    }

    private PasswordItem.ActionCallback mNoteItemActionCallback = new PasswordItem.ActionCallback() {
        public void onEdit(String noteId) {
            History.newItem("password/" + noteId);
        }

        public void onDelete(final String noteId) {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put(CerberusProtocol.NotesDelete.ARG_ID, new JSONString(noteId));
            CerberusWeb.sJsonRpcClient.call(CerberusProtocol.NotesDelete.METHOD, paramsJson, new JsonRpcClient.Callback() {
                public void onSuccess(Object data) {
                    EncodedPassword note = CerberusWeb.sNotes.get(noteId);
                    CerberusWeb.sNotes.remove(noteId);
                    CerberusWeb.showMessage("Deleted password '" + note.getTitle() + "'", true);
                    refreshNotes();
                }

                public void onError(JsonRpcException caught) {
                    CerberusWeb.showMessage("Delete failed: " + caught.getMessage(), false);
                }
            });
        }
    };

   
}
