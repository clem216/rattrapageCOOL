package com.zergwar.network.packets;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import com.zergwar.util.math.ByteUtils;

public class Packet {

	// constants
	public static final int ID_PACKET0HANDSHAKE         = 0;
	public static final int ID_PACKET1PLANET            = 1;
	public static final int ID_PACKET2ROUTE             = 2;
	public static final int ID_PACKET3PLAYERJOIN        = 3;
	public static final int ID_PACKET4PLAYERLEAVE       = 4;
	public static final int ID_PACKET5PLAYERINFO        = 5;
	public static final int ID_PACKET6PROBEPING         = 6;
	public static final int ID_PACKET7PROBEPONG         = 7;
	public static final int ID_PACKET8READYNOTREADY     = 8;
	public static final int ID_PACKET9GAMESTART         = 9;
	public static final int ID_PACKET10PLANETARYUPDATE  = 10;
	public static final int ID_PACKET11NEWTURN          = 11;
	public static final int ID_PACKET12PLANETSELECT     = 12;
	public static final int ID_PACKET13TRANSFERT        = 13;
	public static final int ID_PACKET14TRANSFERTFAILURE = 14;
	public static final int ID_PACKET15TRANSFERTSUCCESS = 15;

	/**
	 * Structure du paquet :
	 * [HEADER][PKLENGTH][[PKTYPE][DATA, DATA, DATA, ...]]
	 *    12       4          4             n           */
	
	// STATIC
	public static String NET_PREFIX = "0651FF23DDEE"; // Random, header qui identifie notre appli
	
	// Dataholders
	private ByteArrayOutputStream os; // Pas de temps réel , donc OK d'utiliser ça
	private DataOutputStream dos;
	private int packetID;
	private byte[] packetData = new byte[0];
	
	public Packet() {
		this.os = new ByteArrayOutputStream();
		this.dos = new DataOutputStream(os);
	}

	/**
	 * Ajoute un entier au packet
	 * @param i
	 * @throws IOException 
	 */
	public void writeInt(int i) throws IOException {
		this.dos.writeInt(i);
	}
	
	/**
	 * Ajoute un float au packet
	 * @param f
	 * @throws IOException 
	 */
	public void writeFloat(float f) throws IOException {
		this.dos.writeFloat(f);
	}
	
	/**
	 * Ajoute un long au packet
	 * @param l
	 * @throws IOException 
	 */
	public void writeLong(long l) throws IOException {
		this.dos.writeLong(l);
	}
	
	/**
	 * Ajoute un short au paquet
	 * @param s
	 * @throws IOException 
	 */
	public void writeShort(short s) throws IOException {
		this.dos.writeShort(s);
	}
	
	/**
	 * Ajoute un boolean au paquet
	 * @param b
	 * @throws IOException 
	 */
	public void writeBoolean(boolean b) throws IOException {
		this.dos.writeBoolean(b);
	}
	
	/**
	 * Ajoute une chaine UTF8 au paquet
	 * @param str
	 * @throws IOException
	 */
	public void writeString(String str) throws IOException {
		this.dos.writeUTF(str);
	}
	
	/**
	 * Ecrit un byte unique dans le paquet
	 * @param b
	 * @throws IOException 
	 */
	public void writeByte(byte b) throws IOException {
		this.dos.write(b);
	}
	
	/**
	 * Ecrit un nombre arbitraire de bytes dans le paquet
	 * @param ba
	 * @throws IOException
	 */
	public void writeBytes(byte[] ba) throws IOException {
		this.dos.write(ba);
	}
	
	/**
	 * Renvoie l'ID de paquet
	 * @return
	 */
	public int getPacketID() {
		return this.packetID;
	}
	
	/**
	 * Renvoie le packet sous sa forme
	 * transferrable (binaire)
	 * @return
	 * @throws IOException 
	 */
	public void build() throws IOException
	{	
		dos.close();
		os.close();
		
		byte[] result = os.toByteArray();

		packetData = ByteUtils.concatenate(
			ByteUtils.hexStringToByteArray(NET_PREFIX),
			ByteUtils.intToByteArray(result.length),
			ByteUtils.intToByteArray(getPacketID()),
			result
		);
	}
	
	
	/**
	 * STATIC §§
	 * Decode un paquet
	 * @param rawdata
	 * @return
	 */
	public static Packet decode(int packetID, byte[] rawData)
	{	
		// Pas de data, on sort
		if(rawData.length == 0)
			return null;
		
		// Reconstruction du paquet original
		switch(packetID) {
			case ID_PACKET0HANDSHAKE:
				return Packet0Handshake.fromRaw(rawData);
			case ID_PACKET1PLANET:
				return Packet1Planet.fromRaw(rawData);
			case ID_PACKET2ROUTE:
				return Packet2Route.fromRaw(rawData);
			case ID_PACKET3PLAYERJOIN:
				return Packet3PlayerJoin.fromRaw(rawData);
			case ID_PACKET4PLAYERLEAVE:
				return Packet4PlayerLeave.fromRaw(rawData);
			case ID_PACKET5PLAYERINFO:
				return Packet5PlayerInfo.fromRaw(rawData);
			case ID_PACKET6PROBEPING:
				return Packet6ProbePing.fromRaw(rawData);
			case ID_PACKET7PROBEPONG:
				return Packet7ProbePong.fromRaw(rawData);
			case ID_PACKET8READYNOTREADY:
				return Packet8ReadyNotReady.fromRaw(rawData);
			case ID_PACKET9GAMESTART:
				return Packet9GameStart.fromRaw(rawData);
			case ID_PACKET10PLANETARYUPDATE:
				return Packet10PlanetaryUpdate.fromRaw(rawData);
			case ID_PACKET11NEWTURN:
				return Packet11NewTurn.fromRaw(rawData);
			case ID_PACKET12PLANETSELECT:
				return Packet12PlanetSelect.fromRaw(rawData);
			case ID_PACKET13TRANSFERT:
				return Packet13Transfert.fromRaw(rawData);
			case ID_PACKET14TRANSFERTFAILURE:
				return Packet14TransfertFailure.fromRaw(rawData);
			case ID_PACKET15TRANSFERTSUCCESS:
				return Packet15TransfertSuccess.fromRaw(rawData);
			default: return null;
		}
	}
	
	/**
	 * Reconstruit un paquet depuis une chaine
	 * vide
	 * @param data
	 * @return
	 */
	public static Packet fromRaw(byte[] data) {
		return null;
	}

	/**
	 * Affichage de debug du packet
	 */
	public String toString() {
		return "[PKT, Datalen = "+os.size()+", PktID = "+this.getPacketID()+"\n"
			  +"[RAW : " + ByteUtils.bytesArrayToHexString(packetData)+"\n"
			  +"/PKT]";
	}

	/**
	 * renvoie la donnée brute du paquet
	 * @return
	 */
	public byte[] getData() {
		return this.packetData;
	}
}
