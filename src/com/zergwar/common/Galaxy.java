package com.zergwar.common;

import java.util.concurrent.CopyOnWriteArrayList;

import com.zergwar.util.config.Configuration;
import com.zergwar.util.log.Logger;

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
	
	/**
	 * Initialise la galaxie sur la base des
	 * données de config
	 */
	public void initGalaxy() {
		Logger.log("Populating galaxy...");
		this.loadPlanets();
		this.loadRoutes();
		Logger.log("Galaxy loaded. "
				   +this.planets.size()
				   +" planet(s) and "
				   +this.routes.size()
				   +" route(s) loaded.");
	}
	
	/**
	 * Charge les planètes
	 */
	public void loadPlanets()
	{
		for(String planetStr : Configuration.PLANETES) {
			String[] planetStrT = planetStr.split(" ");
			if(planetStrT.length != 4) break;
			
			Planet planet = new Planet(
				planetStrT[0],
				Float.valueOf(planetStrT[1]),
				Float.valueOf(planetStrT[2]),
				Integer.valueOf(planetStrT[3]),
				-1,
				0
			);
			
			this.planets.add(planet);
		}
	}
	
	/**
	 * Charge les routes
	 */
	public void loadRoutes()
	{
		for(String[] route : Configuration.ROUTES) {
			if (route.length != 2) break;
			
			Planet source = getPlanetByName(route[0]);
			Planet dest = getPlanetByName(route[1]);
			
			if(source != null && dest != null)
				this.routes.add(new Route(source, dest));
		}
	}

	/**
	 * Renvoie une planète basée sur son nom
	 * ou null si aucune n'est trouvée
	 * @param string
	 * @return
	 */
	public Planet getPlanetByName(String name)
	{
		for(Planet p : this.planets)
			if(p.getName().equals(name)) return p;
		return null;
	}
}
