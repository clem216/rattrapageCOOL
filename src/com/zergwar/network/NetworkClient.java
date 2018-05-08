package com.zergwar.network;

import java.io.InputStream;
import java.net.Socket;

import com.zergwar.network.packets.Packet;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class NetworkClient extends Thread implements Runnable{

	private NetworkAgent agent;
	private Socket socket;
	private boolean isRunning;
	private int state;
	
	public static final int ST_WAIT_HANDSHAKE = 1;
	public static final int ST_READ_DATALEN   = 2;
	public static final int ST_READ_PKTYPE    = 3;
	public static final int ST_READ_DATA      = 4;
	
	/**
	 * Instancie un thread de réception client
	 * @param agent
	 * @param socket
	 */
	public NetworkClient(NetworkAgent agent, Socket socket) {
		this.agent = agent;
		this.socket = socket;
		this.start();
	}
	
	/**
	 * Démarre le thread
	 */
	public void start() {
		this.isRunning = true;
		super.start();
	}

	/**
	 * Boucle de réception
	 */
	public void run()
	{
		int dataLenIndex = 0;
		int dataLen = 0;
		int dataIndex = 0;
		int pkType = 0;
		
		byte[] buffer = new byte[6];
		byte[] dataLenBuffer = new byte[4];
		byte[] data = new byte[0];
		
		try {
			InputStream in = socket.getInputStream();
			
			while(isRunning)
			{
				if(socket == null) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				if(socket.isClosed()) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				if(in == null) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				
				if(in.available() > 0) {
					
					byte b = (byte)in.read();
					bufferize(buffer, b);

					switch(state) {
						case ST_WAIT_HANDSHAKE:
							
							if(b == (byte)0xEE && ByteUtils.bytesArrayToHexString(buffer).equals("0651FF23DDEE")) {
								state = ST_READ_DATALEN;
								dataLenIndex = 0;
							}
							
							break;
						case ST_READ_DATALEN:
							dataLenBuffer[dataLenIndex++] = b;
							
							if(dataLenIndex > 3) {
								state = ST_READ_PKTYPE;
								dataLen = ByteUtils.byteArrayToInt(dataLenBuffer);
								if(dataLen == 0) state = ST_WAIT_HANDSHAKE;
								dataLenIndex = 0;
							}
							
							break;
						case ST_READ_PKTYPE:
							dataLenBuffer[dataLenIndex++] = b;
							
							if(dataLenIndex > 3) {
								state = ST_READ_DATA;
								pkType = ByteUtils.byteArrayToInt(dataLenBuffer);
								dataIndex = 0;
								data = new byte[dataLen];
							}
							
							break;
						case ST_READ_DATA:
							
							if(dataIndex < dataLen)
								data[dataIndex++] = b;
							
							if(dataIndex >= dataLen) {
								processRawIncomingPacket(pkType, data);
								state = ST_WAIT_HANDSHAKE;
							}
							
							break;
						default:
							state = ST_WAIT_HANDSHAKE;
							break;
					}
				}
			}
		} catch(Exception e) {
			Logger.log("Client crashed : " + e);
			die(NetworkCode.ERR_GENERIC);
		}
	}
	
	/**
	 * Traite un paquet brut reçu
	 * @param dataIndex
	 */
	private void processRawIncomingPacket(int packetID, byte[] data)
	{
		Packet packet = Packet.decode(packetID, data);
		if(packet != null)
			this.agent.onPacketReceived(this, packet);
	}

	/**
	 * Put in circular 12 byte buffer
	 * @param buffer
	 * @param b
	 */
	private void bufferize(byte[] buffer, byte b)
	{
		for(int i=1; i<buffer.length; i++)
		{
			buffer[i-1] = buffer[i];
		}
		
		buffer[5] = b;
	}

	/**
	 * Le client est arrêté par erreur ou legit
	 */
	public void die(NetworkCode reason) {
		this.isRunning = false;
		this.agent.onClientDisconnected(this, reason);
	}
}
