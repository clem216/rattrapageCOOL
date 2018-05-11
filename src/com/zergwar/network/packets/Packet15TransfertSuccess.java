package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet15TransfertSuccess extends Packet {

	public String sourcePlanet;
	public String destPlanet;
	public int transferType;
	
	public Packet15TransfertSuccess(String sourcePlanet, String destPlanet, int transferType) {
		super();
		
		this.sourcePlanet = sourcePlanet;
		this.destPlanet = destPlanet;
		this.transferType = transferType;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeString(this.sourcePlanet);
			this.writeString(this.destPlanet);
			this.writeInt(this.transferType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET15TRANSFERTSUCCESS;
	}
	
	/**
	 * Construit une nouvelle instance
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data) {
		try
		{
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			String sourcePlanet = dis.readUTF();
			String destPlanet = dis.readUTF();
			int transferType = dis.readInt();
			dis.close();
			
			return new Packet15TransfertSuccess(
				sourcePlanet,
				destPlanet,
				transferType
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
