package com.example.jumpnote.web.client.code;

import com.google.gwt.user.client.Random;

public class NoteDecoderDefault implements NoteDecoder {
	
	
	private String decodeString(String source) {
		return generateRandomString(source);
	}
	
	private String generateRandomString(String source) {
		int length=source.length();
		StringBuffer sb  = new StringBuffer(length);
		
		for (int i=0;i<length;i++) {
			int rand = Random.nextInt()%(26*2);
			char character;
			if (rand >= 26) {
				//Caps
				character = (char)('A' + (char)(rand-26));
			} else {
				//lowercase
				character = (char)('a' + (char)(rand));
			}
			sb.append(character);
		}
		return sb.toString();
	}
	
	public boolean isDecoderValid() {
		return false;
	}
	
	public String decodeTitle (String title)
	{
		return decodeString(title);
	}
	
	public String decodeBody (String body) 
	{
		return decodeString(body);
	}
	
	public String encodeTitle (String title)
	{
		//TODO, throw exception here, don't let the user corrupt his own data
		return title;
	}
	
	public String encodeBody (String body) 
	{
		//TODO, throw exception here, don't let the user corrupt his own data
		return body;
	}

	
}
