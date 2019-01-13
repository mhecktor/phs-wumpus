package de.fh;

import java.util.ArrayList;
import java.util.Collections;

import de.fh.agent.WumpusHunterAgent;
import de.fh.field.Field;
import de.fh.player.Player;
import de.fh.util.FieldUtil;
import de.fh.util.Point;
import de.fh.util.Vector2;
import de.fh.util.enums.DangerLevel;
import de.fh.util.enums.Direction;
import de.fh.util.enums.FieldType;
import de.fh.wumpus.HunterPercept;
import de.fh.wumpus.WumpusDangerMap;
import de.fh.wumpus.enums.HunterAction;
import de.fh.wumpus.enums.HunterActionEffect;

/*
 * DIESE KLASSE VERÄNDERN SIE BITTE NUR AN DEN GEKENNZEICHNETEN STELLEN
 * wenn die Bonusaufgabe bewertet werden soll.
 */
 /**
  * @author christophstockhoff (changes)
  */
public class MyWumpusAgent extends WumpusHunterAgent {

	// Ausgabevariable
	public static boolean DEBUG = false;

	private HunterPercept percept;

	// Aktuelle Position und Richtung
	private Player player;
	private Point pos;
	private Direction d;

	// Interne gespeicherte Map
	private ArrayList<Field> wumpusMap;
	private int eastWall, southWall = -1;

	// Info-Vars
	private int goldFound;
	private int wumpusKilled;
	private int score;
	// private int shots;
	private int rumbleDistance;
	private int stenchDistance;
	private Vector2 levelSize;
	private Vector2 startPosition;

	// Relevant fuer Wumpus
	private WumpusDangerMap wumpusDangerMap;

	public static void main( String[] args ) {
		MyWumpusAgent agent = new MyWumpusAgent( "" );
		MyWumpusAgent.start( agent, "127.0.0.1", 5000 );
	}

	public MyWumpusAgent( String name ) {
		super( name );
	}

	/**
	 * In dieser Methode kann das Wissen �ber die Welt (der State, der
	 * Zustand)
	 * entsprechend der aktuellen Wahrnehmungen anpasst, und die "interne Welt",
	 * die Wissensbasis, des Agenten kontinuierlich ausgebaut werden.
	 *
	 * Wichtig: Diese Methode wird aufgerufen, bevor der Agent handelt, d.h.
	 * bevor die action()-Methode aufgerufen wird...
	 *
	 * @param percept
	 *            Aktuelle Wahrnehmung
	 * @param actionEffect
	 *            Reaktion des Servers auf vorhergew�hlte Aktion
	 */
	@Override
	public void updateState( HunterPercept percept, HunterActionEffect actionEffect ) {

		/**
		 * Je nach Sichtbarkeit & Schwierigkeitsgrad (laut Serverkonfiguration)
		 * aktuelle Wahrnehmung des Hunters.
		 * Beim Wumpus erhalten Sie je nach Level mehr oder weniger
		 * Mapinformationen.
		 */
		this.percept = (HunterPercept) percept;

		score--;

		switch( actionEffect ) {
			case GAME_INITIALIZED:
				init();
				break;
			case BUMPED_INTO_WALL:
				switch( d ) {
					case EAST:
						FieldUtil.getFieldByPoint( new Point( pos.x + 1, pos.y ), wumpusMap ).addType( FieldType.WALL );
						eastWall = pos.getX() + 1;
						wumpusDangerMap.setEastWall( eastWall );
						break;
					case NORTH:
						// FieldUtil.getFieldByPoint( new Point( pos.x, pos.y -
						// 1 ), wumpusMap ).addType( FieldType.WALL );
						break;
					case SOUTH:
						FieldUtil.getFieldByPoint( new Point( pos.x, pos.y + 1 ), wumpusMap ).addType( FieldType.WALL );
						southWall = pos.getY() + 1;
						wumpusDangerMap.setSouthWall( southWall );
						break;
					case WEST:
						// FieldUtil.getFieldByPoint( new Point( pos.x - 1,
						// pos.y ), wumpusMap ).addType( FieldType.WALL );
						break;
					default:
						break;

				}
				break;
			case BUMPED_INTO_HUNTER:
				// Nur bei Multiplayermodus
				// Letzte Bewegungsaktion war ein Zusammenstoß mit einem
				// weiteren
				// Hunter
				break;
			case GOLD_FOUND:
				FieldUtil.getFieldByPoint( pos, wumpusMap ).removeType( FieldType.GOLD );
				score += 100;
				goldFound++;
				break;
			case MOVEMENT_SUCCESSFUL:
				movePlayer();
				break;
			case NO_MORE_SHOOTS:
				System.out.println( "LOOSER - no more shoots" );
				player.setArrows( 0 );
				break;
			case WUMPUS_KILLED:
				score += 100;
				wumpusKilled++;
				break;
			case GAME_OVER:
				printInfo();
				break;
			default:
				break;

		}
		player.addFieldToHistory( new Field( pos, FieldType.PLAYER ) );
		player.setAction( nextAction );

		if( nextAction == HunterAction.SHOOT ) player.reduceArrows();
		if( DEBUG ) printWumpusMap();

		refreshWumpusDangerMap();
	}

