package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet3PlayerJoin extends Packet {

	public int playerID;
	public int playerColor;
	public String playerName;
	public boolean playerReady;
	
	public Packet3PlayerJoin(String name, int id, int color, boolean ready) {
		super();
		
		this.playerName = name;
		this.playerID = id;
		this.playerColor = color;
		this.playerReady = ready;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeInt(this.playerColor);
			this.writeString(this.playerName);
			this.writeBoolean(this.playerReady);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET3PLAYERJOIN;
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
			int playerColor = dis.readInt();
			String playerName = dis.readUTF();
			boolean playerReady = dis.readBoolean();
			dis.close();
			
			return new Packet3PlayerJoin(
				playerName,
				playerID,
				playerColor,
				playerReady
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet3PlayerJoin ***\n-> PlayerID="+this.playerID
			  +"\n-> PlayerColor="+this.playerColor
			  +"\n-> PlayerName="+this.playerName
			  +"\n-> PlayerReady="+this.playerReady
			  +"\n*** ]";
	}
}
