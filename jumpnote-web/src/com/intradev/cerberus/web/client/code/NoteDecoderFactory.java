package com.intradev.cerberus.web.client.code;

public class NoteDecoderFactory {
	
	private static NoteDecoder noteDecoder = new NoteDecoderDefault();
	
	
	public static void installNoteDecoder (String passcode) {
		noteDecoder = new NoteDecoderImpl(passcode);
		
	}
	
	public static NoteDecoder getNoteDecoder() {
		return noteDecoder;
	}

}
