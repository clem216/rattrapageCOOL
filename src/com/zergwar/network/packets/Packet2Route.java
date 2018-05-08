package com.zergwar.network.packets;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Packet2Route extends Packet {

	public String source;
	public String destination;
	
	public Packet2Route(String source, String destination) {
		super();
		
		this.source = source;
		this.destination = destination;
		
		this.init();
	}
	
	/**
	 * Initialise le contenu
	 */
	public void init() {
		try {
			this.writeString(source);
			this.writeString(destination);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Renvoie l'ID de paquet
	 */
	@Override
	public int getPacketID() {
		return Packet.ID_PACKET2ROUTE;
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
			String source = dis.readUTF();
			String destination = dis.readUTF();
			dis.close();
			
			return new Packet2Route(
				source,
				destination
			);
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		return "[Packet2Route ***\n-> Source="+this.source
			  +"\n-> Destination="+this.destination
			  +"\n*** ]";
	}
}
