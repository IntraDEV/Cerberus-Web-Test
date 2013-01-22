package com.intradev.cerberus.web.client.code;

import com.google.gwt.json.client.JSONObject;
import com.intradev.cerberus.web.client.ModelJso;

public class EncodedPassword {
	
	private ModelJso.Password impl;
	
	private String cachedBody;
	private String cachedTitle;
	
	public EncodedPassword(ModelJso.Password underlyingModel) {
		impl = underlyingModel;
	}
	
	public EncodedPassword(String title, String body) {
		


    	PasswordDecoder nd=PasswordDecoderFactory.getPasswordDecoder();
    	String newTitle = nd.encodeTitle(title);
    	//impl.setTitle(newTitle);
    	
    	String newBody = nd.encodeBody(body);    	
    	//impl.setBody(newBody);
		
    	impl = ModelJso.Password.create(newTitle, newBody);
		
	}
	
    public String getId()
    {
    	return impl.getId();
    }
    
    public void setId(String id)
    {
    	impl.setId(id);
    }
    
    public String getOwnerId()
    {
    	return impl.getOwnerId();
    }
	
    public String getTitle()
    {
    	PasswordDecoder nd=PasswordDecoderFactory.getPasswordDecoder();
    	cachedTitle = nd.decodeTitle(impl.getTitle());  	
    	return cachedTitle;
    }

    public String getBody()
    {
    	PasswordDecoder nd=PasswordDecoderFactory.getPasswordDecoder();
    	cachedBody = nd.decodeBody(impl.getBody());
    	return cachedBody;
    }	
    
    public JSONObject getJSONObject() {
    	return new JSONObject(impl);
    }
    
    public boolean isDecodable() {
    	PasswordDecoder nd=PasswordDecoderFactory.getPasswordDecoder();
    	return nd.isDecoderValid(impl.getBody());
    }

}
