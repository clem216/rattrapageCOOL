package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet6ProbePing extends Packet {

	public long timestamp;
	
	public Packet6ProbePing(long timestamp) {
		super();
		
		this.timestamp = timestamp;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeLong(this.timestamp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET6PROBEPING;
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
			long timestamp = dis.readLong();
			dis.close();
			
			return new Packet6ProbePing(
				timestamp
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet6ProbePing ***\n-> timestamp="+this.timestamp
			  +"\n*** ]";
	}
}
