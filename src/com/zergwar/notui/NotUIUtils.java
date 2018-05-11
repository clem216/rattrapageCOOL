package com.zergwar.notui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import com.zergwar.common.Planet;

/**
 * Utilitaires de la NotUI
 */
public class NotUIUtils {
	
	private static HashMap<String, Image> imageCache;
	private static Map<String, Font> cache = new ConcurrentHashMap<String, Font>();
	private static HashMap<Integer, BufferedImage> maskLibrary;
	
	public static Font getFont(String name) {
	    Font font = null;
	    if (cache != null) {
	      if ((font = cache.get(name)) != null) {
	        return font;
	      }
	    }
	    String fName = name;
	    try {
	      InputStream is = NotUIUtils.class.getResourceAsStream(fName);
	      font = Font.createFont(Font.TRUETYPE_FONT, is);
	    } catch (Exception ex) {
	      ex.printStackTrace();
	      System.err.println(fName + " not loaded.  Using serif font.");
	      font = new Font("serif", Font.PLAIN, 24);
	    }
	    return font;
	  }

	/**
	 * Charge une image
	 * @param string
	 * @return
	 */
	public static Image loadTex(String tex)
	{
		// Initialise le cache si pas fait
		if(imageCache == null)
			imageCache = new HashMap<String, Image>();
		
		// Charge l'image en cache si existe
		if(imageCache.containsKey(tex))
			return imageCache.get(tex);
		
		// Sinon charge
		try {
			Image img = ImageIO.read(NotUIUtils.class.getResourceAsStream(tex));
			imageCache.put(tex, img);
			return img;
		} catch(Exception e) {}
		
		// Ne devrait pas arriver
		return null;
	}
	
	/**
	 * Procède au rendu "MD" d'une planète
	 * @param g
	 * @param p
	 */
	public static void renderPlanet(Graphics2D g, Planet p, float tick, Color planetColor)
	{
		// Crée le buffer temporaire
		int width = p.getDiameter() / 4;
		
		BufferedImage buffer = new BufferedImage(
			width,
			width,
			BufferedImage.TYPE_INT_ARGB
		);
		
		// Détermine le type de planète
		String pType = "pType1.jpg";
		
		if(p.getName() != null) {
			char firstLetter = p.getName().charAt(0);
			if(firstLetter < 'C')
				pType = "pType2.jpg";
			else if(firstLetter == 'C' || (firstLetter > 'M' && firstLetter < 'S'))
				pType = "pType3.jpg";
			else if(firstLetter >= 'S')
				pType = "pType4.jpg";
		}
		
		Graphics2D gb = buffer.createGraphics();
		
		// Dessine la texture décalée
		gb.setClip(new Ellipse2D.Float(0, 0, width, width));
		gb.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		gb.drawImage(NotUIUtils.loadTex(pType), (int)tick%width, 0, width, width, null);
		gb.drawImage(NotUIUtils.loadTex(pType), (int)tick%width - width, 0, width, width, null);
		
		gb.drawImage(NotUIUtils.loadTex("pmask.png"), 0, 0, width, width, null);
		gb.dispose();
		
		// Dessine l'atmosphère / couleur de joueur
		if(planetColor != null)
		{
			BufferedImage mask = getMaskedAtmos(planetColor);
			
			g.drawImage(mask,
				(int)(p.getX() + 75 - width / 2) - 8,
				(int)(p.getY() + 100 - width / 2) - 8,
				width + 16,
				width + 16,
				null
			);
		} else
			g.drawImage(NotUIUtils.loadTex("atmos.png"),
				(int)(p.getX() + 75 - width / 2) - 8,
				(int)(p.getY() + 100 - width / 2) - 8,
				width + 16,
				width + 16,
				null
			);
		
		// Dessine la planète
		g.drawImage(
			buffer,
			(int)(p.getX() + 75 - width / 2),
			(int)(p.getY() + 100 - width / 2),
			null
		);
	}
	
	/**
	 * Calcule le masque atmosphérique suivant
	 * la couleur des joueurs
	 * @param colorIndex
	 * @return
	 */
	private static BufferedImage getMaskedAtmos(Color planetColor)
	{
		int colorIndex = planetColor.getRGB();
		
		// Si la librairie est nulle, init
		if(maskLibrary == null) {
			maskLibrary = new HashMap<Integer, BufferedImage>();
		}
		
		if(maskLibrary.containsKey(colorIndex))
			return maskLibrary.get(colorIndex);
		else {
			BufferedImage mask = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
			Graphics2D mg = mask.createGraphics();
			mg.drawImage(NotUIUtils.loadTex("batmos.png"), 0, 0, 128, 128, null);
			mg.dispose();
			changeColor(mask, 0, 0, 0, planetColor.getRed(), planetColor.getGreen(), planetColor.getBlue());
			maskLibrary.put(colorIndex, mask);
			return mask;
		}
	}

	/**
	 * Changes all pixels of an old color into a new color, preserving the
	 * alpha channel.
	 */
	private static void changeColor(
	        BufferedImage imgBuf,
	        int oldRed, int oldGreen, int oldBlue,
	        int newRed, int newGreen, int newBlue) {

	    int RGB_MASK = 0x00ffffff;
	    int oldRGB = oldRed << 16 | oldGreen << 8 | oldBlue;
	    int toggleRGB = oldRGB ^ (newRed << 16 | newGreen << 8 | newBlue);

	    int w = imgBuf.getWidth();
	    int h = imgBuf.getHeight();

	    int[] rgb = imgBuf.getRGB(0, 0, w, h, null, 0, w);
	    for (int i = 0; i < rgb.length; i++) {
	        if ((rgb[i] & RGB_MASK) == oldRGB) {
	            rgb[i] ^= toggleRGB;
	        }
	    }
	    imgBuf.setRGB(0, 0, w, h, rgb, 0, w);
	}
}
