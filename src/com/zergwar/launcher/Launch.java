package com.zergwar.launcher;

import javax.swing.JOptionPane;

import com.zergwar.client.GameClient;
import com.zergwar.server.GameServer;
import com.zergwar.util.config.Configuration;
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
		
		Object[] options = {"Lancer le client",
		                    "Lancer le serveur",
		                    "Fermer"};
		
		int result = JOptionPane.showOptionDialog(null,
		    "Souhaitez-vous d�marrer le serveur ZergWar, ou un client de test ?",
		    "Choix du mode de lancement",
		    JOptionPane.YES_NO_CANCEL_OPTION,
		    JOptionPane.QUESTION_MESSAGE,
		    null,
		    options,
		    options[2]);
		
		switch(result) {
			case 0:
				this.startTestClient();
				break;
			case 1:
				this.startServer();
				break;
			default:
				System.exit(0);
				break;
		}
		
	}
	
	/**
	 * D�marre le serveur
	 */
	public void startServer() {
		Logger.log("D�marrage du serveur...");
		String mapIndex = JOptionPane.showInputDialog("Saisir l'ID de map souhait� :\n[0] Map COOL\n[1] Custom Large 1", "0");
		
		int mapID = -1;
		
		try {
			mapID = Integer.valueOf(mapIndex);
		} catch(Exception e) {
			JOptionPane.showConfirmDialog(null, "Erreur : map invalide !");
		}
		
		if(mapID > Configuration.PLANETES.length && mapID < 0)
			JOptionPane.showConfirmDialog(null, "Erreur : map invalide !");
		
		// D�marre le serveur
		GameServer server = new GameServer(mapID);
		server.start();
	}
	
	/**
	 * D�marre un client de test
	 */
	public void startTestClient() {
		Logger.log("D�marrage d'un client de test !");
		String serverIP = JOptionPane.showInputDialog("Entrez l'adresse IPv4 du serveur :", "127.0.0.1");
		new GameClient(serverIP, 65530);
	}
}
