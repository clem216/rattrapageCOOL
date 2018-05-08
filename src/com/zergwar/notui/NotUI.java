package com.zergwar.notui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JFrame;

import com.zergwar.common.Planet;
import com.zergwar.tests.TestClient;

public class NotUI extends JFrame {

	// GENERIC
	private static final long serialVersionUID = 1L;
	private Font regular, bold;
	private TestClient client;
	
	// STATIC
	public static final int MENU_ID_PROBING    = 0;
	public static final int MENU_ID_CONNECTING = 1;
	public static final int MENU_ID_ERROR      = 2;
	public static final int MENU_ID_GAME       = 3;
	private int menuID;
	
	public NotUI(TestClient client) {
		this.client = client;
		this.menuID = MENU_ID_PROBING;
		this.initFonts();
	}
	
	/**
	 * Initialise l'UI
	 */
	public void initUI() {
		this.setSize(1000, 800);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setBackground(Color.black);
		this.setVisible(true);
	}
	
	/**
	 * Initialise les polices
	 */
	public void initFonts() {
		this.regular = NotUIUtils.getFont("Inconsolata-Regular.ttf").deriveFont(18f);
		this.bold = NotUIUtils.getFont("Inconsolata-Bold.ttf").deriveFont(18f);
	}
	
	/**
	 * Dessine la galaxie
	 * @param g
	 */
	public void renderGame(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(regular);
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());

		switch(menuID) {
			case MENU_ID_PROBING:
				renderMenuProbing(g);
				break;
			case MENU_ID_ERROR:
				renderMenuError(g);
				break;
			case MENU_ID_CONNECTING:
				renderMenuConnecting(g);
				break;
			case MENU_ID_GAME:
				renderMenuGame(g);
				break;
			default: break;
		}
	}
	
	/**
	 * Dessine le plateau et le jeu
	 * @param g
	 */
	private void renderMenuGame(Graphics2D g) {
		drawCenteredString(g, Color.white, regular, "ZergWar | GameBoard | Synced", getWidth() / 2, 60);
		g.setColor(Color.white);
		g.drawRect(65, 100, 860, 500);
		
		for(Planet p : this.client.galaxy.planets) {
			g.drawArc(
				(int)(p.getX() + 75 - p.getDiameter() / 8),
				(int)(p.getY() + 100 - p.getDiameter() / 8),
				p.getDiameter() / 4,
				p.getDiameter() / 4,
				0,
				360
			);
			
			drawCenteredString(g, Color.WHITE, regular, p.getName(), (int)p.getX() + 75, (int)p.getY() + 100+ p.getDiameter() / 4 + 20);
		}
	}

	/**
	 * Dessine le menu d'erreur
	 * @param g
	 */
	private void renderMenuError(Graphics2D g) {
		drawCenteredString(g, Color.red, regular, "!! Une erreur est survenue !!", getWidth() / 2, getHeight() / 2 - 12);
		drawCenteredString(g, Color.orange, regular, "Erreur : "+client.getCurrentException(), getWidth() / 2, getHeight() / 2 + 12);
	}

	/**
	 * Rendu du menu de connexion
	 * @param g
	 */
	private void renderMenuProbing(Graphics2D g) {
		drawCenteredString(g, Color.white, regular, "<< Attente de la connexion au serveur >>", getWidth() / 2, getHeight() / 2);
	}
	

	/**
	 * Rendu du menu de connexion
	 * @param g
	 */
	private void renderMenuConnecting(Graphics2D g) {
		drawCenteredString(g, Color.white, regular, "... Connexion à la partie ZergWar ...", getWidth() / 2, getHeight() / 2 - 36);
		drawCenteredString(g, Color.gray, regular, client.getCurrentStatus(), getWidth() / 2, getHeight() / 2);
	}

	/**
	 * Dessine une chaine centrée aux coordonnées spécifiée
	 */
	public void drawCenteredString(Graphics2D g, Color c, Font f, String string, int x, int y) {
		FontMetrics fm = g.getFontMetrics(f);
		g.setColor(c);
		g.drawString(string, x - fm.stringWidth(string) / 2, y);
	}
	
	/**
	 * Dessine une chaine centrée aux coordonnées spécifiée
	 */
	public void drawCenteredShadowedString(Graphics2D g, Color c, Color s, Font f, String string, int x, int y) {
		FontMetrics fm = g.getFontMetrics(f);
		g.setColor(s);
		g.drawString(string, x - fm.stringWidth(string) / 2 + 2, y + 2);
		g.setColor(c);
		g.drawString(string, x - fm.stringWidth(string) / 2, y);
	}

	public void paint(Graphics g) {
		super.paint(g);
		this.renderGame((Graphics2D)g);
	}

	/**
	 * Affiche une erreur depuis le client
	 * @param e
	 */
	public void onError() {
		setMenu(MENU_ID_ERROR);
	}

	/**
	 * Redéfinit le menu en cours
	 * @param menuIdError
	 */
	public void setMenu(int menuID) {
		this.menuID = menuID;
		this.repaint();
	}
}
