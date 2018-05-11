package com.zergwar.notui;

import java.awt.Graphics2D;

/**
 * Instancie une particule de la NotUI
 * @author PC_DEV
 *
 */
public abstract class NotUIParticle
{
	private float xPos;
	private float yPos;
	private int timeToLife;
	private float particleTick;
	
	public NotUIParticle(int ttl, float x, float y)
	{
		this.timeToLife = ttl;
		this.xPos = x;
		this.yPos = y;
	}

	public void tick()
	{
		particleTick += 0.1f;
		
		// ttl <0 = infinite lifetime
		if(timeToLife > 0)
			timeToLife--;
	}
	
	/**
	 * Renvoie si la particule est en vie
	 * @return
	 */
	public boolean alive() {
		return this.timeToLife != 0;
	}
	
	/**
	 * Renvoie la coord X de la particule
	 * @return
	 */
	public float getX() {
		return this.xPos;
	}
	
	/**
	 * Renvoie la coordonnée Y de la particule
	 * @return
	 */
	public float getY() {
		return this.yPos;
	}
	
	/**
	 * Définit la nouvelle coordonnée X de la particule
	 * @param x
	 */
	public void setX(float x) {
		this.xPos = x;
	}
	
	/**
	 * Définit la nouvelle coordonnée Y de la particule
	 * @param y
	 */
	public void setY(float y) {
		this.yPos = y;
	}
	
	/**
	 * Renvoie la progression interne de la particule
	 * @return
	 */
	public float getParticleTick() {
		return this.particleTick;
	}
	
	// Rendu de la particule
	public abstract void renderParticle(Graphics2D g);
}
