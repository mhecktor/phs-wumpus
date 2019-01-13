package de.fh.wumpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Stack;

import de.fh.MyWumpusAgent;
import de.fh.field.Field;
import de.fh.player.Player;
import de.fh.util.FieldUtil;
import de.fh.util.Point;
import de.fh.util.enums.DangerLevel;
import de.fh.util.enums.Direction;
import de.fh.util.enums.FieldType;
import de.fh.wumpus.enums.HunterAction;

/**
 * @author christophstockhoff
 */
public class WumpusDangerMap {

	private Player player;
	private Direction d;
	private Field lastField, currentField;

	private int globalRumbleCounter = 0;
	private int killCounter = 0;
	private HashMap<Integer, WumpusBot> wumpies;

	public static DangerLevel dangerLevel = DangerLevel.BRAVE;

	public ArrayList<Field> wumpusMap;
	private ArrayList<WumpusField> dangerFields;

	private int lastIntensity;
	private WumpusField fieldShot;
	private int tryShots;

	private final int stenchDistance;

	private boolean rumble;
	private boolean scream;
	public Hashtable<Integer, Integer> stenchRadar;

	private static int northWall, eastWall, southWall, westWall;
	private static boolean southWallLocated, eastWallLocated;

	private ArrayList<Field> currentScope;

	public WumpusDangerMap( ArrayList<Field> wumpusMap, int stenchDistance, Player p ) {
		this.wumpusMap = wumpusMap;
		this.stenchDistance = stenchDistance;
		this.player = p;
		wumpies = new HashMap<>();
		northWall = westWall = 0;

		dangerFields = new ArrayList<>();
		lastIntensity = stenchDistance + 1;
	}

	public void setCurrentField( Field currentField, Direction d ) {
		this.d = d;
		this.lastField = ( ( lastField == null ) ? currentField : this.currentField );
		this.currentField = currentField;
	}

	public HunterAction getAction() {
		refreshMaps();

		switch( dangerLevel ) {
			case SCARED:
				return scaredActions();
			case BRAVE:
				return braveActions();
		}
		return null;

	}

	private HunterAction scaredActions() {
		Stack<HunterAction> actions = new Stack<>();

		if( player.getArrows() <= 0 && lastIntensity <= 3 ) {
			Field tField = findSafeField();
			actions = FieldUtil.actionsToField( tField, wumpusMap, currentField.getPoint(), d );
		}

		if( actions.isEmpty() )
			return null;
		else
			return actions.pop();
	}

	private HunterAction braveActions() {
		Stack<HunterAction> actions = new Stack<>();

		if( dangerFields.size() == 1 ) {

			actions = safeShot();

		} else if( dangerFields.size() > 1 ) {
			if( lastIntensity <= 1 ) {
				actions = FieldUtil.actionsToField( dangerFields.get( 0 ).getField(), wumpusMap, currentField.getPoint(), d );
				if( actions.size() < 2 ) {
					fieldShot = dangerFields.get( 0 );
					actions.push( HunterAction.SHOOT );
				}
			} else if( lastIntensity <= 2 ) {
				if( tryShots < 2 && performTryShot() ) { return HunterAction.SHOOT; }

				Field tField = findSafeField();
				if( !tField.equalsOtherFieldCoords( currentField ) ) actions = FieldUtil.actionsToField( tField, wumpusMap, currentField.getPoint(), d );

			}
		}

		if( actions.isEmpty() )
			return null;
		else
			return actions.pop();
	}

	private Stack<HunterAction> safeShot() {
		Stack<HunterAction> actions;
		Field tWumpus = dangerFields.get( 0 ).getField();
		boolean tShot = false;

		switch( player.getDirection() ) {
			case NORTH:
				if( tWumpus.getPoint().getY() < player.getLoc().getY() && tWumpus.getPoint().getX() == player.getLoc().getX() ) {
					tShot = true;
				}
				break;
			case EAST:
				if( tWumpus.getPoint().getX() > player.getLoc().getX() && tWumpus.getPoint().getY() == player.getLoc().getY() ) {
					tShot = true;
				}
				break;
			case SOUTH:
				if( tWumpus.getPoint().getY() > player.getLoc().getY() && tWumpus.getPoint().getX() == player.getLoc().getX() ) {
					tShot = true;
				}
				break;
			case WEST:
				if( tWumpus.getPoint().getX() < player.getLoc().getX() && tWumpus.getPoint().getY() == player.getLoc().getY() ) {
					tShot = true;
				}
				break;

		}

		if( tShot ) {
			actions = new Stack<>();
			actions.push( HunterAction.SHOOT );
			return actions;
		} else {
			actions = FieldUtil.actionsToField( tWumpus, wumpusMap, currentField.getPoint(), d );
			if( actions.size() < 2 ) {
				fieldShot = dangerFields.get( 0 );

				actions.push( HunterAction.SHOOT );
			}
			return actions;
		}
	}

