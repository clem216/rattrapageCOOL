package com.zergwar.common;

import com.zergwar.network.NetworkAgent;
import com.zergwar.network.NetworkCode;
import com.zergwar.network.NetworkEventListener;
import com.zergwar.network.NetworkPacket;
import com.zergwar.util.log.Logger;

/**
 * Game server
 */
public class GameServer implements NetworkEventListener {

	public NetworkAgent netAgent;
	public Galaxy galaxy;
	
	public GameServer() {
		this.netAgent = new NetworkAgent();
		this.netAgent.registerNetworkListener(this);
	}

	public void start() {
		Logger.log("Starting gameserver...");
		this.netAgent.start();
	}
	
	public void stop() {
		Logger.log("Stopping gameserver...");
		this.netAgent.stop();
	}

	@Override
	public void onClientConnected(Client client) {
		Logger.log(client+" connected !");
	}

	@Override
	public void onClientDisconnected(Client client, NetworkCode reason) {
		Logger.log(client+" disconnected !");
	}

	@Override
	public void onClientPacketReceived(Client client, NetworkPacket packet) {
		Logger.log(client+" sent packet "+packet);
	}

	@Override
	public void onNetworkError(NetworkCode error, String errorMessage) {
		Logger.log("Network error -> "+error+" ("+errorMessage+")");
	}
}
