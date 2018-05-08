package com.zergwar.notui;

import java.awt.Font;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utilitaires de la NotUI
 */
public class NotUIUtils {
	
	private static Map<String, Font> cache = new ConcurrentHashMap<String, Font>();
	  
	public static Font getFont(String name) {
	    Font font = null;
	    if (cache != null) {
	      if ((font = cache.get(name)) != null) {
	        return font;
	      }
	    }
	    String fName = name;
	    try {
	      InputStream is = NotUIUtils.class.getResourceAsStream(fName);
	      font = Font.createFont(Font.TRUETYPE_FONT, is);
	    } catch (Exception ex) {
	      ex.printStackTrace();
	      System.err.println(fName + " not loaded.  Using serif font.");
	      font = new Font("serif", Font.PLAIN, 24);
	    }
	    return font;
	  }
}
