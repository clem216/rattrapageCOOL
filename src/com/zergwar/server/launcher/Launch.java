package com.zergwar.server.launcher;

import com.zergwar.common.GameServer;
import com.zergwar.util.log.Logger;

/**
 * Classe de lancement
 */
public class Launch {

	@SuppressWarnings("unused")
	private String[] launchArguments;
	
	/**
	 * Point d'entrée du logiciel
	 * @param args
	 */
	public static void main(String[] args) {
		new Launch(args);
	}
	
	/**
	 * Classe de lancement instanciée
	 * @param arguments
	 */
	public Launch(String[] arguments) {
		this.launchArguments = arguments;
		this.startServer();
		this.startTestClient();
	}
	
	/**
	 * Démarre le serveur
	 */
	public void startServer() {
		Logger.log("Démarrage du serveur...");
		GameServer server = new GameServer();
		server.start();
	}
	
	/**
	 * Démarre un client de test
	 */
	public void startTestClient() {
		Logger.log("Démarrage d'un client de test !");
	}
}
