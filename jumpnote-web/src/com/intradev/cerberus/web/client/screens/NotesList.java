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
import com.intradev.cerberus.allshared.JumpNoteProtocol;
import com.intradev.cerberus.web.client.JumpNoteWeb;
import com.intradev.cerberus.web.client.ModelJso;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.ModelJso.Note;
import com.intradev.cerberus.web.client.code.EncodedNote;
import com.intradev.cerberus.web.client.controls.NoteItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The notes list screen.
 */
public class NotesList extends Screen {

    private static NotesListUiBinder uiBinder = GWT.create(NotesListUiBinder.class);

    interface NotesListUiBinder extends UiBinder<Widget, NotesList> {
    }
    

    @UiField
    FlowPanel passwordList;

    @UiField
    PushButton createButton;
    
    @UiField
    PushButton setKeyCodeButton;
    
    private JumpNoteWeb masterInstance;

    public NotesList(JumpNoteWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        masterInstance = instance;
        refreshNotes();
    }

    @UiHandler("createButton")
    void onClick(ClickEvent e) {
        History.newItem("note");
    }
    
    @UiHandler("setKeyCodeButton")
    void onSetKeyCodeButton(ClickEvent e) {
        //History.newItem("note");
    	JumpNoteWeb.showMessage("This will allow the user to change the keypass in the future", true);
    	masterInstance.displayRequestKeypassScreen();
    }

    public void refreshNotes() {
    	passwordList.clear();
        if (JumpNoteWeb.sNotes.keySet().isEmpty()) {
            Label emptyLabel = new Label();
            emptyLabel.setStyleName("empty");
            emptyLabel.setText("You haven't written any notes, create one by clicking " +
                    "'Create Note' below!");
            passwordList.add(emptyLabel);
        } else {
            List<EncodedNote> notes = new ArrayList<EncodedNote>();
            for (String id : JumpNoteWeb.sNotes.keySet()) {
            	EncodedNote note = JumpNoteWeb.sNotes.get(id);
                notes.add(note);
            }

            Collections.sort(notes, new Comparator<EncodedNote>() {
                public int compare(EncodedNote o1, EncodedNote o2) {
                    return o1.getTitle().compareTo(o2.getTitle());
                }
            });

            for (EncodedNote note : notes) {
                NoteItem itemWidget = new NoteItem(note, mNoteItemActionCallback);
                passwordList.add(itemWidget);
            }
        }
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        refreshNotes();
        return this;
    }

    private NoteItem.ActionCallback mNoteItemActionCallback = new NoteItem.ActionCallback() {
        public void onEdit(String noteId) {
            History.newItem("note/" + noteId);
        }

        public void onDelete(final String noteId) {
            JSONObject paramsJson = new JSONObject();
            paramsJson.put(JumpNoteProtocol.NotesDelete.ARG_ID, new JSONString(noteId));
            JumpNoteWeb.sJsonRpcClient.call(JumpNoteProtocol.NotesDelete.METHOD, paramsJson, new JsonRpcClient.Callback() {
                public void onSuccess(Object data) {
                    EncodedNote note = JumpNoteWeb.sNotes.get(noteId);
                    JumpNoteWeb.sNotes.remove(noteId);
                    JumpNoteWeb.showMessage("Deleted note '" + note.getTitle() + "'", true);
                    refreshNotes();
                }

                public void onError(JsonRpcException caught) {
                    JumpNoteWeb.showMessage("Delete failed: " + caught.getMessage(), false);
                }
            });
        }
    };

   
}