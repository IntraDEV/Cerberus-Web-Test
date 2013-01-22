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
 * Heavily modified from original source from google
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
import com.intradev.cerberus.allshared.CerberusProtocol;
import com.intradev.cerberus.allshared.CerberusProtocol.NotesCreate;
import com.intradev.cerberus.allshared.CerberusProtocol.NotesEdit;
import com.intradev.cerberus.web.client.CerberusWeb;
import com.intradev.cerberus.web.client.ModelJso;
import com.intradev.cerberus.web.client.Screen;
import com.intradev.cerberus.web.client.code.EncodedPassword;

import java.util.List;

/**
 * The create/edit note screen.
 */
public class PasswordEditor extends Screen {

    private static NoteEditorUiBinder uiBinder = GWT.create(NoteEditorUiBinder.class);

    interface NoteEditorUiBinder extends UiBinder<Widget, PasswordEditor> {
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
    
    
    private CerberusWeb masterInstance;

    public PasswordEditor(CerberusWeb instance) {
        initWidget(uiBinder.createAndBindUi(this));
        masterInstance = instance;
        heading.setInnerText("New Password");
    }

    public PasswordEditor(String editNoteId) {
        this.mEditNoteId = editNoteId;
        initWidget(uiBinder.createAndBindUi(this));
        heading.setInnerText("Editing password: " + editNoteId);
        passwordTitle.setText(CerberusWeb.sNotes.get(mEditNoteId).getTitle());
        passwordBody.setText(CerberusWeb.sNotes.get(mEditNoteId).getBody());
    }

    @Override
    public Screen fillOrReplace(List<String> args) {
        if (args.size() == 0) {
            if (mEditNoteId == null) {
                this.passwordTitle.setText("");
                this.passwordBody.setText("");
                return this;
            } else
                return new PasswordEditor(masterInstance);
        } else if (args.size() == 1) {
            if (args.get(0).equals(mEditNoteId))
                return this;
            else
                return new PasswordEditor(args.get(0));
        }

        return null;
    }

    @Override
    public void onShow() {
        CerberusWeb.hideMessage();
        passwordTitle.setFocus(true);
        super.onShow();
    }

    @UiHandler("saveButton")
    void onSaveClick(ClickEvent e) {
        String method;

        EncodedPassword note = new EncodedPassword(passwordTitle.getText(), passwordBody.getText());

        if (mEditNoteId == null) {
            method = CerberusProtocol.NotesCreate.METHOD;
        } else {
            method = CerberusProtocol.NotesEdit.METHOD;
            note.setId(mEditNoteId);
        }

        JSONObject paramsJson = new JSONObject();
        assert(NotesCreate.ARG_NOTE.compareTo(NotesEdit.ARG_PASSWORD)==0);
        //paramsJson.put(CerberusProtocol.NotesEdit.ARG_PASSWORD, new JSONObject(note));
        paramsJson.put(CerberusProtocol.NotesEdit.ARG_PASSWORD, note.getJSONObject());

        CerberusWeb.showMessage("Saving...", false);
        CerberusWeb.sJsonRpcClient.call(method, paramsJson, new JsonRpcClient.Callback() {
            public void onSuccess(Object data) {
                JSONObject responseJson = (JSONObject) data;
                ModelJso.Note note = (ModelJso.Note) responseJson.get(CerberusProtocol.NotesEdit.RET_PASSWORD)
                        .isObject().getJavaScriptObject();
                CerberusWeb.sNotes.put(note.getId(), new EncodedPassword(note));
                CerberusWeb.showMessage("Password saved.", true);
                History.newItem("home");
            }

            public void onError(JsonRpcException caught) {
                CerberusWeb.showMessage("Error saving: " + caught.getMessage(), false);
            }
        });
    }

    @UiHandler("revertButton")
    void onRevertClick(ClickEvent e) {
        History.newItem("home");
    }
}
