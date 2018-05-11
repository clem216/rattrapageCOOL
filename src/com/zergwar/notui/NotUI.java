package com.zergwar.notui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JFrame;

import com.zergwar.client.ClientState;
import com.zergwar.client.GameClient;
import com.zergwar.client.RemotePlayer;
import com.zergwar.common.Planet;
import com.zergwar.common.Route;
import com.zergwar.network.packets.Packet12PlanetSelect;

public class NotUI extends JFrame implements KeyListener, MouseListener, MouseMotionListener {

	// GENERIC
	private static final long serialVersionUID = 1L;
	private Font regular, bold;
	private GameClient client;
	private boolean enhancedMode;
	
	// STATIC
	public static final int MENU_ID_PROBING      = 0;
	public static final int MENU_ID_CONNECTING   = 1;
	public static final int MENU_ID_ERROR        = 2;
	public static final int MENU_ID_GAME         = 3;
	public static final int MENU_ID_DISCONNECTED = 4;
	public static final int MENU_ID_FINISHED     = 5;
	public static final int MENU_ID_ALREADYIG    = 6;
	
	private int menuID;
	private float tick;
	
	// Graphics-related
	private BufferStrategy bufferStrategy;
	
	public NotUI(GameClient client) {
		this.client = client;
		this.menuID = MENU_ID_PROBING;
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
		
		// Enregistre les listeners
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		
		// Instancie une bufferstrategy
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
		(new Thread()
		{
			long lastrenderTime = System.currentTimeMillis();
			
			// Executes a render loop
			public void run()
			{
				while(true)
				{
					try
					{
						Graphics2D g = (Graphics2D)bufferStrategy.getDrawGraphics();
						renderGameFrame(g);
						g.dispose();
						bufferStrategy.show();
						
						long remainingToSleep = 40 - (System.currentTimeMillis() - lastrenderTime);
						if(remainingToSleep < 0 ) remainingToSleep = 0;
						lastrenderTime = System.currentTimeMillis();
						
						tick += .2f;
						
						Thread.sleep(remainingToSleep);
					} catch (Exception e) {}
				}
			}
		}).start();
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
			case MENU_ID_FINISHED:
				renderMenuFinished(g);
				break;
			case MENU_ID_ALREADYIG:
				renderMenuAlreadyIG(g);
				break;
			default: break;
		}
	}
	
	/**
	 * Dessine le menu de victoire/défaite
	 * @param g
	 */
	private void renderMenuFinished(Graphics2D g) {
		g.setColor(Color.white);
		g.setFont(bold.deriveFont(36f));
		g.setStroke(new BasicStroke(4));
		
		if(this.client.isWinner()) {
			g.setColor(Color.green);
			g.drawRect(65, 300, 860, 40);
			g.drawString("VICTOIRE", 420, 333);
		} else {
			g.setColor(Color.red);
			g.drawRect(65, 300, 860, 40);
			g.drawString("DEFAITE... CUISANTE !", 330, 333);
		}
		
		g.setStroke(new BasicStroke(1));
		g.setFont(regular);
		drawCenteredString(g, Color.white, regular, "NOMBRE DE ZERGS RESTANTS AU GAGNANT", getWidth() / 2, 400);
		drawCenteredString(g, Color.green, bold, ""+this.client.getFinalZergCount(), getWidth() / 2, 425);
	
		drawCenteredString(g, Color.white, regular, "Merci d'avoir joué à ZergWar", getWidth() / 2, 480);
		drawCenteredString(g, Color.white, regular, "Appuyer sur <ECHAP> pour revenir au salon de jeu", getWidth() / 2, 500);
	}

	/**
	 * Dessine le plateau et le jeu
	 * @param g
	 */
	private void renderMenuGame(Graphics2D g) {
		
		if(enhancedMode) {
			g.drawImage(NotUIUtils.loadTex("space.png"), (int)(this.tick/8)%1920, 0, null);
			g.drawImage(NotUIUtils.loadTex("space.png"), (int)(this.tick/8)%1920, 0, -1920, 1080, null);
			g.drawImage(NotUIUtils.loadTex("gameui.png"), 10, 30, null);
		} else {
			drawCenteredString(g, Color.white, bold, "ZergWar | GameBoard:: Galaxy View", getWidth() / 2, 60);
			drawCenteredString(g, Color.gray, regular, "Jeu de conquête spatiale en tour à tour", getWidth() / 2, 80);
		}
		
		if(enhancedMode)
			g.setColor(new Color(255, 255, 255, 96));
		else
			g.setColor(Color.orange);
		
		for(Route r : this.client.galaxy.routes)
		{
			if(enhancedMode)
				g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[]{2,3}, 0));
			
			g.drawLine(
				(int)r.getSource().getX() + 75,
				(int)r.getSource().getY() + 100,
				(int)r.getDest().getX() + 75,
				(int)r.getDest().getY() + 100
			);
			
			if(enhancedMode)
				g.setStroke(new BasicStroke(1));
		}
		
		g.setColor(Color.white);
		if(client.getCurrentPlayer() != null) {
			if(this.client.getCurrentPlayer().getPlayerID() == this.client.getPlayerId())
			{
				g.setColor(Color.green);
				g.drawRect(66, 101, 858, 498);
			}
		}
		
		if(!enhancedMode)
			g.drawRect(65, 100, 860, 500);
		
		for(Planet p : this.client.galaxy.planets) {
			
			if(!enhancedMode)
				if(p.isEmpty()) {
					g.setColor(Color.green);
					g.drawArc(
						(int)(p.getX() + 75 - p.getDiameter() / 8),
						(int)(p.getY() + 100 - p.getDiameter() / 8),
						p.getDiameter() / 4,
						p.getDiameter() / 4,
						0,
						360
					);
					drawCenteredString(g, Color.gray, regular, p.getArmyCount() + "/" + "\u221E", (int)p.getX() + 75, (int)p.getY() + 120 + p.getDiameter() / 4 + 20);
				} else {
					g.setColor(this.client.getRemotePlayerByID(p.getOwnerID()).getPlayerColor());
					g.fillArc(
						(int)(p.getX() + 75 - p.getDiameter() / 8),
						(int)(p.getY() + 100 - p.getDiameter() / 8),
						p.getDiameter() / 4,
						p.getDiameter() / 4,
						0,
						360
					);
					drawCenteredString(g, this.client.getRemotePlayerByID(p.getOwnerID()).getPlayerColor(), regular, p.getArmyCount() + "/" + "\u221E", (int)p.getX() + 75, (int)p.getY() + 120 + p.getDiameter() / 4 + 20);
				}
			
			if(enhancedMode) {
				RemotePlayer player = this.client.getRemotePlayerByID(p.getOwnerID());
				Color c = null;
				if(player != null)
					c = player.getPlayerColor();
				
				NotUIUtils.renderPlanet(g, p, tick, c);
			}
			
			// Si hovered
			if(this.client.getState() == ClientState.IN_GAME)
			if(p.equals(this.client.getHoveredPlanet())) {
				g.setColor(Color.white);
				g.setStroke(new BasicStroke(2));
				g.drawArc(
					(int)(p.getX() + 75 - p.getDiameter() / 8 - 10 - (int)(5 * Math.sin(tick))),
					(int)(p.getY() + 100 - p.getDiameter() / 8 - 10 - (int)(5 * Math.sin(tick))),
					p.getDiameter() / 4 + 20 + (int)(10 * Math.sin(tick)),
					p.getDiameter() / 4 + 20 + (int)(10 * Math.sin(tick)),
					0,
					360
				);
				g.setStroke(new BasicStroke(1));
			}
			

			// si sélectionnée
			if(p.equals(this.client.getSelectedPlanet())) {
				g.setStroke(new BasicStroke(2));
				g.setColor(Color.red);
				g.drawArc(
					(int)(p.getX() + 75 - p.getDiameter() / 8 - 10),
					(int)(p.getY() + 100 - p.getDiameter() / 8 - 10),
					p.getDiameter() / 4 + 20,
					p.getDiameter() / 4 + 20,
					0,
					360
				);
				g.setStroke(new BasicStroke(1));
			}
			
			// Si target
			if(p.equals(this.client.getTargetPlanet())) {
				g.setStroke(new BasicStroke(2));
				g.setColor(Color.red);
				g.drawArc(
					(int)(p.getX() + 75 - p.getDiameter() / 8 - 10),
					(int)(p.getY() + 100 - p.getDiameter() / 8 - 10),
					p.getDiameter() / 4 + 20,
					p.getDiameter() / 4 + 20,
					0,
					360
				);
				g.drawLine(
					(int)(p.getX() + 75),
					(int)(p.getY() + 100) - p.getDiameter() / 2,
					(int)(p.getX() + 75),
					(int)(p.getY() + 100) + p.getDiameter() / 2
				);
				g.drawLine(
					(int)(p.getX() + 75) - p.getDiameter() / 2,
					(int)(p.getY() + 100),
					(int)(p.getX() + 75) + p.getDiameter() / 2,
					(int)(p.getY() + 100)
				);
				g.setStroke(new BasicStroke(1));
			}
			
			// Si une planète est sélectionnée ET une est hovered
			if(this.client.getSelectedPlanet() != null && this.client.getHoveredPlanet() != null && this.client.getTargetPlanet() == null) {
				g.setColor(Color.white);
				g.setStroke(new BasicStroke((int)(3+1.5d* Math.sin(tick))));
				g.drawLine(
					(int)this.client.getSelectedPlanet().getX() + 75,
					(int)this.client.getSelectedPlanet().getY() + 100,
					(int)this.client.getHoveredPlanet().getX() + 75,
					(int)this.client.getHoveredPlanet().getY() + 100
				);
				g.setStroke(new BasicStroke(1));
			}
			

			// Si une planète est sélectionnée ET une autre est target
			if(this.client.getSelectedPlanet() != null && this.client.getTargetPlanet() != null) {
				g.setStroke(new BasicStroke(3));
				g.setColor(Color.red);
				g.drawLine(
					(int)this.client.getSelectedPlanet().getX() + 75,
					(int)this.client.getSelectedPlanet().getY() + 100,
					(int)this.client.getTargetPlanet().getX() + 75,
					(int)this.client.getTargetPlanet().getY() + 100
				);
				g.setStroke(new BasicStroke(1));
			}
			
			drawCenteredString(g, Color.WHITE, regular, p.getName(), (int)p.getX() + 75, (int)p.getY() + 100+ p.getDiameter() / 4 + 20);
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
		
		// Si en lobby, affiche les instructions du lobby
		if(this.client.getState() == ClientState.IN_LOBBY) {
			g.setColor(Color.white);
			g.drawRect(65, 660, 860, 70);
			g.drawRect(67, 662, 856, 66);
			drawCenteredString(g, Color.WHITE, bold, "Salon | En attente d'autres joueurs", 495, 680);
			drawCenteredString(g, Color.GRAY, regular, "Appuyez sur <R> pour indiquer que vous êtres prêt(e) à jouer !", 495, 700);
			drawCenteredString(g, Color.GRAY, regular, "La partie commence lorsqu'au moins 2 joueurs connectés sont marqués comme prêts", 495, 720);
		}
		
		// Affiche le gamestart si nécessaire
		if(this.client.getState() == ClientState.GAME_STARTING) {
			g.setColor(Color.black);
			g.fillRect(120, 260, getWidth() - 240, 55);
			g.setColor(Color.WHITE);
			g.drawRect(120, 260, getWidth() - 240, 55);
			g.drawRect(120, 315, getWidth() - 240, 3);
			g.setFont(regular.deriveFont(38f));
			g.drawString("Démarrage de la partie...", 280, 300);
		}
		
		// Affiche le tour en cours
		if(this.client.getCurrentPlayer() != null)
		{
			drawCenteredString(g, Color.white, regular, "TOUR DE", 840, 680);
			drawCenteredString(g, this.client.getCurrentPlayer().getPlayerColor(),
					bold, this.client.getCurrentPlayer().getName(), 840, 700);
			drawCenteredString(g, Color.white, regular, "TRANSFERTS RESTANTS", 840, 730);
			drawCenteredString(g, Color.green, bold, ""+this.client.getRemainingTransfers(), 840, 750);
			
			// Indication de tour
			if(this.client.getCurrentPlayer().getPlayerID() == this.client.getPlayerId())
			{
				g.setColor(Color.green);
				g.setFont(bold);
				g.drawString("C'est votre tour !", 65, 680);
				g.setFont(regular);
				g.drawString("Veuillez déplacer vos unités", 65, 700);
				g.drawString("Cliquez sur la planète source, puis cible pour déplacer vos armées", 65, 720);
				g.drawString("Appuyez sur <ECHAP> pour déselectionner une planète", 65, 740);
			} else
			{
				g.setColor(Color.white);
				g.setFont(regular);
				g.drawString("Un autre joueur est en train de jouer", 65, 680);
				g.drawString("Le déplacement de ses unités est en cours", 65, 700);
			}
		}
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
			g.setColor(Color.white);
			
			// Dessine une barre sous le joueur qui joue
			// actuellement
			if(p.equals(this.client.getCurrentPlayer())) {
				g.setStroke(new BasicStroke((int)(3+1.5d* Math.sin(tick / 2.0f))));
				g.drawRect(x + position * 130, y, 120, 30);
				g.setStroke(new BasicStroke(1));
				g.fillRect(x + position * 130, y + 35, 120, 5);
			} else
				g.drawRect(x + position * 130, y, 120, 30);
			
			g.setColor(p.getPlayerColor());
			g.fillRect(x + position * 130 + 5, y + 5, 30, 20);
			g.setColor(Color.white);
			g.setFont(bold.deriveFont(12f));
			g.drawString(p.getName(), x + position * 130 + 40, y + 13);
			
			g.setFont(regular.deriveFont(12f));
			
			if(this.client.getState() == ClientState.IN_LOBBY)
				if(p.isReady()) {
					g.setColor(Color.green);
					g.drawString("READY", x + position * 130 + 40, y + 25);
				} else {
					g.setColor(Color.gray);
					g.drawString("NOT READY", x + position * 130 + 40, y + 25);
				}
			
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
		drawCenteredString(g, Color.orange, regular, "Erreur : "+client.getCurrentStatus(), getWidth() / 2, getHeight() / 2 + 12);
	}
	
	/**
	 * Dessine le menu d'erreur
	 * @param g
	 */
	private void renderMenuAlreadyIG(Graphics2D g) {
		drawCenteredString(g, Color.cyan, regular, "La partie a déjà commencé !", getWidth() / 2, getHeight() / 2 - 12);
		drawCenteredString(g, Color.white, regular, "Merci d'attendre la fin de la partie en cours", getWidth() / 2, getHeight() / 2 + 12);
		drawCenteredString(g, Color.white, regular, "ou vous connecter à un autre serveur", getWidth() / 2, getHeight() / 2 + 32);
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

	@Override
	public void keyPressed(KeyEvent kev) {
		
	}

	@Override
	public void keyReleased(KeyEvent kev)
	{
		switch(kev.getKeyCode())
		{
			case KeyEvent.VK_R:
				if(this.client.getState() == ClientState.IN_LOBBY)
					this.client.onPlayerReady();
				break;
			case KeyEvent.VK_ESCAPE:
				if(this.client.getState() == ClientState.IN_VICTORY_MENU) {
					this.client.resetClient();
				} else if(this.client.getState() == ClientState.IN_GAME
						  && this.client.isMyTurn()) {
					this.client.setSelectedPlanet(null);
					this.client.send(new Packet12PlanetSelect(
						this.client.getPlayerId(),
						"NONE",
						0
					));
				}
				break;
			case KeyEvent.VK_B:
				enhancedMode = !enhancedMode;
				break;
			default: break;
		}
	}
	
	/**
	 * La souris a bougé
	 * @param x
	 * @param y
	 */
	private void onMouseMoved(int x, int y) {
		if(this.client.getState() == ClientState.IN_GAME && this.client.isMyTurn())
		this.client.setHoveredPlanet(x, y);
	}

	@Override
	public void keyTyped(KeyEvent kev) {}

	@Override
	public void mouseDragged(MouseEvent mev) {
		onMouseMoved(mev.getX(), mev.getY());
	}

	@Override
	public void mouseMoved(MouseEvent mev) {
		onMouseMoved(mev.getX(), mev.getY());
	}

	@Override
	public void mouseClicked(MouseEvent mev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent mev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent mev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent mev) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent mev) {
		if(this.client.getState() == ClientState.IN_GAME && this.client.isMyTurn())
		this.client.setSelectedPlanet(mev.getX(), mev.getY());
	}
}
