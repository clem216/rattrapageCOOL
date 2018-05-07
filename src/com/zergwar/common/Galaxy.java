package com.zergwar.common;

import java.util.concurrent.CopyOnWriteArrayList;

public class Galaxy {

	public CopyOnWriteArrayList<Planet> planets;
	public CopyOnWriteArrayList<Route> routes;
	
	/**
	 * Galaxie
	 */
	public Galaxy() {
		this.planets = new CopyOnWriteArrayList<Planet>();
		this.routes = new CopyOnWriteArrayList<Route>();
	}
}
