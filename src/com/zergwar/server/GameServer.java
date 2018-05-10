package com.zergwar.server;

import java.awt.Color;
import java.util.Random;
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
import com.zergwar.network.packets.Packet13Transfert;
import com.zergwar.network.packets.Packet14TransfertFailure;
import com.zergwar.network.packets.Packet15TransfertSuccess;
import com.zergwar.network.packets.Packet16Victory;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.network.packets.Packet2Route;
import com.zergwar.network.packets.Packet3PlayerJoin;
import com.zergwar.network.packets.Packet5PlayerInfo;
import com.zergwar.network.packets.Packet6ProbePing;
import com.zergwar.network.packets.Packet8ReadyNotReady;
import com.zergwar.network.packets.Packet9GameStart;
import com.zergwar.util.config.Configuration;
import com.zergwar.util.log.Logger;

/**
 * Game server
 */
public class GameServer implements NetworkEventListener {

	// constants
	public static int SERVER_PORT = 65530;     // < 1024, w/firewall ou > 1024 avec
	public static int MAX_CLIENTS = 8;         // limiter a 2 dans l'exemple
	public static int INACTIVITY_DELAY = 1000; // ms
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
	private NetworkClient currentPlayer;
	private int remainingTransfers;
	private CopyOnWriteArrayList<NetworkClient> remainingInTurn;
	
	public GameServer() {
		this.galaxy = new Galaxy();
		this.probeBeacon = new Timer();
		this.remainingInTurn = new CopyOnWriteArrayList<NetworkClient>();
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
						if(cli.getInactivity() > INACTIVITY_DELAY) {
							Logger.log("Disconnecting client "+cli+" for inactivity");
							netAgent.onClientDisconnected(cli, NetworkCode.ERR_REGULAR_DISCONNECT);
						}
					}
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
		
		// ACK
		client.sendPacket(new Packet0Handshake());
				
		// Synchro des joueurs
		Logger.log("Syncing other player references...");
		client.setState(NetworkClientState.SYNCING_PLAYERS);
		for(NetworkClient cli : this.netAgent.getClients()) 
		{
			Packet3PlayerJoin jPacket = new Packet3PlayerJoin(
				cli.getPlayerName(),
				cli.getPlayerId(),
				cli.getColor().getRGB(),
				cli.getReady()
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
				client.getColor().getRGB(),
				client.getReady()
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
				onClientConnected(client);
				break;
			case "Packet8ReadyNotReady":
				Packet8ReadyNotReady rPacket = (Packet8ReadyNotReady)packet;
				onReadyStatusChanged(client, rPacket.readyState);
				break;
			case "Packet12PlanetSelect":
				netAgent.broadcast(packet, client);
				break;
			case "Packet13Transfert":
				resolveTransfert(client, (Packet13Transfert)packet);
				break;
			default: break;
		}
	}

	/**
	 * Résoud un ordre de transfert
	 * 1. est-ce le bon joueur ?
	 * 2. Route accessible ?
	 * 3. Planète source appartient bien au joueur
	 * 4. Résolution de bataille / capture
	 * @param packet
	 */
	private void resolveTransfert(NetworkClient author, Packet13Transfert packet)
	{
		// Si la planète source est identique à la cible, abandon immédiat
		if(packet.sourcePlanet.equals(packet.destPlanet)) {
			this.netAgent.broadcast(new Packet14TransfertFailure(
				packet.playerID,
				packet.sourcePlanet,
				packet.destPlanet,
				"Transfert d'une planète à elle-même interdit !"
			), null);
		
			return;
		}
		
		// Si la planète source n'appartient pas au joueur du tour, fin
		Planet src = galaxy.getPlanetByName(packet.sourcePlanet);
		Planet dst = galaxy.getPlanetByName(packet.destPlanet);
		if(src == null || dst == null) {
			author.sendPacket(new Packet14TransfertFailure(
				packet.playerID,
				packet.sourcePlanet,
				packet.destPlanet,
				"Erreur : planète source/cible non trouvée, vérifier le serveur."
			));
			
			return;
		} else
		
		// Renvoie l'ID propriétaire
		if(src.getOwnerID() != this.currentPlayer.getPlayerId())
		{
			author.sendPacket(new Packet14TransfertFailure(
				packet.playerID,
				packet.sourcePlanet,
				packet.destPlanet,
				"Vous ne pouvez pas attaquer depuis une planète que vous ne contrôlez pas"
			));
			
			return;
		}
		
		// On continue, sinon
		if(author.getPlayerId() == this.currentPlayer.getPlayerId())
		{
			Route r = galaxy.getRoute(packet.sourcePlanet, packet.destPlanet);
			if(r != null) {
				resolveBattle(author, src, dst);
			} else {
				author.sendPacket(new Packet14TransfertFailure(
					packet.playerID,
					packet.sourcePlanet,
				packet.destPlanet,
					"Aucune route ne permet de rejoindre "+packet.destPlanet+" depuis "+packet.sourcePlanet
				));
			}
		} else {
			author.sendPacket(new Packet14TransfertFailure(
				packet.playerID,
				packet.sourcePlanet,
				packet.destPlanet,
				"Unauthorized Operation"
			));
		}
	}

