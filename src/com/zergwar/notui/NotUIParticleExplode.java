package com.zergwar.notui;

import java.awt.Graphics2D;
import java.util.Random;

public class NotUIParticleExplode extends NotUIParticle
{
	private int delay, xDecay, yDecay;
	private float size;
	
	public NotUIParticleExplode(int delay, float x, float y) {
		super(delay + 64, x, y);
		this.delay = delay;
		this.size = 6 * ( new Random().nextFloat() + .2f);
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if(delay>0) {
			delay--;
			return;
		}
		
		xDecay++;
		if(xDecay > 7) {
			xDecay = 0;
			yDecay++;
		}
	}
	
	@Override
	public void renderParticle(Graphics2D g)
	{	
		if(delay>0) return;
		
		g.drawImage(
			NotUIUtils.loadTex("expl.png"),
			(int)(getX() - size/2),
			(int)(getY() - size/2),
			(int)(getX() + 2*size),
			(int)(getY() + 2*size),
			xDecay * 128,
			yDecay * 128,
			(xDecay+1) * 128,
			(yDecay+1) * 128,
			null
		);
	}
}
