package com.zergwar.network;

import com.zergwar.network.packets.Packet;

/**
 * Interface d'�coute des �v�nements r�seau
 */
public interface NetworkEventListener {

	public void onClientConnected(NetworkClient client);
	public void onClientDisconnected(NetworkClient client, NetworkCode reason);
	public void onClientPacketReceived(NetworkClient client, Packet packet);
	public void onNetworkError(NetworkCode error, String errorMessage);
	
}
