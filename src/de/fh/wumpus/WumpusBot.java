package de.fh.wumpus;

import java.util.ArrayList;
import java.util.HashSet;

import de.fh.field.Field;
import de.fh.player.Player;
import de.fh.util.FieldUtil;
import de.fh.util.Point;
import de.fh.wumpus.enums.HunterAction;

/**
 * @author christophstockhoff
 */
public class WumpusBot {

	private ArrayList<Field> dangerFields;

	private ArrayList<HashSet<HunterAction>> actions;

	private boolean stenched = false;

	private int rumbleCounter;
	private boolean alive;

	private boolean detected;
	private int lastIntensity, intensity;

	public WumpusBot( int rumbleCounter, int stenchDistance, boolean rumble ) {
		this.alive = true;
		this.rumbleCounter = rumbleCounter;
		actions = new ArrayList<>();
		refreshActions( rumble );
	}

	public void refreshWumpus( Point playerLoc, ArrayList<Field> currentScope, int intensity, Player p ) {
		ArrayList<Field> tNewFields = new ArrayList<Field>();

		// Erzeuge Stench
		for( int y = -intensity; y < ( intensity + 1 ); y++ ) {
			int tXMin = ( -intensity + Math.abs( y ) );
			int tXMax = ( intensity - Math.abs( y ) );

			Field tMinStenchField = FieldUtil.getFieldByPoint( new Point( playerLoc.getX() + tXMin, playerLoc.getY() + y ), currentScope );
			Field tMaxStenchField = FieldUtil.getFieldByPoint( new Point( playerLoc.getX() + tXMax, playerLoc.getY() + y ), currentScope );

			if( detected ) {
				for( Field sf : dangerFields ) {
					if( sf.isWalkable() ) {
						if( tMinStenchField != null && FieldUtil.distance( sf.getPoint(), tMinStenchField.getPoint() ) <= rumbleCounter ) {
							if( !tNewFields.contains( tMinStenchField ) ) tNewFields.add( tMinStenchField );
						}
						if( tXMin != tXMax ) {
							if( tMaxStenchField != null && FieldUtil.distance( sf.getPoint(), tMaxStenchField.getPoint() ) <= rumbleCounter ) {
								if( !tNewFields.contains( tMaxStenchField ) ) tNewFields.add( tMaxStenchField );
							}
						}
					}
				}
			} else {
				if( tMinStenchField != null ) tNewFields.add( tMinStenchField );
				if( tXMin != tXMax ) {
					if( tMaxStenchField != null ) tNewFields.add( tMaxStenchField );
				}
			}
		}

		rumbleCounter = 0;
		dangerFields = tNewFields;
		stenched = true;

		// Wumpus wurde das erste mal erkannt
		detected = true;

		this.intensity = intensity;

	}

	public ArrayList<Field> getDangerFields() {
		return dangerFields;
	}

	public void setAlive( boolean alive ) {
		this.alive = alive;
	}

	public boolean isAlive() {
		return alive;
	}

	public void setDetected( boolean detected ) {
		this.detected = detected;
	}

	public void setRumbleCounter( int rumbleCounter ) {

		this.rumbleCounter = rumbleCounter;
	}

	public int getRumbleCounter() {
		return rumbleCounter;
	}

	public boolean isStenched() {
		return stenched;
	}

	public void setStenched( boolean stenched ) {
		this.stenched = stenched;
	}

	public void setIntensity( int intensity ) {
		lastIntensity = this.intensity;
		this.intensity = intensity;
	}

	public int getIntensity() {
		return intensity;
	}

	public int getLastIntensity() {
		return lastIntensity;
	}

	public void refreshActions( boolean rumble ) {
		HashSet<HunterAction> tActions = new HashSet<>();

		if( rumble ) {
			tActions.add( HunterAction.GO_FORWARD );

			tActions.add( HunterAction.SIT );
			tActions.add( HunterAction.TURN_LEFT );
			tActions.add( HunterAction.TURN_RIGHT );
			actions.add( tActions );
		} else {
			tActions.add( HunterAction.SIT );
			tActions.add( HunterAction.TURN_LEFT );
			tActions.add( HunterAction.TURN_RIGHT );
			actions.add( tActions );
		}
	}

	public ArrayList<HashSet<HunterAction>> getActions() {
		return actions;
	}

}
