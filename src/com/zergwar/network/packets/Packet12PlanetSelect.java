package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet12PlanetSelect extends Packet {

	public int playerID;
	public String planetName;
	public int selectionType;
	
	public Packet12PlanetSelect(int playerID, String planetName, int selectionType) {
		super();
		
		this.playerID = playerID;
		this.planetName = planetName;
		this.selectionType = selectionType;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeString(this.planetName);
			this.writeInt(this.selectionType);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET12PLANETSELECT;
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
			String planetName = dis.readUTF();
			int selectionType = dis.readInt();
			dis.close();
			
			return new Packet12PlanetSelect(
				playerID,
				planetName,
				selectionType
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet12PlanetSelect ***\n-> PlayerID="+this.playerID
			  +"\n-> PlanetName="+this.planetName
			  +"\n-> SelectionType="+this.selectionType
			  +"\n*** ]";
	}
}
