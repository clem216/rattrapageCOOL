package com.zergwar.util.config;

/**
 * Configuration par défaut !!
 */
public class Configuration {

	// Options d'équilibrage
	public static int NB_ZERG_DEPART = 200;
	public static int NOMBRE_JOUEURS = 2;
	public static int BONUS_DENSITE = 10;
	public static int BONUS_DIAMETRE = 10;
	public static int TAUX_REGEN_NOMINAL = 10;
	
	// Options de transfert
	public static int[] NOMBRE_TRANSFERTS = { 1, 2, 3, 4 };
	
	// Définition des planètes
	public static String[] PLANETES = {
		"Kepler 809.0 28.0 80",
		"Gliese 786.0 187.0 150",
		"CoRoT 797.0 407.0 40",
		"Wasp 550.0 187.0 60",
		"Ursae 89.0 190.0 150",
		"Kelt 309.0 137.0 60",
		"Leonis 25.0 22.0 100",
		"Rox 450.0 20.0 60",
		"Tau 30.0 410.0 60",
		"Eridani 400.0 270.0 60",
		"BetaPic 716.0 293.0 60",
		"Draconis 476.0 407.0 100"
	};
	
	// Définition des routes
	public static String[][] ROUTES = {
		{ "Leonis", "Rox" },
		{ "Leonis", "Tau" },
		{ "Leonis", "Ursae" },
		{ "Leonis", "Kelt" },
		{ "Tau", "Ursae" },
		{ "Tau", "Eridani" },
		{ "Tau", "Draconis" },
		{ "Ursae", "Kelt" },
		{ "Ursae", "Wasp" },
		{ "Ursae", "Eridani" },
		{ "Kelt", "Rox" },
		{ "Kelt", "Wasp" },
		{ "Kelt", "Eridani" },
		{ "Eridani", "Rox" },
		{ "Eridani", "Wasp" },
		{ "Eridani", "Draconis" },
		{ "Draconis", "Wasp" },
		{ "Draconis", "BetaPic" },
		{ "Draconis", "CoRoT" },
		{ "Rox", "Wasp" },
		{ "Rox", "Kepler" },
		{ "Wasp", "Kepler" },
		{ "Wasp", "Gliese" },
		{ "Wasp", "BetaPic" },
		{ "BetaPic", "Gliese" },
		{ "BetaPic", "CoRoT" },
		{ "Kepler", "Gliese" },
		{ "Gliese", "CoRoT" }, 
	};
	
}
