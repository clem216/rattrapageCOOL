package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet1Planet extends Packet {

	public String name;
	public float coordX;
	public float coordY;
	public int diameter;
	public int ownerID;
	public int armyCount;
	
	/**
	 * Nouveau paquet de synchro planète
	 * @param name
	 * @param coordX
	 * @param coordY
	 * @param diameter
	 * @param ownerID
	 * @param armyCount
	 */
	public Packet1Planet(String name, float coordX, float coordY, int diameter, int ownerID, int armyCount)
	{
		super();
		
		this.name = name;
		this.coordX = coordX;
		this.coordY = coordY;
		this.diameter = diameter;
		this.ownerID = ownerID;
		this.armyCount = armyCount;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeString(name);
			this.writeFloat(coordX);
			this.writeFloat(coordY);
			this.writeInt(diameter);
			this.writeInt(ownerID);
			this.writeInt(armyCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET1PLANET;
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
			String name = dis.readUTF();
			float coordX = dis.readFloat();
			float coordY = dis.readFloat();
			int diameter = dis.readInt();
			int ownerID = dis.readInt();
			int armyCount = dis.readInt();
			dis.close();
			
			return new Packet1Planet(
				name,
				coordX,
				coordY,
				diameter,
				ownerID,
				armyCount
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet1Planet ***\n-> Name="+this.name
			  +"\n-> OwnerID="+this.ownerID
			  +"\n-> Coords={"+this.coordX+","+this.coordY+"}"
			  +"\n-> Diameter="+this.diameter
			  +"\n-> ArmyCount="+this.armyCount+"\n*** ]";
	}
}