	/**
	 * Diesen Part erweitern Sie so, dass die n�chste(n) sinnvolle(n)
	 * Aktion(en),
	 * auf Basis der vorhandenen Zustandsinformationen und gegebenen Zielen,
	 * ausgef�hrt wird/werden.
	 * Der action-Part soll den Agenten so intelligent wie m�glich handeln
	 * lassen
	 *
	 * Beispiel: Wenn die letzte Wahrnehmung
	 * "percept.isGlitter() == true" enthielt, ist "HunterAction.GRAB" eine
	 * geeignete T�tigkeit. Wenn Sie wissen, dass ein Quadrat "unsicher"
	 * ist, k�nnen Sie wegziehen
	 *
	 * @return Die n�chste HunterAction die vom Server ausgef�hrt werden
	 *         soll
	 */

	@Override
	public HunterAction action() {
		nextAction = null;

		// TODO eventuell koennte er noch zurucklaufen
		if( player.getArrows() <= 0 && WumpusDangerMap.dangerLevel != DangerLevel.SCARED ) {
			System.out.println( "Vorsichtig: Keine Schuesse mehr vorhanden!!!" );
			WumpusDangerMap.dangerLevel = DangerLevel.SCARED;
		}
		// if( goldFound == 1 && wumpusDangerMap.allWumpiKilled() ) nextAction =
		// returnToHome();

		if( nextAction == null ) nextAction = wumpusDangerAction();

		if( goldFound == 1 && nextAction == null ) nextAction = returnToHome();

		// Wenn der Wumpus ungefaehrlich ist, dann suche eine "normale" Aktion
		// aus
		if( nextAction == null ) nextAction = FieldUtil.actionsToFieldType( FieldType.GOLD, wumpusMap, pos, d );

		if( nextAction == null ) nextAction = getNextAction();

		if( nextAction == null ) nextAction = returnToHome();

		if( nextAction == HunterAction.QUIT_GAME ) printInfo();

		return nextAction;

	}

	/*
	 * Bereich f�r eigene Methoden
	 */