	private boolean performTryShot() {
		switch( player.getDirection() ) {
			case NORTH:
				for( int i = 0; i < dangerFields.size(); i++ ) {
					if( dangerFields.get( i ).getPoint().getX() == player.getLoc().getX() && dangerFields.get( i ).getPoint().getY() < player.getLoc().getY() ) {
						fieldShot = dangerFields.get( i );
						tryShots++;
						return true;
					}
				}
				break;
			case EAST:
				for( int i = 0; i < dangerFields.size(); i++ ) {
					if( dangerFields.get( i ).getPoint().getY() == player.getLoc().getY() && dangerFields.get( i ).getPoint().getX() > player.getLoc().getX() ) {
						fieldShot = dangerFields.get( i );
						tryShots++;
						return true;
					}
				}
				break;
			case SOUTH:
				for( int i = 0; i < dangerFields.size(); i++ ) {
					if( dangerFields.get( i ).getPoint().getX() == player.getLoc().getX() && dangerFields.get( i ).getPoint().getY() > player.getLoc().getY() ) {
						fieldShot = dangerFields.get( i );
						tryShots++;
						return true;
					}
				}
				break;
			case WEST:
				for( int i = 0; i < dangerFields.size(); i++ ) {
					if( dangerFields.get( i ).getPoint().getY() == player.getLoc().getY() && dangerFields.get( i ).getPoint().getX() < player.getLoc().getX() ) {
						fieldShot = dangerFields.get( i );
						tryShots++;
						return true;
					}
				}
				break;
		}
		return false;
	}

	private void refreshMaps() {
		Point tCurrentPoint = currentField.getPoint();

		if( scream )
			killedWumpus();
		else if( fieldShot != null ) noKill();
		fieldShot = null;

		lastIntensity = stenchDistance + 1;

		// Erzeuge leeren Stench-Scope
		currentScope = new ArrayList<>();
		for( int y = -stenchDistance; y < ( stenchDistance + 1 ); y++ ) {
			int tXMin = ( -stenchDistance + Math.abs( y ) );
			int tXMax = ( stenchDistance - Math.abs( y ) );

			for( int wx = tXMin; wx <= tXMax; wx++ ) {
				Point p = new Point( tCurrentPoint.getX() + wx, tCurrentPoint.getY() + y );
				Field tField = new Field( p, FieldType.STENCH );

				if( isFieldWalkable( tField ) ) currentScope.add( tField );

			}
		}

		if( rumble ) globalRumbleCounter++;

		// Fuer alle Wumpis relevant
		for( Entry<Integer, WumpusBot> e : wumpies.entrySet() ) {
			if( rumble ) e.getValue().setRumbleCounter( e.getValue().getRumbleCounter() + 1 );
			if( !stenchRadar.containsKey( e.getKey() ) ) e.getValue().setIntensity( lastIntensity );
			e.getValue().setStenched( false );
			e.getValue().refreshActions( rumble );
		}

		// Wumpus sniffed
		for( Entry<Integer, Integer> e : stenchRadar.entrySet() ) {
			if( !wumpies.containsKey( e.getKey() ) ) wumpies.put( e.getKey(), new WumpusBot( globalRumbleCounter, stenchDistance, rumble ) );

			WumpusBot tWumpus = wumpies.get( e.getKey() );
			tWumpus.refreshWumpus( tCurrentPoint, currentScope, e.getValue(), player );

			lastIntensity = e.getValue();
		}

		removeInvalidFields();

		// Baue dir die globale DangerMap zusammen
		dangerFields = new ArrayList<>();
		for( Entry<Integer, WumpusBot> e : wumpies.entrySet() ) {
			if( e.getValue().isStenched() ) {
				for( Field f : e.getValue().getDangerFields() ) {

					WumpusField tWumpusField = null;
					for( int i = 0; i < dangerFields.size() && tWumpusField == null; i++ ) {
						if( dangerFields.get( i ).getField().equalsOtherFieldCoords( f ) ) {
							tWumpusField = dangerFields.get( i );
						}
					}
					if( tWumpusField == null ) {
						tWumpusField = new WumpusField( f );
						dangerFields.add( tWumpusField );
					}
					tWumpusField.addWumpusID( e.getKey() );
				}
			}
		}

		printDangerFields();
	}

