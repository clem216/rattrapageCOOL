package com.zergwar.server;

import com.zergwar.network.packets.Packet;

public class NetworkTransaction {

	private NetworkClient dest;
	private Packet packet;
	
	public NetworkTransaction(NetworkClient dest, Packet packet) {
		this.dest = dest;
		this.packet = packet;
	}

	/**
	 * Renvoie le paquet lié à la transaction
	 * @return
	 */
	public Packet getPacket() {
		return packet;
	}
	
	/**
	 * Renvoie le client lié à la transaction
	 * @return
	 */
	public NetworkClient getClient() {
		return this.dest;
	}
}
