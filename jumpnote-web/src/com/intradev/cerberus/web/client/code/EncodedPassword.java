package com.intradev.cerberus.web.client.code;

import com.google.gwt.json.client.JSONObject;
import com.intradev.cerberus.web.client.ModelJso;

public class EncodedPassword {
	
	private ModelJso.Note impl;
	
	private String cachedBody;
	private String cachedTitle;
	
	public EncodedPassword(ModelJso.Note underlyingModel) {
		impl = underlyingModel;
	}
	
	public EncodedPassword(String title, String body) {
		


    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
    	String newTitle = nd.encodeTitle(title);
    	//impl.setTitle(newTitle);
    	
    	String newBody = nd.encodeBody(body);    	
    	//impl.setBody(newBody);
		
    	impl = ModelJso.Note.create(newTitle, newBody);
		
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
//    	if (cachedTitle != null) {
//    		return cachedTitle;
//    	}
    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
    	cachedTitle = nd.decodeTitle(impl.getTitle());  	
    	return cachedTitle;
    }
//    public void setTitle(String title)
//    {
//    	cachedTitle=null;
//    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
//    	String newTitle = nd.encodeTitle(title);
//    	impl.setTitle(newTitle);
//    }
    public String getBody()
    {
//    	if (cachedBody != null) {
//    		return cachedBody;
//    	}
    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
    	cachedBody = nd.decodeBody(impl.getBody());
    	return cachedBody;
    }
//    public void setBody(String body)
//    {
//    	cachedBody=null;
//    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
//    	String newBody = nd.encodeBody(body);    	
//    	impl.setBody(newBody);
//    }

//	public boolean isPendingDelete() {
//		return impl.isPendingDelete();
//	}
//
//
//	public Date getCreatedDate() {
//		return impl.getCreatedDate();
//	}
//
//
//	public Date getModifiedDate() {
//		return impl.getModifiedDate();
//	}
	
    
    public JSONObject getJSONObject() {
    	return new JSONObject(impl);
    }
    
    public boolean isDecodable() {
    	PasswordDecoder nd=PasswordDecoderFactory.getNoteDecoder();
    	return nd.isDecoderValid(impl.getBody());
    }

}
