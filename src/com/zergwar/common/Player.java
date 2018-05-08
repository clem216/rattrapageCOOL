package com.zergwar.common;

public class Player {

	// Static
	public static int idCount = 1;
	
	// Name
	private String name;
	private int id;
	
	/**
	 * Instancie le player
	 * @param name
	 */
	public Player(String name) {
		this.name = name;
		this.id = idCount++;
	}
	
	/**
	 * Renvoie l'ID de joueur
	 * @return
	 */
	public int getID() {
		return this.id;
	}
	
	/**
	 * Renvoie le nom du joueur
	 * @return
	 */
	public String getName() {
		return this.name;
	}
}