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
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.intradev.cerberus.allshared.JsonRpcClient;
import com.intradev.cerberus.allshared.JsonRpcException;
import com.intradev.cerberus.allshared.JumpNoteProtocol;
import com.intradev.cerberus.allshared.JumpNoteProtocol.NotesCreate;
import com.intradev.cerberus.allshared.JumpNoteProtocol.NotesEdit;
import com.intradev.cerberus.web.client.JumpNoteWeb;
import com.intradev.cerberus.web.client.ModelJso;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedNote;

import java.util.List;

/**
 * The create/edit note screen.
 */
public class NoteEditor extends Screen {

    private static NoteEditorUiBinder uiBinder = GWT.create(NoteEditorUiBinder.class);

    interface NoteEditorUiBinder extends UiBinder<Widget, NoteEditor> {
    }

    private String mEditNoteId;

    @UiField
    HeadingElement heading;

    @UiField
    TextBox passwordTitle;

    @UiField
    TextArea passwordBody;

    @UiField
    PushButton saveButton;

    @UiField
    PushButton revertButton;
    
    
    private JumpNoteWeb masterInstance;

    public NoteEditor(JumpNoteWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        masterInstance = instance;
        heading.setInnerText("New Note");
    }

    public NoteEditor(String editNoteId) {
        this.mEditNoteId = editNoteId;
        initWidget(uiBinder.createAndBindUi(this));
        heading.setInnerText("Editing note: " + editNoteId);
        passwordTitle.setText(JumpNoteWeb.sNotes.get(mEditNoteId).getTitle());
        passwordBody.setText(JumpNoteWeb.sNotes.get(mEditNoteId).getBody());
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        if (args.size() == 0) {
            if (mEditNoteId == null) {
                this.passwordTitle.setText("");
                this.passwordBody.setText("");
                return this;
            } else
                return new NoteEditor(masterInstance);
        } else if (args.size() == 1) {
            if (args.get(0).equals(mEditNoteId))
                return this;
            else
                return new NoteEditor(args.get(0));
        }

        return null;
    }

    @Override
    public void onShow() {
        JumpNoteWeb.hideMessage();
        passwordTitle.setFocus(true);
        super.onShow();
    }

    @UiHandler("saveButton")
    void onSaveClick(ClickEvent e) {
        String method;

        EncodedNote note = new EncodedNote(passwordTitle.getText(), passwordBody.getText());

        if (mEditNoteId == null) {
            method = JumpNoteProtocol.NotesCreate.METHOD;
        } else {
            method = JumpNoteProtocol.NotesEdit.METHOD;
            note.setId(mEditNoteId);
        }

        JSONObject paramsJson = new JSONObject();
        assert(NotesCreate.ARG_NOTE.compareTo(NotesEdit.ARG_NOTE)==0);
        //paramsJson.put(JumpNoteProtocol.NotesEdit.ARG_NOTE, new JSONObject(note));
        paramsJson.put(JumpNoteProtocol.NotesEdit.ARG_NOTE, note.getJSONObject());

        JumpNoteWeb.showMessage("Saving...", false);
        JumpNoteWeb.sJsonRpcClient.call(method, paramsJson, new JsonRpcClient.Callback() {
            public void onSuccess(Object data) {
                JSONObject responseJson = (JSONObject) data;
                ModelJso.Note note = (ModelJso.Note) responseJson.get(JumpNoteProtocol.NotesEdit.RET_NOTE)
                        .isObject().getJavaScriptObject();
                JumpNoteWeb.sNotes.put(note.getId(), new EncodedNote(note));
                JumpNoteWeb.showMessage("Note saved.", true);
                History.newItem("home");
            }

            public void onError(JsonRpcException caught) {
                JumpNoteWeb.showMessage("Error saving: " + caught.getMessage(), false);
            }
        });
    }

    @UiHandler("revertButton")
    void onRevertClick(ClickEvent e) {
        History.newItem("home");
    }
}
