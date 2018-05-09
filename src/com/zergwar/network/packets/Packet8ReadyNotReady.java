package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet8ReadyNotReady extends Packet {

	public int playerID;
	public boolean readyState;
	
	/**
	 * Instancie un paquet ready not ready
	 * @param playerID
	 * @param readyState
	 */
	public Packet8ReadyNotReady(int playerID, boolean readyState)
	{
		super();
		
		this.playerID = playerID;
		this.readyState = readyState;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeInt(this.playerID);
			this.writeBoolean(this.readyState);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET8READYNOTREADY;
	}
	
	/**
	 * Construit une nouvelle instance
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data)
	{
		try
		{
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
			int playerID = dis.readInt();
			boolean readyState = dis.readBoolean();
			dis.close();
			
			return new Packet8ReadyNotReady(
				playerID,
				readyState
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet8ReadyNotReady ***\n-> playerID=" + this.playerID
			  +"\n-> PlayerReady=" + this.readyState
			  +"\n*** ]";
	}
}
