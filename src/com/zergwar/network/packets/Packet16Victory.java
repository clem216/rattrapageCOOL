package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet16Victory extends Packet {

	public int playerID;
	public int finalZergCount;
	
	public Packet16Victory(int playerID, int finalZergCount) {
		super();
		
		this.playerID = playerID;
		this.finalZergCount = finalZergCount;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeInt(this.finalZergCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET16VICTORY;
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
			int transferCount = dis.readInt();
			dis.close();
			
			return new Packet16Victory(
				playerID,
				transferCount
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet16Victory ***\n-> PlayerID="+this.playerID
			  +"\n-> FinalZergCount="+this.finalZergCount
			  +"\n*** ]";
	}
}
