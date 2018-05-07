package com.zergwar.util.log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe utilitaire pour le log
 */
public final class Logger {

	// Sévérités
	public static int SEVERITY_NONE     = 0;
	public static int SEVERITY_INFO     = 1;
	public static int SEVERITY_WARN     = 2;
	public static int SEVERITY_ERROR    = 3;
	public static int SEVERITY_CRITICAL = 4;
	
	// Dateformatter
	public static SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd-HH:MM:ss");
	
	/**
	 * Loggue un message avec la sévérité par défaut
	 * (info)
	 * @param message
	 */
	public static void log(String message) {
		log(message, SEVERITY_NONE);
	}
	
	/**
	 * Ajoute une ligne au log
	 * @param message
	 * @param severity
	 */
	public static void log(String message, int severity) {
		String priority = "INFO";
		if(severity == 2) priority = "WARN";
		if(severity == 3) priority = "ERROR";
		if(severity == 4) priority = "CRITICAL";
		System.out.println("["+getTimestamp()+"], @["+getCallerClassName()+"], !["+priority+"] "+message);
	}
	
	/**
	 * Renvoie une empreinte de temps formattée
	 * @return
	 */
	private static String getTimestamp() {
		return dateFormat.format(new Date());
	}

	/**
	 * Méthode permettant de détecter la classe
	 * appelante, info nécessaire au débug
	 * @return
	 */
	public static String getCallerClassName() { 
	    StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
	    String callerClassName = null;
	    for (int i=1; i<stElements.length; i++) {
	        StackTraceElement ste = stElements[i];
	        if (callerClassName==null) {
	            callerClassName = ste.getClassName();
	        } else if (!callerClassName.equals(ste.getClassName())) {
	            return ste.getClassName();
	        }
	    }
	    return null;
	 }

	/**
	 * Affiche le contenu d'un tableau de STRING
	 * @param args
	 * @return
	 */
	public static String printStringTable(String[] strTable) {
		String result="";
		for(String str : strTable) {
			result+=","+str;
		}
		if(result.length()>1)
			return "["+result.substring(1)+"]";
		else
			return "[]";
	}
}
