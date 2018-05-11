package com.zergwar.client;

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
import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;
import com.zergwar.network.packets.Packet10PlanetaryUpdate;
import com.zergwar.network.packets.Packet11NewTurn;
import com.zergwar.network.packets.Packet12PlanetSelect;
import com.zergwar.network.packets.Packet13Transfert;
import com.zergwar.network.packets.Packet14TransfertFailure;
import com.zergwar.network.packets.Packet16Victory;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.network.packets.Packet2Route;
import com.zergwar.network.packets.Packet3PlayerJoin;
import com.zergwar.network.packets.Packet4PlayerLeave;
import com.zergwar.network.packets.Packet5PlayerInfo;
import com.zergwar.network.packets.Packet6ProbePing;
import com.zergwar.network.packets.Packet7ProbePong;
import com.zergwar.network.packets.Packet8ReadyNotReady;
import com.zergwar.notui.NotUI;
import com.zergwar.server.NetworkCode;
import com.zergwar.util.log.Logger;
import com.zergwar.util.math.ByteUtils;

public class GameClient {

	private static long INACTIVITY_TIME = 3000; //ms
	
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
	private boolean isReady;
	
	private RemotePlayer currentPlayer;
	private int remainingTransfers;
	
	private Timer onlineVerifyTimer;
	
	private Planet targetPlanet;
	private Planet hoveredPlanet;
	private Planet selectedPlanet;

	private String serverIP;
	private int serverPort;
	
	private RemotePlayer winner;

	private int winningZergs;
	
	public GameClient(String serverIP, int port)
	{
		this.serverIP = serverIP;
		this.serverPort = port;
		
		this.initClient();
	}