	private void init() {
		WumpusDangerMap.dangerLevel = DangerLevel.BRAVE;
		// Aufgabenrelevante-Initialisierungen
		switch( startInfo.getAgentDirection() ) {
			case EAST:
				d = Direction.EAST;
				break;
			case NORTH:
				d = Direction.NORTH;
				break;
			case SOUTH:
				d = Direction.SOUTH;
				break;
			case WEST:
				d = Direction.WEST;
				break;
			default:
				break;
		}
		score = startInfo.getScore();
		rumbleDistance = startInfo.getRumbleDistance();
		stenchDistance = startInfo.getStenchDistance();
		levelSize = startInfo.getLevelSize();
		startPosition = startInfo.getStartPosition();
		goldFound = 0;
		wumpusKilled = 0;

		pos = new Point( startPosition.getX(), startPosition.getY() );

		wumpusMap = new ArrayList<>();
		wumpusMap.add( new Field( new Point( 0, 0 ), FieldType.WALL ) );
		wumpusMap.add( new Field( new Point( 1, 0 ), FieldType.WALL ) );
		wumpusMap.add( new Field( new Point( 0, 1 ), FieldType.WALL ) );

		Field tStartField = new Field( new Point( startPosition.getX(), startPosition.getY() ), FieldType.START );
		wumpusMap.add( tStartField );
		FieldUtil.getFieldByPoint( pos, wumpusMap ).addType( FieldType.EMPTY );

		if( percept.isBreeze() ) {
			wumpusMap.add( new Field( new Point( 2, 1 ), FieldType.BREEZE ) );
			wumpusMap.add( new Field( new Point( 1, 2 ), FieldType.BREEZE ) );
		} else {
			Field f1 = new Field( new Point( 2, 1 ), FieldType.EMPTY );
			Field f2 = new Field( new Point( 1, 2 ), FieldType.EMPTY );
			f1.addType( FieldType.EMPTY );
			f2.addType( FieldType.EMPTY );
			wumpusMap.add( f1 );
			wumpusMap.add( f2 );
		}

		player = new Player( pos, d, startInfo.getShots() );
		player.addFieldToHistory( tStartField );
		wumpusDangerMap = new WumpusDangerMap( wumpusMap, stenchDistance, player );
	}

	private void movePlayer() {
		if( nextAction == HunterAction.TURN_LEFT ) {
			// Muss gemacht werden, da Modulo auch negative Zahlen liefern kann
			int tOrd = ( d.ordinal() - 1 ) % 4;
			if( tOrd < 0 ) tOrd += 4;
			d = Direction.values()[ tOrd ];
		} else if( nextAction == HunterAction.TURN_RIGHT ) {
			d = Direction.values()[ ( d.ordinal() + 1 ) % 4 ];
		} else if( nextAction == HunterAction.GO_FORWARD ) {
			Field f;
			f = FieldUtil.getFieldByPoint( pos, wumpusMap );
			f.removeType( FieldType.PLAYER );

			switch( d ) {
				case EAST:
					pos.x++;
					break;
				case NORTH:
					pos.y--;
					break;
				case SOUTH:
					pos.y++;
					break;
				case WEST:
					pos.x--;
					break;
				default:
					break;

			}
			f = FieldUtil.getFieldByPoint( pos, wumpusMap );
			f.addType( FieldType.VISITED );
			f.addType( FieldType.PLAYER );
			refreshMapAfterMove();
		}
		player.setDirection( d );
	}

	private void refreshWumpusDangerMap() {
		wumpusDangerMap.setRumble( percept.isRumble() );
		wumpusDangerMap.setScream( percept.isScream() );
		wumpusDangerMap.setStenchRadar( percept.getWumpusStenchRadar() );
	}

	private void refreshMapAfterMove() {
		Field top, left, right, bottom;

		top = FieldUtil.entryExisting( new Point( pos.getX(), pos.getY() - 1 ), wumpusMap );
		left = FieldUtil.entryExisting( new Point( pos.getX() - 1, pos.getY() ), wumpusMap );
		right = FieldUtil.entryExisting( new Point( pos.getX() + 1, pos.getY() ), wumpusMap );
		bottom = FieldUtil.entryExisting( new Point( pos.getX(), pos.getY() + 1 ), wumpusMap );

		if( right.getPoint().getX() == eastWall ) right.addType( FieldType.WALL );
		if( bottom.getPoint().getY() == southWall ) bottom.addType( FieldType.WALL );

		if( percept.isBreeze() ) {
			// -> Brise
			if( top.getType() != FieldType.WALL ) top.addType( FieldType.BREEZE );
			if( left.getType() != FieldType.WALL ) left.addType( FieldType.BREEZE );
			if( right.getType() != FieldType.WALL ) right.addType( FieldType.BREEZE );
			if( bottom.getType() != FieldType.WALL ) bottom.addType( FieldType.BREEZE );
		} else {
			top.addType( FieldType.EMPTY );
			left.addType( FieldType.EMPTY );
			right.addType( FieldType.EMPTY );
			bottom.addType( FieldType.EMPTY );
		}

		if( percept.isBump() ) {
			// -> Stoßen (Wandwahrnehmung)
			if( pos.x == 1 ) left.addType( FieldType.WALL );
			if( pos.y == 1 ) top.addType( FieldType.WALL );
		}
		if( percept.isGlitter() ) {
			// -> Funkeln
			FieldUtil.getFieldByPoint( pos, wumpusMap ).addType( FieldType.GOLD );
		}

	}

