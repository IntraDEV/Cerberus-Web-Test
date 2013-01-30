
package com.intradev.cerberus.web.client.resources;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

/**
 * Resources used by the web client.
 */
public interface Resources extends ClientBundle {
	
    @Source("cerberus-web-icon-small.png")
    ImageResource cerberus_logo_small();

    public interface Style extends CssResource {
        String mainBlock();

        String nameSpan();
    }
}
