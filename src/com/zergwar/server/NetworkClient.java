package com.zergwar.server;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import com.zergwar.network.packets.Packet;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class NetworkClient extends Thread implements Runnable{

	private NetworkAgent agent;
	private Socket socket;
	private boolean isRunning;
	@SuppressWarnings("unused")
	private NetworkClientState gameState;
	private int state;
	private int playerID;
	private String playerName;
	private Color playerColor;
	private boolean isReady;
	private int zergCount;
	
	public static final int ST_WAIT_HANDSHAKE = 1;
	public static final int ST_READ_DATALEN   = 2;
	public static final int ST_READ_PKTYPE    = 3;
	public static final int ST_READ_DATA      = 4;
	
	private long lastUpdateTimestamp;
	
	/**
	 * Instancie un thread de réception client
	 * @param agent
	 * @param socket
	 */
	public NetworkClient(NetworkAgent agent, Socket socket) {
		this.agent = agent;
		this.socket = socket;
		this.lastUpdateTimestamp = System.currentTimeMillis();
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
	 * Définit le nouvel état de jeu du client
	 * @param state
	 */
	public void setState(NetworkClientState state) {
		this.gameState = state;
	}

	/**
	 * Boucle de réception
	 */
	public void run()
	{
		byte[] received = new byte[0];
		
		try {
			InputStream in = socket.getInputStream();
			
			while(isRunning)
			{
				if(socket == null) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				if(socket.isClosed()) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				if(in == null) die(NetworkCode.ERR_REGULAR_DISCONNECT);
				
				if(in.available() > 0)
				{
					received = new byte[in.available()];
					in.read(received);
					
					processIncomingDatachunk(received);
				}
				
			}
		} catch(Exception e) {
			Logger.log("Client is not available anymore. Skipping");
			die(NetworkCode.ERR_GENERIC);
		}
	}
	
	/**
	 * Traite un fragment de données entrant
	 */
	private void processIncomingDatachunk(byte[] datachunk)
	{
		int dataLenIndex = 0;
		int dataLen = 0;
		int dataIndex = 0;
		int pkType = 0;
		
		byte[] buffer = new byte[6];
		byte[] dataLenBuffer = new byte[4];
		byte[] data = new byte[0];
		
		for(byte b : datachunk)
		{
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

	/**
	 * Sends a packet to the client
	 * @param packet0Handshake
	 */
	public void sendPacket(Packet packet) {
		if(this.socket != null)
			if(this.socket.isConnected()) {
				try {
					packet.build();
					this.socket.getOutputStream().write(packet.getData());
				} catch (IOException e) {
					die(NetworkCode.ERR_GENERIC);
					Logger.log("Can't send packet : "+packet+", client already disconnected. Skipping.");
				}
			}
	}

	/**
	 * Définit l'ID du joueur
	 * @param pID
	 */
	public void setPlayerId(int pID) {
		this.playerID = pID;
	}
	
	/**
	 * Définit la couleur du joueur
	 * @param color
	 */
	public void setColor(Color color) {
		this.playerColor = color;
	}
	
	/**
	 * Renvoie l'ID du joueur
	 */
	public int getPlayerId() {
		return this.playerID;
	}
	
	/**
	 * Renvoie la couleur du joueur
	 * @return
	 */
	public Color getColor() {
		return this.playerColor;
	}

	/**
	 * Renvoie le nom du joueur
	 * @return
	 */
	public String getPlayerName() {
		return this.playerName;
	}

	/**
	 * Définit le nom du joueur
	 * @param string
	 */
	public void setPlayerName(String name) {
		this.playerName = name;
	}
	
	/**
	 * Met à jour le timestamp de réception
	 */
	public void update() {
		this.lastUpdateTimestamp = System.currentTimeMillis();
	}

	/**
	 * Calcule l'inactivité du client
	 * @return
	 */
	public long getInactivity() {
		return System.currentTimeMillis() - lastUpdateTimestamp;
	}

	/**
	 * Renvoie si le joueur est pret
	 * @return
	 */
	public boolean getReady() {
		return this.isReady;
	}

	/**
	 * Le joueur est prêt
	 * @param readyState
	 */
	public void setReady(boolean readyState) {
		this.isReady = readyState;
	}
	
	/**
	 * Renvoie une représentation symbolique
	 * de ce joueur
	 */
	public String toString() {
		return "(" + this.getPlayerName() + "::" + this.getPlayerId() +")";
	}

	/**
	 * Sauvegarde la quantité de zergs controles, a titre d'info
	 * @param totalOwnedZergs
	 */
	public void setTotalZergCount(int totalOwnedZergs) {
		this.zergCount = totalOwnedZergs;
	}
	
	/**
	 * Renvoie le dernier compte de zergs possédés
	 * @return
	 */
	public int getTotalZergCount() {
		return this.zergCount;
	}

	/**
	 * Le serveur déconnecte ce client
	 */
	public void disconnect() {
		this.agent.onClientDisconnected(this, NetworkCode.ERR_ELIMINATED);
	}

	/**
	 * Renvoie le socket utilisé pour la liaison
	 * @return
	 */
	public Socket getSocket() {
		return this.socket;
	}
}
