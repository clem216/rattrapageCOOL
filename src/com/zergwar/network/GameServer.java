package com.zergwar.network;

import com.zergwar.common.Galaxy;
import com.zergwar.common.Planet;
import com.zergwar.common.Route;
import com.zergwar.network.packets.Packet;
import com.zergwar.network.packets.Packet0Handshake;
import com.zergwar.network.packets.Packet1Planet;
import com.zergwar.network.packets.Packet2Route;
import com.zergwar.util.log.Logger;

/**
 * Game server
 */
public class GameServer implements NetworkEventListener {

	// constants
	public static int SERVER_PORT = 995; // < 1024, w/firewall
	
	// Net agents
	private NetworkAgent netAgent;
	private Galaxy galaxy;
	
	public GameServer() {
		this.galaxy = new Galaxy();
		this.netAgent = new NetworkAgent(SERVER_PORT);
		this.netAgent.registerNetworkListener(this);
	}

	public void start() {
		Logger.log("Starting gameserver...");
		this.galaxy.initGalaxy();
		this.netAgent.start();
	}
	
	public void stop() {
		Logger.log("Stopping gameserver...");
		this.netAgent.stop();
	}

	@Override
	public void onClientConnected(NetworkClient client) {
		Logger.log(client+" connected !");
	}

	@Override
	public void onClientDisconnected(NetworkClient client, NetworkCode reason) {
		Logger.log(client+" disconnected !");
	}

	@Override
	public void onClientPacketReceived(NetworkClient client, Packet packet)
	{
		Logger.log("Received packet from " +client+" : "+packet.getClass().getSimpleName());
		
		switch(packet.getClass().getSimpleName())
		{
			case "Packet0Handshake":
				
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
				
				// Synchro plateau : etats des joueurs et tour en cours
				Logger.log("Starting final datasync...");
				client.setState(NetworkClientState.SYNCED);
				
				break;
			default: break;
		}
	}

	@Override
	public void onNetworkError(NetworkCode error, String errorMessage) {
		Logger.log("Network error -> "+error+" ("+errorMessage+")");
	}
}
