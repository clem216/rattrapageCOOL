package com.zergwar.network.packets;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

import com.zergwar.util.math.ByteUtils;

public class Packet {

	protected CopyOnWriteArrayList<PacketEntry> entries;
	public static String NET_PREFIX = "0651FF23DDEE"; // Random, header qui identifie notre appli
	
	/** CONSTRUCTEUR **/
	public Packet() {
		this.entries = new CopyOnWriteArrayList<PacketEntry>();
	}
	
	/**
	 * Ajoute une donnée au paquet
	 * @param obj
	 */
	public void append(Object obj) {
		if(this.entries != null)
			this.entries.add(new PacketEntry(obj));
	}
	
	/**
	 * Renvoie le packet sous sa forme
	 * transferrable (binaire)
	 * @return
	 */
	public byte[] build() {
		byte[] result = ByteUtils.hexStringToByteArray(NET_PREFIX);
		for(PacketEntry entry : this.entries) {
			result = ByteUtils.concatenate(result, entry.toBytes());
		}
		return result;
	}
	
	
	/**
	 * STATIC §§
	 * Decode un paquet
	 * @param rawdata
	 * @return
	 */
	public static Packet decode(byte[] rawData)
	{	
		// Check d'appartenance aux paquets de l'application
		if(!ByteUtils.bytesArrayToHexString(rawData).startsWith(NET_PREFIX))
			return null;
		
		// Si il n'y a aucune data, skip
		if(rawData.length<13) return null;
		
		// Si il y a de la data :
		byte[] dataSet = Arrays.copyOfRange(rawData, 13, rawData.length);
		int index = 0;
		
		Packet packet = new Packet();
		boolean malformed = false;
		
		// itération sur le dataset
		while(index < dataSet.length)
		{	
			switch(dataSet[index]) {
			
				/** Signed Integer **/
				case 'i':
					
					if(dataSet.length-index > 3) {
						packet.append(ByteUtils.byteArrayToInt(
							Arrays.copyOfRange(rawData, index, index+4)
						));
					} else malformed = true;
					index+=4;
					break;
					
				/** UTF-8 String **/
				case 'S':
					
					int strlen = 0;
					
					// lecture du nombre d'octets de la chaine
					if(dataSet.length-index > 3) {
						strlen = ByteUtils.byteArrayToInt(
							Arrays.copyOfRange(rawData, index, index+4)
						);
						index+=4;
					} else malformed = true;
					
					// lecture de la chaine
					if(dataSet.length-index > strlen) {
						String data = new String(
							Arrays.copyOfRange(rawData, index, index+strlen),
							StandardCharsets.UTF_8
						);
						packet.append(data);
						index+=strlen;
					} else malformed = true;
					
					break;
				
				/** Float **/
				case 'f':
					
					if(dataSet.length - index > 3) {
						float data = ByteUtils.byteArrayToFloat(
							Arrays.copyOfRange(rawData, index, index+4)
						);
						packet.append(data);
						index += 4;
					}
					
					break;
					
				/** Boolean **/
				case 'b':
					
					if(dataSet.length - index > 0) {
						boolean b = dataSet[index]>0;
						packet.append(b);
						index++;
					}
					
					break;
					
				// RIEN / ERREUR
				default: index++;
			}
		}
		
		// Si une erreur rencontrée, on jette le packet
		if(malformed) return null;
		
		// Sinon, on renvoie le décodage
		return packet;
		
	}
	
	/**
	 * Liste les entrées du paquet
	 * @return
	 */
	public String listEntries() {
		String result = "";
		int count = 0;
		
		for(PacketEntry e : entries)
			result += count+" -> "+e+"\n";
		
		return result;
	}

	/**
	 * Affichage de debug du packet
	 */
	public String toString() {
		return "[PKT _____________ "+this.entries.size()
			  +" entries]\n"+listEntries()
			  +"______________PKT]";
	}
}
