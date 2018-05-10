package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet14TransfertFailure extends Packet {

	public int playerID;
	public String sourcePlanet;
	public String destPlanet;
	public String failureReason;
	
	public Packet14TransfertFailure(int playerID, String sourcePlanet, String destPlanet, String error) {
		super();
		
		this.playerID = playerID;
		this.sourcePlanet = sourcePlanet;
		this.destPlanet = destPlanet;
		this.failureReason = error;
		
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
			this.writeString(this.failureReason);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET14TRANSFERTFAILURE;
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
			String failureReason = dis.readUTF();
			dis.close();
			
			return new Packet14TransfertFailure(
				playerID,
				sourcePlanet,
				destPlanet,
				failureReason
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet14TransfertFailure ***\n-> PlayerID="+this.playerID
			  +"\n-> PlanetSource="+this.sourcePlanet
			  +"\n-> PlanetDest="+this.destPlanet
			  +"\n-> FailReason="+this.failureReason
			  +"\n*** ]";
	}
}
