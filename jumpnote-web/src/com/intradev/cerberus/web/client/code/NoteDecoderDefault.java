package com.intradev.cerberus.web.client.code;

import com.google.gwt.user.client.Random;

public class NoteDecoderDefault implements NoteDecoder {
	
	
	public boolean isDecoderValid(String t) {
		return true;
	}
	
	public String decodeTitle (String title)
	{
		return title;
	}
	
	public String decodeBody (String body) 
	{
		return body;
	}
	
	public String encodeTitle (String title)
	{
		return title;
	}
	
	public String encodeBody (String body) 
	{
		return body;
	}

	
}
