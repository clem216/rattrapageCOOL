package com.zergwar.notui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;

import com.zergwar.common.Planet;
import com.zergwar.common.Route;
import com.zergwar.tests.RemotePlayer;
import com.zergwar.tests.TestClient;

public class NotUI extends JFrame {

	// GENERIC
	private static final long serialVersionUID = 1L;
	private Font regular, bold;
	private TestClient client;
	
	// STATIC
	public static final int MENU_ID_PROBING      = 0;
	public static final int MENU_ID_CONNECTING   = 1;
	public static final int MENU_ID_ERROR        = 2;
	public static final int MENU_ID_GAME         = 3;
	public static final int MENU_ID_DISCONNECTED = 4;
	
	private int menuID;
	private Timer renderTimer;
	
	// Graphics-related
	private BufferStrategy bufferStrategy;
	
	public NotUI(TestClient client) {
		this.client = client;
		this.menuID = MENU_ID_PROBING;
		this.renderTimer = new Timer();
		this.initFonts();
		this.startRender();
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

		this.createBufferStrategy(2);
		this.bufferStrategy = this.getBufferStrategy();
	}
	
	/**
	 * Initialise les polices
	 */
	public void initFonts() {
		this.regular = NotUIUtils.getFont("Inconsolata-Regular.ttf").deriveFont(18f);
		this.bold = NotUIUtils.getFont("Inconsolata-Bold.ttf").deriveFont(18f);
	}
	
	/**
	 * Démarre le rendu actif
	 * par buffer
	 */
	public void startRender()
	{
		long lastrenderTime = System.currentTimeMillis();
		this.renderTimer.schedule(new TimerTask()
		{
			public void run() {
				if(System.currentTimeMillis() - lastrenderTime > 20L) {
					Graphics2D g = (Graphics2D)bufferStrategy.getDrawGraphics();
					renderGameFrame(g);
					g.dispose();
					bufferStrategy.show();
				}
			}
			
		}, 0, 10L);
	}
	
	/**
	 * Dessine la galaxie
	 * @param g
	 */
	public void renderGameFrame(Graphics2D g)
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
			case MENU_ID_DISCONNECTED:
				renderMenuDisconnected(g);
				break;
			default: break;
		}
	}
	
	/**
	 * Dessine le plateau et le jeu
	 * @param g
	 */
	private void renderMenuGame(Graphics2D g) {
		drawCenteredString(g, Color.white, bold, "ZergWar | GameBoard:: Galaxy View", getWidth() / 2, 60);
		drawCenteredString(g, Color.gray, regular, "Jeu de conquête spatiale en tour à tour", getWidth() / 2, 80);
		
		g.setColor(Color.orange);
		
		for(Route r : this.client.galaxy.routes)
		{
			g.drawLine(
				(int)r.getSource().getX() + 75,
				(int)r.getSource().getY() + 100,
				(int)r.getDest().getX() + 75,
				(int)r.getDest().getY() + 100
			);
		}
		
		g.setColor(Color.white);
		g.drawRect(65, 100, 860, 500);
		
		for(Planet p : this.client.galaxy.planets) {
			
			g.setColor(Color.green);
			
			g.drawArc(
				(int)(p.getX() + 75 - p.getDiameter() / 8),
				(int)(p.getY() + 100 - p.getDiameter() / 8),
				p.getDiameter() / 4,
				p.getDiameter() / 4,
				0,
				360
			);
			
			drawCenteredString(g, Color.WHITE, regular, p.getName(), (int)p.getX() + 75, (int)p.getY() + 100+ p.getDiameter() / 4 + 20);
			drawCenteredString(g, p.getOwnerColor(), regular, p.getArmyCount() + "/" + "\u221E", (int)p.getX() + 75, (int)p.getY() + 120 + p.getDiameter() / 4 + 20);
		}
		
		// affiche les joueurs connectés
		if(!this.client.getPlayers().isEmpty())
			drawConnectedPlayers(g, this.client.getPlayers(), 65, 605);
		
		// affiche le nom du joueur que l'on joue
		if(this.client.getPlayerName() != null && this.client.getPlayerColor() != null)
			drawPlayer(g, this.client.getPlayerName(), this.client.getPlayerColor(), 65, 40);
		
		// affiche le statutg
		g.setColor(Color.WHITE);
		g.drawString(this.client.getCurrentStatus(), 75, getHeight() - 25);
		
		// affiche l'heure de jeu
		drawTimestamp(g, this.client.getServerTimestamp());
	}

	/**
	 * Dessine la timestamp serveur
	 * @param g
	 * @param serverTimestamp
	 */
	private void drawTimestamp(Graphics2D g, long time)
	{
		if(time > 0) {
			SimpleDateFormat formatter = new SimpleDateFormat("HH:MM:ss");
			g.setColor(Color.white);
			g.drawString(
				"Server time " + formatter.format(new Date(time)),
				748,
				80
			);
		}
	}

	/**
	 * Dessine notre propre icone de jeu
	 * @param g
	 * @param playerName
	 * @param playerColor
	 * @param x
	 * @param y
	 */
	private void drawPlayer(Graphics2D g, String playerName, Color playerColor, int x, int y)
	{
		g.setColor(Color.white);
		g.drawString("Vous jouez", x, y + 15);
		g.setColor(playerColor);
		g.fillRect(x, y + 25, 40, 20);
		g.setColor(Color.white);
		g.setFont(bold);
		g.drawString(playerName, x + 50, y + 40);
		g.setFont(regular);
	}

	/**
	 * Dessine les onglets des joueurs connectés
	 * @param players
	 * @param x
	 * @param y
	 */
	private void drawConnectedPlayers(Graphics2D g, CopyOnWriteArrayList<RemotePlayer> players, int x, int y)
	{
		int position = 0;
		
		for(RemotePlayer p : players)
		{
			g.setFont(regular.deriveFont(12f));
			g.setColor(Color.white);
			g.drawRect(x + position * 130, y, 120, 30);
			g.setColor(p.getPlayerColor());
			g.fillRect(x + position * 130 + 5, y + 5, 30, 20);
			g.setColor(Color.white);
			g.drawString(p.getName(), x + position * 130 + 40, y + 18);
			g.setFont(regular);
			
			position++;
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
	 * Dessine le menu d'erreur
	 * @param g
	 */
	private void renderMenuDisconnected(Graphics2D g) {
		drawCenteredString(g, Color.white, regular, "Vous avez été déconnecté de la partie !", getWidth() / 2, getHeight() / 2 - 12);
		drawCenteredString(g, Color.red, regular, "Timeout error", getWidth() / 2, getHeight() / 2 + 12);
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
		g.setColor(Color.BLACK);
		g.drawString(string, x - fm.stringWidth(string) / 2, y + 2);
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

	/**
	 * Inhibe le comportement par défaut pour
	 * permettre le rendu actif
	 */
	
	@Override
	public void paint(Graphics g) {}

	@Override
	public void update(Graphics g) {}
	
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
