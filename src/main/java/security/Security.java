/*
 * 
 * 36E CLOUD CONNECTOR
 * This is a project of 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * This project is a closed source and proprietary software package.
 * None of the contents of this software is to be used for uses not intended,
 * And no one is to interface with the software in methods not defined or previously decided by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * No changes should be done to this project without prior authorization by 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 2017 (C) 36TH ELEMENT TECHNOLOGIES PVT. LTD.
 * 
 */
package security;

import java.security.MessageDigest;
import java.util.Random;

/**
 *
 * @author karan This class is mainly created to help the other classes of the
 * security package.
 *
 */
public class Security {

    // Generate Random string of 'x' size.
    public static String getSaltString(int size) {
	String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890_-";
	StringBuilder salt = new StringBuilder();
	Random rnd = new Random();
	while (salt.length() < size) { // length of the random string.
	    int index = (int) (rnd.nextFloat() * SALTCHARS.length());
	    salt.append(SALTCHARS.charAt(index));
	}
	String saltStr = salt.toString();
	return saltStr;

    }

    //// Hash Algorithms.
    // MD5 Hashing.
    public static String hashMD5(String plainText) {
	try {
	    // Lets hash the plain text and return the hash value.
	    MessageDigest md5 = MessageDigest.getInstance("MD5");
	    byte[] array = md5.digest(plainText.getBytes());
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < array.length; ++i) {
		sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
	    }
	    return sb.toString();

	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

    // SHA-1 Hashing
    public static String hashSHA1(String plainText) {
	try {
	    // Lets hash the plain text and return the hash value.
	    MessageDigest md5 = MessageDigest.getInstance("SHA-1");
	    byte[] array = md5.digest(plainText.getBytes());
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < array.length; ++i) {
		sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
	    }
	    return sb.toString();

	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }
    
    // SHA - 256 Hashing.
    public static String hashSHA256(String plainText) {
	try {
	    // Lets hash the plain text and return the hash value.
	    MessageDigest md5 = MessageDigest.getInstance("SHA-256");
	    byte[] array = md5.digest(plainText.getBytes());
	    StringBuilder sb = new StringBuilder();
	    for (int i = 0; i < array.length; ++i) {
		sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
	    }
	    return sb.toString();

	} catch (Exception e) {
	    // There was an Error.
	    return null;
	}
    }

}
