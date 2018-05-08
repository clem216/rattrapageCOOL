package com.zergwar.network.packets;

import java.io.IOException;

public class Packet4PlayerLeave extends Packet {

	public Packet4PlayerLeave() {
		super();
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeByte((byte)0xEF);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET0HANDSHAKE;
	}
	
	/**
	 * Construit une nouvelle instance
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data) {
		return new Packet4PlayerLeave();
	}
}
