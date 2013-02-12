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
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
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
    
    
    private HandlerRegistration handlerRegistration;
    private RootPanel parent;
    private Widget positionDownFrom;
    
    public static Popover createPopover(ActionCallback actionCallback, RootPanel parent, Widget positionDownFrom, String title, String body) 
    {
	
        Popover p=new Popover(actionCallback,parent,title,body);
        
    	p.parent = parent;
    	p.positionDownFrom = positionDownFrom;

        parent.add(p);
        
        p.positionMe();
               
        return p;
    }
    
    private void positionMe() {
    	int parentLeft = positionDownFrom.getAbsoluteLeft();
    	int parentTop = positionDownFrom.getAbsoluteTop();
    	int parentHeight = positionDownFrom.getOffsetHeight();
    	int parentBottom = parentTop + parentHeight;

        int arrowHeight= arrow.getOffsetHeight();

        parent.setWidgetPosition(this, parentLeft, parentBottom + arrowHeight);
    }

    private Popover(ActionCallback actionCallback, Panel parent, String title, String body) {
        initWidget(uiBinder.createAndBindUi(this));

        popoverTitle.setInnerText(title);
        popoverContent.setInnerText(body);

        sinkEvents(Event.ONCLICK);
        
        
        handlerRegistration = Window.addResizeHandler(new ResizeHandler() {           
            @Override
            public void onResize(ResizeEvent event) {
            	Popover.this.positionMe();
            }
        });
       
    }
    
    public void terminate() {
    	this.handlerRegistration.removeHandler();
    	this.removeFromParent();
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
    	terminate();
        e.stopPropagation();
    }

    public interface ActionCallback {
        public void onEdit(String passwordId);
        public void onDelete(String passwordId);
    }
}
