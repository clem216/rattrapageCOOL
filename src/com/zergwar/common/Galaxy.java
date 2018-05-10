package com.zergwar.common;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import com.zergwar.util.config.Configuration;
import com.zergwar.util.log.Logger;

public class Galaxy {

	public CopyOnWriteArrayList<Planet> planets;
	public CopyOnWriteArrayList<Route> routes;
	private int currentMap;
	
	// maps in static config
	public static int MAP_COOL          = 0;
	public static int MAP_CUSTOM_PAUL_1 = 1;
	
	/**
	 * Galaxie
	 */
	public Galaxy(int mapID) {
		this.currentMap = mapID;
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
		for(String planetStr : Configuration.PLANETES[this.currentMap]) {
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
		for(String[] route : Configuration.ROUTES[this.currentMap]) {
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

	/**
	 * Renvoie une planète vide au hasard parmi
	 * les planètes du jeu
	 * @return
	 */
	public Planet getRandomEmptyPlanet()
	{
		ArrayList<Planet> emptyPlanets = this.getEmptyPlanets();
		Random r = new Random();
		return emptyPlanets.get(r.nextInt(emptyPlanets.size()));
	}

	/**
	 * Renvoie la liste des planètes inoccupées de la galaxie
	 * @return
	 */
	private ArrayList<Planet> getEmptyPlanets() {
		ArrayList<Planet> emptyPlanets = new ArrayList<Planet>();
		for(Planet p : this.planets)
			if(p.isEmpty())
				emptyPlanets.add(p);
		return emptyPlanets;
	}

	/**
	 * Renvoie la route entre deux planètes,
	 * null si la planète cible égale la planète
	 * source ou si aucune route ne les relie
	 * directement
	 * @param sourcePlanet
	 * @param destPlanet
	 * @return
	 */
	public Route getRoute(String sourcePlanet, String destPlanet)
	{
		Planet s = getPlanetByName(sourcePlanet);
		Planet d = getPlanetByName(destPlanet);
		if(s == null || d == null) return null;
		
		for(Route r : routes) {
			if((r.getSource().equals(s) && r.getDest().equals(d))
			  || (r.getSource().equals(d) && r.getDest().equals(s)))
			  return r;
		}
		
		return null;
	}
}
