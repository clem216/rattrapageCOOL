package com.zergwar.network.packets;

public class Packet0Handshake extends Packet {

	public Packet0Handshake() {
		super();
		
		// Construit le contenu
		this.append("<<HELLO>>");
	}
}
