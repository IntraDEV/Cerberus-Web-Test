package com.intradev.cerberus.web.client.code;

import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.gwt.json.client.JSONObject;
import com.intradev.cerberus.allshared.Model;
import com.intradev.cerberus.web.client.ModelJso;

public class EncodedNote {
	
	private ModelJso.Note impl;
	
	private String cachedBody;
	private String cachedTitle;
	
	public EncodedNote(ModelJso.Note underlyingModel) {
		impl = underlyingModel;
	}
	
	public EncodedNote(String title, String body) {
		


    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
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
    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
    	cachedTitle = nd.decodeTitle(impl.getTitle());  	
    	return cachedTitle;
    }
//    public void setTitle(String title)
//    {
//    	cachedTitle=null;
//    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
//    	String newTitle = nd.encodeTitle(title);
//    	impl.setTitle(newTitle);
//    }
    public String getBody()
    {
//    	if (cachedBody != null) {
//    		return cachedBody;
//    	}
    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
    	cachedBody = nd.decodeBody(impl.getBody());
    	return cachedBody;
    }
//    public void setBody(String body)
//    {
//    	cachedBody=null;
//    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
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
    	NoteDecoder nd=NoteDecoderFactory.getNoteDecoder();
    	return nd.isDecoderValid(impl.getBody());
    }

}
