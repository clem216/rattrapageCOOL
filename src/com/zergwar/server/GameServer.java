package com.zergwar.server;

import java.awt.Color;
import java.util.Timer;
import java.util.TimerTask;

import com.zergwar.common.Galaxy;
import com.zergwar.common.Planet;
import com.zergwar.common.Route;
import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.network.packets.Packet2Route;
import com.zergwar.network.packets.Packet3PlayerJoin;
import com.zergwar.network.packets.Packet5PlayerInfo;
import com.zergwar.network.packets.Packet6ProbePing;
import com.zergwar.util.log.Logger;

/**
 * Game server
 */
public class GameServer implements NetworkEventListener {

	// constants
	public static int SERVER_PORT = 995; // < 1024, w/firewall
	public static int MAX_CLIENTS = 8;   // limiter a 2 dans l'exemple
	public static int currentPlayerID;
	
	public static final int ST_GAME_LOBBY   = 0;
	public static final int ST_GAME_STARTED = 1;
	public static final int ST_GAME_RESULTS = 2;
	
	// Couleurs & noms de joueurs possibles
	public static Color[] colorTable = {
		Color.red,
		Color.blue,
		Color.green,
		Color.yellow,
		Color.magenta,
		Color.cyan,
		Color.pink,
		Color.orange
	};
	
	public static String[] nameTable = {
		"Kerrigan",
		"Stukov",
		"The Overmind",
		"Amon",
		"Zagara",
		"Dehaka",
		"Zurvan"
	};
	
	// Net agents
	private NetworkAgent netAgent;
	private Galaxy galaxy;
	private boolean paused;
	private Timer probeBeacon;
	private Thread clientCleanThread;
	
	public GameServer() {
		this.galaxy = new Galaxy();
		this.probeBeacon = new Timer();
		this.netAgent = new NetworkAgent(SERVER_PORT);
		this.netAgent.registerNetworkListener(this);
		this.initCleanThread();
	}

	public void start() {
		Logger.log("Starting gameserver...");
		this.galaxy.initGalaxy();
		this.netAgent.start();
		this.startBeacon();
	}
	
	public void stop() {
		Logger.log("Stopping gameserver...");
		this.netAgent.stop();
		this.stopBeacon();
	}
	
