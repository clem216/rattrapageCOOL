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
	public static String[][] PLANETES = {
		
		// MAP 0 : COOL
		{
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
		},
		
		// MAP 1 : LARGE 1
		{
			"Aiur 108.0 59.0 100",
			"Calliope 37.0 130.0 60",
			"Eugenia 118.0 180.0 80",
			"Illa 234.0 100.0 50",
			"Cyclades 250.0 200.0 40",
			"Norion 395.0 257.0 70",
			"Lapalak 170.0 303.0 50",
			"Char 92.0 340.0 100",
			"Ixos 213.0 365.0 80",
			"Praxis 144.0 422.0 70",
			"Ulnar 420.0 365.0 40",
			"ChauSara 528.0 325.0 60",
			"MarSara 536.0 395.0 50",
			"NeoAntioch 616.0 402.0 90",
			"Hyperion 467.0 428.0 60",
			"Atraxis 511.0 196.0 100",
			"Alinor 633.0 264.0 50",
			"Zerus 519.0 97.0 80",
			"SolIV 591.0 82.0 90",
			"Kerbin 655.0 145.0 80",
			"Ulysse 750.0 181.0 40",
			"Rubeus 720.0 396.0 130",
			"Ganymède 379.0 39.0 110"
		}
	};
	
	// Définition des routes
	public static String[][][] ROUTES = {
		
		// MAP 0 : COOL
		{
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
			{ "Gliese", "CoRoT" }
		},
		
		// MAP 1 : LARGE CUSTOM
		{
			{ "ChauSara", "MarSara" },
			{ "ChauSara", "NeoAntioch" },
			{ "Ulnar", "Norion" },
			{ "Cyclades", "Norion" },
			{ "MarSara", "NeoAntioch" },
			{ "Rubeus", "NeoAntioch" },
			{ "Ulnar", "Hyperion" },
			{ "Rubeus", "ChauSara" },
			{ "Ulnar", "MarSara" },
			{ "Norion", "Atraxis" },
			{ "Norion", "Alinor" },
			{ "Norion", "Ixos" },
			{ "Ixos", "Ulnar" },
			{ "Ixos", "Praxis" },
			{ "Praxis", "Lapalak" },
			{ "Lapalak", "Char" },
			{ "Char", "Eugenia" },
			{ "Eugenia", "Calliope" },
			{ "Eugenia", "Aiur" },
			{ "Eugenia", "Illa" },
			{ "Illa", "Aiur" },
			{ "Cyclades", "Illa" },
			{ "Zerus", "SolIV" },
			{ "Zerus", "Atraxis" },
			{ "Zerus", "Alinor" },
			{ "Char", "Praxis" },
			{ "Calliope", "Aiur" },
			{ "Lapalak", "Eugenia" },
			{ "Lapalak", "Ixos" },
			{ "Ixos", "Hyperion" },
			{ "Hyperion", "MarSara" },
			{ "ChauSara", "Alinor" },
			{ "Atraxis", "Alinor" },
			{ "Kerbin", "Alinor" },
			{ "Kerbin", "SolIV" },
			{ "Kerbin", "Ulysse" },
			{ "Rubeus", "Ulysse" },
			{ "Zerus", "Illa" },
			{ "Rubeus", "Alinor" },
			{ "Lapalak", "Cyclades" },
			{ "Norion", "Illa" },
			{ "Ganymède", "Illa" },
			{ "Ganymède", "Zerus" },
			{ "Alinor", "Ulysse" },
		}
	};
	
}
