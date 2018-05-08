package com.zergwar.common;

public class Route {

	private Planet source;
	private Planet dest;
	
	/**
	 * Instancie une route
	 * @param source
	 * @param dest
	 */
	public Route(Planet source, Planet dest) {
		this.source = source;
		this.dest = dest;
	}

	/**
	 * Renvoie la planete source
	 * @return
	 */
	public Planet getSource() {
		return this.source;
	}
	
	/**
	 * Renvoie la planete dest
	 * @return
	 */
	public Planet getDest() {
		return this.dest;
	}
}