	private HunterAction returnToHome() {
		HunterAction tAction = FieldUtil.actionsToFieldType( FieldType.START, wumpusMap, pos, d );
		if( tAction == null )
			return HunterAction.QUIT_GAME;
		else
			return tAction;
	}

	private HunterAction getNextAction() {
		// Steht auf einem Feld mit Gold
		if( FieldUtil.getFieldByPoint( pos, wumpusMap ).getType() == FieldType.GOLD ) {
			Field f = FieldUtil.getFieldByPoint( pos, wumpusMap );
			f.addType( FieldType.VISITED );
			f.removeType( FieldType.GOLD );
			return HunterAction.GRAB;
		} else {
			return FieldUtil.actionsToFieldType( FieldType.EMPTY, wumpusMap, pos, d );
		}
	}

	private HunterAction wumpusDangerAction() {
		wumpusDangerMap.setCurrentField( FieldUtil.getFieldByPoint( pos, wumpusMap ), d );
		return wumpusDangerMap.getAction();
	}

	private void printInfo() {
		System.out.println( "###############" );
		System.out.println( "###  INFOS  ###" );
		System.out.println( "###############" );
		System.out.println( " Anzahl gefundenen Goldes: " + goldFound );
		System.out.println( " Anzahl getoeteter Wumpi: " + wumpusKilled );
		System.out.println( " Score: " + score );
		System.out.println( " Shots: " + player.getArrows() );
		System.out.println( " RumbleDistance: " + rumbleDistance );
		System.out.println( " StenchDistance: " + stenchDistance );
		System.out.println( " AgentDirection: " + d );
		System.out.println( " LevelSize: " + levelSize );
		System.out.println( " StartPosition: " + startPosition );
		System.out.println( " PlayerPosition: " + pos );
		System.out.println( "_______________" );
		printWumpusMap();
	}

	private void printWumpusMap() {
		System.out.println( "------" );
		ArrayList<Field> tList = new ArrayList<>();

		for( Field f : wumpusMap )
			tList.add( f );

		// Sortieren der printList
		Collections.sort( tList, ( p1, p2 ) -> new Integer( p1.getPoint().getX() ).compareTo( new Integer( p2.getPoint().getX() ) ) );
		Collections.sort( tList, ( p1, p2 ) -> new Integer( p1.getPoint().getY() ).compareTo( new Integer( p2.getPoint().getY() ) ) );

		// Ausgabe der printList
		int currentRow = 0;
		int currentCol = 0;
		for( Field f : tList ) {
			if( f.getPoint().getY() != currentRow ) {
				System.out.print( "\n" );
				currentRow = f.getPoint().getY();
				currentCol = 0;
			}
			while( currentCol < f.getPoint().getX() ) {
				System.out.print( "  " );
				currentCol++;
			}
			switch( f.getType() ) {
				case BREEZE:
					System.out.print( "~" + " " );
					break;
				case EMPTY:
					System.out.print( "?" + " " );
					break;
				case GOLD:
					System.out.print( "G" + " " );
					break;
				case HOLE:
					System.out.print( "O" + " " );
					break;
				case PLAYER:
					System.out.print( "P" + " " );
					break;
				case START:
					System.out.print( "S" + " " );
					break;
				case STENCH:
					System.out.print( "@" + " " );
					break;
				case VISITED:
					System.out.print( "." + " " );
					break;
				case WALL:
					System.out.print( "#" + " " );
					break;
				case WUMPUS:
					System.out.print( "W" + " " );
					break;
				default:
					System.out.print( f.getType().ordinal() + " " );
					break;

			}
			currentCol++;
		}
		System.out.println( "\n------" );
	}
}
