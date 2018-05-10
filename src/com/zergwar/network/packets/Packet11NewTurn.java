package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet11NewTurn extends Packet {

	public int playerID;
	public int transferCount;
	
	public Packet11NewTurn(int playerID, int transferCount) {
		super();
		
		this.playerID = playerID;
		this.transferCount = transferCount;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeInt(this.transferCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET11NEWTURN;
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
			
			return new Packet11NewTurn(
				playerID,
				transferCount
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet11NewTurn ***\n-> PlayerID="+this.playerID
			  +"\n-> TransferCount="+this.transferCount
			  +"\n*** ]";
	}
}
