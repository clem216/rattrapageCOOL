package com.zergwar.network.packets;

import java.io.IOException;

public class Packet17AlreadyInGame extends Packet {

	public Packet17AlreadyInGame() {
		super();
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeByte((byte)0xDD);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET17ALREADYINGAME;
	}
	
	/**
	 * Construit une nouvelle instance
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data) {
		return new Packet17AlreadyInGame();
	}
}
