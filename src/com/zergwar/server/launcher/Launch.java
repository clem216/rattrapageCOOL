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
	 * Point d'entr�e du logiciel
	 * @param args
	 */
	public static void main(String[] args) {
		new Launch(args);
	}
	
	/**
	 * Classe de lancement instanci�e
	 * @param arguments
	 */
	public Launch(String[] arguments) {
		this.launchArguments = arguments;
		this.startServer();
		this.startTestClient();
	}
	
	/**
	 * D�marre le serveur
	 */
	public void startServer() {
		Logger.log("D�marrage du serveur...");
		GameServer server = new GameServer();
		server.start();
	}
	
	/**
	 * D�marre un client de test
	 */
	public void startTestClient() {
		Logger.log("D�marrage d'un client de test !");
	}
}
