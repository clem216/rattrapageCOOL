package com.zergwar.server;

public enum NetworkClientState {
	UNKNOWN,
	HANDSHAKED,
	SYNCING_PLANETS,
	SYNCING_ROUTES,
	SYNCING_PLAYERS,
	SYNCED
}
