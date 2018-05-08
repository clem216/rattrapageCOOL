package com.zergwar.network.packets;

import java.io.IOException;

public class Packet7ProbePong extends Packet {

	public Packet7ProbePong() {
		super();
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeByte((byte)0xFE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET7PROBEPONG;
	}
	
	/**
	 * Construit une nouvelle instance
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data) {
		return new Packet7ProbePong();
	}
}
