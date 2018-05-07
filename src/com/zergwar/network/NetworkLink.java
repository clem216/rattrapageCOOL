package com.zergwar.network;

import java.net.Socket;

import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;

/**
 * Le networklink gère la connexion entre le serveur
 * et un client, sur un thread de communication dédié
 * ainsi que les éventuelles erreurs / déconnexions
 * de la ligne.
 */
public class NetworkLink extends Thread implements Runnable {

	private Socket socket;
	private NetworkAgent agent;
	private NetworkLinkState state;
	
	public NetworkLink(NetworkAgent agent, Socket socket) {
		this.socket = socket;
		this.agent = agent;
		this.state = NetworkLinkState.INACTIVE;
	}
	
	/**
	 * Ouvre la discussion
	 */
	public void open() {
		this.sendPacket(new Packet0Handshake());
	}
	
	/**
	 * Ferme le link
	 */
	public void close() {
		
	}
	
	/**
	 * Envoie un packet
	 * @param packet
	 */
	public void sendPacket(Packet packet) {
		
	}
}
