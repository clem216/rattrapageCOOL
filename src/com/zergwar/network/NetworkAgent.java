package com.zergwar.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

import com.zergwar.util.log.Logger;

/**
 * Agent de gestion du multijoueur
 * 
 * Protocole réseau :
 * ------------------
 * Applicable sur couche TCP et UDP, pas de reordering intégré.
 * Sans acquittement
 * 
 * Tramage :
 * ---------
 * [0651ff23ddee][ENTRY_01][ENTRY_02][ENTRY_03]...[ENTRY_N]
 *  -  en-tete -
 *  
 *  Détail des en-têtes :
 *  ---------------------
 *  [B][<DATASIZE>][datadatadata...data]
 *  1 octet de définition du type de données, 'i','f','b','l','d','c','S','B' ou type inconnu 'E'
 *  (optionnel) DATASIZE (int 4bytes) la longueur des données à suivre dans le cadre d'une chaine
 *  ou d'un bloc de datas binaires
 *  
 *  Contrôle d'intégrité des trames
 *  -------------------------------
 *  aucun d'intégré, repose sur implementation locale TCP.
 */

public class NetworkAgent {

	// constants
	public static int SERVER_PORT = 995; // < 1024, w/firewall
	
	// Privates
	private CopyOnWriteArrayList<NetworkEventListener> listeners;
	private CopyOnWriteArrayList<NetworkLink> links;
	private NetworkAgentExecutor agent;
	
	/** CONSTRUCTEUR **/
	public NetworkAgent() {
		this.listeners = new CopyOnWriteArrayList<NetworkEventListener>();
		this.links = new CopyOnWriteArrayList<NetworkLink>();
		this.agent = new NetworkAgentExecutor(this);
	}
	
	/**
	 * Démarre la gestion réseau
	 */
	public void start() {
		if(!this.agent.isStarted())
			this.agent.start();
		else
			Logger.log("NetworkAgent can't be started : already running. Skipping.");
	}
	
	/**
	 * Interrompt la gestion réseau
	 */
	public void stop() {
		if(this.agent.isStarted())
			this.agent.kill();
		else
			Logger.log("NetworkAgent can't be stopped : not running. Skipping.");
	}
	
	/**
	 * Ajoute un listener à la liste d'écoute
	 * @param listener : le listener à ajouter
	 */
	public void registerNetworkListener(NetworkEventListener listener) {
		if(this.listeners != null)
			this.listeners.add(listener);
	}
	
	/**
	 * Retire un listener de la liste d'écoute
	 * @param listener : le listener à retirer
	 */
	public void removeNetworkListener(NetworkEventListener listener) {
		if(this.listeners != null)
			this.listeners.remove(listener);
	}

	/**
	 * Efface tous les listeners
	 */
	public void clearNetworkListeners() {
		if(this.listeners != null)
			this.listeners.clear();
	}
	
	/**
	 * Nested class, moniteur réseau
	 */
	public class NetworkAgentExecutor extends Thread implements Runnable {
		
		private NetworkAgent agent;
		private boolean running;
		
		/** CONSTRUCTEUR **/
		public NetworkAgentExecutor(NetworkAgent agent) {
			this.agent = agent;
		}
		
		/**
		 * Stoppe l'exécuteur
		 */
		public void kill() {
			Logger.log("Stopping netagent...");
			this.running = false;
		}

		/**
		 * Renvoie le statut d'exécution de l'exécuteur
		 * @return
		 */
		public boolean isStarted() {
			return running;
		}

		/**
		 * Boucle réseau
		 */
		public void run()
		{
			try
			{
				ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
				
				while(running) {
					Socket socket = serverSocket.accept();
					this.onInboundConnection(socket);
				}
				
				serverSocket.close();
			} catch (IOException e) {
				Logger.log("netagent just crashed. Reason follows.");
				e.printStackTrace();
			}
			
			Logger.log("Netagent stopped.");
		}
		
		/**
		 * Sur une connexion entrante
		 * @param socket
		 */
		private void onInboundConnection(Socket socket) {
			if(links != null)
			{
				NetworkLink link = new NetworkLink(agent, socket);
				Logger.log("Creation du Link avec le client entrant via socket @"+socket);
				links.add(link);
			} else
				Logger.log("Links unitialized. This is impossible, but just happened. Too bad.");
		}

		@Override
		public void start() {
			Logger.log("Starting netagent...");
			this.running = true;
			super.start();
		}
	}
}
