package com.zergwar.server;

import com.zergwar.network.packets.Packet;

public class NetworkTransaction {

	public NetworkClient dest;
	public Packet packet;
	
	public NetworkTransaction(NetworkClient dest, Packet packet) {
		this.dest = dest;
		this.packet = packet;
	}
}