	private void removeInvalidFields() {
		for( Entry<Integer, WumpusBot> e : wumpies.entrySet() ) {
			WumpusBot tWumpus = e.getValue();

			for( int i = tWumpus.getActions().size() - 1; i >= 0 && !tWumpus.getActions().get( i ).contains( HunterAction.GO_FORWARD ); i-- ) {
				boolean deleted = false;
				int a = 0;
				while( a < tWumpus.getDangerFields().size() && !deleted ) {
					if( player.getFieldHistory().get( i ).equalsOtherFieldCoords( tWumpus.getDangerFields().get( a ) ) ) deleted = true;
					if( !deleted ) a++;
				}
				if( a < tWumpus.getDangerFields().size() ) {
					tWumpus.getDangerFields().remove( a );
				}

			}

			int tWumpusIntensity = tWumpus.getIntensity();
			if( tWumpusIntensity == stenchDistance + 1 ) {
				switch( player.getAction() ) {
					case GO_FORWARD:
						if( tWumpus.getLastIntensity() == 3 ) {
							for( Field iField : currentScope ) {
								boolean deleted = false;
								int a = 0;
								while( a < tWumpus.getDangerFields().size() && !deleted ) {
									if( iField.equalsOtherFieldCoords( tWumpus.getDangerFields().get( a ) ) ) deleted = true;
									if( !deleted ) a++;
								}
								if( a < tWumpus.getDangerFields().size() ) {
									tWumpus.getDangerFields().remove( a );
								}
							}
						}
						break;
					case TURN_LEFT:
					case TURN_RIGHT:
						if( tWumpus.getLastIntensity() == 3 ) {
							for( Field iField : currentScope ) {
								boolean deleted = false;
								int a = 0;
								while( a < tWumpus.getDangerFields().size() && !deleted ) {
									if( iField.equalsOtherFieldCoords( tWumpus.getDangerFields().get( a ) ) && !isCornerField( iField ) ) deleted = true;
									if( !deleted ) a++;
								}
								if( a < tWumpus.getDangerFields().size() ) {
									tWumpus.getDangerFields().remove( a );
								}
							}
						}
						break;
					default:
						break;

				}
			}
		}

	}

	private boolean isCornerField( Field f ) {
		/* @formatter:off */
		if( f.equalsOtherFieldCoords( new Field( player.getLoc().getX(), player.getLoc().getY() - 3, FieldType.WUMPUS) )
			|| f.equalsOtherFieldCoords( new Field( player.getLoc().getX() + 3, player.getLoc().getY(), FieldType.WUMPUS) )
			|| f.equalsOtherFieldCoords( new Field( player.getLoc().getX(), player.getLoc().getY() + 3, FieldType.WUMPUS) )
			|| f.equalsOtherFieldCoords( new Field( player.getLoc().getX() - 3, player.getLoc().getY(), FieldType.WUMPUS) ) )
			return true;
		/* @formatter:on */
		return false;
	}

	private void printDangerFields() {
		// Print-Zeugs
		if( MyWumpusAgent.DEBUG ) {
			for( WumpusField f : dangerFields ) {
				System.out.print( " - " + f.getField().getPoint() );
				System.out.print( " {" + f.getWumpusIDs().get( 0 ) );
				for( int i = 1; i < f.getWumpusIDs().size(); i++ )
					System.out.print( ", " + f.getWumpusIDs().get( i ) );

				System.out.print( "}" );
			}
			if( dangerFields.size() != 0 ) System.out.println();
		}
	}

	private void noKill() {
		shootField( fieldShot.getField() );
		dangerFields.remove( fieldShot );
	}

