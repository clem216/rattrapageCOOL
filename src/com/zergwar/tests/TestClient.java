package com.zergwar.tests;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import com.zergwar.common.Galaxy;
import com.zergwar.common.Planet;
import com.zergwar.common.Route;
import com.zergwar.network.NetworkCode;
import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.network.packets.Packet2Route;
import com.zergwar.network.packets.Packet3PlayerJoin;
import com.zergwar.network.packets.Packet4PlayerLeave;
import com.zergwar.network.packets.Packet5PlayerInfo;
import com.zergwar.network.packets.Packet6ProbePing;
import com.zergwar.network.packets.Packet7ProbePong;
import com.zergwar.notui.NotUI;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class TestClient {

	private NotUI ui;
	private NetworkThread networkThread;
	private Exception currentException;
	private String status;
	public Galaxy galaxy;
	private ClientState state;
	private CopyOnWriteArrayList<RemotePlayer> players;
	private int playerID;
	private String playerName;
	private Color playerColor;
	private long serverTimestamp;
	
	private Timer onlineVerifyTimer;
	
	public TestClient(String serverIP, int port) {
		this.initNotUI();
		this.state = ClientState.IDLE;
		this.galaxy = new Galaxy();
		this.players = new CopyOnWriteArrayList<RemotePlayer>();
		this.networkThread = new NetworkThread(this);
		this.networkThread.connect(serverIP, port);
		this.playerID = -1;
		this.playerColor = Color.black;
		this.onlineVerifyTimer = new Timer();
		
		this.startOnlineVerify();
	}

	/**
	 * Démarre la tache de monitoring de l'état
	 * ONLINE
	 */
	private void startOnlineVerify()
	{
		this.onlineVerifyTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(state != ClientState.IDLE
				  && System.currentTimeMillis() - serverTimestamp > 400) {
					onConnectionLost();
				}
			}
			
		}, 0, 400L);
	}

	/**
	 * La connexion a été perdue
	 */
	private void onConnectionLost() {
		this.state = ClientState.IDLE;
		this.ui.setMenu(NotUI.MENU_ID_DISCONNECTED);
		this.ui.repaint();
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
		
		switch(packet.getClass().getSimpleName()) {
			case "Packet0Handshake":
				if(this.state == ClientState.IDLE)
				{
					this.state = ClientState.SYNCING_PLANETS;
					this.status = "[ Sync'ing game with remote server ]";
					ui.repaint();
				} else if(this.state == ClientState.SYNCING_PLANETS)
				{
					this.state = ClientState.SYNCING_ROUTES;
				} else if(this.state == ClientState.SYNCING_ROUTES)
				{
					this.state = ClientState.SYNCING_PLAYERS;
					this.ui.setMenu(NotUI.MENU_ID_GAME);
				}
				break;
			case "Packet1Planet":
				if(this.state == ClientState.SYNCING_PLANETS) {
					Packet1Planet pPacket = (Packet1Planet)packet;
					this.status = "[ Receiving planet's '"+pPacket.name+"' data ]";
					ui.repaint();
					galaxy.planets.add(new Planet(
						pPacket.name,
						pPacket.coordX,
						pPacket.coordY,
						pPacket.diameter,
						pPacket.ownerID,
						pPacket.armyCount
					));
				}
				break;
			case "Packet2Route":
				if(this.state == ClientState.SYNCING_ROUTES) {
					Packet2Route rPacket = (Packet2Route)packet;
					this.status = "[ Receiving route data ]";
					ui.repaint();
					galaxy.routes.add(new Route(
						galaxy.getPlanetByName(rPacket.source),
						galaxy.getPlanetByName(rPacket.destination)
					));
				}
				break;
			case "Packet3PlayerJoin":
				Packet3PlayerJoin jPacket = (Packet3PlayerJoin)packet;
				this.status = "[ Player " + jPacket.playerName + " joined the game ]";
				ui.repaint();
				this.players.add(new RemotePlayer(
					jPacket.playerName,
					jPacket.playerID,
					new Color(jPacket.playerColor)
				));
				break;
			case "Packet4PlayerLeave":
				Packet4PlayerLeave lPacket = (Packet4PlayerLeave)packet;
				RemotePlayer ply = getRemotePlayerByID(lPacket.playerID);
				if(ply != null) {
					this.status = "[ Player " + ply.getName() + " left the game ]";
					this.players.remove(ply);
				}
				ui.repaint();
				break;
			case "Packet5PlayerInfo":
				Packet5PlayerInfo iPacket = (Packet5PlayerInfo)packet;
				this.playerID = iPacket.playerID;
				this.playerColor = new Color(iPacket.playerColor);
				this.playerName = iPacket.playerName;
				this.status = "[ Receiving your initial data from server ]";
				ui.repaint();
				break;
			case "Packet6ProbePing":
				Packet6ProbePing pPacket = (Packet6ProbePing)packet;
				this.send(new Packet7ProbePong());
				this.serverTimestamp = pPacket.timestamp;
				this.ui.repaint();
			default: break;
		}
	}
	
	/**
	 * Renvoie le joueur possédant l'ID spécifié
	 * @param playerID
	 * @return
	 */
	public RemotePlayer getRemotePlayerByID(int playerID)
	{
		for(RemotePlayer ply : this.players)
			if(ply.getPlayerID() == playerID)
				return ply;
		return null;
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
	
	/**
	 * Envoie un paquet au serveur
	 * @param handshake
	 */
	public void send(Packet packet) {
		try {
			packet.build();
			this.networkThread.sendRaw(packet.getData());
		} catch (IOException e) {
			Logger.log("Unable to send packet "+packet+", build failed !");
			e.printStackTrace();
		}
	}

	public void die(NetworkCode err) {
		Logger.log("Client network thread crashed : "+err);
		this.ui.setMenu(NotUI.MENU_ID_ERROR);
	}

	
	/***********************************
	 * NetworkThread Nested Class
	 **********************************/
	public class NetworkThread extends Thread implements Runnable {
		
		private TestClient client;
		private Socket socket;
		private boolean isRunning;
		private int state;
		
		private String ip;
		private int port;
		
		public static final int ST_WAIT_HANDSHAKE = 1;
		public static final int ST_READ_DATALEN   = 2;
		public static final int ST_READ_PKTYPE    = 3;
		public static final int ST_READ_DATA      = 4;
		
		public NetworkThread(TestClient client) {
			this.client = client;
		}
		
		/**
		 * Send data to server
		 * @param data
		 */
		public void sendRaw(byte[] data) {
			if(socket != null)
				if(socket.isConnected())
					try {
						socket.getOutputStream().write(data);
					} catch (IOException e) {
						Logger.log("Unable to send packet, i/o failure.");
						e.printStackTrace();
					}
		}

		/**
		 * Client async receive loop
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
				socket = new Socket(ip, port);
				
				InputStream in = socket.getInputStream();
				OutputStream out = socket.getOutputStream();
				
				// avant d'écouter, lance un handshake
				Packet0Handshake handshake = new Packet0Handshake();
				handshake.build();
				out.write(handshake.getData());
				
				// switche la vue à "connecting"
				status = "[ Waiting for server handshake ]";
				ui.setMenu(NotUI.MENU_ID_CONNECTING);
				
				// Lance la boucle d'écoute réseau
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
		 * Processes a reconstructed datachunk
		 */
		private void processRawIncomingPacket(int packetID, byte[] data)
		{
			Packet packet = Packet.decode(packetID, data);
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

	/**
	 * Renvoie l'ID du joueur joué par ce compte
	 * @return
	 */
	public int getPlayerId() {
		return this.playerID;
	}
	
	/**
	 * Renvoie la couleur du joueur
	 * @return
	 */
	public Color getPlayerColor() {
		return this.playerColor;
	}

	/**
	 * Renvoie la liste des joueurs distants dans la partie
	 * @return
	 */
	public CopyOnWriteArrayList<RemotePlayer> getPlayers() {
		return this.players;
	}

	/**
	 * Renvoie le nom de joueur
	 * @return
	 */
	public String getPlayerName() {
		return this.playerName;
	}

	/**
	 * Renvoie la timestamp server
	 * @return
	 */
	public long getServerTimestamp() {
		return this.serverTimestamp;
	}
}