	/**
	 * Initialise un thread de nettoyage des clients ne
	 * répondant plus (permet la détection des timeouts)
	 */
	public void initCleanThread()
	{
		this.clientCleanThread = new Thread()
		{
			public void run()
			{
				while(true)
				{
					// Cleane les clients n'ayant pas donné signe de vie
					// depuis plus de 400ms
					for(NetworkClient cli : netAgent.getClients()) {
						if(cli.getInactivity() > 400) {
							Logger.log("Disconnecting client "+cli+" for inactivity");
							netAgent.onClientDisconnected(cli, NetworkCode.ERR_REGULAR_DISCONNECT);
						}
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {e.printStackTrace();}
				}
			}
		};
		
		this.clientCleanThread.start();
	}
	
	/**
	 * Démarre le probeur.
	 * Toutes les 200 msecondes il envoie un timestamp,
	 * auquel les clients répondent. Si un client ne
	 * répond pas dans les 400ms, il est considéré
	 * comme offline.
	 */
	public void startBeacon() {
		this.probeBeacon.schedule(new TimerTask()
		{
			public void run()
			{
				netAgent.broadcast(
					new Packet6ProbePing(System.currentTimeMillis()),
					null
				);
			}
		}, 0, 200L);
	}
	
	/**
	 * Interrompt le probeur
	 */
	public void stopBeacon() {
		this.probeBeacon.cancel();
	}

	@Override
	public void onClientConnected(NetworkClient client)
	{

		pauseGame();
		
		// Synchro : réponse au handshake
		client.setState(NetworkClientState.HANDSHAKED);
		Logger.log("Replying to client "+client+" handshake !");
		client.sendPacket(new Packet0Handshake());
		
		// Synchro plateau : les planètes
		Logger.log("Starting planet datasync...");
		client.setState(NetworkClientState.SYNCING_PLANETS);
		for(Planet p : galaxy.planets)
		{
			Packet1Planet pPacket = new Packet1Planet(
				p.getName(),
				p.getX(),
				p.getY(),
				p.getDiameter(),
				p.getOwnerID(),
				p.getArmyCount()
			);
			
			try {
				pPacket.build();
				client.sendPacket(pPacket);
			} catch(Exception e) {
				Logger.log("Couldn't send sync info for planet "+p+". reason follows.");
				e.printStackTrace();
			}
		}
		
		// ACK
		client.sendPacket(new Packet0Handshake());
		
		// Synchro plateau : les routes
		Logger.log("Starting route datasync...");
		client.setState(NetworkClientState.SYNCING_ROUTES);
		for(Route r : galaxy.routes) 
		{
			Packet2Route rPacket = new Packet2Route(
				r.getSource().getName(),
				r.getDest().getName()
			);
			
			try {
				rPacket.build();
				client.sendPacket(rPacket);
			} catch(Exception e) {
				Logger.log("Couldn't send sync info for route "+r+". reason follows.");
				e.printStackTrace();
			}
		}
		
		// ACK
		client.sendPacket(new Packet0Handshake());

		Logger.log("Syncing own player data ...");
		
		// Attribution d'un numéro de joueur
		client.setPlayerId(currentPlayerID);
		
		// Attribution d'une couleur au joueur
		client.setColor(colorTable[currentPlayerID]);
		
		// Attribution d'un nom au joueur
		client.setPlayerName(nameTable[currentPlayerID++]);
		
		// Envoi de la MaJ
		client.sendPacket(new Packet5PlayerInfo(
			client.getPlayerName(),
			client.getPlayerId(),
			client.getColor().getRGB()
		));
				
		// Synchro des joueurs
		Logger.log("Syncing other player references...");
		client.setState(NetworkClientState.SYNCING_PLAYERS);
		for(NetworkClient cli : this.netAgent.getClients()) 
		{
			Packet3PlayerJoin jPacket = new Packet3PlayerJoin(
				cli.getPlayerName(),
					cli.getPlayerId(),
				cli.getColor().getRGB()
			);
			
			try {
				jPacket.build();
				client.sendPacket(jPacket);
			} catch(Exception e) {
				Logger.log("Couldn't send sync info for player "+cli+". reason follows.");
				e.printStackTrace();
			}
		}
		
		// Synchro à tous les joueurs
		this.netAgent.broadcast(
			new Packet3PlayerJoin(
				client.getPlayerName(),
				client.getPlayerId(),
				client.getColor().getRGB()
			),
			client
		);
		
		unpauseGame();
	}

	@Override
	public void onClientDisconnected(NetworkClient client, NetworkCode reason) {
		Logger.log(client+" disconnected !");
	}

	@Override
	public void onClientPacketReceived(NetworkClient client, Packet packet)
	{
		client.update();
		
		switch(packet.getClass().getSimpleName())
		{
			case "Packet0Handshake":
				this.onClientConnected(client);
				break;
			default: break;
		}
	}

	/**
	 * Met le jeu en pause
	 */
	public void pauseGame() {
		Logger.log("Pausing game");
		this.paused = true;
	}

	/**
	 * Sort le jeu de la pause
	 */
	public void unpauseGame() {
		Logger.log("Unpause game");
		this.paused = false;
	}
	
	/**
	 * Renvoie l'état de pause du jeu
	 * @return
	 */
	public boolean isPaused() {
		return paused;
	}
	
	@Override
	public void onNetworkError(NetworkCode error, String errorMessage) {
		Logger.log("Network error -> "+error+" ("+errorMessage+")");
	}
}
