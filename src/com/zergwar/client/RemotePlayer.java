package com.zergwar.client;

import java.awt.Color;

public class RemotePlayer {

	private int id;
	private Color color;
	private String playerName;
	private boolean ready;
	
	public RemotePlayer(String playerName, int playerID, Color color) {
		this.id = playerID;
		this.playerName = playerName;
		this.color = color;
		this.ready = false;
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
	
	/**
	 * Renvoie l'état ready
	 * @return
	 */
	public boolean isReady() {
		return this.ready;
	}

	/**
	 * Set readyness level
	 * @param readyState
	 */
	public void setReady(boolean readyState) {
		this.ready = readyState;
	}

}