	private void killedWumpus() {
		killCounter++;

		for( Entry<Integer, WumpusBot> e : wumpies.entrySet() ) {
			e.getValue().setAlive( false );
			e.getValue().setDetected( false );
		}

		dangerFields = new ArrayList<>();
	}

	public boolean allWumpiKilled() {
		if( rumble ) return false;
		return killCounter >= wumpies.size();

	}

	private void shootField( Field f ) {
		for( Entry<Integer, WumpusBot> e : wumpies.entrySet() ) {
			Field tField = FieldUtil.getFieldByPoint( f.getPoint(), e.getValue().getDangerFields() );
			e.getValue().getDangerFields().remove( tField );
		}
	}

	private Field findSafeField() {
		ArrayList<Neighbour> tNeighbours = new ArrayList<>();
		Point tPos = currentField.getPoint();

		tNeighbours.add( new Neighbour( FieldUtil.getFieldByPoint( new Point( tPos.getX(), tPos.getY() - 1 ), wumpusMap ), stenchDistance + 1 ) );
		tNeighbours.add( new Neighbour( FieldUtil.getFieldByPoint( new Point( tPos.getX() - 1, tPos.getY() ), wumpusMap ), stenchDistance + 1 ) );
		tNeighbours.add( new Neighbour( FieldUtil.getFieldByPoint( new Point( tPos.getX() + 1, tPos.getY() ), wumpusMap ), stenchDistance + 1 ) );
		tNeighbours.add( new Neighbour( FieldUtil.getFieldByPoint( new Point( tPos.getX(), tPos.getY() + 1 ), wumpusMap ), stenchDistance + 1 ) );

		for( Neighbour n : tNeighbours ) {
			int tMinDistance = stenchDistance + 1;
			if( n.getField() == null ) continue;
			if( !n.getField().isWalkable() ) {
				n.setDistance( -1 );
				continue;
			}

			for( WumpusField iDangerField : dangerFields ) {
				if( FieldUtil.distance( n.getField().getPoint(), iDangerField.getField().getPoint() ) < tMinDistance ) {
					n.setDistance( FieldUtil.distance( n.getField().getPoint(), iDangerField.getField().getPoint() ) );
					tMinDistance = n.getDistance();
				}
			}
		}
		Collections.sort( tNeighbours, ( n1, n2 ) -> n2.getDistance().compareTo( n1.getDistance() ) );
		int tHighestDistance = tNeighbours.get( 0 ).getDistance();
		for( Neighbour n : tNeighbours ) {
			if( tHighestDistance == -1 )
				tHighestDistance = n.getDistance();
			else if( tHighestDistance != n.getDistance() ) tHighestDistance = n.getDistance();
		}
		if( tHighestDistance == tNeighbours.get( 0 ).getDistance() ) { return currentField; }

		return tNeighbours.get( 0 ).getField();
	}

	class Neighbour {
		private Field f;
		private int distance;

		public Neighbour( Field f, int distance ) {
			this.f = f;
			this.distance = distance;
		}

		public void setDistance( int distance ) {
			this.distance = distance;
		}

		public Integer getDistance() {
			return distance;
		}

		public Field getField() {
			return f;
		}

		@Override
		public String toString() {
			return f.getPoint() + "->" + distance;
		}
	}

	public static boolean isFieldWalkable( Field f ) {
		if( f.getPoint().getX() <= westWall ) return false;
		if( f.getPoint().getY() <= northWall ) return false;
		if( eastWallLocated ) {
			if( f.getPoint().getX() >= eastWall ) return false;
		}
		if( southWallLocated ) {
			if( f.getPoint().getY() >= southWall ) return false;
		}
		return true;
	}

	public void setRumble( boolean rumble ) {
		this.rumble = rumble;
	}

	public void setScream( boolean scream ) {
		this.scream = scream;
	}

	public void setStenchRadar( Hashtable<Integer, Integer> stenchRadar ) {
		this.stenchRadar = stenchRadar;
	}

	public void setSouthWall( int southWall ) {
		WumpusDangerMap.southWall = southWall;
		southWallLocated = true;
	}

	public void setEastWall( int eastWall ) {
		WumpusDangerMap.eastWall = eastWall;
		eastWallLocated = true;
	}
}
