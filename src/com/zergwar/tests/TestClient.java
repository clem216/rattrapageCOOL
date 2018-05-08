package com.zergwar.tests;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;
import com.zergwar.notui.NotUI;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class TestClient {

	private NotUI ui;
	private NetworkThread networkThread;
	private Exception currentException;
	private String status;
	
	public TestClient(String serverIP, int port) {
		this.initNotUI();
		this.networkThread = new NetworkThread(this);
		this.networkThread.connect(serverIP, port);
	}

	/**
	 * Initialise l'UI
	 */
	private void initNotUI() {
		this.ui = new NotUI(this);
		this.ui.initUI();
	}
	
	/**
	 * renvoie l'exception courante, ou null si aucune
	 * @return
	 */
	public Exception getCurrentException() {
		return this.currentException;
	}
	
	/**
	 * Lorsqu'un paquet est reçu !
	 * @param packet
	 */
	public void onPacketReceived(Packet packet) {
		Logger.log("Incoming packet received by client : "+packet);
	}
	
	/**
	 * Sur erreur du client, affiche la gui appropriée
	 * @param e
	 */
	public void onError(Exception e) {
		Logger.log("catched client exception "+e);
		if(e != null)
			this.currentException = e;
		this.ui.onError();
	}
	
	/**
	 * renvoie le statut actuel du client
	 * @return
	 */
	public String getCurrentStatus() {
		return (this.status == null)?"":this.status;
	}
	
	/**
	 * Socket a pu se connecter
	 */
	public void onSocketConnected() {
		this.status = "[ Waiting for handshake ]";
		this.ui.setMenu(NotUI.MENU_ID_CONNECTING);
	}
	
	/***********************************
	 * NetworkThread Nested Class
	 **********************************/
	public class NetworkThread extends Thread implements Runnable {
		
		private TestClient client;
		private Socket socket;
		private String ip;
		private int port;
		private boolean isRunning;
		private int remainingData;
		private byte[] buffer;
		private int state;
		
		private static final int ST_WAIT_HEADER  = 1;
		private static final int ST_READ_DATALEN = 2;
		private static final int ST_READ_DATA    = 3;
		
		public NetworkThread(TestClient client) {
			this.client = client;
		}
		
		public void run()
		{	
			Logger.log("Starting client networking thread...");
			
			try
			{
				this.socket = new Socket(InetAddress.getByName(ip), port);
				
				this.state = ST_WAIT_HEADER;
				this.buffer = new byte[12];

				client.onSocketConnected();
				
				// Send a handshake
				Packet handshake = new Packet0Handshake();
				this.socket.getOutputStream().write(handshake.build());
				
				InputStream in = this.socket.getInputStream();
				while(isRunning)
				{
					if(in.available() > 0)
					{
						int readlen = Math.min(in.available(), remainingData);
						byte[] received = new byte[readlen];
						in.read(received);
						
						for(byte b : received)
							processIncomingDataByte(b);
					}
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				client.onError(e);
			} catch (IOException e) {
				e.printStackTrace();
				client.onError(e);
			}
			Logger.log("Client networking thread unexpectedly stopped working !");
		}
		
		/**
		 * Adds an incoming datachunk to the receiver
		 * @param received
		 */
		int dataLenIndex;
		int dataLen;
		int dataIndex;
		byte[] data;
		byte[] dataLenBuffer;
		
		private void processIncomingDataByte(byte b)
		{
			bufferize(buffer, b);
			
			switch(this.state) {
				case ST_WAIT_HEADER:
					if(b == 0xEE)
						if(ByteUtils.bytesArrayToHexString(buffer).equals("0651FF23DDEE")) {
							this.state = ST_READ_DATALEN;
							dataLenIndex = 0;
						}
					break;
				case ST_READ_DATALEN:
					dataLenBuffer[dataLenIndex++] = b;
					if(dataLenIndex>3) {
						dataLen = ByteUtils.byteArrayToInt(dataLenBuffer);
						this.state = ST_READ_DATA;
						this.data = new byte[dataLen];
						dataIndex = 0;
					}
					break;
				case ST_READ_DATA:
					if(dataIndex < dataLen) {
						data[dataIndex] = b;
					} else {
						processCompleteDataChunk(data);
						this.state = ST_WAIT_HEADER;
					}
					break;
				default: break;
			}
		}

		/**
		 * Ajoute au buffer circulaire de 12
		 * @param buffer2
		 * @param b
		 */
		private void bufferize(byte[] buffer, byte b) {
			for(int i=1; i<buffer.length; i++) {
				buffer[i-1] = buffer[i];
			}
			buffer[11] = b;
		}

		/**
		 * Processes a reconstructed datachunk
		 */
		private void processCompleteDataChunk(byte[] buffer)
		{
			Packet packet = Packet.decode(buffer);
			if(packet != null)
				client.onPacketReceived(packet);
		}

		/**
		 * Se connecte aux coordonnées IP fournies
		 * @param ip
		 * @param port
		 */
		public void connect(String ip, int port) {
			this.isRunning = true;
			this.ip = ip;
			this.port = port;
			start();
		}
	}

}
