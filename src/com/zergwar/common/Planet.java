package com.zergwar.common;

public class Planet {

	private String name;
	private float coordX;
	private float coordY;
	private int diameter;
	private Player owner;
	
	public Planet(String name, Float coordX, Float coordY, Integer diameter) {
		this.name = name;
		this.coordX = coordX;
		this.coordY = coordY;
		this.diameter = diameter;
		this.owner = null;
	}

	/**
	 * Renvoie le nom de la plan�te
	 * @return
	 */
	public String getName() {
		return this.name;
	}
	
	/**
	 * renvoie X de la planete
	 * @return
	 */
	public float getX() {
		return coordX;
	}
	
	/**
	 * renvoie Y de la planete
	 * @return
	 */
	public float getY() {
		return coordY;
	}
	
	/**
	 * Renvoie le diam�tre de la plan�te
	 * @return
	 */
	public int getDiameter() {
		return this.diameter;
	}

	/**
	 * Renvoie le propri�taire de la plan�te
	 * @return
	 */
	public Player getOwner() {
		return this.owner;
	}
}
