package com.zergwar.network;

import com.zergwar.common.Client;

/**
 * Interface d'écoute des événements réseau
 */
public interface NetworkEventListener {

	public void onClientConnected(Client client);
	public void onClientDisconnected(Client client, NetworkCode reason);
	public void onClientPacketReceived(Client client, NetworkPacket packet);
	public void onNetworkError(NetworkCode error, String errorMessage);
	
}
