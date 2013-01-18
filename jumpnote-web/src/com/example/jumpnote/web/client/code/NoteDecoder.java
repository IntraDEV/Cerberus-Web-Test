package com.example.jumpnote.web.client.code;

public interface NoteDecoder {
	
	public String decodeTitle (String title);
	public String decodeBody (String body);
	public String encodeTitle (String title);
	public String encodeBody (String body);
	
	public boolean isDecoderValid(String encrypted);

}
