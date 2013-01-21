
package com.example.jumpnote.web.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the web client.
 */
public interface Resources extends ClientBundle {
	
	
    @Source("btn_change_keycode_off.png")
    ImageResource btn_edit_keycode_password_off();

    @Source("btn_change_keycode_on.png")
    ImageResource btn_edit_keycode_password_on();
	
    @Source("btn_change_keycode_off.png")
    ImageResource btn_create_keycode_password_off();

    @Source("btn_change_keycode_on.png")
    ImageResource btn_create_keycode_password_on();
	
    @Source("btn_create_password_off.png")
    ImageResource btn_create();

    @Source("btn_create_password_on.png")
    ImageResource btn_create_on();

    @Source("btn_delete_off.png")
    ImageResource btn_delete();

    @Source("btn_delete_on.png")
    ImageResource btn_delete_on();

    @Source("btn_revert_off.png")
    ImageResource btn_revert();

    @Source("btn_revert_on.png")
    ImageResource btn_revert_on();

    @Source("btn_edit_off.png")
    ImageResource btn_edit();

    @Source("btn_edit_on.png")
    ImageResource btn_edit_on();

    @Source("btn_save_off.png")
    ImageResource btn_save();

    @Source("btn_save_on.png")
    ImageResource btn_save_on();
    
    @Source("cerberus-web-icon-small.png")
    ImageResource cerberus_logo_small();

    public interface Style extends CssResource {
        String mainBlock();

        String nameSpan();
    }
}
