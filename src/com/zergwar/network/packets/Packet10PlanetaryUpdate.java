package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet10PlanetaryUpdate extends Packet {

	public String planetName;
	public int ownerID;
	public int armyCount;
	
	public Packet10PlanetaryUpdate(String planetName, int ownerID, int armyCount) {
		super();
		
		this.planetName = planetName;
		this.ownerID = ownerID;
		this.armyCount = armyCount;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeString(this.planetName);
			this.writeInt(this.ownerID);
			this.writeInt(this.armyCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET10PLANETARYUPDATE;
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
			String planetName = dis.readUTF();
			int ownerID = dis.readInt();
			int armyCount = dis.readInt();
			dis.close();
			
			return new Packet10PlanetaryUpdate(
				planetName,
				ownerID,
				armyCount
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet10PlanetaryUpdate ***\n-> PlanetName="+this.planetName
			  +"\n-> OwnerID="+this.ownerID
			  +"\n-> ArmyCount="+this.armyCount
			  +"\n*** ]";
	}
}
