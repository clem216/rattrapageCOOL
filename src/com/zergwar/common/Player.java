package com.zergwar.common;

import java.awt.Color;

public class Player {

	// Static
	public static int idCount = 1;
	
	// Name
	private String name;
	private int id;
	private Color color;
	
	/**
	 * Instancie le player
	 * @param name
	 */
	public Player(String name, Color color) {
		this.name = name;
		this.color = color;
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

	/**
	 * Renvoie la couleur du joueur
	 * @return
	 */
	public Color getColor() {
		return color;
	}
}