package com.zergwar.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;
import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet4PlayerLeave;
import com.zergwar.util.log.Logger;

/**
 * Agent de gestion du multijoueur
 * 
 * Protocole réseau :
 * ------------------
 * Applicable sur couche TCP, sur UDP pas de reordering intégré.
 * Sans acquittement systématique
 * 
 * Tramage :
 * ---------
 * [0651ff23ddee][DataLength][ENTRY_01][ENTRY_02][ENTRY_03]...[ENTRY_N]
 *  -  en-tete - int 32bits - 
 *  
 *  Data Length est un entier de 32 bits qui contient la taille de tout
 *  ce qui va suivre !
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
	
	// Privates
	private CopyOnWriteArrayList<NetworkEventListener> listeners;
	private CopyOnWriteArrayList<NetworkClient> clients;
	private NetworkAgentExecutor agent;
	private int serverPort;
	
	/** CONSTRUCTEUR **/
	public NetworkAgent(int port) {
		this.serverPort = port;
		this.listeners = new CopyOnWriteArrayList<NetworkEventListener>();
		this.clients = new CopyOnWriteArrayList<NetworkClient>();
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
				ServerSocket serverSocket = new ServerSocket(serverPort);
				
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
			if(clients != null)
			{
				NetworkClient client = new NetworkClient(agent, socket);
				Logger.log("Inbound connection from @"+socket);
				clients.add(client);
			} else
				Logger.log("Unable to connect");
		}

		@Override
		public void start() {
			Logger.log("Starting netagent...");
			this.running = true;
			super.start();
		}
	}

	/**
	 * Un client a cessé de fonctionner
	 * @param networkClient
	 */
	public void onClientDisconnected(NetworkClient networkClient, NetworkCode reason)
	{			
		// D'abord, notifier du quit
		this.broadcast(
			new Packet4PlayerLeave(networkClient.getPlayerId()),
			networkClient
		);
		
		// Ensuite, MaJ du datamodel
		for(NetworkEventListener listener : this.listeners)
			listener.onClientDisconnected(networkClient, reason);
		this.clients.remove(networkClient);
	}

	/**
	 * Un client a envoyé un paquet
	 * @param networkClient
	 * @param packet
	 */
	public void onPacketReceived(NetworkClient networkClient, Packet packet) {
		for(NetworkEventListener listener : this.listeners)
			listener.onClientPacketReceived(networkClient, packet);
	}

	/**
	 * Envoie un paquet à tous les clients (author-exclusif)
	 * @param packet
	 * @param author 
	 */
	public void broadcast(Packet packet, NetworkClient author) {
		for(NetworkClient client : this.clients)
			if(client != author)
				client.sendPacket(packet);
	}

	/**
	 * Renvoie la table de clients connectés
	 * @return
	 */
	public CopyOnWriteArrayList<NetworkClient> getClients() {
		return this.clients;
	}
}
