package com.zergwar.network;

import com.zergwar.common.Client;

/**
 * Interface d'�coute des �v�nements r�seau
 */
public interface NetworkEventListener {

	public void onClientConnected(Client client);
	public void onClientDisconnected(Client client, NetworkCode reason);
	public void onClientPacketReceived(Client client, NetworkPacket packet);
	public void onNetworkError(NetworkCode error, String errorMessage);
	
}
