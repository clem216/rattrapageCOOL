package com.zergwar.tests;

import java.awt.Color;

public class RemotePlayer {

	private int id;
	private Color color;
	private String playerName;
	
	public RemotePlayer(String playerName, int playerID, Color color) {
		this.id = playerID;
		this.playerName = playerName;
		this.color = color;
	}
	
	/**
	 * Renvoie l'ID de joueur
	 * @return
	 */
	public int getPlayerID() {
		return this.id;
	}
	
	/**
	 * Renvoie la couleur du joueur
	 * @return
	 */
	public Color getPlayerColor() {
		return this.color;
	}
	
	/**
	 * Renvoie le nom du joueur
	 * @return
	 */
	public String getName() {
		return this.playerName;
	}

}
