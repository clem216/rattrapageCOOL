package com.zergwar.network;

import com.zergwar.common.Galaxy;
import com.zergwar.network.packets.Packet;
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
	public void onClientPacketReceived(NetworkClient client, Packet packet) {
		Logger.log(client+" sent packet "+packet);
	}

	@Override
	public void onNetworkError(NetworkCode error, String errorMessage) {
		Logger.log("Network error -> "+error+" ("+errorMessage+")");
	}
}
