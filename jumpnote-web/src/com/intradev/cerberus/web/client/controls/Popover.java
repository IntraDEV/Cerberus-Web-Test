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
 * This code is modified from the original version from google
 */

package com.intradev.cerberus.web.client.controls;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A list item widget, representing a password.
 */
public class Popover extends Composite {

    private static PopoverUiBinder uiBinder = GWT.create(PopoverUiBinder.class);

    interface PopoverUiBinder extends UiBinder<Widget, Popover> {
    }

    @UiField
    DivElement popopone;
    
    @UiField
    DivElement arrow;
    
    @UiField
    SpanElement popoverTitle;

    @UiField
    SpanElement popoverContent;

   
    @UiField
    PushButton deleteButton;

    ActionCallback mActionCallback;
    
    
    public static Popover createPopover(ActionCallback actionCallback, RootPanel parent, String title, String body) 
    {
		int pw=parent.getOffsetWidth();
		int ph=parent.getOffsetHeight();
		//int pl=parent.getAbsoluteLeft();
		
        Popover p=new Popover(actionCallback,parent,title,body);
        

        parent.add(p);
        
        int pow=p.getOffsetWidth();
        //int pow=p.getElement().getClientWidth();
   
        int pah=p.arrow.getOffsetHeight();
        
        int left=((pw-pow)/2);
        int top = ph + pah;
        
        
        parent.setWidgetPosition(p, left, top);
        return p;
    }

    private Popover(ActionCallback actionCallback, RootPanel parent, String title, String body) {
        initWidget(uiBinder.createAndBindUi(this));

        mActionCallback = actionCallback;
        popoverTitle.setInnerText(title);
        popoverContent.setInnerText(body);

        sinkEvents(Event.ONCLICK);
        

    }

    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONCLICK) {
            event.stopPropagation();
            event.preventDefault();
            return;
        }
        super.onBrowserEvent(event);
    }

    @UiHandler("deleteButton")
    void onEditClick(ClickEvent e) {
    	this.removeFromParent();
        e.stopPropagation();
    }

    public interface ActionCallback {
        public void onEdit(String passwordId);
        public void onDelete(String passwordId);
    }
}
