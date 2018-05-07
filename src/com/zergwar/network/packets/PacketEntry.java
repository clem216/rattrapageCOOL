package com.zergwar.network.packets;

import java.nio.charset.StandardCharsets;

import com.zergwar.util.math.ByteUtils;

public class PacketEntry {

	public Object entry;
	
	/**
	 * Instancie une nouvelle entr�e de packet
	 * @param entry
	 */
	public PacketEntry(Object entry) {
		this.entry = entry;
	}
	
	/**
	 * Renvoie la repr�sentation de l'entr�e en octets
	 * @return
	 */
	public byte[] toBytes()
	{
		if(entry instanceof Integer)
			return ByteUtils.concatenate(new byte[]{(byte)'i'}, ByteUtils.intToByteArray((int)entry));
		else if(entry instanceof String)
			return ByteUtils.concatenate(
				new byte[] {'S'},
				ByteUtils.intToByteArray(((String)entry).length()),
				((String)entry).getBytes(StandardCharsets.UTF_8)
			);
		else if(entry instanceof Boolean)
			return new byte[]{(byte)'b', (byte)(((boolean)entry)?'1':'0')};
		else if(entry instanceof Float)
			return ByteUtils.concatenate(
				new byte[] {'f'},
				ByteUtils.floatToByteArray((float)entry)
			);
		else
			return new byte[]{(byte)'E'};
	}
	
	/**
	 * Renvoie la repr�sentation en chaine
	 */
	public String toString() {
		if(entry instanceof String) return (String)entry;
		else if(entry instanceof Float) return ""+(float)entry;
		else if(entry instanceof Boolean) return ""+(boolean)entry;
		else if(entry instanceof Integer) return ""+(int)entry;
		else if(entry instanceof byte[]) return ""+ByteUtils.bytesArrayToHexString((byte[])entry);
		else return super.toString();
	}
}