	/**
	 * Résoud une bataille entre deux planètes valides
	 * @param sourcePlanet
	 * @param destPlanet
	 */
	private void resolveBattle(NetworkClient author, Planet src, Planet dst)
	{
		Logger.log("Résolution de la bataille menée par "+author+" de "+src+" à "+dst);
		
		// Si une seule armée ou aucun restent, le transfert est impossible
		if(src.getArmyCount() < 2)
		{
			author.sendPacket(new Packet14TransfertFailure(
				author.getPlayerId(),
				src.getName(),
				dst.getName(),
				"Une seule armée est en garnison. Transfert impossible"
			));
			
			return;
		}
		
		int armiesToTransfer = (int)Math.round(src.getArmyCount() / 2);
		int armiesRemaining = src.getArmyCount() - armiesToTransfer;
	
		// Suivant le propriétaire, combat ou pas combat
		if(dst.getOwnerID() != src.getOwnerID())
		{
			int battleResult = dst.getArmyCount() - armiesToTransfer;
			if(battleResult >= 0)
				dst.setArmyCount(battleResult);
			else
			{
				// Changement de proprio
				dst.setArmyCount(-battleResult);
				dst.setOwner(this.currentPlayer.getPlayerId());
			}
		} else
		{
			dst.setOwner(this.currentPlayer.getPlayerId());
			dst.setArmyCount(dst.getArmyCount() + armiesToTransfer);
		}
		
		// Dans tous les cas, retrait sur la planète source
		src.setArmyCount(armiesRemaining);
		
		// notification du transfert réussi
		netAgent.broadcast(new Packet15TransfertSuccess(), null);
		
		// ... et synchro de la source et de la dest
		netAgent.broadcast(new Packet10PlanetaryUpdate(
			src.getName(),
			src.getOwnerID(),
			src.getArmyCount()
		), null);
		
		netAgent.broadcast(new Packet10PlanetaryUpdate(
			dst.getName(),
			dst.getOwnerID(),
			dst.getArmyCount()
		), null);
		
		/**
		 * Victoire ?
		 */
		NetworkClient victoriousClient = checkVictory();
		if(victoriousClient != null) {
			onGameFinished(victoriousClient);
			return;
		}
		
		// Si aucun ticket de mouvement dispo, joueur suivant
		this.remainingTransfers--;
		if(this.remainingTransfers == 0)
			nextPlayerTurn();
	}

	/**
	 * Lance le tour du joueur suivant
	 */
	private void nextPlayerTurn()
	{
		if(this.remainingInTurn.size() > 0)
		{
			// Prochain joueur
			this.currentPlayer = this.remainingInTurn.remove(0);
			Logger.log("C'est à "+this.currentPlayer+" de jouer !");
			
			// Repioche des transferts
			this.setRemainingTransfers(getRandomTransferCount());
			
			// Synchro du changement de tour
			this.netAgent.broadcast(new Packet11NewTurn(
				this.currentPlayer.getPlayerId(),
				getRemainingTransfers()
			), null);
		} else
			resolveEndOfTurn();
	}

	/**
	 * Exécute la regen des zergs sur l'ensemble des planètes
	 */
	private void resolveEndOfTurn()
	{
		/**
		 * Résoud la regen
		 */
		resolveRegen();
		
		/**
		 * Relance les tours de joueurs
		 */
		this.remainingInTurn.clear();
		
		for(NetworkClient cli : this.netAgent.getClients())
			this.remainingInTurn.add(cli);
		
		// Cas exceptionnel ou tout le monde a déco, interrompt la boucle
		if(this.remainingInTurn.size() == 0) {
			resetGameServer();
			return;
		}
		
		nextPlayerTurn();
	}

	/**
	 * Renvoie l'éventuel client remplissant les
	 * conditions de victoire, null sinon
	 * @return
	 */
	private NetworkClient checkVictory()
	{
		for(NetworkClient client : this.netAgent.getClients())
		{
			int totalOwnedPlanets = 0;
			int totalOwnedZergs = 0;
			
			for(Planet p : this.galaxy.planets) {
				if(p.getOwnerID() == client.getPlayerId()) {
					totalOwnedPlanets++;
					totalOwnedZergs += p.getArmyCount();
				}
			}
			
			// Enregistre le nombre de zergs contrôlés (pour ref)
			client.setTotalZergCount(totalOwnedZergs);
			
			// si le nombre de planetes possédées vaut celui
			// de la galaxie, alors victoire
			if(totalOwnedPlanets == this.galaxy.planets.size())
				return client;
		}
		
		return null;
	}

