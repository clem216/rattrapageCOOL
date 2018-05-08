package com.zergwar.util.math;

import java.nio.ByteBuffer;

public class ByteUtils {

	/**
	 * Convertit un entier signé en tableau d'octets
	 * @param value
	 * @return
	 */
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	/**
	 * Convertit un tableau d'octets en entier signé
	 * @param bytes
	 * @return
	 */
	public static int byteArrayToInt( byte[] b ) {
		return (b[0] << 24)&0xff000000|
			   (b[1] << 16)&0x00ff0000|
			   (b[2] <<  8)&0x0000ff00|
			   (b[3] <<  0)&0x000000ff;
	  }
	
	/**
	 * Concatène deux tableaux de bytes
	 * @param a
	 * @param b
	 * @return
	 */
	public static byte[] concatenate(byte[] a, byte[] b) {
		byte[] c = new byte[a.length + b.length];
		System.arraycopy(a, 0, c, 0, a.length);
		System.arraycopy(b, 0, c, a.length, b.length);
		return c;
	}
	
	/**
	 * Concatène N tableaux de bytes
	 * @param b
	 * @return
	 */
	public static byte[] concatenate(byte[]... b) {
		byte[] r = new byte[]{};
		for(byte[] bt : b)
			r = concatenate(r, bt);
		return r;
	}
	
	/**
	 * Octets vers chaine hexa
	 */
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesArrayToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	/**
	 * Hexa vers octets
	 * @param s
	 * @return
	 */
	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	/**
	 * Float vers bytes
	 * @param value
	 * @return
	 */
	public static byte[] floatToByteArray(float value) {
	    byte[] bytes = new byte[4];
	    ByteBuffer.wrap(bytes).putFloat(value);
	    return bytes;
	}

	/**
	 * Bytes vers float
	 * @param bytes
	 * @return
	 */
	public static float byteArrayToFloat(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getFloat();
	}
}
