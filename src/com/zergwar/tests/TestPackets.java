package com.zergwar.tests;

import com.zergwar.network.packets.Packet;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class TestPackets {

	public static void main(String[] args) {
		System.out.println("Construction du paquet\n");
		Packet packet = new Packet();
		packet.append("HELLO");
		packet.append(-1);
		packet.append(true);
		packet.append(new byte[] {0x01, 0x02, (byte)0xFF});
		System.out.println(packet.toString());
		System.out.println("\nEncodage !");
		byte[] built = packet.build();
		System.out.println(ByteUtils.bytesArrayToHexString(built));
		System.out.println("\nDécodage !");
		Packet decode = Packet.decode(built);
		System.out.println("\nRésultant :");
		System.out.println(decode);
	}

}
