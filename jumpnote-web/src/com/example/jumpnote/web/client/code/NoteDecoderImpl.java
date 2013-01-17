package com.example.jumpnote.web.client.code;

//import java.io.UnsupportedEncodingException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.Provider;
//import java.security.Security;

/*
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
*/
import com.googlecode.gwt.crypto.bouncycastle.DataLengthException;
import com.googlecode.gwt.crypto.bouncycastle.InvalidCipherTextException;
import com.googlecode.gwt.crypto.client.AESFastCipher;

import com.google.gwt.user.client.Random;

public class NoteDecoderImpl implements NoteDecoder {
	
	//private String passcode;
	//private SecretKey key;
	private AESFastCipher cipher; 
	private final int KEY_LENGTH=16;
	
	private byte[] getKey(String passcode) {
		byte[] key = passcode.getBytes();
        if (key.length < KEY_LENGTH) {
            byte[] newkey = new byte[KEY_LENGTH];
            for (int i=0;i<newkey.length;i++) {
                newkey[i]=(byte)i;
            }
            for (int i=0;i<key.length;i++) {
                newkey[i]=key[i];
            }
            key=newkey;
        }

        return key;
	}
	
	NoteDecoderImpl(String passcode) {
		//this.passcode = passcode;
		byte[] key=getKey(passcode);
		//this.cipher = Cipher.getInstance("AES");
		//this.key = new SecretKeySpec(key, "AES"); // 256 bit key for AES
		this.cipher = new AESFastCipher();
		this.cipher.setKey(key);
	}
	

	
//	private void encode(String data) {
//		try {
//			byte[] keycode=getKey();
//			//Cipher cipher = Cipher.getInstance("AES"); 
//			SecretKey key = new SecretKeySpec(new byte[64], "AES"); // 256 bit key for AES      
//			cipher.init(Cipher.ENCRYPT_MODE, key);
//			cipher.
//		} catch (Exception e) {
//			System.out.println(e);
//		}
//	}
	
    // Private routine that does the gritty work.

//    private byte[] callCipher( byte[] data ) 
//    {
//    	
//    	cipher.decrypt(cipherText)
////        int    size = 
////                   cipher.getOutputSize( data.length );
////        byte[] result = new byte[ size ];
////        //int    olen = cipher.processBytes( data, 0,
////        //                      data.length, result, 0 );
////        int olen = cipher.update(data, 0, data.length, result, 0 );
////        olen += cipher.doFinal( result, olen );
////
////        if( olen < size ){
////            byte[] tmp = new byte[ olen ];
////            System.arraycopy( 
////                             result, 0, tmp, 0, olen );
////            result = tmp;
////        }
////
////        return result;
//    }
//
//    // Encrypt arbitrary byte array, returning the
//    // encrypted data in a different byte array.
//
//    public synchronized byte[] encrypt( byte[] data ) 
//    {
//        if( data == null || data.length == 0 ){
//            return new byte[0];
//        }
//
//        cipher.init( Cipher.ENCRYPT_MODE, key );
//        return callCipher( data );
//    }
//
//    // Encrypts a string.
//
//    public byte[] encryptString( String data ) 
//    {
//        if( data == null || data.length() == 0 ){
//            return new byte[0];
//        }
//        
//        byte[] s=null;
////        try {
//        	s=encrypt( data.getBytes("UTF-8") );
////        } catch (UnsupportedEncodingException use) {
////        	s=encrypt( data.getBytes() );
////        }        
//        return s;
//    }
//
//    // Decrypts arbitrary data.
//
//    public synchronized byte[] decrypt( byte[] data )
//    {
//        if( data == null || data.length == 0 ){
//            return new byte[0];
//        }
//
//        cipher.init( Cipher.DECRYPT_MODE, key );
//        return callCipher( data );
//    }
//
//    // Decrypts a string that was previously encoded
//    // using encryptString.
//
//    public String decryptString( byte[] data ) 
//    {
//        if( data == null || data.length == 0 ){
//            return "";
//        }
//       
//        String s=null;
////        try {
//        	s=new String( decrypt( data ),"UTF-8" );
////        } catch (UnsupportedEncodingException use) {
////        	s=new String(decrypt( data ));
////        }        
//        return s;
//    }
//    
//    private byte[] stringToByte(String source) {
//    	//String encoded = javax.xml.bind.DatatypeConverter.printBase64Binary(data);
//    	byte[] decoded = javax.xml.bind.DatatypeConverter.parseBase64Binary(source);
//    	return decoded;
//    }
//    
//    private String byteToString(byte[] source) {
//    	String encoded = javax.xml.bind.DatatypeConverter.printBase64Binary(source);
//    	//byte[] decoded = javax.xml.bind.DatatypeConverter.parseBase64Binary(encoded);
//    	return encoded;
//    }
	
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
    
	private String decodeString(String source) {
		boolean decodeSuccess=false;
		String output=null;
		try {
			output = cipher.decrypt(source);
			decodeSuccess=true;
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			
//			byte[] data=stringToByte(source);
//			output=decryptString(data);
//			decodeSuccess=true;
//		} catch (Exception e){
//			e.printStackTrace();
//		}
		
		if (decodeSuccess == false) {
			return generateRandomString(source);
		} else {
			return output;
		}
	}
	
	private String encodeString(String source) {
		boolean encodeSuccess=false;
		String output=null;
		try {
			output = cipher.encrypt(source);
			encodeSuccess = true;
		} catch (DataLengthException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidCipherTextException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		try {
//			byte[] data=encryptString(source);
//			output=byteToString(data);
//			encodeSuccess=true;
//		} catch (Exception e){
//			e.printStackTrace();
//		}
//		
		if (encodeSuccess == false) {
			return source;
		} else {
			return output;
		}
	}
	
	public boolean isDecoderValid() {
		return true;
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
		return encodeString(title);
	}
	
	public String encodeBody (String body) 
	{
		return encodeString(body);
	}

}
