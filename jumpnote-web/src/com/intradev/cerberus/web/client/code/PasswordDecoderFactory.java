package com.intradev.cerberus.web.client.code;

public class PasswordDecoderFactory {
	
	private static PasswordDecoder passwordDecoder = new PasswordDecoderDefault();
	
	
	public static void installNoteDecoder (String passcode) {
		passwordDecoder = new PasswordDecoderImpl(passcode);
		
	}
	
	public static PasswordDecoder getNoteDecoder() {
		return passwordDecoder;
	}

}
