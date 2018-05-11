package com.zergwar.notui;

import java.awt.Graphics2D;
import java.util.Random;

public class NotUIParticleBrood extends NotUIParticle {

	float x1, y1, x2, y2;
	float size;
	float instSize;
	double angle;
	
	public NotUIParticleBrood(float x1, float y1, float x2, float y2) {
		super(((int)distance(x1, y1, x2, y2) + 1) / 2, x1, y1);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		
		this.size = 9 + new Random().nextInt(2);
		this.angle = angle(x1, y1, x2, y2);
	}

	/**
	 * Angle entre deux points
	 * 
	 * ATTENTION : c'est PAS beau
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static float angle(float x1, float y1, float x2, float y2)
    {
        float dx=x2-x1,dy=y2-y1, PI=(float)Math.PI;
        double angle=0.0f;
        
        if(dx==0)
           if(dy==0)angle=0;
           else if(dy>0)angle=PI/2;
           else angle=PI*3/2;
        else if(dy==0)
           if(dx>0)angle=0;
           else angle=PI;
        else if(dx<0)angle=Math.atan(dy/dx)+PI;
        else if(dy<0)angle=Math.atan(dy/dx)+(2*PI);
        else angle=Math.atan(dy/dx);
        return (float)angle;
    }
	 
	/**
	 * Calcule la distance entre deux points
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private static double distance(float x1, float y1, float x2, float y2)
	{
		return Math.sqrt((x2-x1) * (x2-x1) + (y2-y1) * (y2-y1));
	}

	@Override
	public void tick() {
		super.tick();
		instSize = (float) (size + Math.sin(getParticleTick() / 2));
		
		this.setX((float)(getX() + 2 * Math.cos(angle)));
		this.setY((float)(getY() + 2 * Math.sin(angle)));
		
		if(distance(getX(), getY(), x2, y2) < 20)
			this.die();
	}
	
	@Override
	public void renderParticle(Graphics2D g)
	{
		g.drawImage(NotUIUtils.loadTex("overlord.png"), (int)(getX() - instSize/2), (int)(getY() - instSize/2), (int)instSize, (int)instSize, null);
	}

}