	/**
	 * résoud la régénération sur les planètes
	 */
	private void resolveRegen()
	{
		for(Planet p : this.galaxy.planets)
		{
			if(p.getOwnerID() != -1) {
				p.setArmyCount(p.getArmyCount() + 10);
			}
			
			// Synchro générale
			this.netAgent.broadcast(new Packet10PlanetaryUpdate(
				p.getName(),
				p.getOwnerID(),
				p.getArmyCount()
			), null);
		}
	}

	/**
	 * La partie est gagnée
	 * @param victoriousClient
	 */
	private void onGameFinished(NetworkClient victoriousClient)
	{
		this.netAgent.broadcast(new Packet16Victory(
			victoriousClient.getPlayerId(),
			victoriousClient.getTotalZergCount()
		), null);
	}

	/**
	 * Réinitialise le serveur de jeu
	 * pour une nouvelle partie
	 */
	private void resetGameServer() {
		this.initializeGameboard();
		// TODO not supported yet
	}

	/**
	 * Lorsqu'un client a changé de statut
	 * @param client
	 */
	private void onReadyStatusChanged(NetworkClient client, boolean readyState)
	{
		Logger.log("Client "+client+" readyness changed");
		client.setReady(readyState);
		this.netAgent.broadcast(
			new Packet8ReadyNotReady(client.getPlayerId(), client.getReady()),
			null
		);
		
		// Vérifie si tous les clients sont prets (>2)
		if(checkAllClientsReady()) {
			onGameReady();
		}
	}

	/**
	 * la partie est prête à démarrer
	 */
	private void onGameReady()
	{
		// Notifie les clients du début de la partie
		this.netAgent.broadcast(new Packet9GameStart(), null);
		
		// Attend 3 secondes avant d'envoyer les positions
		// de départ
		
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				initializeGameboard();
			}
		}, 3000L);
	}

	/**
	 * Initialise la partie
	 */
	private void initializeGameboard()
	{
		for(NetworkClient client : this.netAgent.getClients())
		{
			Planet p = galaxy.getRandomEmptyPlanet();
			p.setOwner(client.getPlayerId());
			p.setArmyCount(Configuration.NB_ZERG_DEPART);
			
			/**
			 * Log du choix effectué
			 */
			Logger.log("La planète de départ du joueur "+client+" sera "+p);
			
			/* Envoie la mise à jour de la planète de départ
			 * à tous les joueurs de la partie   		 */
			this.netAgent.broadcast(new Packet10PlanetaryUpdate(
				p.getName(),
				p.getOwnerID(),
				p.getArmyCount()
			), null);
		}
		
		/**
		 * Les planètes de départ sont désormais attribuées
		 * C'est au joueur inscrit en premier de commencer 
		 */
		
		// recharge la liste des clients restant à jouer
		// Structure en file permettant la gestion facile
		// de la déconnexion intempestive sans interruption
		for(NetworkClient cli : this.netAgent.getClients())
			this.remainingInTurn.add(cli);
		
		NetworkClient firstPlayer = this.remainingInTurn.remove(0); // dépile le premier
		if(firstPlayer != null)
		{
			Logger.log(firstPlayer+" joue en premier !");
			
			this.setCurrentPlayer(firstPlayer);
			this.setRemainingTransfers(getRandomTransferCount());
			
			this.netAgent.broadcast(new Packet11NewTurn(
				firstPlayer.getPlayerId(),
				getRemainingTransfers()
			), null);
		}
	}

	/**
	 * Définit le nombre de transferts restants
	 * @param tc
	 */
	private void setRemainingTransfers(int tc) {
		this.remainingTransfers = tc;
	}

	/**
	 * Renvoie le nombre de transferts restants
	 * @return
	 */
	private int getRemainingTransfers() {
		return this.remainingTransfers;
	}

	/**
	 * Définit le joueur dont c'est actuellement le tour
	 * @param firstPlayer
	 */
	private void setCurrentPlayer(NetworkClient player) {
		this.currentPlayer = player;
	}

	/**
	 * Renvoie un nombre de transferts au hasard
	 * parmi les possibilitées de la config
	 * @return
	 */
	private int getRandomTransferCount() {
		Random r = new Random();
		return Configuration.NOMBRE_TRANSFERTS[r.nextInt(Configuration.NOMBRE_TRANSFERTS.length)];
	}

	/**
	 * Vérifie si tous les clients sont prêts
	 * @return 
	 */
	private boolean checkAllClientsReady()
	{
		int rdyCount = 0;
		for(NetworkClient client : this.netAgent.getClients())
			if(client.getReady())
				rdyCount++;
		return (rdyCount>0) && rdyCount == this.netAgent.getClients().size(); // TODO change to 1
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
