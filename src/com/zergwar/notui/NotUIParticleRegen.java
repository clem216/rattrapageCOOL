package com.zergwar.notui;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class NotUIParticleRegen extends NotUIParticle
{
	private String delta;

	public NotUIParticleRegen(int delta, float x, float y) {
		super(30, x, y);
		this.delta = (delta>0) ? ("+"+delta) : ""+delta;
	}
	
	@Override
	public void tick() {
		super.tick();
		setY(getY() - 1);
	}
	
	@Override
	public void renderParticle(Graphics2D g)
	{
		FontMetrics fm = g.getFontMetrics();
		int decayX = fm.stringWidth(delta) / 2;
		
		g.setColor(Color.black);
		g.drawString(delta, (int)getX()+1 - decayX, (int)getY());
		g.drawString(delta, (int)getX()-1 - decayX, (int)getY());
		g.drawString(delta, (int)getX() - decayX, (int)getY()-1);
		g.drawString(delta, (int)getX() - decayX, (int)getY()+1);
		
		if(delta.startsWith("+"))
			g.setColor(Color.WHITE);
		else
			g.setColor(Color.RED);
		
		g.drawString(delta, (int)getX() - decayX, (int)getY());
	}
}