	/**
	 * Initialise le client
	 * @param serverIP
	 * @param port
	 */
	private void initClient()
	{
		this.initNotUI();
		this.state = ClientState.IDLE;
		this.galaxy = new Galaxy(-1); // Not loaded locally
		this.players = new CopyOnWriteArrayList<RemotePlayer>();
		this.networkThread = new NetworkThread(this);
		this.networkThread.connect(this.serverIP, this.serverPort);
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
				  && System.currentTimeMillis() - serverTimestamp > INACTIVITY_TIME) {
					onConnectionLost();
				}
			}
			
		}, 0, 400L);
	}
	
	/**
	 * Interrompt le monitoring de l'état
	 */
	private void stopOnlineVerify()
	{
		this.onlineVerifyTimer.cancel();
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
	public void onPacketReceived(Packet packet)
	{
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
				} else if(this.state == ClientState.SYNCING_PLAYERS) {
					this.state = ClientState.IN_LOBBY;
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
					Logger.log("Player "+ply+"left the game");
					this.status = "[ Player " + ply.getName() + " left the game ]";
					this.players.remove(ply);
				}
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
				break;
			case "Packet8ReadyNotReady":
				Packet8ReadyNotReady rPacket = (Packet8ReadyNotReady)packet;
				RemotePlayer rPly = this.getRemotePlayerByID(rPacket.playerID);
				if(rPly != null)
					rPly.setReady(rPacket.readyState);
				break;
			case "Packet9GameStart":
				this.state = ClientState.GAME_STARTING;
				break;
			case "Packet10PlanetaryUpdate":
				if(this.state == ClientState.GAME_STARTING)
					this.state = ClientState.IN_GAME;
				
				this.targetPlanet = null;
				
				Packet10PlanetaryUpdate uPacket = (Packet10PlanetaryUpdate)packet;
				Planet p = galaxy.getPlanetByName(uPacket.planetName);
				if(p != null) {
					p.setOwner(uPacket.ownerID);
					p.setArmyCount(uPacket.armyCount);
				}
				break;
			case "Packet11NewTurn":
				Packet11NewTurn tPacket = (Packet11NewTurn)packet;
				
				this.selectedPlanet = null;
				this.hoveredPlanet = null;
				this.targetPlanet = null;
				
				this.currentPlayer = this.getRemotePlayerByID(tPacket.playerID);
				this.remainingTransfers = tPacket.transferCount;
				break;
			case "Packet12PlanetSelect":
				Packet12PlanetSelect mPacket = (Packet12PlanetSelect)packet;
				System.out.println("Received update selection status for "+mPacket.planetName);
				if(mPacket.selectionType == 1)
					this.selectedPlanet = this.galaxy.getPlanetByName(mPacket.planetName);
				else if(mPacket.selectionType == 2)
					this.hoveredPlanet = this.galaxy.getPlanetByName(mPacket.planetName);
				else if(mPacket.selectionType == 3)
					this.targetPlanet = this.galaxy.getPlanetByName(mPacket.planetName);
				else {
					this.targetPlanet = null;
					this.selectedPlanet = null;
					this.hoveredPlanet = null;
				}
				break;
			case "Packet14TransfertFailure":
				Packet14TransfertFailure tffPacket = (Packet14TransfertFailure)packet;
				
				if(this.currentPlayer != null)
					if(this.currentPlayer.getPlayerID() == tffPacket.playerID)
						this.status = tffPacket.failureReason;
				
				this.selectedPlanet = null;
				this.hoveredPlanet = null;
				this.targetPlanet = null;
				
				break;
			case "Packet15TransfertSuccess":
				this.remainingTransfers--;
				
				this.selectedPlanet = null;
				this.hoveredPlanet = null;
				this.targetPlanet = null;
				
				break;
			case "Packet16Victory":
				
				this.selectedPlanet = null;
				this.hoveredPlanet = null;
				this.targetPlanet = null;
				
				Packet16Victory vPacket = (Packet16Victory)packet;
				if(vPacket.finalZergCount >= 0)
				{
					this.stopOnlineVerify();
					if(vPacket.finalZergCount > 0)
						this.winner = this.getRemotePlayerByID(vPacket.playerID);
					
					this.winningZergs = vPacket.finalZergCount;
					this.state = ClientState.IN_VICTORY_MENU;
					this.ui.setMenu(NotUI.MENU_ID_FINISHED);
				} else {
					this.status = "[ Le joueur "+ this.getRemotePlayerByID(vPacket.playerID) + " a été éliminé !]";
				}
				
				break;
			case "Packet17AlreadyInGame":
				this.state = ClientState.IN_ALREADY_IG_MENU;
				this.ui.setMenu(NotUI.MENU_ID_ALREADYIG);
				break;
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
		
		private GameClient client;
		private Socket socket;
		private boolean isRunning;
		private int state;
		
		private String ip;
		private int port;
		
		public static final int ST_WAIT_HANDSHAKE = 1;
		public static final int ST_READ_DATALEN   = 2;
		public static final int ST_READ_PKTYPE    = 3;
		public static final int ST_READ_DATA      = 4;
		
		public NetworkThread(GameClient client) {
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

	/**
	 * Renvoie l'état actuel du client
	 * @return
	 */
	public ClientState getState() {
		return this.state;
	}

	/**
	 * Si le player notifie qu'il est
	 * pret
	 */
	public void onPlayerReady()
	{
		this.isReady = !this.isReady;
		this.send(
			new Packet8ReadyNotReady(this.playerID, this.isReady)
		);
	}
	
	/**
	 * Renvoie le joueur jouant actuellement.
	 * NULL si aucun ou dans la phase regen
	 * @return
	 */
	public RemotePlayer getCurrentPlayer() {
		return this.currentPlayer;
	}
	
	/**
	 * Renvoie le nombre de transferts
	 * du joueur actuellement en train
	 * de jouer
	 * @return
	 */
	public int getRemainingTransfers() {
		return this.remainingTransfers;
	}
	
	/**
	 * Renvoie la planète située sous le curseur
	 * de la souris, si il y a
	 * @return
	 */
	public Planet getHoveredPlanet() {
		return this.hoveredPlanet;
	}
	
	/**
	 * Renvoie la planète actuellement sélectionnée,
	 * si il y a
	 * @return
	 */
	public Planet getSelectedPlanet() {
		return this.selectedPlanet;
	}
	
	/**
	 * Renvoie la planète cible
	 * @return
	 */
	public Planet getTargetPlanet() {
		return this.targetPlanet;
	}

	/**
	 * Sélectionne la planète aux coordonées x,y
	 * déselect si aucune
	 * @param x
	 * @param y
	 */
	public void setSelectedPlanet(int x, int y)
	{
		Planet p = findPlanetAtCoordinates(x, y);
		
		// Envoie la notification de déselection si nécessaire
		if(p == null && this.selectedPlanet != null) {
			this.selectedPlanet = p;
			this.send(new Packet12PlanetSelect(
				this.playerID,
				"",
				1
			));
		}
		
		if(p != null && this.getCurrentPlayer() != null)
			if(this.getCurrentPlayer().getPlayerID() == this.playerID)
			{
				if(this.selectedPlanet != null)
				{
					// Affichage de la planète cible aux autres
					this.targetPlanet = p;
					this.send(new Packet12PlanetSelect(
						this.playerID,
						p.getName(),
						3
					));
					
					// Envoi de l'ordre de transfert
					// ordre décalé de 800ms pour laisser
					// le temps aux autres de voir le déplacement
					// To be fixed, pas très joli =)
					new Timer().schedule(new TimerTask()
					{
						public void run() {
							send(new Packet13Transfert(
								playerID,
								selectedPlanet.getName(),
								p.getName()
							));
							
							selectedPlanet = null;
							targetPlanet = null;
						}
					}, 800L);
				} else {
					this.selectedPlanet = p;
					this.send(new Packet12PlanetSelect(
						this.playerID,
						p.getName(),
						1
					));
				}
			}
	}

	/**
	 * Sélectionne la planète en surbrillance
	 * si aucune, null
	 * @param x
	 * @param y
	 */
	public void setHoveredPlanet(int x, int y)
	{
		Planet p = findPlanetAtCoordinates(x, y);
		
		// Si aucune planète survolée, notifie de la désync
		if(p == null && this.hoveredPlanet != null)
			this.send(new Packet12PlanetSelect(
				this.playerID,
				"",
				2
			));
		
		this.hoveredPlanet = p;
		
		if(p != null && this.getCurrentPlayer() != null)
			if(this.getCurrentPlayer().getPlayerID() == this.playerID)
				this.send(new Packet12PlanetSelect(
					this.playerID,
					p.getName(),
					2
				));
	}

	/**
	 * Trouve la planète aux coordonnées x, y
	 * @param x
	 * @param y
	 * @return
	 */
	private Planet findPlanetAtCoordinates(int x, int y)
	{
		for(Planet p : this.galaxy.planets) {
			if(distance(p.getX() + 75, p.getY() + 100, x, y) < p.getDiameter() / 4)
				return p;
		}
		
		return null;
	}
	
	/**
	 * Renvoie la distance entre deux points
	 * @param f
	 * @param g
	 * @param x
	 * @param y
	 * @return
	 */
	private double distance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	/**
	 * Renvoie si il s'agit de mon tour de jeu
	 * @return
	 */
	public boolean isMyTurn() {
		if(this.currentPlayer == null) return false;
		return this.currentPlayer.getPlayerID() == this.playerID;
	}

	/**
	 * Renvoie si le client est le gagnant de la partie ou non
	 * @return
	 */
	public boolean isWinner() {
		if(this.winner == null) return false;
		return this.winner.getPlayerID() == this.playerID;
	}

	/**
	 * Renvoie le nombre de zergs restant au perdant
	 * @return
	 */
	public int getFinalZergCount() {
		return this.winningZergs;
	}

	/**
	 * Réinitialise le client et se reconnecte au serveur
	 */
	public void resetClient() {
		this.stopOnlineVerify();
		this.initClient();
	}

	/**
	 * Définit la planète actuellement sélectionnée
	 * @param object
	 */
	public void setSelectedPlanet(Planet planet) {
		this.selectedPlanet = planet;
	}
}
