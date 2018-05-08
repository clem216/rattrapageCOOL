package com.zergwar.network;

public enum NetworkClientState {
	UNKNOWN,
	HANDSHAKED,
	SYNCING_PLANETS,
	SYNCING_ROUTES,
	SYNCED
}
