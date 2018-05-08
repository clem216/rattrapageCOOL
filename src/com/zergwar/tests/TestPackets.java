package com.zergwar.tests;

import java.io.IOException;

import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.util.math.ByteUtils;

public class TestPackets {

	public static void main(String[] args) throws IOException {
		System.out.println("Construction du paquet\n");
		
		Packet packet = new Packet1Planet("TèstPlänet&",156.4f, -112.2f, 125, 2, 200);
		packet.build();
		System.out.println(packet.toString());
		
		System.out.println("\nEncodage !");
		System.out.println("Raw result : "+ByteUtils.bytesArrayToHexString(packet.getData()));
	}

}
