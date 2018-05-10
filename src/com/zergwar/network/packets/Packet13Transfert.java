package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet13Transfert extends Packet {

	public int playerID;
	public String sourcePlanet;
	public String destPlanet;

	public Packet13Transfert(int playerID, String sourcePlanet, String destPlanet) {
		super();
		
		this.playerID = playerID;
		this.sourcePlanet = sourcePlanet;
		this.destPlanet = destPlanet;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeString(this.sourcePlanet);
			this.writeString(this.destPlanet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET13TRANSFERT;
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
			int playerID = dis.readInt();
			String sourcePlanet = dis.readUTF();
			String destPlanet = dis.readUTF();
			dis.close();
			
			return new Packet13Transfert(
				playerID,
				sourcePlanet,
				destPlanet
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet12PlanetSelect ***\n-> PlayerID="+this.playerID
			  +"\n-> PlanetSource="+this.sourcePlanet
			  +"\n-> PlanetDest="+this.destPlanet
			  +"\n*** ]";
	}
}
